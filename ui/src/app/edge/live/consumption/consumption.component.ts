import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ChannelAddress, Edge, Service, Websocket, EdgeConfig } from '../../../shared/shared';
import { ModalController } from '@ionic/angular';
import { ConsumptionModalComponent } from './modal/modal.component';

@Component({
    selector: 'consumption',
    templateUrl: './consumption.component.html'
})
export class ConsumptionComponent {

    private static readonly SELECTOR = "consumption";

    public config: EdgeConfig = null;
    public edge: Edge = null;
    public evcsComponents: EdgeConfig.Component[] = null;

    constructor(
        public service: Service,
        private websocket: Websocket,
        private route: ActivatedRoute,
        public modalCtrl: ModalController,
    ) { }

    ngOnInit() {
        let channels = [];
        this.service.getConfig().then(config => {
            this.config = config;
            this.evcsComponents = config.getComponentsImplementingNature("io.openems.edge.evcs.api.Evcs").filter(component => !(component.factoryId == 'Evcs.Cluster') && !component.isEnabled == false)
            for (let component of this.evcsComponents) {
                channels.push(
                    new ChannelAddress(component.id, 'ChargePower'),
                )
            }
        })
        this.service.setCurrentComponent('', this.route).then(edge => {
            this.edge = edge;
            channels.push(
                new ChannelAddress('_sum', 'ConsumptionActivePower'),
                new ChannelAddress('_sum', 'ConsumptionMaxActivePower'),
            )
            this.edge.subscribeChannels(this.websocket, ConsumptionComponent.SELECTOR, channels);
        });
    }


    ngOnDestroy() {
        if (this.edge != null) {
            this.edge.unsubscribeChannels(this.websocket, ConsumptionComponent.SELECTOR);
        }
    }

    async presentModal() {
        const modal = await this.modalCtrl.create({
            component: ConsumptionModalComponent,
            componentProps: {
                edge: this.edge,
                evcsComponents: this.evcsComponents
            }
        });
        return await modal.present();
    }

    currentTotalChargingPower(): number {
        return this.sumOfChannel("ChargePower");
    }

    private sumOfChannel(channel: String): number {
        let sum = 0;
        this.evcsComponents.forEach(component => {
            let channelValue = this.edge.currentData.value.channel[component.id + "/" + channel];
            if (channelValue != null) {
                sum += channelValue;
            };
        });
        return sum;
    }

}
