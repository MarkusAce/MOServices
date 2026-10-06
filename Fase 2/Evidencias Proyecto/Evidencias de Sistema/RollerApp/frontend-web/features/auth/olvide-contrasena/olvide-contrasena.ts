import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

type Paso = 'correo' | 'codigo' | 'nueva-contrasena';

@Component({
  selector: 'app-olvide-contrasena',
  imports: [ReactiveFormsModule],
  templateUrl: './olvide-contrasena.html',
  styleUrl: '../login/login.scss',
})
export class OlvideContrasenaComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);

  paso = signal<Paso>('correo');
  enviando = signal(false);
  correoEnviado = '';
  private resetToken = '';

  formCorreo = this.fb.nonNullable.group({ correo: ['', [Validators.required, Validators.email]] });
  formCodigo = this.fb.nonNullable.group({ codigo: ['', Validators.required] });
  formNuevaContrasena = this.fb.nonNullable.group({
    nuevaContrasena: ['', [Validators.required, Validators.minLength(6)]],
  });

  enviarCorreo(): void {
    if (this.formCorreo.invalid) return;
    this.enviando.set(true);
    this.correoEnviado = this.formCorreo.getRawValue().correo;

    this.auth.olvideContrasena(this.correoEnviado).subscribe({
      next: (respuesta) => {
        this.paso.set('codigo');
        this.enviando.set(false);
      },
      error: (error) => {
        this.toast.mostrar(error.error?.error ?? 'No se pudo enviar el código.', 'error');
        this.enviando.set(false);
      },
    });
  }

  verificarCodigo(): void {
    if (this.formCodigo.invalid) return;
    this.enviando.set(true);

    this.auth.verificarCodigoRecuperacion(this.correoEnviado, this.formCodigo.getRawValue().codigo).subscribe({
      next: (respuesta) => {
        this.resetToken = respuesta.resetToken;
        this.paso.set('nueva-contrasena');
        this.enviando.set(false);
      },
      error: (error) => {
        this.toast.mostrar(error.error?.error ?? 'Código incorrecto.', 'error');
        this.enviando.set(false);
      },
    });
  }

  restablecer(): void {
    if (this.formNuevaContrasena.invalid) return;
    this.enviando.set(true);

    this.auth.restablecerContrasena(this.resetToken, this.formNuevaContrasena.getRawValue().nuevaContrasena).subscribe({
      next: () => {
        this.toast.mostrar('Contraseña actualizada. Ya puedes iniciar sesión.', 'exito');
        this.router.navigateByUrl('/login');
      },
      error: (error) => {
        this.toast.mostrar(error.error?.error ?? 'No se pudo restablecer la contraseña.', 'error');
        this.enviando.set(false);
      },
    });
  }
}
