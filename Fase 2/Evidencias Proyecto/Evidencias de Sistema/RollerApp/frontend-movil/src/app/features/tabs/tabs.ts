import { CarritoService } from '../../core/services/carrito.service';
import { Component, inject } from '@angular/core';
import { IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel } from '@ionic/angular';
import { addIcons } from 'ionicons';
import { gridOutline, receiptOutline, constructOutline, personOutline, statsChartOutline, settingsOutline, calendarOutline, cartOutline } from 'ionicons/icons';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-tabs',
  imports: [IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel],
  templateUrl: './tabs.html',
})
export class TabsComponent {
  auth = inject(AuthService);
  carrito = inject(CarritoService);

  constructor() {
    addIcons({ gridOutline, receiptOutline, constructOutline, personOutline, statsChartOutline, settingsOutline, calendarOutline, cartOutline });
  }
}
