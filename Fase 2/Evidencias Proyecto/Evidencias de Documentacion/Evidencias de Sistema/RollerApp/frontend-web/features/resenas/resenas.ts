import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ResenaService } from '../../core/services/resena.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';
import { Resena } from '../../core/models/resena.model';

@Component({
  selector: 'app-resenas',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './resenas.html',
  styleUrl: './resenas.scss',
})
export class ResenasComponent implements OnInit {
  private resenaService = inject(ResenaService);
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);

  resenas = signal<Resena[]>([]);
  cargando = signal(true);
  enviando = signal(false);

  form = this.fb.nonNullable.group({
    descripcion: ['', Validators.required],
    calidad: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
    servicio: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.resenaService.listarTodas().subscribe({
      next: (r) => {
        this.resenas.set(r);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
    });
  }

  enviar(): void {
    if (this.form.invalid) return;

    this.enviando.set(true);
    this.resenaService.crear(this.form.getRawValue()).subscribe({
      next: () => {
        this.toast.mostrar('¡Gracias por tu reseña!', 'exito');
        this.form.reset({ descripcion: '', calidad: 5, servicio: 5 });
        this.enviando.set(false);
        this.cargar();
      },
      error: (error) => {
        this.toast.mostrar(error.error?.error ?? 'No se pudo publicar la reseña.', 'error');
        this.enviando.set(false);
      },
    });
  }
}
