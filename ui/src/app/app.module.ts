import { NgModule, ErrorHandler } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
//import { RouterModule, RouteReuseStrategy, Routes } from '@angular/router';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { SplashScreen } from '@ionic-native/splash-screen/ngx';
import { StatusBar } from '@ionic-native/status-bar/ngx';

// modules
import { IonicModule, IonicRouteStrategy } from '@ionic/angular';
import { SharedModule } from './shared/shared.module';
import { AboutModule } from './about/about.module';
import { IndexModule } from './index/index.module';
import { EdgeModule } from './edge/edge.module';

// components
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';

// services
import { Language } from './shared/translate/language';

// locale Data
import { LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localDE from '@angular/common/locales/de';
import { PopoverPage } from './shared/popover/popover.component';
import { PopoverPageModule } from './shared/popover/popover.module';
import { SettingsModule } from './settings/settings.module';
import { SettingsModule as EdgeSettingsModule } from './edge/settings/settings.module';
import { RouteReuseStrategy } from '@angular/router';
import { ServiceWorkerModule } from '@angular/service-worker';
import { environment as env } from '../environments/environment';
import { FormlyModule } from '@ngx-formly/core';
import { RepeatTypeComponent } from './edge/settings/component/shared/repeat';
import { EvcsModalPageModule } from './edge/index/widget/evcs/evcs-modal/evcs-modal.module';
import { StorageModalPageModule } from './edge/index/widget/storage/storage-modal/storage-modal.module';
import { GridModalPageModule } from './edge/index/widget/grid/grid-modal/grid-modal.module';
import { ConsumptionModalPageModule } from './edge/index/widget/consumption/consumption-modal/consumption-modal.module';
import { ProductionModalPageModule } from './edge/index/widget/production/production-modal/production-modal.module';


@NgModule({
  declarations: [
    AppComponent,
    RepeatTypeComponent
  ],
  entryComponents: [PopoverPage],
  imports: [
    BrowserModule,
    IonicModule.forRoot(),
    FormlyModule.forRoot({
      types: [
        { name: 'repeat', component: RepeatTypeComponent },
      ],
    }),
    AppRoutingModule,
    SharedModule,
    AboutModule,
    SettingsModule,
    EdgeModule,
    EdgeSettingsModule,
    IndexModule,
    EvcsModalPageModule,
    StorageModalPageModule,
    GridModalPageModule,
    ConsumptionModalPageModule,
    ProductionModalPageModule,
    TranslateModule.forRoot({
      loader: { provide: TranslateLoader, useClass: Language }
    }),
    PopoverPageModule,
    env.production && env.backend == "OpenEMS Backend" ? ServiceWorkerModule.register('ngsw-worker.js', { enabled: true }) : [],
  ],
  providers: [
    StatusBar,
    SplashScreen,
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    // { provide: ErrorHandler, useExisting: Service },
    { provide: LOCALE_ID, useValue: 'de' }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor() {
    registerLocaleData(localDE);
  }
}
