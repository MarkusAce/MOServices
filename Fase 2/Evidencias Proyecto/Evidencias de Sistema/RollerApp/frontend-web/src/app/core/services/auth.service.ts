import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginResponse, MensajeRespuesta, Usuario } from '../models/usuario.model';

const CLAVE_STORAGE = 'rollerapp_sesion';

interface SesionGuardada {
  token: string;
  usuario: Usuario;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private baseUrl = `${environment.apiUrl}/auth`;

  private sesionSignal = signal<SesionGuardada | null>(this.leerSesionGuardada());

  usuarioActual = computed(() => this.sesionSignal()?.usuario ?? null);
  estaLogeado = computed(() => this.sesionSignal() !== null);
  token = computed(() => this.sesionSignal()?.token ?? null);
  esAdmin = computed(() => this.usuarioActual()?.rol === 'ADMIN');
  esTecnico = computed(() => this.usuarioActual()?.rol === 'TECNICO');
  esVendedor = computed(() => this.usuarioActual()?.rol === 'VENDEDOR');

  registrar(datos: { nombre: string; apellido: string; correo: string; contrasena: string }): Observable<MensajeRespuesta> {
    return this.http.post<MensajeRespuesta>(`${this.baseUrl}/registro`, datos);
  }

  login(correo: string, contrasena: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, { correo, contrasena }).pipe(
      tap((respuesta) => this.guardarSesion(respuesta))
    );
  }

  logout(returnUrl?: string): void {
    try { localStorage.removeItem(CLAVE_STORAGE); } catch {}
    this.sesionSignal.set(null);
    this.router.navigate(['/login'], {queryParams: returnUrl && !returnUrl.startsWith('/login') ? {returnUrl} : {}});
  }

  actualizarPerfil(datos: { nombre?: string; apellido?: string; correo?: string }): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.baseUrl}/perfil`, datos).pipe(
      tap((usuario) => {
        const sesion = this.sesionSignal();
        if (sesion) this.guardarSesion({ token: sesion.token, usuario });
      })
    );
  }

  verificarContrasenaActual(contrasenaActual: string): Observable<MensajeRespuesta> {
    return this.http.post<MensajeRespuesta>(`${this.baseUrl}/verificar-contrasena-actual`, { contrasenaActual });
  }

  enviarCodigoVerificacion(contrasenaActual: string): Observable<{ enviado: boolean; codigoDemo: string | null; correo: string }> {
    return this.http.post<{ enviado: boolean; codigoDemo: string | null; correo: string }>(
      `${this.baseUrl}/enviar-codigo`, { contrasenaActual }
    );
  }

  verificarCodigo(codigo: string): Observable<MensajeRespuesta> {
    return this.http.post<MensajeRespuesta>(`${this.baseUrl}/verificar-codigo`, { codigo });
  }

  cambiarContrasena(nuevaContrasena: string): Observable<MensajeRespuesta> {
    return this.http.put<MensajeRespuesta>(`${this.baseUrl}/cambiar-contrasena`, { nuevaContrasena });
  }

  solicitarCambioCorreo(contrasenaActual: string, nuevoCorreo: string): Observable<MensajeRespuesta> {
    return this.http.post<MensajeRespuesta>(`${this.baseUrl}/solicitar-cambio-correo`, { contrasenaActual, nuevoCorreo });
  }

  confirmarCambioCorreo(codigo: string): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.baseUrl}/confirmar-cambio-correo`, { codigo }).pipe(
      tap(usuario => {
        const sesion = this.sesionSignal();
        if (sesion) this.guardarSesion({ token: sesion.token, usuario });
      })
    );
  }

  olvideContrasena(correo: string): Observable<{ mensaje: string; codigoDemo: string | null }> {
    return this.http.post<{ mensaje: string; codigoDemo: string | null }>(`${this.baseUrl}/olvide-contrasena`, { correo });
  }

  verificarCodigoRecuperacion(correo: string, codigo: string): Observable<{ resetToken: string }> {
    return this.http.post<{ resetToken: string }>(`${this.baseUrl}/verificar-codigo-recuperacion`, { correo, codigo });
  }

  restablecerContrasena(resetToken: string, nuevaContrasena: string): Observable<MensajeRespuesta> {
    return this.http.put<MensajeRespuesta>(`${this.baseUrl}/restablecer-contrasena`, { resetToken, nuevaContrasena });
  }

  listarUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.baseUrl}/usuarios`);
  }

  crearPersonal(datos: { nombre: string; apellido: string; correo: string; contrasena: string; rol: string }): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.baseUrl}/usuarios/personal`, datos);
  }
  cambiarRol(id: number, rol: string): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.baseUrl}/usuarios/${id}/rol`, { rol });
  }

  cambiarEstadoUsuario(id: number, activo: boolean): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.baseUrl}/usuarios/${id}/estado`, { activo });
  }

  private guardarSesion(respuesta: LoginResponse): void {
    const sesion: SesionGuardada = { token: respuesta.token, usuario: respuesta.usuario };
    this.sesionSignal.set(sesion);
    try { localStorage.setItem(CLAVE_STORAGE, JSON.stringify(sesion)); } catch {}
  }

  private leerSesionGuardada(): SesionGuardada | null {
    try {
      const guardado = localStorage.getItem(CLAVE_STORAGE);
      if (!guardado) return null;
      const sesion = JSON.parse(guardado) as SesionGuardada;
      return typeof sesion?.token === 'string' && Number.isSafeInteger(sesion?.usuario?.id) && ['ADMIN','VENDEDOR','TECNICO','CLIENTE'].includes(sesion?.usuario?.rol) ? sesion : null;
    } catch { return null; }
  }
}
