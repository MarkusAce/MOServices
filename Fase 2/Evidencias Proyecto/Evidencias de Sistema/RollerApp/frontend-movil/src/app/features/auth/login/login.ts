import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonItem, IonLabel, IonInput, IonButton,
} from '@ionic/angular';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule, RouterLink,
    IonContent, IonHeader, IonTitle, IonToolbar, IonItem, IonLabel, IonInput, IonButton,
  ],
  templateUrl: './login.html',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  enviando = false;

  form = this.fb.nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
    contrasena: ['', Validators.required],
  });

  enviar(): void {
    if (this.enviando) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando = true;
    const { correo, contrasena } = this.form.getRawValue();

    this.auth.login(correo, contrasena).subscribe({
      next: () => {
        this.toast.mostrar('Sesión iniciada correctamente.', 'exito');
        const destino = this.auth.esAdmin() ? '/tabs/admin' :
          this.auth.esTecnico() ? '/tabs/mis-visitas' : this.auth.esVendedor() ? '/tabs/admin-pedidos' : '/tabs/catalogo';
        const retorno = this.route.snapshot.queryParamMap.get('returnUrl');
        this.router.navigateByUrl(retorno?.startsWith('/') && !retorno.startsWith('//') ? retorno : destino);
      },
      error: (error) => {
        this.toast.mostrar(error.error?.error ?? 'No se pudo iniciar sesión.', 'error');
        this.enviando = false;
      },
    });
  }
}
