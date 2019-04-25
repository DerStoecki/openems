import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Service, Utils, Websocket, EdgeConfig } from '../../../../shared/shared';
import { IGNORE_NATURES } from '../shared/shared';

@Component({
  selector: IndexComponent.SELECTOR,
  templateUrl: './index.component.html'
})
export class IndexComponent implements OnInit {

  private static readonly SELECTOR = "indexComponentInstall";

  public list: {
    readonly nature: EdgeConfig.Nature,
    readonly factories: EdgeConfig.Factory[]
  }[] = [];

  constructor(
    private route: ActivatedRoute,
    protected utils: Utils,
    private websocket: Websocket,
    private service: Service,
  ) {
  }

  ngOnInit() {
    this.service.setCurrentEdge(this.route);
    this.service.getConfig().then(config => {
      for (let natureId in config.natures) {
        if (IGNORE_NATURES.includes(natureId)) {
          continue;
        }

        let nature = config.natures[natureId];
        let factories = [];
        for (let factoryId of nature.factoryIds) {
          factories.push(config.factories[factoryId]);
        }
        this.list.push({
          nature: nature,
          factories: factories
        });
      }
    });
  }

}