import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';
import { AgendaBloque } from '../../../core/models/agenda.model';
@Component({selector:'app-calendario',template:`
<section aria-label="Calendario de disponibilidad" class="calendario">
<header><button type="button" (click)="mover(-1)" [disabled]="disabled" aria-label="Mes anterior">‹</button><strong aria-live="polite">{{titulo()}}</strong><button type="button" (click)="mover(1)" [disabled]="disabled" aria-label="Mes siguiente">›</button></header>
<div class="dias"><span>L</span><span>M</span><span>M</span><span>J</span><span>V</span><span>S</span><span>D</span>
@for(d of dias();track $index){@if(d){<button type="button" [disabled]="disabled || !disponible(d)" [class.seleccionado]="fecha===d" [attr.aria-label]="d + (disponible(d)?', horarios disponibles':', sin horarios')" [attr.aria-pressed]="fecha===d" (click)="fecha=d">{{numero(d)}}</button>}@else{<span></span>}}</div>
<p>Selecciona un día habilitado y luego un horario.</p>
<div class="horarios">@for(b of horarios();track b.id){<button type="button" [disabled]="disabled" [class.seleccionado]="selectedId===b.id" [attr.aria-pressed]="selectedId===b.id" (click)="seleccionar.emit(b.id)">{{b.horaInicio.slice(0,5)}} – {{b.horaFin.slice(0,5)}}</button>}</div>
@if(!horarios().length){<p>No hay horarios disponibles para el día seleccionado.</p>}
</section>`,styles:[`:host{display:block}.calendario{border:1px solid #d4d9dc;border-radius:12px;padding:1rem;background:white;color:#172b32}header{display:flex;justify-content:space-between;align-items:center;margin-bottom:.8rem}.dias{display:grid;grid-template-columns:repeat(7,minmax(0,1fr));gap:.3rem;text-align:center}.dias button{min-height:40px;padding:.4rem;border:1px solid #9db8bf;background:#eef7f7;border-radius:6px;color:#17343c;cursor:pointer}.dias button:disabled{border-color:transparent;background:#f5f5f5;color:#999;cursor:default}.dias .seleccionado,.horarios .seleccionado{background:#17343c;color:white}.horarios{display:flex;flex-wrap:wrap;gap:.5rem}.horarios button,header button{padding:.6rem;border:1px solid #9db8bf;border-radius:6px;background:white;color:#17343c}button:focus-visible{outline:3px solid #287b98;outline-offset:2px}p{font-size:.9rem}`]})
export class CalendarioComponent implements OnChanges {
 @Input() bloques:AgendaBloque[]=[];@Input() disabled=false;@Input() selectedId=0;
 @Output() seleccionar=new EventEmitter<number>();
 private firma='';
 mes=new Date(new Date().getFullYear(),new Date().getMonth(),1);fecha='';
 ngOnChanges():void {const firma=this.bloques.map(b=>`${b.id}:${b.fecha}:${b.horaInicio}`).join('|');if(firma===this.firma)return;this.firma=firma;const orden=[...this.bloques].sort((a,b)=>a.fecha.localeCompare(b.fecha));if(!orden.some(b=>b.fecha===this.fecha)){this.fecha=orden[0]?.fecha??'';if(this.fecha){const [y,m]=this.fecha.split('-').map(Number);this.mes=new Date(y,m-1,1);}}}
 titulo():string{return this.mes.toLocaleDateString('es-CL',{month:'long',year:'numeric'});}
 mover(n:number):void{this.mes=new Date(this.mes.getFullYear(),this.mes.getMonth()+n,1);this.fecha='';}
 dias():string[]{const y=this.mes.getFullYear(),m=this.mes.getMonth();const vacios=(this.mes.getDay()+6)%7;return [...Array(vacios).fill(''),...Array.from({length:new Date(y,m+1,0).getDate()},(_,i)=>`${y}-${String(m+1).padStart(2,'0')}-${String(i+1).padStart(2,'0')}`)];}
 disponible(d:string):boolean{return this.bloques.some(b=>b.fecha===d);}
 numero(d:string):number{return Number(d.slice(-2));}
 horarios():AgendaBloque[]{return this.bloques.filter(b=>b.fecha===this.fecha).sort((a,b)=>a.horaInicio.localeCompare(b.horaInicio));}
}
