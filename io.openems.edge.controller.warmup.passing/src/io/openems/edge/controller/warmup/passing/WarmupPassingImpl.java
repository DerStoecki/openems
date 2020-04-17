package io.openems.edge.controller.warmup.passing;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.warmup.passing.api.ControllerWarmupChannel;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Designate(ocd = Config.class, factory = true)
@Component(name="Warmup.Passing", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class WarmupPassingImpl extends AbstractOpenemsComponent implements OpenemsComponent, ControllerWarmupChannel, Controller {

	private final Logger log = LoggerFactory.getLogger(WarmupPassingImpl.class);

	@Reference
	protected ComponentManager cpm;

	private JsonObject warmupstate;		//Container that is used to save to file and read from file. Identical to file content.
	private final File storage = new File("warmupcontroller.json");	//Name of file the container warmupstate is saved in, location is same directory the .jar is in.
	private static final DateTimeFormatter timeformat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");	//How date+time is formatted in the file.
	private LocalDateTime lastTimestampRuntime, adjustedStartDate;	//Timestamps needed for time calculations.
	private final int activationInterval = 1;	//Interval in minutes between activation of recurring code in run() method.
	private boolean isSwitchedOn;
	private int totalLengthMinutes;	//Total length of the heating program loaded in minutes

	public WarmupPassingImpl() {

		super(OpenemsComponent.ChannelId.values(),
				ControllerWarmupChannel.ChannelId.values(),
				Controller.ChannelId.values());
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.noError().setNextValue(true);
		isSwitchedOn = false;
		if(storage.isFile()) {		//check if file exists, if not create it with standard parameters.
			loadConfigFile();
			if(this.noError().getNextValue().get()){
				if(warmupstate.get("elapsedTime").getAsInt() == 0){     //No heating run was in progress.
                    this.logInfo(this.log, "Ready to start next heating run.");
				} else {		//If elapsedTime is not 0, a heating run was in progress and has been interrupted. Try to resume.
                    this.playPauseWarmupController().setNextWriteValue(true);
				}
			}
		} else {
			createDefaultConfigFile(config);
		}

        this.getElapsedTimeWarmupProgram().setNextValue(warmupstate.get("elapsedTime").getAsInt());
        this.getLengthWarmupProgram().setNextValue(totalLengthMinutes);
	}

	@Deactivate
	public void deactivate() {
		super.deactivate();
	}

	private void createDefaultConfigFile(Config config){
		this.logInfo(this.log, "No config file found. Creating default config file.");
		warmupstate = new JsonObject();
		try{
			warmupstate.addProperty("startDate", LocalDateTime.now().format(timeformat));
			warmupstate.addProperty("lastTimestamp", LocalDateTime.now().format(timeformat));
		} catch (DateTimeException exc) {
			this.noError().setNextValue(false);
            this.logInfo(this.log, "Time format error, couldn't store timestamps in Json object.");
			throw exc;
		}
		warmupstate.addProperty("elapsedTime", 0);
		this.getElapsedTimeWarmupProgram().setNextValue(0);
		warmupstate.addProperty("timeStepDurationMinutes", config.stepLength());	//Duration of each heating step, in minutes. Default 1 day.
		for(int i = 0; i < config.stepNumber(); i++){		//Create default entries for temperature list. Unit is dezidegree celsius.
			warmupstate.addProperty("temp"+i, (config.startTemp()+(i*config.tempIncrease()))*10);
		}
		totalLengthMinutes = 5 * warmupstate.get("timeStepDurationMinutes").getAsInt();

		saveToConfigFile();
	}

	private void saveToConfigFile() {
		try (FileWriter writer = new FileWriter(storage)) {		//Write JSON file
			writer.write(warmupstate.toString());
			writer.flush();
			this.logInfo(this.log, "Saving to config file");
		} catch (IOException e) {
			this.noError().setNextValue(false);
			this.logInfo(this.log, "Error, couldn't write to config file " + storage);
			e.printStackTrace();
		}
	}

	private void loadConfigFile() throws OpenemsError.OpenemsNamedException {
		this.logInfo(this.log, "Loading config file ");
		try (FileReader reader = new FileReader(storage)) {
			JsonParser jParser = new JsonParser();
			Object obj = jParser.parse(reader);
			warmupstate = (JsonObject) obj;
		} catch (FileNotFoundException e) {
			this.noError().setNextValue(false);
			this.logInfo(this.log, "Error, couldn't find file " + storage);
			e.printStackTrace();
		} catch (IOException e) {
			this.noError().setNextValue(false);
			this.logInfo(this.log, "Error, couldn't read from file " + storage + ". File may be corrupted, please replace/delete and try again. If you delete the file, the controller will create an appropriate file with standard parameters.");
			e.printStackTrace();
		} catch (JsonParseException js){
			this.noError().setNextValue(false);
			this.logInfo(this.log, "Error, couldn't translate contents of " + storage + ". Incompatible JSON format or file may be corrupted, please replace/delete and try again. If you delete the file, the controller will create an appropriate file with standard parameters.");
			throw js;
		}

		try{
			lastTimestampRuntime = LocalDateTime.parse(warmupstate.get("lastTimestamp").getAsString(), timeformat);
		} catch (DateTimeParseException exc) {
//				this.noError().setNextValue(false);
			this.logInfo(this.log, "Error reading timestamp from file. Trying to recover by setting new timestamp, may result in incorrect timing of program.");
			lastTimestampRuntime = LocalDateTime.now();
			throw exc;
		}

		if(this.noError().getNextValue().get()){
			this.logInfo(this.log, "Successfully read config file. Temperature list:");
			int count = 0;
			while(warmupstate.has("temp"+count)){   //read all available temperature entries.
				this.logInfo(this.log, "Temp" + count + " = " + warmupstate.get("temp"+count).getAsInt()*0.1 + "°C");
				count++;
			}
			if(count == 0){
                this.noError().setNextValue(false);
                this.logInfo(this.log, "Error, could not find temperature entries in file " + storage);
            }
			totalLengthMinutes = warmupstate.get("timeStepDurationMinutes").getAsInt()*count;
			this.logInfo(this.log, "There are " + count + " temperature entries. Each entry is set to run for " + warmupstate.get("timeStepDurationMinutes").getAsInt()/60 + "h " + warmupstate.get("timeStepDurationMinutes").getAsInt()%60 + "m, for a total running time of " + totalLengthMinutes /1440 + "d " + totalLengthMinutes /60 + "h " + totalLengthMinutes %60 + "m.");

			if(warmupstate.get("elapsedTime").getAsInt() < 0 || warmupstate.get("elapsedTime").getAsInt() >= totalLengthMinutes){
                warmupstate.addProperty("elapsedTime", 0);  //Make sure elapsed time is not out of bounds.
            }
		} else {
			this.playPauseWarmupController().setNextWriteValue(false);
			this.logInfo(this.log, "Encountered an error, deactivating.");
		}
	}

	private void loadHeatingProgramFromFile(String filepath) throws OpenemsError.OpenemsNamedException {
		File filetoload = new File(filepath);
		if(filetoload.isFile()) {	//Check if file exists
			try (FileReader reader = new FileReader(filetoload)) {
				JsonParser jParser = new JsonParser();
				Object obj = jParser.parse(reader);
				JsonObject newProgram = (JsonObject) obj;
			} catch (FileNotFoundException e) {
				this.noError().setNextValue(false);
				this.logInfo(this.log, "Error, couldn't find file " + filetoload);
				e.printStackTrace();
			} catch (IOException e) {
				this.noError().setNextValue(false);
				this.logInfo(this.log, "Error, couldn't read from file " + filetoload);
				e.printStackTrace();
			} catch (JsonParseException js){
				this.noError().setNextValue(false);
				this.logInfo(this.log, "Error, couldn't translate contents of " + filetoload + ". Incompatible JSON format or file may be corrupted.");
				throw js;
			}


			//more stuff here, not yet finished


		} else {
			this.logInfo(this.log, "Error, found no such file:" + filepath);
		}
		this.loadWarmupProgram().setNextWriteValue("done");
	}


	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		//Load program button
		if(this.loadWarmupProgram().getNextWriteValue().isPresent()){
			if(!this.loadWarmupProgram().getNextWriteValue().get().equals("done")) {
				if (this.playPauseWarmupController().getNextWriteValue().isPresent()) {    //Controller needs to be paused or not yet activated.
					if (!this.playPauseWarmupController().getNextWriteValue().get()) {
						loadHeatingProgramFromFile(this.loadWarmupProgram().getNextWriteValue().get());
					}
				} else {
					loadHeatingProgramFromFile(this.loadWarmupProgram().getNextWriteValue().get());
				}
			}
		}

	    //Forward/rewind button
	    if(this.goToMinuteWarmupProgram().getNextWriteValue().isPresent()){
	    	if(this.goToMinuteWarmupProgram().getNextWriteValue().get() >= 0){	//Negative values mean "do nothing". Do not need to check if >totalLength, code will execute "heating finished" branch in that case.
				warmupstate.addProperty("elapsedTime", this.goToMinuteWarmupProgram().getNextWriteValue().get());
				lastTimestampRuntime = LocalDateTime.now().minusMinutes(activationInterval);   //Adjust this so that recurring code executes immediately and updates the temperature.
				this.goToMinuteWarmupProgram().setNextWriteValue(-1);	//Deactivate forward/rewind button
				saveToConfigFile();		//Save changed parameters to file.
			}
        }

        //The play/pause channel has a value and no error is true
		if(this.playPauseWarmupController().getNextWriteValue().isPresent() && this.noError().getNextValue().get()){

			//Pause has just been pressed
			if(isSwitchedOn && !this.playPauseWarmupController().getNextWriteValue().get()){
                lastTimestampRuntime = LocalDateTime.now();
                this.getWarmupTemperature().setNextValue(0);    //Set a low temperature to stop heating.
				isSwitchedOn = false;   //track state
			}

			//Play has just been pressed.
			if(!isSwitchedOn && this.playPauseWarmupController().getNextWriteValue().get()){

			    if(warmupstate.get("elapsedTime").getAsInt() == 0){     //Start of new heating run.
                    try{
                        lastTimestampRuntime = LocalDateTime.now();
                        warmupstate.addProperty("startDate", lastTimestampRuntime.format(timeformat));		//Save start time.
                        warmupstate.addProperty("lastTimestamp", lastTimestampRuntime.format(timeformat));	//This is used to calculate elapsed time.
                        adjustedStartDate = LocalDateTime.now();    //This timestamp is used to calculate elapsed time
                        this.getWarmupTemperature().setNextValue(warmupstate.get("temp0").getAsInt());		//Set temperature value to first temp entry. This one should always exist.
                        this.logInfo(this.log, "Starting heating run at " + (warmupstate.get("temp0").getAsInt()*0.1) + "°C. Duration is " + totalLengthMinutes /60 + "h " + totalLengthMinutes %60 + "m.");
                    } catch (DateTimeException exc) {
                        this.noError().setNextValue(false);
                        throw exc;
                    }
                    saveToConfigFile();
                } else {
                    this.logInfo(this.log, "Resuming heating run that was started at " + warmupstate.get("startDate").getAsString() + ".");
                    this.logInfo(this.log, "Heating was paused/interrupted at " + warmupstate.get("lastTimestamp").getAsString() + ". The pause lasted for " + ChronoUnit.HOURS.between(lastTimestampRuntime, LocalDateTime.now()) + "h " + ChronoUnit.MINUTES.between(lastTimestampRuntime, LocalDateTime.now())%60 + "m.");
                    adjustedStartDate = LocalDateTime.now().minusMinutes(warmupstate.get("elapsedTime").getAsInt());
                    lastTimestampRuntime = LocalDateTime.now().minusMinutes(activationInterval);   //Adjust this so that recurring code executes immediately.
                }
                isSwitchedOn = true;
			}

            //Recurring code during heating run. Activate every activationInterval. SaveToConfigFile() is part of this code, so should not do it every second to reduce system load.
            if(this.playPauseWarmupController().getNextWriteValue().get() && ChronoUnit.MINUTES.between(lastTimestampRuntime, LocalDateTime.now()) >= activationInterval){
                lastTimestampRuntime = LocalDateTime.now();
                try{
                    warmupstate.addProperty("lastTimestamp", lastTimestampRuntime.format(timeformat)); //Store timestamp
                } catch (DateTimeException exc) {
                    this.noError().setNextValue(false);
                    this.logInfo(this.log, "Time format error, couldn't store lastTimestamp in Json object.");
                    throw exc;
                }
                warmupstate.addProperty("elapsedTime", ChronoUnit.MINUTES.between(adjustedStartDate, LocalDateTime.now()));
                this.getElapsedTimeWarmupProgram().setNextValue(warmupstate.get("elapsedTime").getAsInt());     //Update elapsed time
                if(warmupstate.get("elapsedTime").getAsInt()< totalLengthMinutes){  //Check that end is not reached yet.
                    int tempentry = warmupstate.get("elapsedTime").getAsInt()/warmupstate.get("timeStepDurationMinutes").getAsInt();    //Gives position in temperature list
                    this.getWarmupTemperature().setNextValue(warmupstate.get("temp"+tempentry).getAsInt());		//Set temperature value corresponding to execution state.
                    this.logInfo(this.log, "Setting temperature entry temp" + tempentry + ", which is " + (warmupstate.get("temp"+tempentry).getAsInt()*0.1) + "°C.");
                    this.logInfo(this.log, "Elapsed time is " + warmupstate.get("elapsedTime").getAsInt()/1440 + "d " + warmupstate.get("elapsedTime").getAsInt()/60 + "h " + warmupstate.get("elapsedTime").getAsInt()%60 + "m, total length is " + totalLengthMinutes /1440 + "d " + totalLengthMinutes /60 + "h " + totalLengthMinutes %60 + "m.");
                } else {    //Heating run has reached end.
                    this.logInfo(this.log, "Heating run that was started at " + warmupstate.get("startDate").getAsString() + " has finished.");
					this.logInfo(this.log, "Ready to start next heating run.");
                    this.getWarmupTemperature().setNextValue(0);    //Set a low temperature to stop heating.
                    warmupstate.addProperty("elapsedTime", 0);  //Reset everything
                    this.playPauseWarmupController().setNextWriteValue(false);
                    isSwitchedOn = false;
                }
                saveToConfigFile();		//Save changed parameters to file.
            }

		}

	}

}
