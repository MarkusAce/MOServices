import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonContent, IonHeader, IonToolbar, IonTitle } from '@ionic/angular';
import { AuthService } from '../../core/services/auth.service';
interface Acceso { titulo: string; detalle: string; ruta: string; parametros?: Record<string,string>; }
@Component({selector:'app-herramientas',imports:[RouterLink,IonContent,IonHeader,IonToolbar,IonTitle],templateUrl:'./herramientas.html',styleUrl:'./herramientas.scss'})
export class HerramientasComponent {
 auth=inject(AuthService);
 comercial:Acceso[]=[{titulo:'Clientes',detalle:'Contactos e historial',ruta:'/tabs/clientes'},{titulo:'Pedidos de venta',detalle:'Ventas y seguimiento',ruta:'/tabs/admin-pedidos'}];
 administracion:Acceso[]=[
 {titulo:'Productos',detalle:'Catálogo e imágenes',ruta:'/tabs/admin-productos'},
 {titulo:'Mecanismos',detalle:'Disponibilidad y precios',ruta:'/tabs/admin-mecanismos'},
 {titulo:'Facturación',detalle:'Campos configurables del cliente',ruta:'/tabs/facturacion/configurar'},
 {titulo:'Telas',detalle:'Disponibilidad y precios',ruta:'/tabs/admin-telas'},
 {titulo:'Servicios',detalle:'Precios y comisión técnica',ruta:'/tabs/admin-servicios'},
 {titulo:'Agenda',detalle:'Crear horarios y asignar técnicos',ruta:'/tabs/admin-agenda'},
 {titulo:'Usuarios',detalle:'Crear personal y cambiar permisos',ruta:'/tabs/admin-usuarios'},
 {titulo:'Comisiones',detalle:'Consultar y marcar pagadas',ruta:'/tabs/admin-comisiones'},
 {titulo:'Postventa',detalle:'Resolver solicitudes de clientes',ruta:'/tabs/admin-postventa'},
 {titulo:'Incidencias de pago',detalle:'Conciliar pagos y asignar visitas',ruta:'/tabs/admin-incidencias'},
 {titulo:'Comunicaciones',detalle:'Historial y reintento de correos',ruta:'/tabs/admin-comunicaciones'},
 {titulo:'Cotizaciones',detalle:'Presupuestos y paginación',ruta:'/tabs/admin-operaciones',parametros:{seccion:'cotizaciones'}},
 {titulo:'Auditoría',detalle:'Actividad administrativa',ruta:'/tabs/admin-operaciones',parametros:{seccion:'auditoria'}}];
 publicos:Acceso[]=[{titulo:'Servicios disponibles',detalle:'Qué ofrecemos y sus precios',ruta:'/tabs/servicios'},{titulo:'Reseñas',detalle:'Opiniones y experiencia',ruta:'/tabs/resenas'},{titulo:'Solicitar postventa',detalle:'Ayuda con una compra',ruta:'/tabs/postventa'}];
}
