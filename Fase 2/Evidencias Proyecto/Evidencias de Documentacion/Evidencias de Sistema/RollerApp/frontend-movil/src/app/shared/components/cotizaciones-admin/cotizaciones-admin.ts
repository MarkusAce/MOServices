import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Cotizacion } from '../../../core/models/cotizacion.model';
import { environment } from '../../../../environments/environment';
@Component({selector:'app-cotizaciones-admin',imports:[CurrencyPipe,DatePipe],template:`<h2>Cotizaciones y compras pendientes</h2><p>Antes del pago, la compra se conserva como cotización pendiente. El pedido se crea al confirmar Webpay.</p>
@if(error()){<p role="alert">No se pudieron cargar las cotizaciones.</p>}
@for(c of datos();track c.id){<article>Cotización #{{c.id}} · {{c.productoNombre}} · {{c.estado==='PENDIENTE_PAGO'?'Pendiente de pago':c.estado}}<p>{{c.total | currency:'CLP':'symbol-narrow':'1.0-0'}} · {{c.creadoEn | date:'dd/MM/yyyy'}} · {{c.direccion}}, {{c.comuna}}</p></article>}
<button (click)="cargar(pagina-1)" [disabled]="pagina===0 || ocupado()">Anterior</button><span> Página {{pagina+1}} de {{total || 1}} </span><button (click)="cargar(pagina+1)" [disabled]="pagina+1>=total || ocupado()">Siguiente</button>`,styleUrl:'./cotizaciones-admin.scss'})
export class CotizacionesAdminComponent implements OnInit {
 private http=inject(HttpClient);datos=signal<Cotizacion[]>([]);error=signal(false);ocupado=signal(false);pagina=0;total=0;
 ngOnInit():void{this.cargar(0);}
 cargar(p:number):void{this.ocupado.set(true);this.error.set(false);this.http.get<{content:Cotizacion[];totalPages:number}>(environment.apiUrl+'/cotizaciones/admin',{params:{pagina:p,cantidad:20}}).subscribe({next:r=>{this.pagina=p;this.total=r.totalPages;this.datos.set(r.content);this.ocupado.set(false);},error:()=>{this.ocupado.set(false);this.error.set(true);}});}
}
