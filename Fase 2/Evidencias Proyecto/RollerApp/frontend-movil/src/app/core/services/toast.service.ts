import { Injectable, inject } from '@angular/core';
import { ToastController } from '@ionic/angular';

@Injectable({ providedIn: 'root' })
export class ToastService {
  private controlador = inject(ToastController);

  mostrar(mensaje: string, tipo: 'exito' | 'error' = 'exito'): void {
    void this.controlador.create({
      message: mensaje,
      color: tipo === 'exito' ? 'success' : 'danger',
      duration: 4000,
      position: 'top',
      buttons: [{ text: 'Cerrar', role: 'cancel' }],
    }).then(toast => toast.present());
  }
}
