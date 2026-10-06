import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { rolGuard } from './core/guards/rol.guard';

export const routes: Routes = [
  { path: 'facturacion', canActivate: [authGuard], loadComponent: () => import('./features/facturacion/facturacion').then(m => m.FacturacionComponent) },
  { path: 'facturacion/clientes/:id', canActivate: [rolGuard(['ADMIN','VENDEDOR'])], loadComponent: () => import('./features/facturacion/facturacion').then(m => m.FacturacionComponent) },
  { path: 'facturacion/configurar', canActivate: [rolGuard(['ADMIN'])], data: {configurar:true}, loadComponent: () => import('./features/facturacion/facturacion').then(m => m.FacturacionComponent) },
  { path: 'login', loadComponent: () => import('./features/auth/login/login').then((m) => m.LoginComponent) },
  { path: 'registro', loadComponent: () => import('./features/auth/registro/registro').then((m) => m.RegistroComponent) },
  { path: 'servicios', loadComponent: () => import('./features/servicios/servicios').then(m => m.ServiciosComponent) },
  { path: 'catalogo', loadComponent: () => import('./features/catalogo/catalogo').then((m) => m.CatalogoComponent) },
  { path: 'productos/:id', loadComponent: () => import('./features/producto-detalle/producto-detalle').then((m) => m.ProductoDetalleComponent) },
  { path: 'carrito', canActivate: [authGuard], loadComponent: () => import('./features/carrito/carrito').then(m => m.CarritoComponent) },
  { path: 'agenda', canActivate: [authGuard], loadComponent: () => import('./features/agenda/agenda').then((m) => m.AgendaComponent) },
  { path: 'pago/retorno', canActivate: [authGuard], loadComponent: () => import('./features/pago/pago-retorno').then((m) => m.PagoRetornoComponent) },
  { path: 'clientes', canActivate: [rolGuard(['ADMIN','VENDEDOR'])], loadComponent: () => import('./features/clientes/clientes').then(m=>m.ClientesComponent) },
  { path: 'clientes/:id', canActivate: [rolGuard(['ADMIN','VENDEDOR'])], loadComponent: () => import('./features/clientes/clientes').then(m=>m.ClientesComponent) },
  { path: 'ventas/pedidos', canActivate: [rolGuard(['VENDEDOR', 'ADMIN'])], loadComponent: () => import('./features/admin/pedidos/admin-pedidos').then(m => m.AdminPedidosComponent) },
  { path: 'mis-visitas', canActivate: [rolGuard(['TECNICO'])], loadComponent: () => import('./features/mis-visitas/mis-visitas').then((m) => m.MisVisitasComponent) },
  { path: 'mis-pedidos', canActivate: [authGuard], loadComponent: () => import('./features/mis-pedidos/mis-pedidos').then((m) => m.MisPedidosComponent) },
  { path: 'resenas', loadComponent: () => import('./features/resenas/resenas').then((m) => m.ResenasComponent) },
  { path: 'postventa', loadComponent: () => import('./features/postventa/postventa').then((m) => m.PostventaComponent) },
  { path: 'perfil', canActivate: [authGuard], loadComponent: () => import('./features/perfil/perfil').then((m) => m.PerfilComponent) },
  { path: 'olvide-contrasena', loadComponent: () => import('./features/auth/olvide-contrasena/olvide-contrasena').then((m) => m.OlvideContrasenaComponent) },

  {
    path: 'admin',
    canActivate: [rolGuard(['ADMIN'])],
    children: [
      { path: '', loadComponent: () => import('./features/admin/dashboard/admin-dashboard').then((m) => m.AdminDashboardComponent) },
      { path: 'comunicaciones', loadComponent: () => import('./features/admin/comunicaciones/comunicaciones').then(m=>m.ComunicacionesComponent) },
      { path: 'productos', loadComponent: () => import('./features/admin/productos/admin-productos').then((m) => m.AdminProductosComponent) },
      { path: 'servicios', loadComponent: () => import('./features/admin/servicios/admin-servicios').then(m => m.AdminServiciosComponent) },
      { path: 'mecanismos', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/mecanismos/admin-mecanismos').then(m => m.AdminMecanismosComponent) },
      { path: 'telas', loadComponent: () => import('./features/admin/telas/admin-telas').then((m) => m.AdminTelasComponent) },
      { path: 'usuarios', loadComponent: () => import('./features/admin/usuarios/admin-usuarios').then((m) => m.AdminUsuariosComponent) },
      { path: 'comisiones', loadComponent: () => import('./features/admin/comisiones/admin-comisiones').then((m) => m.AdminComisionesComponent) },
      { path: 'postventa', loadComponent: () => import('./features/admin/postventa/admin-postventa').then((m) => m.AdminPostventaComponent) },
      { path: 'incidencias-pago', loadComponent: () => import('./features/admin/incidencias/admin-incidencias').then((m) => m.AdminIncidenciasComponent) },
      { path: 'carrito', canActivate: [authGuard], loadComponent: () => import('./features/carrito/carrito').then(m => m.CarritoComponent) },
  { path: 'agenda', loadComponent: () => import('./features/admin/agenda/admin-agenda').then((m) => m.AdminAgendaComponent) },
      {
        path: 'pedidos',
        canActivate: [rolGuard(['ADMIN', 'TECNICO'])],
        loadComponent: () => import('./features/admin/pedidos/admin-pedidos').then((m) => m.AdminPedidosComponent),
      },
    ],
  },

  { path: '', redirectTo: 'catalogo', pathMatch: 'full' },
  { path: '**', redirectTo: 'catalogo' },
];
