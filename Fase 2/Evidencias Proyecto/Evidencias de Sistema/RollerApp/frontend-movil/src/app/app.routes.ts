import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { rolGuard } from './core/guards/rol.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login/login').then((m) => m.LoginComponent) },
  { path: 'registro', loadComponent: () => import('./features/auth/registro/registro').then((m) => m.RegistroComponent) },
  { path: 'olvide-contrasena', loadComponent: () => import('./features/auth/olvide-contrasena/olvide-contrasena').then((m) => m.OlvideContrasenaComponent) },
  { path: 'productos/:id', loadComponent: () => import('./features/producto-detalle/producto-detalle').then((m) => m.ProductoDetalleComponent) },
  { path: 'agenda', canActivate: [authGuard], loadComponent: () => import('./features/agenda/agenda').then((m) => m.AgendaComponent) },
  { path: 'pago/retorno', canActivate: [authGuard], loadComponent: () => import('./features/pago/pago-retorno').then((m) => m.PagoRetornoComponent) },

  {
    path: 'tabs',
    loadComponent: () => import('./features/tabs/tabs').then((m) => m.TabsComponent),
    children: [
      { path: 'facturacion', canActivate: [authGuard], loadComponent: () => import('./features/facturacion/facturacion').then(m => m.FacturacionComponent) },
      { path: 'facturacion/clientes/:id', canActivate: [rolGuard(['ADMIN','VENDEDOR'])], loadComponent: () => import('./features/facturacion/facturacion').then(m => m.FacturacionComponent) },
      { path: 'facturacion/configurar', canActivate: [rolGuard(['ADMIN'])], data: {configurar:true}, loadComponent: () => import('./features/facturacion/facturacion').then(m => m.FacturacionComponent) },

      { path: 'admin-servicios', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/servicios/admin-servicios').then(m => m.AdminServiciosComponent) },
      { path: 'admin', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/dashboard/admin-dashboard').then((m) => m.AdminDashboardComponent) },
      { path: 'admin-agenda', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/agenda/admin-agenda').then(m => m.AdminAgendaComponent) },
      { path: 'admin-operaciones', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/operaciones/admin-operaciones').then((m) => m.AdminOperacionesComponent) },
      { path: 'admin-pedidos', canActivate: [rolGuard(['ADMIN','VENDEDOR'])], loadComponent: () => import('./features/admin/pedidos/admin-pedidos').then((m) => m.AdminPedidosComponent) },
      { path: 'carrito', canActivate: [authGuard], loadComponent: () => import('./features/carrito/carrito').then(m => m.CarritoComponent) },
      { path: 'postventa', loadComponent: () => import('./features/postventa/postventa').then(m => m.PostventaComponent) },
      { path: 'resenas', loadComponent: () => import('./features/resenas/resenas').then(m => m.ResenasComponent) },
      { path: 'admin-productos', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/productos/admin-productos').then(m => m.AdminProductosComponent) },
      { path: 'admin-mecanismos', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/mecanismos/admin-mecanismos').then(m => m.AdminMecanismosComponent) },
      { path: 'admin-telas', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/telas/admin-telas').then(m => m.AdminTelasComponent) },
      { path: 'admin-comisiones', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/comisiones/admin-comisiones').then(m => m.AdminComisionesComponent) },
      { path: 'admin-usuarios', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/usuarios/admin-usuarios').then(m => m.AdminUsuariosComponent) },
      { path: 'admin-postventa', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/postventa/admin-postventa').then(m => m.AdminPostventaComponent) },
      { path: 'admin-comunicaciones', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/comunicaciones/comunicaciones').then(m => m.ComunicacionesComponent) },
      { path: 'admin-incidencias', canActivate: [rolGuard(['ADMIN'])], loadComponent: () => import('./features/admin/incidencias/admin-incidencias').then(m => m.AdminIncidenciasComponent) },
      { path: 'clientes', canActivate: [rolGuard(['ADMIN','VENDEDOR'])], loadComponent: () => import('./features/clientes/clientes').then(m => m.ClientesComponent) },
      { path: 'clientes/:id', canActivate: [rolGuard(['ADMIN','VENDEDOR'])], loadComponent: () => import('./features/clientes/clientes').then(m => m.ClientesComponent) },
      { path: 'servicios', loadComponent: () => import('./features/servicios/servicios').then(m => m.ServiciosComponent) },
      { path: 'herramientas', loadComponent: () => import('./features/herramientas/herramientas').then(m => m.HerramientasComponent) },
      { path: 'catalogo', loadComponent: () => import('./features/catalogo/catalogo').then((m) => m.CatalogoComponent) },
      { path: 'mis-pedidos', canActivate: [authGuard], loadComponent: () => import('./features/mis-pedidos/mis-pedidos').then((m) => m.MisPedidosComponent) },
      { path: 'mis-visitas', canActivate: [rolGuard(['TECNICO'])], loadComponent: () => import('./features/mis-visitas/mis-visitas').then((m) => m.MisVisitasComponent) },
      { path: 'perfil', canActivate: [authGuard], loadComponent: () => import('./features/perfil/perfil').then((m) => m.PerfilComponent) },
      { path: '', pathMatch: 'full', loadComponent: () => import('./features/tabs/tabs-inicio').then((m) => m.TabsInicioComponent) },
    ],
  },

  { path: '', redirectTo: 'tabs/catalogo', pathMatch: 'full' },
  { path: '**', redirectTo: 'tabs/catalogo' },
];
