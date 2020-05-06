package io.openems.edge.bridge.gpio;

import com.pi4j.wiringpi.Gpio;
import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.gpio.api.GpioBridge;
import io.openems.edge.bridge.gpio.task.GpioBridgeReadTask;
import io.openems.edge.bridge.gpio.task.GpioBridgeWriteTask;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import org.osgi.service.cm.ConfigurationException;
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

import static com.pi4j.wiringpi.Gpio.wiringPiSetup;

@Designate(ocd = Config.class, factory = true)
@Component(name = "GpioBridge",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)
public class GpioBridgeImpl extends AbstractOpenemsComponent implements OpenemsComponent, GpioBridge, EventHandler {

    private final GpioBridgeWorker worker = new GpioBridgeWorker();
    private Map<String, GpioBridgeReadTask> readTasks = new ConcurrentHashMap<>();
    private Map<String, GpioBridgeWriteTask> writeTasks = new ConcurrentHashMap<>();

    public GpioBridgeImpl() {
        super(OpenemsComponent.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.enabled()) {
            //needed to read Gpio Input data
            Gpio.wiringPiSetupGpio();
            this.worker.activate(super.id());
        }
    }


    @Deactivate
    public void deactivate() {
        super.deactivate();
        this.worker.deactivate();
    }


    /**
     * Adds a gpio task to the Map and enables input for gpio.
     *
     * @param id   Id of the Gpio Device
     * @param task the created GpioBridgeTask by the GpioDevice
     *             <p>
     *             Gpio.pinMode --> Declares that the allocated Pin is an Input
     */
    @Override
    public void addGpioReadTask(String id, GpioBridgeReadTask task) throws ConfigurationException {
        if (!getReadTasks().containsKey(id)) {
            getReadTasks().put(id, task);
            Gpio.pinMode(task.getRequest(), Gpio.INPUT);
        } else {
            throw new ConfigurationException(id, "Gpio Already Configured, make sure to change Unique Id !");
        }

    }


    @Override
    public void removeGpioReadTask(String id) {
        this.readTasks.remove(id);
    }

    @Override
    public void addGpioWriteTask(String id, GpioBridgeWriteTask task) throws ConfigurationException {
        if (!getWriteTasks().containsKey(id)) {
            getWriteTasks().put(id, task);
            Gpio.pinMode(task.getPosition(), Gpio.OUTPUT);
        } else {
            throw new ConfigurationException(id, "Gpio Already in List, Try another Id if you are certain it's a new GPIO");
        }
    }

    @Override
    public void removeGpioWriteTask(String id) {
        this.getWriteTasks().remove(id);

    }


    @Override
    public Map<String, GpioBridgeReadTask> getReadTasks() {
        return this.readTasks;
    }

    @Override
    public Map<String, GpioBridgeWriteTask> getWriteTasks() {
        return this.writeTasks;
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


        /**
         * Get's the input data as 0 or 1 and sets the respond as true or false
         * to the GpioDevice Channel.
         * Note!
         * OLD VERSION:
         * Raspberry Pi from Consolinno : Logic is swapped due to relinking etc.
         * that's why gpio >=1 --> no error/ offline
         * gpio == 0 error/online
         * <p>
         * NEW VERSION : Not Inverse logic
         */
        @Override
        protected void forever() throws Throwable {

            getReadTasks().values().forEach(task -> {
                if (Gpio.digitalRead(task.getRequest()) >= 1) {
                    task.setResponse(true);
                } else {
                    task.setResponse(false);
                }

            });
            getWriteTasks().values().forEach(task -> {
                boolean errorFlag = task.getRequest();
                Gpio.digitalWrite(task.getPosition(), errorFlag);
            });

        }
    }


    @Override
    public void handleEvent(Event event) {
        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)) {
            this.worker.triggerNextRun();
        }
    }


}
