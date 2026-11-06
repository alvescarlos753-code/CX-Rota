package com.cxwebstudio.cxrota;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.HardwareBuffer;
import android.os.Build;
import android.view.Display;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Somente leitura e sugestão; NUNCA clica em botões de Uber/99. */
public class OfferReaderService extends AccessibilityService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager windowManager;
    private LinearLayout overlayView;
    private TextToSpeech tts;
    private TextRecognizer recognizer;
    private long lastScreenshotAt = 0;
    private boolean screenshotBusy = false;
    private String lastSignature = "";
    private long lastReadAt = 0;
    private Runnable hideRunnable;

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        }
        if (SettingsStore.voice(this)) {
            tts = new TextToSpeech(this, status -> {
                if (status == TextToSpeech.SUCCESS && tts != null)
                    tts.setLanguage(new Locale("pt", "BR"));
            });
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!SettingsStore.consent(this) || event == null || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString();
        String source;
        if (pkg.equals("com.ubercab.driver")) source = "Uber Moto";
        else if (pkg.equals("com.app99.driver")) source = "99 Moto";
        else return;

        int eventType = event.getEventType();
        if (eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                && eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) { captureOcrIfApproved(source); return; }
        Set<String> lines = new LinkedHashSet<>();
        readNodes(root, lines, 0, new int[] {0});
        String data = String.join("\n", lines);
        OfferEngine.Offer offer = OfferParser.parse(source, data);
        if (offer == null) {
            captureOcrIfApproved(source);
            return;
        }
        processOffer(source, offer);
    }

    private void processOffer(String source, OfferEngine.Offer offer) {
        if (offer == null) return;
        String signature = source + "|" + OfferEngine.number(offer.fare) + "|"
                + OfferEngine.number(offer.distanceKm) + "|" + OfferEngine.number(offer.minutes);
        long now = System.currentTimeMillis();
        if (signature.equals(lastSignature) && now - lastReadAt < 60000) return;
        lastReadAt = now;
        lastSignature = signature;
        OfferEngine.Result result = OfferEngine.evaluate(offer, SettingsStore.rules(this));
        SettingsStore.writeOffer(this, result);
        if (SettingsStore.overlay(this) && Settings.canDrawOverlays(this)) showOverlay(result);
        speak(result);
    }

    /** Captura apenas tela de Uber/99, sem gravar imagem e somente com OCR autorizado. */
    private void captureOcrIfApproved(String source) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || recognizer == null
                || !SettingsStore.ocr(this) || !SettingsStore.consent(this)) return;
        long now = System.currentTimeMillis();
        if (screenshotBusy || now - lastScreenshotAt < 3500) return;
        screenshotBusy = true;
        lastScreenshotAt = now;
        try {
            takeScreenshot(Display.DEFAULT_DISPLAY, getMainExecutor(), new TakeScreenshotCallback() {
                @Override public void onSuccess(ScreenshotResult result) {
                    Bitmap software = null;
                    try {
                        HardwareBuffer buffer = result.getHardwareBuffer();
                        Bitmap hardwareBitmap = Bitmap.wrapHardwareBuffer(buffer, result.getColorSpace());
                        if (hardwareBitmap != null) {
                            software = hardwareBitmap.copy(Bitmap.Config.ARGB_8888, false);
                            hardwareBitmap.recycle();
                        }
                        buffer.close();
                        if (software == null) { screenshotBusy = false; return; }
                        final Bitmap bitmap = software;
                        recognizer.process(InputImage.fromBitmap(bitmap, 0))
                                .addOnSuccessListener(text -> {
                                    OfferEngine.Offer found = OfferParser.parse(source, text.getText());
                                    if (found != null) processOffer(source, found);
                                })
                                .addOnCompleteListener(task -> {
                                    bitmap.recycle();
                                    screenshotBusy = false;
                                });
                    } catch (Exception ignored) {
                        if (software != null) software.recycle();
                        screenshotBusy = false;
                    }
                }
                @Override public void onFailure(int errorCode) {
                    screenshotBusy = false;
                }
            });
        } catch (Exception ignored) { screenshotBusy = false; }
    }

    private static void readNodes(AccessibilityNodeInfo node, Set<String> lines, int depth, int[] visited) {
        if (node == null || depth > 20 || visited[0]++ > 500 || lines.size() >= 100) return;
        CharSequence text = node.getText();
        if (text != null && text.length() > 0 && text.length() < 400) lines.add(text.toString().trim());
        CharSequence desc = node.getContentDescription();
        if (desc != null && desc.length() > 0 && desc.length() < 400) lines.add(desc.toString().trim());
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                readNodes(child, lines, depth + 1, visited);
                child.recycle();
            }
        }
    }

    private TextView label(String content, int size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(content);
        v.setTextSize(size);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }
    private int dp(float x) { return Math.round(getResources().getDisplayMetrics().density * x); }

    private void showOverlay(OfferEngine.Result result) {
        removeOverlay();
        int accent;
        String headline;
        switch (result.decision) {
            case ACCEPT: accent = Color.rgb(38, 220, 155); headline = "ACEITAR · Boa oferta"; break;
            case DECLINE: accent = Color.rgb(254, 105, 117); headline = "RECUSAR · Meta não atingida"; break;
            default: accent = Color.rgb(255, 199, 88); headline = "ATENÇÃO · Confira a oferta"; break;
        }
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(17), dp(14), dp(17), dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(16, 26, 40));
        bg.setCornerRadius(dp(18));
        bg.setStroke(dp(1), accent);
        box.setBackground(bg);
        box.setElevation(dp(12));
        box.addView(label("CX ROTA  •  " + result.offer.platform, 12, Color.rgb(174, 189, 210), true));
        box.addView(label(headline, 19, accent, true));
        box.addView(label(OfferEngine.money(result.offer.fare) + "   ·   "
                + OfferEngine.number(result.offer.distanceKm) + " km   ·   "
                + OfferEngine.number(result.offer.minutes) + " min", 15, Color.WHITE, true));
        box.addView(label(OfferEngine.money(result.perKm) + "/km   |   "
                + OfferEngine.money(result.perHour) + "/h", 13, Color.WHITE, false));
        if (result.costConfigured) {
            box.addView(label("Sobra estimada: " + OfferEngine.money(result.estimatedNet), 12, Color.rgb(174, 189, 210), false));
        }
        box.addView(label(result.reason, 11, Color.rgb(174, 189, 210), false));
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP;
        lp.x = dp(12);
        lp.y = dp(65);
        try {
            windowManager.addView(box, lp);
            overlayView = box;
            hideRunnable = this::removeOverlay;
            handler.postDelayed(hideRunnable, 8500);
        } catch (Exception ignored) { }
    }
    private void speak(OfferEngine.Result r) {
        if (!SettingsStore.voice(this) || tts == null) return;
        String message = r.decision == OfferEngine.Decision.ACCEPT ? "Boa corrida" :
                r.decision == OfferEngine.Decision.DECLINE ? "Corrida abaixo da meta" : "Atenção, confira a corrida";
        tts.speak(message + ". " + r.offer.platform + ". " + OfferEngine.money(r.offer.fare),
                TextToSpeech.QUEUE_FLUSH, null, "cx_rota_offer");
    }
    private void removeOverlay() {
        if (hideRunnable != null) handler.removeCallbacks(hideRunnable);
        hideRunnable = null;
        if (overlayView != null) {
            try { windowManager.removeView(overlayView); } catch (Exception ignored) { }
            overlayView = null;
        }
    }
    @Override public void onInterrupt() { removeOverlay(); }
    @Override public void onDestroy() {
        removeOverlay();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (recognizer != null) recognizer.close();
        super.onDestroy();
    }
}
