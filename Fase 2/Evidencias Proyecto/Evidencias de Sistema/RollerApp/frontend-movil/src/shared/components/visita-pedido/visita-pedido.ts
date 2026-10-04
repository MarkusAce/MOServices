import { Component, Input, Output, EventEmitter, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Pedido } from '../../../core/models/pedido.model';
import { AgendaBloque } from '../../../core/models/agenda.model';
import { CalendarioComponent } from '../calendario/calendario';
@Component({selector:'app-visita-pedido',imports:[CalendarioComponent],template:`
@if(pedido.direccionPendienteVerificacion){<p role="status">Dirección pendiente de revisión: {{pedido.referenciasDireccion || 'Sin referencias adicionales'}}</p><button type="button" (click)="confirmacion='direccion'" [disabled]="ocupado()">Verificar dirección</button>}
@if(pedido.requiereVisita && pedido.visitaEstado!=='COMPLETADO'){<button type="button" (click)="abrir()" [disabled]="ocupado()">{{pedido.visitaId?'Reagendar visita':'Asignar nueva visita'}}</button>
@if(pedido.visitaEstado==='RESERVADO'){<button type="button" (click)="confirmacion='cancelar'" [disabled]="ocupado()">Cancelar visita</button>}}
@if(confirmacion){<p>{{confirmacion==='cancelar'?'¿Cancelar esta visita? El pago se conserva. Esta acción no reembolsa ni cancela el pedido.':'Confirma que revisaste la dirección y sus referencias antes de autorizar el envío del técnico.'}}</p><button (click)="confirmar()" [disabled]="ocupado()">Confirmar</button><button (click)="confirmacion=''" [disabled]="ocupado()">Volver</button>}
@if(mostrar()){<app-calendario [bloques]="bloques()" [disabled]="ocupado()" (seleccionar)="nuevo=$event" [selectedId]="nuevo" /><button (click)="reagendar()" [disabled]="!nuevo || ocupado()">Confirmar nuevo horario</button><button (click)="mostrar.set(false)" [disabled]="ocupado()">Cerrar</button>}
@if(error()){<p role="alert">{{error()}}</p>}`,styleUrl:'./visita-pedido.scss'})
export class VisitaPedidoComponent{
 @Input({required:true}) pedido!:Pedido;@Output() actualizado=new EventEmitter<void>();
 private http=inject(HttpClient);bloques=signal<AgendaBloque[]>([]);mostrar=signal(false);ocupado=signal(false);error=signal('');nuevo=0;confirmacion='';
 abrir():void{this.error.set('');this.nuevo=0;this.http.get<AgendaBloque[]>(environment.apiUrl+'/agenda/disponibles').subscribe({next:b=>{this.bloques.set(b);this.mostrar.set(true);},error:()=>this.error.set('No se pudieron cargar los horarios.')});}
 confirmar():void{if(this.confirmacion==='cancelar')this.enviar(this.http.delete<Pedido>(`${environment.apiUrl}/admin/pedidos/${this.pedido.id}/visita`));else this.enviar(this.http.put<Pedido>(`${environment.apiUrl}/admin/pedidos/${this.pedido.id}/direccion/verificar`,{}));}
 reagendar():void{if(this.nuevo)this.enviar(this.http.put<Pedido>(`${environment.apiUrl}/admin/pedidos/${this.pedido.id}/visita`,{bloqueId:this.nuevo}));}
 private enviar(obs:import('rxjs').Observable<Pedido>):void{if(this.ocupado())return;this.ocupado.set(true);this.error.set('');obs.subscribe({next:()=>{this.ocupado.set(false);this.confirmacion='';this.mostrar.set(false);this.actualizado.emit();},error:e=>{this.ocupado.set(false);this.error.set(e.error?.mensaje ?? e.error?.detail ?? e.error?.error ?? 'No se pudo actualizar la visita.');}});}
}
