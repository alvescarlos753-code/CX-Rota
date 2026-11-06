(()=>{
'use strict';
const C=window.CXRotaCore,$=id=>document.getElementById(id);
const STORAGE='cxrota-web-v2';
const starterVehicle=()=>({id:'v-'+Date.now(),name:'Minha moto',type:'own',period:'weekly',rent:0,workDaysWeek:6,workDaysMonth:26,fuelPrice:0,kmPerLiter:0,maintenance:0,otherDaily:0,plannedDailyKm:150});
const defaults=()=>({version:2,vehicles:[starterVehicle()],activeVehicle:null,goals:{minKm:1.8,minHour:35},offers:[],sessions:[]});
let db;
try{const raw=localStorage.getItem(STORAGE);db=raw?JSON.parse(raw):defaults();}catch(e){db=defaults();}
if(!db||!Array.isArray(db.vehicles)||!Array.isArray(db.offers)||!Array.isArray(db.sessions)||!db.goals)db=defaults();
if(!db.vehicles.length)db.vehicles=[starterVehicle()];
if(!db.vehicles.some(v=>v.id===db.activeVehicle))db.activeVehicle=db.vehicles[0].id;
let offerPlatform='Uber Moto',vehicleType='own',installEvent=null,toastTimer=null;
let warnedStorage=false;
function save(){try{localStorage.setItem(STORAGE,JSON.stringify(db));}catch(e){
 if(!warnedStorage){warnedStorage=true;const notice=document.createElement('div');notice.className='storage-notice';notice.textContent='Seu navegador bloqueou o armazenamento local. Use o backup JSON antes de fechar esta página.';document.querySelector('.demo-warning').appendChild(notice);}
}}
function toast(msg){const el=$('toast');el.textContent=msg;el.classList.add('visible');clearTimeout(toastTimer);toastTimer=setTimeout(()=>el.classList.remove('visible'),3500);}
const elEsc=s=>String(s??'').replace(/[&<>"']/g,x=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[x]));
const today=()=>{const d=new Date();d.setMinutes(d.getMinutes()-d.getTimezoneOffset());return d.toISOString().slice(0,10);};
const amount=id=>C.num($(id).value,NaN);
const optional=(id,fallback=0)=>$(id).value.trim()===''?fallback:amount(id);
const active=()=>db.vehicles.find(v=>v.id===db.activeVehicle)||db.vehicles[0];
const key=()=>Date.now().toString(36)+'-'+Math.random().toString(36).slice(2,9);
function page(name){document.querySelectorAll('.page').forEach(e=>e.classList.toggle('active',e.id===name));document.querySelectorAll('[data-page]').forEach(e=>e.classList.toggle('active',e.dataset.page===name));window.scrollTo({top:0,behavior:'smooth'});if(name==='dashboard')renderDashboard();if(name==='reports')renderReports();if(name==='vehicles')renderVehicleForm();if(name==='sessions')forecastSession();}
for(const b of document.querySelectorAll('[data-page]'))b.addEventListener('click',()=>page(b.dataset.page));
for(const b of document.querySelectorAll('[data-goto]'))b.addEventListener('click',()=>page(b.dataset.goto));
function displayMoney(v){return C.money(v||0)}
function formatMetric(v){return C.number(v||0)}
function renderDashboard(){
 let all=db.sessions;
 let total=all.reduce((a,s)=>{a.gross+=s.calculated.gross;a.net+=s.calculated.net;a.km+=s.calculated.km;a.uber+=s.calculated.uber;a.ninety+=s.calculated.ninety;a.fuel+=s.calculated.consumptionFuel;a.maintenance+=s.calculated.maintenance;a.rent+=s.calculated.rental;a.fixed+=s.calculated.otherFixed;a.extras+=s.calculated.extras;return a},{gross:0,net:0,km:0,uber:0,ninety:0,fuel:0,maintenance:0,rent:0,fixed:0,extras:0});
 $('metricGross').textContent=displayMoney(total.gross);$('metricNet').textContent=displayMoney(total.net);$('metricKm').textContent=formatMetric(total.km)+' km';$('metricSessions').textContent=all.length+' jornada'+(all.length===1?'':'s')+' registrada'+(all.length===1?'':'s');
 const groups=[['Combustível',total.fuel],['Manutenção',total.maintenance],['Aluguel',total.rent],['Outros fixos',total.fixed],['Extras',total.extras]];
 $('breakdown').innerHTML=all.length?groups.map(([n,v])=>`<div class="breakdown-line"><label>${n}</label><div class="track"><div class="fill" style="width:${Math.max(0,Math.min(100,100*v/Math.max(1,total.gross)))}%"></div></div><strong>${displayMoney(v)}</strong></div>`).join('')+'<div class="divider"></div><div class="itemline"><span>Total de custos</span><b>'+displayMoney(total.gross-total.net)+'</b></div>':'<p class="muted">Finalize uma jornada para visualizar sua distribuição de custos.</p>';
 const totalPlatform=total.uber+total.ninety;
 $('platformBars').innerHTML=[['Uber Moto',total.uber,''],['99 Moto',total.ninety,'ninety']].map(([name,v,cls])=>`<div><div class="platform-summary"><small>${name}</small><b>${displayMoney(v)}</b></div><div class="bar-wrap"><div class="bar-fill ${cls}" style="width:${totalPlatform?Math.min(100,100*v/totalPlatform):0}%"></div></div></div>`).join('');
 const v=active();const m=C.model(v);
 $('vehicleSummary').innerHTML=`<div class="cost-kpi"><small>Custo estimado total por km</small><strong>${C.money(m.costPerKm)}<span style="font-size:13px;color:var(--muted);font-weight:500"> /km</span></strong><div class="hint">${m.complete?'Combustível e custos rateados':'Pendente: informe consumo e combustível'}</div></div><div class="vehicle-details"><div class="itemline"><span>Veículo atual</span><b>${elEsc(v.name)}</b></div><div class="itemline"><span>Modalidade</span><b>${v.type==='rented'?'Alugado':'Próprio'}</b></div><div class="itemline"><span>Aluguel rateado por dia</span><b>${C.money(m.rentalDaily)}</b></div><div class="itemline"><span>Ponto de equilíbrio/dia</span><b>${m.complete?C.money(m.breakEven):'Incompleto'}</b></div></div>`;
}
function offerTabs(){$('offerUber').classList.toggle('selected',offerPlatform==='Uber Moto');$('offer99').classList.toggle('selected',offerPlatform==='99 Moto');}
$('offerUber').onclick=()=>{offerPlatform='Uber Moto';offerTabs();};$('offer99').onclick=()=>{offerPlatform='99 Moto';offerTabs();};
$('offerForm').addEventListener('submit',e=>{e.preventDefault();const o={platform:offerPlatform,fare:amount('offerFare'),km:amount('offerKm'),minutes:amount('offerMinutes'),pickup:$('offerPickup').checked};const r=C.evaluate(o,active(),db.goals);let markup='';
 if(r.state==='missing'){$('offerResult').className='analysis-result review';$('offerResult').innerHTML='<h3>Confira os dados</h3><p>'+elEsc(r.reason)+'</p>';return;}
 const label={accept:'ACEITAR',decline:'RECUSAR',review:'CONFERIR'}[r.state];
 markup=`<div class="eyebrow">${elEsc(o.platform)} · RECOMENDAÇÃO</div><div class="decision">${label}</div><p>${elEsc(r.reason)}</p><div class="result-metrics"><div><small>Ganho bruto/km</small><strong>${C.money(r.perKm)}</strong></div><div><small>Ganho bruto/hora</small><strong>${C.money(r.perHour)}</strong></div><div><small>Margem variável</small><strong>${C.money(r.variableMargin)}</strong></div><div><small>Margem rateada</small><strong>${r.complete?C.money(r.fullNet):'Incompleta'}</strong></div></div><div class="hint">Estimativa com ${C.money(r.costPerKm)}/km de custo rateado. Aluguel não é custo incremental da corrida. Confira sempre a oferta original.</div>`;
 $('offerResult').className='analysis-result '+r.state;$('offerResult').innerHTML=markup;
 db.offers.push({id:key(),timestamp:new Date().toISOString(),vehicleName:active().name,platform:offerPlatform,fare:o.fare,km:o.km,minutes:o.minutes,pickup:o.pickup,result:{state:r.state,perKm:r.perKm,perHour:r.perHour,fullNet:r.fullNet,complete:r.complete}});
 db.offers=C.limitRecords(db.offers,600);save();toast('Oferta avaliada e registrada como simulação.');
});
function readSession(){return {date:$('sessionDate').value,start:amount('sessionStart'),end:amount('sessionEnd'),uber:optional('sessionUber'),ninety:optional('session99'),hours:optional('sessionHours'),fuelPaid:optional('sessionFuel'),extras:optional('sessionExtras'),notes:$('sessionNotes').value.trim()};}
function forecastSession(){
 let r;try{const s=readSession();if(!s.date)throw Error('Selecione a data.');r=C.session(s,active());}catch(e){$('sessionPreview').className='analysis-result idle';$('sessionPreview').innerHTML='<h3>Preencha a jornada</h3><p>'+elEsc(e.message)+'</p>';return null;}
 $('sessionPreview').className='analysis-result '+(r.net>=0?'accept':'decline');
 $('sessionPreview').innerHTML=`<div class="eyebrow">RESULTADO ECONÔMICO ESTIMADO</div><div class="decision">${C.money(r.net)}</div><p>${r.complete?'Com custos completos do veículo':'Cálculo incompleto: configure combustível e consumo da moto para estimar lucro confiável.'}</p><div class="result-metrics"><div><small>Faturamento</small><strong>${C.money(r.gross)}</strong></div><div><small>Quilômetros</small><strong>${C.number(r.km)} km</strong></div><div><small>Custos do dia</small><strong>${C.money(r.expenses)}</strong></div><div><small>Aluguel rateado</small><strong>${C.money(r.rental)}</strong></div></div><div class="itemline"><span>Combustível consumido (estimado)</span><b>${C.money(r.consumptionFuel)}</b></div><div class="itemline"><span>Combustível pago</span><b>${C.money(r.fuelPaid)}</b></div><p class="hint">Pagamento do combustível não é descontado novamente; custo econômico estimado pelo consumo durante a jornada.</p>`;
 return r;
}
for(const input of $('sessionForm').querySelectorAll('input,textarea'))input.addEventListener('input',forecastSession);
$('sessionForm').addEventListener('submit',e=>{e.preventDefault();const s=readSession();let r;try{r=C.session(s,active());}catch(e){toast(e.message);return;}if(!s.date){toast('Informe a data da jornada.');return;}
 if(!r.complete && !confirm('O preço do combustível ou o consumo da moto ainda não foi configurado. Deseja registrar mesmo assim como estimativa incompleta?'))return;
 db.sessions.push({id:key(),created:new Date().toISOString(),vehicleId:active().id,vehicleName:active().name,profileSnapshot:{...active()},date:s.date,form:s,calculated:r});db.sessions=C.limitRecords(db.sessions,1800);save();renderDashboard();toast('Jornada registrada!');page('dashboard');
});
function renderVehicleForm(){
 const v=active();vehicleType=v.type;
 $('vehicleSelect').innerHTML=db.vehicles.map(x=>`<option value="${elEsc(x.id)}" ${x.id===v.id?'selected':''}>${elEsc(x.name)}</option>`).join('');
 $('vehicleName').value=v.name;$('typeOwn').classList.toggle('selected',vehicleType==='own');$('typeRent').classList.toggle('selected',vehicleType==='rented');$('rentFields').hidden=vehicleType!=='rented';
 for(const [id,val] of [['rentValue',v.rent],['daysWeek',v.workDaysWeek],['daysMonth',v.workDaysMonth],['fuelPrice',v.fuelPrice||''],['kmPerLiter',v.kmPerLiter||''],['maintPerKm',v.maintenance],['otherDaily',v.otherDaily],['plannedDailyKm',v.plannedDailyKm],['goalKm',db.goals.minKm],['goalHour',db.goals.minHour]])$(id).value=val;
 $('rentPeriod').value=v.period;previewCost();
}
function readVehicle(){return {id:active().id,name:$('vehicleName').value.trim(),type:vehicleType,period:$('rentPeriod').value,rent:optional('rentValue'),workDaysWeek:amount('daysWeek'),workDaysMonth:amount('daysMonth'),fuelPrice:optional('fuelPrice'),kmPerLiter:optional('kmPerLiter'),maintenance:optional('maintPerKm'),otherDaily:optional('otherDaily'),plannedDailyKm:amount('plannedDailyKm')};}
function previewCost(){try{const v=readVehicle(),m=C.model(v);$('costPreview').innerHTML=`<div class="cost-big"><small>Custo total rateado por km</small><strong>${C.money(m.costPerKm)}</strong><div class="hint">Inclui variável + fixo distribuído pelo km planejado</div></div><div class="itemline"><span>Combustível por km</span><b>${C.money(m.fuelPerKm)}</b></div><div class="itemline"><span>Manutenção por km</span><b>${C.money(v.maintenance)}</b></div><div class="itemline"><span>Aluguel rateado por dia</span><b>${C.money(m.rentalDaily)}</b></div><div class="itemline"><span>Outros fixos por dia</span><b>${C.money(v.otherDaily)}</b></div><div class="divider"></div><div class="itemline"><span>Meta de faturamento bruto/dia para cobrir custos*</span><b>${m.complete?C.money(m.breakEven):'Pendente'}</b></div><p class="hint">*Com ${C.number(v.plannedDailyKm)} km previstos. Não inclui remuneração pessoal, tributos nem imprevistos não cadastrados.</p>${m.complete?'':'<p class="notice">Preencha preço do combustível e consumo médio para avaliar lucro.</p>'}`;}catch(e){$('costPreview').innerHTML='<p class="notice">'+elEsc(e.message)+'</p>';}}
for(const n of $('vehicleForm').querySelectorAll('input,select'))n.addEventListener('input',previewCost);
$('typeOwn').onclick=()=>{vehicleType='own';$('rentFields').hidden=true;$('typeOwn').classList.add('selected');$('typeRent').classList.remove('selected');previewCost();};
$('typeRent').onclick=()=>{vehicleType='rented';$('rentFields').hidden=false;$('typeRent').classList.add('selected');$('typeOwn').classList.remove('selected');previewCost();};
$('vehicleForm').addEventListener('submit',e=>{e.preventDefault();const v=readVehicle(),g={minKm:amount('goalKm'),minHour:amount('goalHour')};const error=C.validateVehicle(v);if(error){toast(error);return;}if(!v.name){toast('Dê um nome para o veículo.');return;}if(!(g.minKm>0&&g.minHour>0)){toast('Metas devem ser positivas.');return;}
 db.vehicles=db.vehicles.map(old=>old.id===db.activeVehicle?v:old);db.goals=g;save();toast('Veículo e metas salvos.');renderDashboard();renderVehicleForm();});
$('vehicleSelect').addEventListener('change',e=>{db.activeVehicle=e.target.value;save();renderVehicleForm();renderDashboard();});
$('newVehicle').onclick=()=>{const v=starterVehicle();v.id='v-'+key();v.name='Novo veículo '+(db.vehicles.length+1);db.vehicles.push(v);db.activeVehicle=v.id;save();renderVehicleForm();toast('Novo veículo adicionado. Configure os custos.');};
$('deleteVehicle').onclick=()=>{if(db.vehicles.length===1){toast('É necessário manter ao menos um veículo.');return;}if(!confirm('Excluir este veículo do cadastro? Jornadas anteriores serão mantidas.'))return;db.vehicles=db.vehicles.filter(v=>v.id!==db.activeVehicle);db.activeVehicle=db.vehicles[0].id;save();renderVehicleForm();renderDashboard();toast('Veículo excluído.');};
function renderReports(){const sessions=[...db.sessions].reverse();$('sessionHistory').innerHTML=sessions.length?sessions.map(x=>`<div class="list-row"><div><b>${elEsc(x.date)} · ${elEsc(x.vehicleName)}</b><small>Uber ${displayMoney(x.calculated.uber)} + 99 ${displayMoney(x.calculated.ninety)} · ${C.number(x.calculated.km)} km ${x.calculated.complete?'':' · ⚠ custos parciais'}</small></div><div class="amount ${x.calculated.net>=0?'positive':'negative'}">${C.money(x.calculated.net)}<div><button data-remove-session="${elEsc(x.id)}" aria-label="Excluir jornada">Excluir</button></div></div></div>`).join(''):'<div class="empty">Nenhuma jornada registrada.</div>';
 const offers=[...db.offers].reverse();$('offerHistory').innerHTML=offers.length?offers.slice(0,150).map(x=>`<div class="list-row"><div><b>${elEsc(x.platform)} · ${C.money(x.fare)}</b><small>${new Date(x.timestamp).toLocaleString('pt-BR')} · ${C.number(x.km)} km · ${C.number(x.minutes)} min · ${elEsc(x.vehicleName)}</small></div><div class="amount">${{accept:'ACEITAR',decline:'RECUSAR',review:'CONFERIR'}[x.result.state]||'CONFERIR'}<div><button data-remove-offer="${elEsc(x.id)}" aria-label="Excluir oferta">Excluir</button></div></div></div>`).join(''):'<div class="empty">Nenhuma oferta avaliada.</div>';
 for(const btn of document.querySelectorAll('[data-remove-session]'))btn.onclick=()=>{if(!confirm('Excluir o registro desta jornada?'))return;db.sessions=db.sessions.filter(x=>x.id!==btn.dataset.removeSession);save();renderReports();renderDashboard();};
 for(const btn of document.querySelectorAll('[data-remove-offer]'))btn.onclick=()=>{db.offers=db.offers.filter(x=>x.id!==btn.dataset.removeOffer);save();renderReports();};
}
function dl(content,type,name){const blob=new Blob([content],{type}),url=URL.createObjectURL(blob),a=document.createElement('a');a.href=url;a.download=name;document.body.appendChild(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(url),1000);}
function csvCell(value){return '"'+String(value??'').replace(/"/g,'""')+'"'}
function csv(rows){return '\ufeff'+rows.map(row=>row.map(csvCell).join(';')).join('\r\n')}
$('exportSessions').onclick=()=>dl(csv([['data','veiculo','km','receita_uber','receita_99','faturamento','combustivel_consumido_estimado','manutencao','aluguel_rateado','outros_fixos','extras','custo_total','lucro_estimado','custos_completos'],...db.sessions.map(s=>[s.date,s.vehicleName,s.calculated.km,s.calculated.uber,s.calculated.ninety,s.calculated.gross,s.calculated.consumptionFuel,s.calculated.maintenance,s.calculated.rental,s.calculated.otherFixed,s.calculated.extras,s.calculated.expenses,s.calculated.net,s.calculated.complete])]),'text/csv;charset=utf-8','cx-rota-jornadas.csv');
$('exportOffers').onclick=()=>dl(csv([['data_hora','veiculo','plataforma','valor','km','minutos','embarque_incluido','recomendacao','margem_estimada'],...db.offers.map(o=>[o.timestamp,o.vehicleName,o.platform,o.fare,o.km,o.minutes,o.pickup,o.result.state,o.result.fullNet])]),'text/csv;charset=utf-8','cx-rota-ofertas.csv');
$('exportBackup').onclick=()=>{dl(JSON.stringify({...db,exportedAt:new Date().toISOString()},null,2),'application/json','cx-rota-backup-'+today()+'.json');toast('Backup salvo. Guarde em local seguro.');};
$('importBackup').onchange=async(e)=>{const file=e.target.files?.[0];if(!file)return;try{if(file.size>3*1024*1024)throw Error('O arquivo excede 3 MB.');let data=JSON.parse(await file.text());if(data.version!==2||!Array.isArray(data.vehicles)||!data.vehicles.length||!Array.isArray(data.sessions)||!Array.isArray(data.offers)||!data.goals)throw Error('Backup incompatível.');if(data.vehicles.length>30||data.sessions.length>1800||data.offers.length>600)throw Error('Backup excede os limites suportados.');for(const v of data.vehicles)if(C.validateVehicle(v))throw Error('Há veículos inválidos no backup.');if(!confirm('Substituir todos os seus dados locais pelo backup selecionado?'))return;db={version:2,vehicles:data.vehicles,activeVehicle:data.activeVehicle,goals:data.goals,sessions:data.sessions,offers:data.offers};if(!db.vehicles.some(v=>v.id===db.activeVehicle))db.activeVehicle=db.vehicles[0].id;save();renderVehicleForm();renderReports();renderDashboard();toast('Backup restaurado.');}catch(err){toast('Não foi possível restaurar: '+err.message)}finally{e.target.value='';}};
$('wipeData').onclick=()=>{if(!confirm('Isso apagará todos os registros e veículos deste navegador. Deseja continuar?'))return;if(!confirm('Confirme novamente: apagar tudo sem possibilidade de recuperação local?'))return;db=defaults();db.activeVehicle=db.vehicles[0].id;save();renderVehicleForm();renderReports();renderDashboard();toast('Dados locais apagados.');};
if('serviceWorker' in navigator)navigator.serviceWorker.register('./sw.js').catch(()=>{});
window.addEventListener('beforeinstallprompt',e=>{e.preventDefault();installEvent=e;$('installBtn').hidden=false;});
$('installBtn').onclick=async()=>{if(!installEvent){toast('No Chrome, use ⋮ > Adicionar à tela inicial.');return;}installEvent.prompt();await installEvent.userChoice;installEvent=null;$('installBtn').hidden=true;};
$('sessionDate').value=today();
// Pré-preenchimento opcional e explícito; outros usuários começam com odômetro vazio.
try {const v=C.num(new URLSearchParams(location.hash.slice(1)).get('kmInicio'));
    if(Number.isFinite(v)&&v>=0&&v<9999999)$('sessionStart').value=String(v);
} catch(e) {}
save();renderVehicleForm();renderDashboard();renderReports();forecastSession();offerTabs();
})();
