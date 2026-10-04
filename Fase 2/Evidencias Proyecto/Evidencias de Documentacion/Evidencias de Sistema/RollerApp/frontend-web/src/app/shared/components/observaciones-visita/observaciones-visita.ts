import { Component, Input, OnChanges, OnDestroy, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { Subscription } from 'rxjs';
import { ObservacionVisitaService, ObservacionVisita } from '../../../core/services/observacion-visita.service';

@Component({
  selector: 'app-observaciones-visita',
  imports: [FormsModule, DatePipe],
  templateUrl: './observaciones-visita.html',
  styleUrl: './observaciones-visita.scss',
})
export class ObservacionesVisitaComponent implements OnChanges, OnDestroy {
  @Input({ required: true }) visitaId!: number;
  @Input() estado: string | null = null;
  private api = inject(ObservacionVisitaService);
  private peticion?: Subscription;
  private escritura?: Subscription;
  abierto = signal(false);
  notas = signal<ObservacionVisita[]>([]);
  cargando = signal(false);
  guardando = signal(false);
  error = signal('');
  aviso = signal('');
  ultima = signal(true);
  private pagina = -1;
  texto = '';

  ngOnChanges(): void {
    this.peticion?.unsubscribe(); this.escritura?.unsubscribe();
    this.abierto.set(false); this.notas.set([]); this.texto = '';
    this.error.set(''); this.aviso.set(''); this.cargando.set(false); this.guardando.set(false);
    this.pagina = -1; this.ultima.set(true);
  }
  ngOnDestroy(): void { this.peticion?.unsubscribe(); this.escritura?.unsubscribe(); }
  puedeEscribir(): boolean { return this.estado === 'RESERVADO' || this.estado === 'COMPLETADO'; }
  abrir(): void {
    this.abierto.set(!this.abierto());
    if (this.abierto() && this.pagina < 0) this.cargar();
  }
  cargar(mas = false): void {
    if (this.cargando() || this.guardando()) return;
    this.cargando.set(true); this.error.set('');
    const siguiente = mas ? this.pagina + 1 : 0;
    this.peticion = this.api.listar(this.visitaId, siguiente).subscribe({
      next: datos => {
        const notas = mas ? [...this.notas(), ...datos.content] : datos.content;
        this.notas.set([...new Map(notas.map(n => [n.id, n])).values()]);
        this.pagina = datos.number; this.ultima.set(datos.last); this.cargando.set(false);
      },
      error: e => { this.cargando.set(false); this.error.set(e.error?.mensaje ?? e.error?.error ?? 'No se pudo cargar el historial.'); },
    });
  }
  guardar(): void {
    const texto = this.texto.trim();
    if (!this.puedeEscribir() || !texto || this.texto.length > 2000 || this.guardando() || this.cargando()) return;
    this.guardando.set(true); this.error.set(''); this.aviso.set('');
    this.escritura = this.api.agregar(this.visitaId, texto).subscribe({
      next: () => {
        this.texto = ''; this.guardando.set(false); this.aviso.set('Observación guardada.'); this.cargar();
      },
      error: e => { this.guardando.set(false); this.error.set(e.error?.mensaje ?? e.error?.error ?? 'No se pudo guardar la observación.'); },
    });
  }
}
