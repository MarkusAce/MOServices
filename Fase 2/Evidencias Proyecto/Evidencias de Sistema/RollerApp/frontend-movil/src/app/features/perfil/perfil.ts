import { RouterLink } from '@angular/router';
import { IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton } from '@ionic/angular';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-perfil',
  imports: [RouterLink, IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, ReactiveFormsModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss',
})
export class PerfilComponent {
  auth = inject(AuthService);
  private fb = inject(FormBuilder);
  private toast = inject(ToastService);

  form = this.fb.nonNullable.group({
    nombre: [this.auth.usuarioActual()?.nombre ?? '', Validators.required],
    apellido: [this.auth.usuarioActual()?.apellido ?? '', Validators.required],
  });

  etapaCorreo = signal<'inicio' | 'codigo'>('inicio');
  etapaClave = signal<'inicio' | 'codigo' | 'nueva'>('inicio');
  procesando = signal(false);

  correoForm = this.fb.nonNullable.group({
    nuevoCorreo: ['', [Validators.required, Validators.email]],
    contrasenaActual: ['', Validators.required],
    codigo: [''],
  });
  claveForm = this.fb.nonNullable.group({
    contrasenaActual: ['', Validators.required],
    codigo: [''],
    nuevaContrasena: ['', [Validators.required, Validators.minLength(8)]],
    confirmarContrasena: ['', Validators.required],
  });

  solicitarCorreo(): void {
    if (this.procesando()) return;
    if (this.correoForm.controls.nuevoCorreo.invalid || this.correoForm.controls.contrasenaActual.invalid) {
      this.correoForm.markAllAsTouched(); return;
    }
    this.procesando.set(true);
    const { contrasenaActual, nuevoCorreo } = this.correoForm.getRawValue();
    this.auth.solicitarCambioCorreo(contrasenaActual, nuevoCorreo).subscribe({
      next: respuesta => { this.etapaCorreo.set('codigo'); this.toast.mostrar(respuesta.mensaje, 'exito'); this.procesando.set(false); },
      error: error => { this.toast.mostrar(error.error?.error ?? 'No pudimos enviar el código.', 'error'); this.procesando.set(false); },
    });
  }

  confirmarCorreo(): void {
    if (this.procesando()) return;
    if (!this.correoForm.controls.codigo.value.trim()) return;
    this.procesando.set(true);
    this.auth.confirmarCambioCorreo(this.correoForm.controls.codigo.value.trim()).subscribe({
      next: () => { this.etapaCorreo.set('inicio'); this.correoForm.reset(); this.toast.mostrar('Correo actualizado y confirmado.', 'exito'); this.procesando.set(false); },
      error: error => { this.toast.mostrar(error.error?.error ?? 'Código incorrecto o vencido.', 'error'); this.procesando.set(false); },
    });
  }

  solicitarClave(): void {
    if (this.procesando()) return;
    if (!this.claveForm.controls.contrasenaActual.value) return;
    this.procesando.set(true);
    this.auth.enviarCodigoVerificacion(this.claveForm.controls.contrasenaActual.value).subscribe({
      next: () => { this.etapaClave.set('codigo'); this.toast.mostrar('Enviamos un código a tu correo actual.', 'exito'); this.procesando.set(false); },
      error: error => { this.toast.mostrar(error.error?.error ?? 'No pudimos enviar el código.', 'error'); this.procesando.set(false); },
    });
  }

  verificarClave(): void {
    if (this.procesando()) return;
    if (!this.claveForm.controls.codigo.value.trim()) return;
    this.procesando.set(true);
    this.auth.verificarCodigo(this.claveForm.controls.codigo.value.trim()).subscribe({
      next: () => { this.etapaClave.set('nueva'); this.procesando.set(false); },
      error: error => { this.toast.mostrar(error.error?.error ?? 'Código incorrecto o vencido.', 'error'); this.procesando.set(false); },
    });
  }

  guardarClave(): void {
    if (this.procesando()) return;
    const { nuevaContrasena, confirmarContrasena } = this.claveForm.getRawValue();
    if (nuevaContrasena.length < 8 || nuevaContrasena !== confirmarContrasena) {
      this.toast.mostrar('La contraseña debe tener al menos 8 caracteres y ambas deben coincidir.', 'error'); return;
    }
    this.procesando.set(true);
    this.auth.cambiarContrasena(nuevaContrasena).subscribe({
      next: () => { this.etapaClave.set('inicio'); this.claveForm.reset(); this.toast.mostrar('Contraseña actualizada correctamente.', 'exito'); this.procesando.set(false); },
      error: error => { this.toast.mostrar(error.error?.error ?? 'No se pudo cambiar la contraseña.', 'error'); this.procesando.set(false); },
    });
  }

  guardar(): void {
    if (this.procesando()) return;
    if (this.form.invalid) return;

    this.procesando.set(true);
    this.auth.actualizarPerfil(this.form.getRawValue()).subscribe({
      next: () => { this.procesando.set(false); this.toast.mostrar('Perfil actualizado correctamente.', 'exito'); },
      error: (error) => { this.procesando.set(false); this.toast.mostrar(error.error?.error ?? 'No se pudo actualizar el perfil.', 'error'); },
    });
  }
}
