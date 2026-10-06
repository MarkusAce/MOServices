import { IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton } from '@ionic/angular';
import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';
import { PostventaService } from '../../../core/services/postventa.service';
import { ToastService } from '../../../core/services/toast.service';
import { DialogoMovilService } from '../../../core/services/dialogo-movil.service';
import { PostventaSolicitud } from '../../../core/models/postventa.model';
@Component({selector:'app-admin-postventa',imports:[IonContent,IonHeader,IonToolbar,IonTitle,IonButtons,IonBackButton,DatePipe],templateUrl:'./admin-postventa.html',styleUrl:'./admin-postventa.scss'})
export class AdminPostventaComponent {
 private api=inject(PostventaService);private toast=inject(ToastService);private dialogos=inject(DialogoMovilService);
 solicitudes=signal<PostventaSolicitud[]>([]);cargando=signal(false);procesando=signal(false);error=signal('');
 ionViewWillEnter():void{this.cargar();}
 cargar():void{this.cargando.set(true);this.error.set('');this.api.listarTodas().pipe(finalize(()=>this.cargando.set(false))).subscribe({next:s=>this.solicitudes.set(s),error:()=>this.error.set('No se pudieron cargar las solicitudes.')});}
 async marcarResuelta(s:PostventaSolicitud):Promise<void>{if(this.procesando())return;if(!await this.dialogos.confirmar('Resolver solicitud',`¿Cerrar la solicitud #${s.id} de ${s.nombre}?`,'Marcar resuelta'))return;if(this.procesando())return;this.procesando.set(true);this.api.cambiarEstado(s.id,'RESUELTA').pipe(finalize(()=>this.procesando.set(false))).subscribe({next:()=>{this.toast.mostrar('Solicitud resuelta.','exito');this.cargar();},error:e=>this.toast.mostrar(e.error?.mensaje ?? e.error?.error ?? 'No se pudo actualizar.','error')});}
 async eliminar(s:PostventaSolicitud):Promise<void>{if(this.procesando())return;if(!await this.dialogos.confirmar('Eliminar solicitud',`¿Eliminar definitivamente la solicitud #${s.id}?`,'Eliminar',true))return;if(this.procesando())return;this.procesando.set(true);this.api.eliminar(s.id).pipe(finalize(()=>this.procesando.set(false))).subscribe({next:()=>{this.toast.mostrar('Solicitud eliminada.','exito');this.cargar();},error:e=>this.toast.mostrar(e.error?.error ?? 'No se pudo eliminar.','error')});}
}
