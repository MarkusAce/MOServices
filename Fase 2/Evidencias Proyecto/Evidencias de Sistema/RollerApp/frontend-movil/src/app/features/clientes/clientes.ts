import { Subscription } from 'rxjs';
import { IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton } from '@ionic/angular';
import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { environment } from '../../../environments/environment';
import { Usuario } from '../../core/models/usuario.model';
import { Cotizacion } from '../../core/models/cotizacion.model';
import { Pedido } from '../../core/models/pedido.model';
import { etiquetaEstado } from '../../core/utils/estado-pedido';
interface Ficha{cliente:Usuario;telefono:string|null;direcciones:string[];cotizaciones:Cotizacion[];pedidos:Pedido[];visitas:{id:number;fecha:string;horaInicio:string;estado:string;tecnico:string|null;cotizacionId:number}[];}
@Component({selector:'app-clientes',imports: [IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, RouterLink,CurrencyPipe,DatePipe],templateUrl:'./clientes.html',styleUrl:'./clientes.scss'})
export class ClientesComponent implements OnInit, OnDestroy {
 private http=inject(HttpClient);private route=inject(ActivatedRoute);clientes=signal<Usuario[]>([]);ficha=signal<Ficha|null>(null);error=signal('');cargando=signal(true);etiquetaEstado=etiquetaEstado;
 private consulta?:Subscription;
 private parametros?:Subscription;
 private activa=false;
 private clienteId=this.route.snapshot.paramMap.get('id');
 ngOnInit():void{this.parametros=this.route.paramMap.subscribe(params=>{this.clienteId=params.get('id');if(this.activa)this.cargar();});}
 ionViewWillEnter():void{this.activa=true;this.cargar();}
 ionViewWillLeave():void{this.activa=false;this.consulta?.unsubscribe();}
 ngOnDestroy():void{this.parametros?.unsubscribe();this.consulta?.unsubscribe();}
 cargar():void{this.consulta?.unsubscribe();this.error.set('');this.cargando.set(true);this.ficha.set(null);const id=this.clienteId;
 if(id)this.consulta=this.http.get<Ficha>(environment.apiUrl+'/admin/clientes/'+id).subscribe({next:f=>{this.ficha.set(f);this.cargando.set(false);},error:()=>{this.error.set('No se pudo cargar la ficha.');this.cargando.set(false);}});
 else this.consulta=this.http.get<Usuario[]>(environment.apiUrl+'/admin/clientes').subscribe({next:c=>{this.clientes.set(c);this.cargando.set(false);},error:()=>{this.error.set('No se pudieron cargar los clientes.');this.cargando.set(false);}});}
}
