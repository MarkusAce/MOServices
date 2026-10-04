import { Injectable, inject } from '@angular/core';
import { AlertController, ToastController } from '@ionic/angular';

@Injectable({ providedIn: 'root' })
export class DialogoMovilService {
  private alertas = inject(AlertController);
  private mensajes = inject(ToastController);

  async confirmar(titulo: string, mensaje: string, accion = 'Confirmar', peligro = false): Promise<boolean> {
    const dialogo = await this.alertas.create({
      header: titulo,
      message: mensaje,
      backdropDismiss: false,
      buttons: [
        { text: 'Cancelar', role: 'cancel' },
        { text: accion, role: 'confirm', cssClass: peligro ? 'ion-color-danger' : undefined },
      ],
    });
    await dialogo.present();
    const resultado = await dialogo.onDidDismiss();
    return resultado.role === 'confirm';
  }

  async informar(mensaje: string, tipo: 'success' | 'danger' = 'success'): Promise<void> {
    const toast = await this.mensajes.create({
      message: mensaje, duration: 3500, position: 'bottom', color: tipo,
      buttons: [{ text: 'Cerrar', role: 'cancel' }],
    });
    await toast.present();
  }
}
