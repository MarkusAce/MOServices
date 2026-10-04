import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { environment } from '../../../../environments/environment';
interface Correo{id:number;destinatario:string;asunto:string;estado:string;intentos:number;creadoEn:string;enviadoEn:string|null;}
interface Estado{smtpConfigurado:boolean;correoEmpresaConfigurado:boolean;webpayAmbiente:string;retornoHttps:boolean;}
@Component({selector:'app-comunicaciones',imports:[DatePipe],templateUrl:'./comunicaciones.html',styleUrl:'./comunicaciones.scss'})
export class ComunicacionesComponent implements OnInit{
 private http=inject(HttpClient);private url=environment.apiUrl+'/admin/comunicaciones';correos=signal<Correo[]>([]);estado=signal<Estado|null>(null);error=signal('');ocupado=signal(false);
 ngOnInit():void{this.cargar();}
 cargar():void{this.error.set('');this.http.get<Estado>(this.url+'/estado').subscribe({next:e=>this.estado.set(e),error:()=>this.error.set('No se pudo consultar la configuración.')});this.http.get<Correo[]>(this.url).subscribe({next:c=>this.correos.set(c),error:()=>this.error.set('No se pudieron consultar los correos.')});}
 reintentar(id:number):void{if(this.ocupado())return;this.ocupado.set(true);this.http.post(this.url+'/'+id+'/reintentar',{}).subscribe({next:()=>{this.ocupado.set(false);this.cargar();},error:e=>{this.ocupado.set(false);this.error.set(e.error?.mensaje ?? e.error?.error ?? 'No se pudo reintentar el envío.');}});}
}
