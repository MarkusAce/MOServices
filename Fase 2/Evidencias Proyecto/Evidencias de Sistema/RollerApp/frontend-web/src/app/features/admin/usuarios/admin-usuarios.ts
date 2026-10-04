import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Usuario } from '../../../core/models/usuario.model';
@Component({ selector: 'app-admin-usuarios', imports: [FormsModule], templateUrl: './admin-usuarios.html', styleUrl: './admin-usuarios.scss' })
export class AdminUsuariosComponent implements OnInit {
  auth = inject(AuthService); private toast = inject(ToastService);
  usuarios = signal<Usuario[]>([]); ocupado = signal(false); error = signal('');
  datos = {nombre:'',apellido:'',correo:'',contrasena:'',rol:'TECNICO'};
  roles = ['CLIENTE','VENDEDOR','TECNICO','ADMIN'];
  cambio = signal<{usuario:Usuario; rol:string}|null>(null);
  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.auth.listarUsuarios().subscribe({next:u=>this.usuarios.set(u), error:()=>this.error.set('No se pudieron cargar los usuarios.')}); }
  crear(): void {
    if (this.ocupado()) return;
    this.ocupado.set(true); this.error.set('');
    this.auth.crearPersonal(this.datos).pipe(finalize(()=>this.ocupado.set(false))).subscribe({
      next:()=>{ this.datos={nombre:'',apellido:'',correo:'',contrasena:'',rol:'TECNICO'};this.cargar();this.toast.mostrar('Personal creado. Entrega la contraseña por un canal privado.','exito'); },
      error:e=>this.error.set(e.error?.mensaje ?? e.error?.detail ?? e.error?.error ?? 'No se pudo crear el usuario.')
    });
  }
  elegirRol(usuario:Usuario, rol:string): void { if (rol!==usuario.rol) this.cambio.set({usuario,rol}); }
  confirmarRol(): void {
    const c=this.cambio(); if(!c||this.ocupado()) return;
    this.ocupado.set(true);this.error.set('');
    this.auth.cambiarRol(c.usuario.id,c.rol).pipe(finalize(()=>this.ocupado.set(false))).subscribe({
      next:()=>{this.cambio.set(null);this.cargar();this.toast.mostrar('Rol actualizado.','exito');},
      error:e=>this.error.set(e.error?.mensaje ?? e.error?.detail ?? e.error?.error ?? 'No se pudo cambiar el rol.')
    });
  }
  alternarActivo(usuario:Usuario): void {
    if(this.ocupado()) return;this.ocupado.set(true);
    this.auth.cambiarEstadoUsuario(usuario.id,!usuario.activo).pipe(finalize(()=>this.ocupado.set(false))).subscribe({
      next:()=>this.cargar(),error:e=>this.error.set(e.error?.mensaje ?? e.error?.error ?? 'No se pudo actualizar el usuario.')
    });
  }
}
