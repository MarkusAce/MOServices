import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
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
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando = true;
    const { correo, contrasena } = this.form.getRawValue();

    this.auth.login(correo, contrasena).subscribe({
      next: () => {
        this.toast.mostrar('Sesión iniciada correctamente.', 'exito');
        const destino = this.auth.esAdmin() ? '/admin' : this.auth.esVendedor() ? '/ventas/pedidos' : this.auth.esTecnico() ? '/mis-visitas' : '/catalogo';
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
