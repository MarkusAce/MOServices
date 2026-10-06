import { Subscription, switchMap, of } from 'rxjs';
import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { environment } from '../../../environments/environment';
interface Campo {id:number;clave:string;etiqueta:string;tipo:string;obligatorio:boolean;activo:boolean;}
@Component({selector:'app-facturacion',imports:[FormsModule,RouterLink],templateUrl:'./facturacion.html',styleUrl:'./facturacion.scss'})
export class FacturacionComponent implements OnInit, OnDestroy {
 private http=inject(HttpClient); private route=inject(ActivatedRoute); private base=environment.apiUrl+'/facturacion';
 cargando=signal(false);campos=signal<Campo[]>([]);valores:Record<string,string>={};mensaje=signal('');ocupado=signal(false);listo=signal(false);
 admin=this.route.snapshot.data['configurar']===true; clienteId=this.route.snapshot.paramMap.get('id');
 nuevo={clave:'',etiqueta:'',tipo:'TEXTO',obligatorio:false,activo:true}; editando:number|null=null;
 private consulta?: Subscription;
 private parametros?: Subscription;
 ngOnInit(){this.parametros=this.route.paramMap.subscribe(()=>this.cargar());}
 ngOnDestroy(){this.parametros?.unsubscribe();this.consulta?.unsubscribe();}
 cargar(){
  this.consulta?.unsubscribe();
  this.admin=this.route.snapshot.data['configurar']===true;
  this.clienteId=this.route.snapshot.paramMap.get('id');
  this.valores={};this.campos.set([]);this.cargando.set(true);this.listo.set(false);this.mensaje.set('');
  this.consulta=this.http.get<Campo[]>(this.base+'/campos').pipe(switchMap(campos=>{
   this.campos.set(campos);
   return this.admin ? of({valores:{} as Record<string,string>}) : this.http.get<{valores:Record<string,string>}>(this.base+(this.clienteId ? '/clientes/'+this.clienteId : '/mis-datos'));
  })).subscribe({next:datos=>{this.valores=datos.valores;this.listo.set(true);this.cargando.set(false);},error:()=>{this.cargando.set(false);this.mensaje.set('No se pudieron cargar los datos de facturación. Intenta nuevamente.');}});
 }
 editar(c:Campo){this.editando=c.id;this.nuevo={...c};}
 cancelar(){this.editando=null;this.nuevo={clave:'',etiqueta:'',tipo:'TEXTO',obligatorio:false,activo:true};}
 guardarCampo(){if(this.ocupado()||!this.listo()||!this.nuevo.etiqueta.trim()||!/^([a-z][a-z0-9_]{0,49})$/.test(this.nuevo.clave))return;this.ocupado.set(true);const req=this.editando===null ? this.http.post<Campo[]>(this.base+'/campos',this.nuevo) : this.http.put<Campo[]>(this.base+'/campos/'+this.editando,this.nuevo);req.subscribe({next:c=>{this.campos.set(c);this.cancelar();this.ocupado.set(false);this.mensaje.set('Campo guardado.');},error:e=>this.fallar(e)});}
 guardar(){if(this.ocupado()||!this.listo()||this.clienteId)return;this.ocupado.set(true);this.http.put<{valores:Record<string,string>}>(this.base+'/mis-datos',{valores:this.valores}).subscribe({next:d=>{this.valores=d.valores;this.ocupado.set(false);this.mensaje.set('Datos de facturación guardados.');},error:e=>this.fallar(e)});}
 private fallar(e:any){this.ocupado.set(false);this.mensaje.set(e.error?.mensaje??e.error?.detail??e.error?.error??'No se pudo guardar.');}
}
