/* CX Rota • regras matemáticas independentes de interface e contas externas. */
(function(root, factory){const api=factory();if(typeof module==='object'&&module.exports)module.exports=api;root.CXRotaCore=api;})(typeof globalThis!=='undefined'?globalThis:this,function(){
'use strict';
const finite=x=>typeof x==='number'&&Number.isFinite(x);
const nonnegative=x=>finite(x)&&x>=0;
const num=(x,fallback=NaN)=>{if(typeof x==='number')return Number.isFinite(x)?x:fallback;if(x===undefined||x===null||String(x).trim()==='')return fallback;let s=String(x).trim().replace(/\s/g,'').replace(/^R\$/,'');if(s.includes(','))s=s.replace(/\./g,'').replace(',','.');else if(/^\d{1,3}(?:\.\d{3})+$/.test(s))s=s.replace(/\./g,'');let n=Number(s);return Number.isFinite(n)?n:fallback};
const money=n=>Number.isFinite(n)?new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(n):'—';
const number=n=>Number.isFinite(n)?new Intl.NumberFormat('pt-BR',{maximumFractionDigits:2}).format(n):'—';
function validateVehicle(v){
 if(!v || !['own','rented'].includes(v.type))return 'Selecione moto própria ou alugada.';
 if(!['daily','weekly','monthly'].includes(v.period))return 'Informe a periodicidade do aluguel.';
 for(let k of ['rent','fuelPrice','kmPerLiter','maintenance','otherDaily','plannedDailyKm'])if(!nonnegative(v[k]))return 'Confira o valor: '+k;
 if(!Number.isInteger(v.workDaysWeek)||v.workDaysWeek<1||v.workDaysWeek>7)return 'Dias de trabalho por semana devem estar entre 1 e 7.';
 if(!Number.isInteger(v.workDaysMonth)||v.workDaysMonth<1||v.workDaysMonth>31)return 'Dias de trabalho por mês devem estar entre 1 e 31.';
 if(!v.plannedDailyKm)return 'Informe os quilômetros planejados por dia.';
 if(v.type==='rented' && v.rent<=0)return 'Informe o valor do aluguel.';
 return '';
}
function model(v){
 const error=validateVehicle(v);if(error)throw Error(error);
 const rentalDaily=v.type==='rented'?(v.period==='daily'?v.rent:v.period==='weekly'?v.rent/v.workDaysWeek:v.rent/v.workDaysMonth):0;
 const fuelConfigured=v.fuelPrice>0&&v.kmPerLiter>0;
 const fuelPerKm=fuelConfigured?v.fuelPrice/v.kmPerLiter:0;
 const variablePerKm=fuelPerKm+v.maintenance;
 const fixedDaily=rentalDaily+v.otherDaily;
 const costPerKm=variablePerKm+fixedDaily/v.plannedDailyKm;
 return {rentalDaily,fuelPerKm,variablePerKm,fixedDaily,costPerKm,breakEven:fixedDaily+variablePerKm*v.plannedDailyKm,complete:fuelConfigured};
}
function evaluate(o,v,goals){
 const fare=num(o.fare),km=num(o.km),mins=num(o.minutes);
 const kmMin=num(goals.minKm),hourMin=num(goals.minHour);
 if(!finite(fare)||fare<=0||!finite(km)||km<=0||!finite(mins)||mins<=0||!finite(kmMin)||kmMin<=0||!finite(hourMin)||hourMin<=0) return {state:'missing',reason:'Informe valor, distância, tempo e metas positivos.'};
 let m;try{m=model(v)}catch(e){return {state:'missing',reason:e.message}};
 const perKm=fare/km,perHour=fare*60/mins,variableMargin=fare-km*m.variablePerKm,fullNet=fare-km*m.costPerKm;
 let state='review',reason='Uma das metas ainda não foi atingida.';
 if(!o.pickup){reason='O deslocamento até o embarque não foi confirmado.';}
 else if(!m.complete){reason='Complete o preço do combustível e a autonomia da moto.';}
 else if(fullNet<=0){state='decline';reason='O valor não cobre o custo total rateado estimado.';}
 else if(perKm>=kmMin&&perHour>=hourMin){state='accept';reason='A oferta atende às metas e cobre os custos estimados.';}
 else if(perKm<kmMin&&perHour<hourMin){state='decline';reason='A oferta está abaixo das duas metas.';}
 return {state,reason,perKm,perHour,variableMargin,fullNet,fare,km,minutes:mins,costPerKm:m.costPerKm,complete:m.complete};
}
function session(s,v){
 let m=model(v),start=num(s.start),end=num(s.end),uber=num(s.uber,0),ninety=num(s.ninety,0),extras=num(s.extras,0),fuelPaid=num(s.fuelPaid,0),hours=num(s.hours,0);
 if(!nonnegative(start)||!nonnegative(end)||end<start||![uber,ninety,extras,fuelPaid,hours].every(nonnegative))throw Error('Confira o odômetro, horas, ganhos e despesas.');
 const km=end-start,gross=uber+ninety,consumptionFuel=km*m.fuelPerKm,maintenance=km*v.maintenance;
 const fixed=m.fixedDaily,expenses=consumptionFuel+maintenance+fixed+extras;
 const net=gross-expenses;
 return {km,gross,uber,ninety,extras,fuelPaid,consumptionFuel,maintenance,rental:m.rentalDaily,otherFixed:v.otherDaily,expenses,net,hours,perHour:hours?gross/hours:null,netHour:hours?net/hours:null,perKm:km?gross/km:null,complete:m.complete};
}
function limitRecords(records,max){return Array.isArray(records)?records.slice(-max):[]}
return {num,money,number,validateVehicle,model,evaluate,session,limitRecords};
});
