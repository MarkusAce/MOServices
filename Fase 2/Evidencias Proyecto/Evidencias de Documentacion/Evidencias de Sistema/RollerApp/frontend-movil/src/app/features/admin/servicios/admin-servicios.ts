import { Component, ViewChild } from '@angular/core';
import { GestionServiciosComponent } from '../../../shared/components/gestion-servicios/gestion-servicios';
import { IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonBackButton } from '@ionic/angular';
@Component({ standalone: true, imports: [GestionServiciosComponent, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonBackButton], template: '<ion-header><ion-toolbar><ion-buttons slot="start"><ion-back-button defaultHref="/tabs/herramientas" text="Volver" /></ion-buttons><ion-title>Servicios</ion-title></ion-toolbar></ion-header><ion-content class="ion-padding"><main class="mobile-page"><app-gestion-servicios /></main></ion-content>' })
export class AdminServiciosComponent {
 @ViewChild(GestionServiciosComponent) servicios?: GestionServiciosComponent;
 ionViewWillEnter(): void { if(this.servicios && !this.servicios.cargando())this.servicios.cargar(); }
}
