import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { IonContent, IonHeader, IonTitle, IonToolbar, IonItem, IonLabel, IonInput, IonButton, IonButtons, IonBackButton, IonSpinner } from '@ionic/angular';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, IonContent, IonHeader, IonTitle, IonToolbar, IonItem, IonLabel, IonInput, IonButton, IonButtons, IonBackButton, IonSpinner],
  templateUrl: './registro.html',
})
export class RegistroComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  enviando = false;
  error = '';
  exito = '';
  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.maxLength(100)]],
    correo: ['', [Validators.required, Validators.email]],
    contrasena: ['', [Validators.required, Validators.minLength(6)]],
    confirmarContrasena: ['', Validators.required],
  });
  enviar(): void {
    this.error = '';
    this.exito = '';
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.error = 'Revisa los campos marcados antes de crear tu cuenta.';
      return;
    }
    const { confirmarContrasena, ...datos } = this.form.getRawValue();
    if (datos.contrasena !== confirmarContrasena) {
      this.error = 'Las contraseñas no coinciden.';
      return;
    }
    if (this.enviando) return;
    this.enviando = true;
    this.auth.registrar({ ...datos, nombre: datos.nombre.trim(), apellido: datos.apellido.trim(), correo: datos.correo.trim() })
      .pipe(finalize(() => this.enviando = false))
      .subscribe({
        next: () => {
          this.exito = 'Cuenta creada correctamente. Ya puedes iniciar sesión.';
          this.form.reset();
        },
        error: (err) => {
          const mensaje = err.error?.mensaje ?? err.error?.error;
          this.error = typeof mensaje === 'string' && mensaje.length < 250 ? mensaje :
            err.status === 0 ? 'No se pudo conectar al servidor. Comprueba tu conexión.' :
            err.status === 409 ? 'El correo ya está registrado.' :
            err.status === 400 ? 'Revisa los datos ingresados.' : 'No se pudo crear la cuenta. Inténtalo nuevamente.';
        },
      });
  }
  irAlLogin(): void { this.router.navigateByUrl('/login'); }
}
