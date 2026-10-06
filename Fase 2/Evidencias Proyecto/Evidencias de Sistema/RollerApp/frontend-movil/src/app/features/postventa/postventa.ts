import { IonContent, IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton } from '@ionic/angular';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PostventaService } from '../../core/services/postventa.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-postventa',
  imports: [IonButtons, IonBackButton, IonContent, IonHeader, IonToolbar, IonTitle, ReactiveFormsModule],
  templateUrl: './postventa.html',
  styleUrl: './postventa.scss',
})
export class PostventaComponent {
  private postventaService = inject(PostventaService);
  private toast = inject(ToastService);

  enviando = signal(false);

  form = inject(FormBuilder).nonNullable.group({
    nombre: ['', Validators.required],
    apellido: ['', Validators.required],
    correo: ['', [Validators.required, Validators.email]],
    telefono: ['', Validators.required],
    comuna: ['', Validators.required],
    descripcion: ['', Validators.required],
  });

  enviar(): void {
    if (this.enviando()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.postventaService.crear(this.form.getRawValue()).subscribe({
      next: () => {
        this.toast.mostrar('Tu solicitud fue enviada. Te contactaremos a la brevedad.', 'exito');
        this.form.reset();
        this.enviando.set(false);
      },
      error: (error) => {
        this.toast.mostrar(error.error?.error ?? 'No se pudo enviar la solicitud.', 'error');
        this.enviando.set(false);
      },
    });
  }
}
