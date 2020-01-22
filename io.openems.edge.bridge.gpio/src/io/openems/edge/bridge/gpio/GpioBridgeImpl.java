package io.openems.edge.bridge.gpio;


import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Gpio;
import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.gpio.api.GpioBridge;
import io.openems.edge.bridge.gpio.task.GpioBridgeTask;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Designate(ocd = Config.class, factory = true)
@Component(name = "GpioBridge",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)
public class GpioBridgeImpl extends AbstractOpenemsComponent implements OpenemsComponent, GpioBridge, EventHandler {

    private final GpioBridgeWorker worker = new GpioBridgeWorker();
    private Map<String, GpioBridgeTask> tasks = new ConcurrentHashMap<>();

    public GpioBridgeImpl() {
        super(OpenemsComponent.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.enabled()) {
            this.worker.activate(super.id());
        }
    }


    @Deactivate
    public void deactivate() {
        super.deactivate();
        this.worker.deactivate();
    }

    @Override
    public void addGpioTask(String id, GpioBridgeTask task) {

        this.tasks.put(id, task);
    }

    @Override
    public void removeGpioTask(String id) {
        this.tasks.remove(id);
    }

    @Override
    public Map<String, GpioBridgeTask> getTasks() {
        return this.tasks;
    }

    private class GpioBridgeWorker extends AbstractCycleWorker {
        @Override
        public void activate(String id) {
            super.activate(id);
        }

        @Override
        public void deactivate() {
            super.deactivate();
        }

        @Override
        protected void forever() throws Throwable {
            GpioController gpio = GpioFactory.getInstance();

            tasks.values().forEach(task -> {
                //
                //  GpioPinDigitalInput input = gpio.provisionDigitalInputPin(getPinProvider(task.getRequest()));
                //   input.addListener((GpioPinListenerDigital) event -> {
                //       //consolinno intern it is swapped --> if true --> signal is there --> no signal send by device
                //       if (event.getState().isHigh()) {
                //           task.setResponse(false);
                //       } else {
                //           task.setResponse(true);
                //       }
                //   });
                //simple solution maybe?
                Gpio.pinMode(task.getRequest(), Gpio.INPUT);
                if (Gpio.digitalRead(task.getRequest()) > 1) {
                    task.setResponse(false);
                } else {
                    task.setResponse(true);
                }


            });
        }
    }

//    private Pin getPinProvider(int request) {
//
//        switch (request) {
//            case 1:
//                return RaspiPin.GPIO_04;
//            case 2:
//                return RaspiPin.GPIO_17;
//            case 3:
//                return RaspiPin.GPIO_27;
//
//
//        }
//        return null;
//    }


    @Override
    public void handleEvent(Event event) {
        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)) {
            this.worker.triggerNextRun();
        }
    }


}
