package io.openems.edge.controller.csvwriter.emv;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.chp.device.api.PowerLevel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;

import io.openems.edge.meter.api.SymmetricMeter;
import io.openems.edge.pwm.device.api.PwmPowerLevelChannel;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Designate(ocd = Config.class, factory = true)
@Component(name = "EmvCsvWriter.Controller")
public class EmvCsvWriterController extends AbstractOpenemsComponent implements OpenemsComponent, Controller {


    @Reference
    ComponentManager cpm;

    private double timeInterval;
    private double timeStamp = 0;

    private List<Thermometer> thermometerList = new ArrayList<>();
    private List<ActuatorRelaysChannel> relaysList = new ArrayList<>();
    private List<PowerLevel> dacList = new ArrayList<>();
    private List<PwmPowerLevelChannel> pwmDeviceList = new ArrayList<>();
    private List<SymmetricMeter> meterList = new ArrayList<>();

    private int dateDay;
    private String fileName;
    private String path = "/home/sshconsolinno/DataLog/";

    private FileWriter csvWriter;

    public EmvCsvWriterController() {
        super(OpenemsComponent.ChannelId.values(),
                Controller.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, IOException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        allocateComponents(config.temperaturSensorList(), "Thermometer");
        allocateComponents(config.relaysDeviceList(), "Relays");
        allocateComponents(config.DacDeviceList(), "Dac");
        allocateComponents(config.PwmDeviceList(), "Pwm");
        allocateComponents(config.meterList(), "meter");
        this.timeInterval = config.timeInterval() * 1000;
        if (timeInterval <= 0) {
            this.timeInterval = 1000;
        }
        this.path = config.path();
        //create /home/sshconsolinno/DataLog if not exist
        createCSVPath();
        //initialize FileName --> Calendar year month and day
        initializeFileName();
        //check if file exists with param. else init
        if (csvNotExist()) {
            initizalizeCsvWriter();
        } else {
            this.csvWriter = new FileWriter(path + fileName, true);
        }
        initializeCsvHead();
    }

    /**
     * Gets the instance of Calendar and allocates the correct FileName as well as setting current day param.
     */
    private void initializeFileName() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        //month starts with 0;
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        this.fileName = "test" + year + month + day + ".csv";
        this.dateDay = day;
    }

    /**
     * Cehcks if the csvFile exists.
     *
     * @return true if file is not existing.
     */
    private boolean csvNotExist() {
        File f = new File(this.path + fileName);
        return !f.exists();
    }


    /**
     * Creates the Path to the CSV Files if not already existing.
     */
    private void createCSVPath() {
        File f = new File(this.path);
        if (!f.exists() || !f.isDirectory()) {
            boolean dirCreated = f.mkdir();
            System.out.println("directory Created: " + dirCreated);
        }
    }

    /**
     * Allocate given Components and checks if they're correct.
     *
     * @param deviceList is the DeviceList configured by the User.
     * @param identifier is needed for switch case and shows if devices have the correct nature.
     * @throws ConfigurationException                                          if allocated component exists but it's wrong instance.
     * @throws io.openems.common.exceptions.OpenemsError.OpenemsNamedException if component doesn't exist.
     */
    private void allocateComponents(String[] deviceList, String identifier) throws ConfigurationException, OpenemsError.OpenemsNamedException {

        AtomicInteger counter = new AtomicInteger();
        counter.set(0);
        OpenemsError.OpenemsNamedException[] openemsNamedExceptions = {null};
        ConfigurationException[] configurationExceptions = {null};

        Arrays.stream(deviceList).forEach(string -> {
            try {
                switch (identifier) {
                    case "Thermometer":
                        if (cpm.getComponent(string) instanceof Thermometer) {
                            this.thermometerList.add(counter.intValue(), cpm.getComponent(string));
                        } else {
                            throw new ConfigurationException("Could not allocate Component: Thermometer " + string,
                                    "Config error; Check your Config --> Temperature Sensor");
                        }
                        break;
                    case "Relays":
                        if (cpm.getComponent(string) instanceof ActuatorRelaysChannel) {
                            this.relaysList.add(counter.intValue(), cpm.getComponent(string));

                        } else {
                            throw new ConfigurationException("Could not allocate Component: Relays " + string,
                                    "Config error; Check your Config --> Relays");
                        }
                        break;
                    case "Dac":
                        if (cpm.getComponent(string) instanceof PowerLevel) {
                            this.dacList.add(counter.intValue(), cpm.getComponent(string));
                        } else {
                            throw new ConfigurationException("Could not allocate Component: Dac " + string,
                                    "Config error; Check your Config --> Dac");
                        }
                        break;
                    case "Pwm":
                        if (cpm.getComponent(string) instanceof PwmPowerLevelChannel) {
                            this.pwmDeviceList.add(counter.intValue(), cpm.getComponent(string));
                        } else {
                            throw new ConfigurationException("Could not allocate Component: Pwm " + string,
                                    "Config error; Check your Config --> Pwm Device");
                        }
                        break;
                    case "meter":
                        if (cpm.getComponent(string) instanceof SymmetricMeter) {

                            this.meterList.add(counter.intValue(), cpm.getComponent(string));
                        } else {
                            throw new ConfigurationException("Could not allocate Component: meter " + string,
                                    "Config error; Check your Config --> meter");
                        }
                        break;

                }
                counter.getAndIncrement();
            } catch (ConfigurationException e) {
                configurationExceptions[0] = e;
            } catch (OpenemsError.OpenemsNamedException e) {
                openemsNamedExceptions[0] = e;
            }
        });
        if (configurationExceptions[0] != null) {
            throw configurationExceptions[0];
        }
        if (openemsNamedExceptions[0] != null) {
            throw openemsNamedExceptions[0];
        }
    }

    /**
     * Appends all Information for the CSV File such as: Time; Id+ChannelId of selected Components.
     */
    private void initializeCsvHead() {

        // initializes the CSV Head --> If same file but new components it's written or new File

        try {
            csvWriter.append("Time");
            csvWriter.append(",");

            // Temperature
            this.thermometerList.forEach(thermometer -> {

                try {
                    String s = thermometer.id() + "/" + thermometer.getTemperature().channelId().id();
                    csvWriterAppendLineForHead(s);
                } catch (IOException e) {
                    e.printStackTrace();

                }
            });


            //Relays
            this.relaysList.forEach(relay -> {
                try {
                    String s = relay.id() + "/" + relay.getRelaysChannel().channelId().id();
                    csvWriterAppendLineForHead(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            //DAC

            this.dacList.forEach(dac -> {

                try {
                    String s = dac.id() + "/" + dac.getPowerLevelChannel().channelId().id();
                    csvWriterAppendLineForHead(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            //PWM
            this.pwmDeviceList.forEach(pwm -> {
                String s = pwm.id() + "/" + pwm.getPwmPowerLevelChannel().channelId().id();
                try {
                    csvWriterAppendLineForHead(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            });
            //Meter
            this.meterList.forEach(meter -> {
                try {
                    String s = meter.id() + "/" + meter.getActiveProductionEnergy().channelId().id();
                    csvWriterAppendLineForHead(s);
                    s = meter.id() + "/" + meter.getActiveConsumptionEnergy().channelId().id();
                    csvWriterAppendLineForHead(s);
                    s = meter.id() + "/" + meter.getActivePower().channelId().id();
                    csvWriterAppendLineForHead(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            csvWriter.append("\n");
            csvWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void csvWriterAppendLineForHead(String s) throws IOException {
        csvWriter.append(s);
        csvWriter.append(",");
    }

    /**
     * Checks for a new Day ---> New File will be created in run().
     *
     * @return true if newDay --> Different Day value.
     */

    private boolean newDay() {
        return this.dateDay != Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    /**
     * If the waiting time is over write again.
     *
     * @return boolean if ready or not.
     */
    private boolean readytoWrite() {
        return System.currentTimeMillis() - this.timeStamp >= timeInterval;
    }

    /**
     * Initialize a new FileWriter for a CSV File.
     *
     * @throws IOException if something is happening.
     */
    public void initizalizeCsvWriter() throws IOException {

        csvWriter = new FileWriter(this.path + this.fileName);
    }

    /**
     * if a new Day approaches --> csv.Writer will be flushed and closed: new file will be created etc
     * <p>
     * in either case the data will be written to the file if configured Time has passed
     */
    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        if (newDay()) {
            // Create new file if it's a new Day
            try {
                csvWriter.flush();
                csvWriter.close();
                initializeFileName();
                initizalizeCsvWriter();
                initializeCsvHead();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (readytoWrite()) {
            // Do Stuff

            try {
                this.timeStamp = System.currentTimeMillis() - 100;
                this.csvWriter = new FileWriter(path + fileName, true);
                //Write Current Time; File Writer maybe broken?
                Calendar calendar = Calendar.getInstance();
                String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
                this.csvWriter.append(time);
                this.csvWriter.append(",");
                //For Each Temperature ; For Each Relay / For Each Dac / Pwm / Meter
                writeTemperatureData();
                writeRelaysData();
                writeDacData();
                writePwmData();
                writeMeterData();
                csvWriter.append("\n");
                csvWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Writes Meter Data in CSV File.
     */
    private void writeMeterData() {
        this.meterList.forEach(meter -> {
            String csvString = "-";

            try {
                //Prod, Consump, ActivePower
                if (meter.getActiveProductionEnergy().getNextValue().isDefined()) {
                    csvString = meter.getActiveProductionEnergy().getNextValue().get().toString() + " "
                            + meter.getActiveProductionEnergy().channelDoc().getUnit().getSymbol();
                }
                csvWriter.append(csvString);
                csvWriter.append(",");
                csvString = "-";
                if (meter.getActiveConsumptionEnergy().getNextValue().isDefined()) {
                    csvString = meter.getActiveConsumptionEnergy().getNextValue().get().toString() + " "
                            + meter.getActiveConsumptionEnergy().channelDoc().getUnit().getSymbol();
                }
                csvWriter.append(csvString);
                csvWriter.append(",");
                csvString = ",";
                if (meter.getActivePower().getNextValue().isDefined()) {
                    csvString = meter.getActivePower().getNextValue().get().toString() + " "
                            + meter.getActivePower().channelDoc().getUnit().getSymbol();
                }
                csvWriter.append(csvString);
                csvWriter.append(",");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }

    /**
     * Writes PWM Data in CSV File.
     */
    private void writePwmData() {
        this.pwmDeviceList.forEach(pwm -> {
            String csvString = "-";
            if (pwm.getPwmPowerLevelChannel().getNextWriteValue().isPresent()) {
                csvString = pwm.getPwmPowerLevelChannel().getNextWriteValue().get().toString() + " "
                        + pwm.getPwmPowerLevelChannel().channelDoc().getUnit();
            }
            try {
                csvWriter.append(csvString);
                csvWriter.append(",");
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    private void writeDacData() {
        this.dacList.forEach(dac -> {
            String csvString = "-";
            if (dac.getPowerLevelChannel().getNextWriteValue().isPresent()) {
                csvString = dac.getPowerLevelChannel().getNextWriteValue().get().toString() + " "
                        + dac.getPowerLevelChannel().channelDoc().getUnit().getSymbol();
            }
            try {
                csvWriter.append(csvString);
                csvWriter.append(",");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * All of the Relays Data will be written in CSV File.
     */
    private void writeRelaysData() {
        this.relaysList.forEach(relay -> {

            String csvString = "-";
            if (relay.getRelaysChannel().getNextValue().isDefined()) {
                if (relay.getRelaysChannel().getNextValue().get()) {
                    csvString = "ON";
                } else {
                    csvString = "OFF";
                }
            }
            try {
                csvWriter.append(csvString);
                csvWriter.append(",");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }

    /**
     * Writes TemperatureData of Temperature List in CSV File.
     */
    private void writeTemperatureData() {
        this.thermometerList.forEach(thermometer -> {
            String csvString = "-";
            //default value is 1128 --> no temperature sensor available
            if (thermometer.getTemperature().getNextValue().isDefined() && (thermometer.getTemperature().getNextValue().get() != 1128)) {
                csvString = thermometer.getTemperature().getNextValue().get().toString()
                        + thermometer.getTemperature().channelDoc().getUnit().getSymbol();
            }

            try {
                this.csvWriter.append(csvString);
                this.csvWriter.append(",");
            } catch (IOException e) {
                e.printStackTrace();
            }


        });
    }

    @Deactivate
    public void deactivate() {
        try {
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.deactivate();

    }


}


