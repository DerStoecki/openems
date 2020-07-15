package io.openems.edge.channeltutorial;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.types.OpenemsType;
import io.openems.edge.channeltutorial.api.ChannelTutorialChannel;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveNatureTable;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.modbusslave.ModbusType;
import io.openems.edge.common.type.CircularTreeMap;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

/**
 * A module to demonstrate how channels work.
 * Basics: A channel is very similar to a variable that you can use to store and read data. More precisely, a channel is
 * a data container with added functionality like active value, next value, meta data and tracking of past values.
 *
 */

@Designate(ocd = Config.class, factory = true)
@Component(name = "Channel.Tutorial", immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = { //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE //
		})
public class ChannelTutorialImpl extends AbstractOpenemsComponent
		implements OpenemsComponent, ChannelTutorialChannel, EventHandler, ModbusSlave {


	private final Logger log = LoggerFactory.getLogger(ChannelTutorialImpl.class);  // Logger that is used to write to console.
	private int counter = 0;  // Variable with changing content to have something to write into the channels.
	private ArrayList<Optional<Integer>> listOfWrittenValues = new ArrayList<>();
	private boolean runonce = true;

	/**
	 * -- declaring a channel --
	 * Channels can be defined in the class itself or in an extra interface. This module does both to demonstrate both
	 * methods. The interface with the other channels for this module is ChanneltestChannel.java in
	 * io.openems.edge.channeltest.api
	 * Having the channels in an extra interface makes them easier to use/find/understand for other programmers that
	 * want to communicate with your module.
	 *
	 */

	// Start of channel declaration.
	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {  // <- always the same

		TEST1(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_ONLY)), // <- This line is the first channel.

		TEST2(Doc.of(OpenemsType.INTEGER)); // <- This line is the second channel.

		private final Doc doc;  // <- always the same

		private ChannelId(Doc doc) {
			this.doc = doc;
		}  // <- always the same

		public Doc doc() {
			return this.doc;
		}  // <- always the same
	}
	// End of channel declaration.

	/**
	 * Here, two channels are declared. The first channel is named TEST1, the second channel is named TEST2.
	 * You can declare a unit and an access type for a channel (TEST1), but you don't need to (TEST2). When you don't
	 * declare unit or access type, the default is picked, which is NONE for unit and READ_ONLY for access type.
	 *
	 */

	// Add methods to shorten channel calls. This is optional.
	public Channel<Integer> channelTest1() {
		return this.channel(ChannelId.TEST1);
	}
	public Channel<Integer> channelTest2() {
		return this.channel(ChannelId.TEST2);
	}

	/**
	 * All channels and implemented interfaces that have channels need to be listed in the constructor. Otherwise the
	 * associated channels do not work.
	 *
	 */

	// Constructor
	public ChannelTutorialImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), // <- An interface that is implemented by this class and has channels.
				ChannelTutorialChannel.ChannelId.values(), // <- Interface specifically written for this class where channels are defined.
				ChannelId.values() // <- Channels defined directly in this module
		);
	}


	/**
	 * It can be useful to put values in your channels in the "activate" method. Channels are initialized with value
	 * "null", which can cause null pointer exceptions. So you either need to account for the possibility of null in the
	 * channel or make sure the value is not null.
	 *
	 */

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		// Callback to catch multiple writes. Discussed in WriteChannel section 3.
		this.exampleWriteChannel4().onSetNextWrite(value -> listOfWrittenValues.add(Optional.ofNullable(value)));

		this.logInfo(this.log, "-- Activating Channeltest module --");

		this.noError().setNextValue(true);  // <- This is the standard method to put a value in a channel. Writes "true" into [next] container.
		boolean contentOfNoErrorChannel = this.noError().getNextValue().get();  // <- Read value in [next] container.
		this.logInfo(this.log, "noError channel [next] value: " + contentOfNoErrorChannel); // <- Now print it to the log.

		// Actually, using ".getNextValue().get()" is not the recommended way to read from a channel.
		// What you should use is ".value().get()", to read from the [active] data container.

		// boolean thisIsNull = this.noError().value().get(); // <- This throws a null pointer exception!
		this.logInfo(this.log, "noError channel [active] value: " + this.noError().value().get()); // <- This is ok.

		// Reading from the [active] container right now would return "null", and trying to put that in a boolean will
		// throw a null pointer exception. You can convert null into string however, so that works.

		// The [active] container is initialized with null and nothing has been put in it so far. [next] contains a
		// value, but "switch process image" has not yet happened to copy the contents of [next] into [active].
		// We can call "switch process image" manually to copy [next] into [active]:

		this.noError().nextProcessImage();
		this.logInfo(this.log, "Executing \".nextProcessImage()\" on noError channel.");
		boolean nowItWorks = this.noError().value().get();
		this.logInfo(this.log, "noError channel [active] value: " + nowItWorks);
		this.logInfo(this.log, "");

		// Now both [next] and [active] of channel "noError" contains data, and we don't have to worry about null
		// pointer exceptions for this channel anymore.

	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}


	/**
	 * -- Data structure of channels --
	 *
	 * OpenEMS has two types of channels with different functionality: the "basic" channel, and the basic channel with
	 * added support for external writes. OpenEMS calls the basic channel ReadChannel and the extended channel
	 * WriteChannel. The WriteChannel extends the class of the ReadChannel, so the WriteChannel inherits all the
	 * functionality of the ReadChannel. Which of the two channels it is, is defined by the "AccessMode" modifier.
	 * "READ_ONLY" is the ReadChannel, "READ_WRITE" is the WriteChannel.
	 * The "read only" tag for the "basic" channel is in regard to access from external sources via JSON/REST calls.
	 * The channel needs to be filled with data in some way too, so there has to be a write method for it. That method is
	 * usable by every module in OpenEMS without restrictions, so if you just consider internal communication by OpenEMS
	 * modules, the "READ_ONLY" tag is misleading. That is why I rather call it "basic" channel.
	 *
	 * The flow of data in a channel is not so straightforward. The data structure of the basic channel is as follows:
	 *
	 * From the JavaDoc of Channel.java:
	 * "Channels implement a 'Process Image' pattern. They provide an 'active' value
	 * which should be used for any operations on the channel value. The 'next'
	 * value is filled by asynchronous workers in the background. At the 'Process
	 * Image Switch' the 'next' value is copied to the 'current' value."
	 *
	 * The WriteChannel has added functionality on top of that. A graphical representation of the data structure of the
	 * two channel types then looks like this:
	 *
	 *   					-- basic channel --							|	  -- write channel extension --
	 * 																	|
	 * 											.setNextValue(...)		|		  .setNextWriteValue(...)
	 *  	                					     v					|				 v
	 * 		[active]   <--switch process image--  [next]         no connection 	 	 [write]
	 *  	   v					 				v					|				v
	 * 	 .value().get()						.getNextValue().get()		|		.getNextWriteValue().get()
	 *
	 *
	 * This picture shows three data containers named [active], [next] and [write]. [active] and [next] are the
	 * containers of the basic channel, the [write] container is the extended functionality of the WriteChannel. Above
	 * the data containers are the commands to write values into the container, below are the commands to read values
	 * from the container. The ChannelId needs to be written in front of each command.
	 * As you can see, you can read from all three containers, but you can only directly write in [next] and [write].
	 * The [active] container is filled with data in a different way:
	 * Channels automatically write their [next] data into the [active] field at the "switch process image" event. The
	 * "switch process image" event is part of the OpenEMS cycle.
	 * (https://openems.github.io/openems.io/openems/latest/edge/architecture.html#_cycle)
	 * The idea is that [next] is dynamic and can change during a cycle, while [active] is more static and changes
	 * value at a predictable timing. Reading [active] will always give the same value during the same cycle, while
	 * this is not guaranteed for [next].
	 * (That's not the complete truth however. You can manually call "switch process image" on a channel anytime to
	 * change the content of [active].)
	 * The recommended method for reading data from a channel is then "value().get()", to read the contents of [active].
	 *
	 * The [write] container is completely separate from [next] and [active]. This is because of Modbus.
	 * A Modbus device has holding registers and coils that you can read from and write to. The read and write is at the
	 * same address, but the values can be different.
	 * Example: Alpha Innotec heat pump, coil address 2 = ventilator status.
	 * 			Read: 0 = off, 1 = on.
	 * 			Write: 1 = force on, 0 = automatic.
	 * So you can write 0 to that coil, and read 1 afterwards.
	 * The OpenEMS implementation of Modbus is to attach an OpenEMS channel to a Modbus address. Since Modbus read can
	 * be a different value than Modbus write, you would need two channels to avoid one value overwriting the other. The
	 * more elegant solution is now the separate [write] container of a write channel, which effectively splits the
	 * channel in two parts. Anything you want to write to Modbus you put in the [write] container, the result of a
	 * Modbus read will be put in the [next] container and after "switch process image" also in [active].
	 *
	 * When do you need a write channel? (excluding Modbus)
	 * The reason to ask this is that when you don't use Modbus, [write] has no connection to [next] and [active]. The
	 * recommended method to read data from a channel is to read from [active], and data is put into [active] by
	 * way of writing into [next] and waiting for "switch process image" to happen. What use has the [write] container
	 * here? The answer is that [write] is used for input from external sources. External source in this context means
	 * your module has no control over the timing of the write, and a write can happen at any time.
	 *
	 * When the external source is literally an external source like a JSON/REST call, then the use of [write] is
	 * enforced by the implementation of JSON/REST in OpenEMS. JSON/REST can only write into [write], not [next].
	 * When the external source is another OpenEMS module that wants to communicate with your module, using write
	 * channels and the [write] container for the inputs into your module is considered good coding practice.
	 * There are no restrictions that prevent other OpenEMS modules to write into [next] of a channel your module declared.
	 * However, depending on the code, doing so might disrupt the function of your module. The convention is then to use
	 * [write] as the container where external writes are expected. Declaring an input channel of your module as a write
	 * channel signals to other coders that yes, this channel is intended to be written to and it is safe to do so. Of
	 * course, you need to treat the [write] container in your module then as a container where the value can change at
	 * anytime.
	 *
	 * To summarize - good coding practice:
	 * Writing in a channel your module declared - use .setNextValue(...)
	 * Writing in a channel of another module - use .setNextWriteValue(...)
	 * (Obviously, the channel in the other module needs to be a WriteChannel for this to work.)
	 *
	 */



	/**
	 * -- Where to put code for channels in the OpenEMS framework --
	 *
	 * Channels work at any place in the framework, as long as they have been properly declared and initialized. The
	 * "properly declared" part refers to the lines of code needed in the constructor. A channel is initialized after
	 * these lines of code in the constructor. These lines are also responsible for adding the channels to the
	 * "switch process image" event. The "switch process image" event will then happen automatically for every channel.
	 *
	 * An example of where channels can be used is given in the "activate()" method of this module. Since channels
	 * contain "null" when they are initialized, it is common to fill them with values in the "activate()" method to
	 * avoid null pointer exceptions. Handling the possibility of "null" in a channel is the main hazard to watch out
	 * for. Even if you declare a channel as Integer, you can still write null into it.
	 *
	 * This module uses the "EventHandler" interface and the handleEvent() method to place the recurring code that
	 * updates the channels. As an example for alternative code, the "run()" method of the "Controller" interface would
	 * have worked just as well.
	 *
	 * The recurring code of this module is placed in the "updateChannels()" method that is called in the handleEvent()
	 * method. The try-catch block here is needed because write channels are used, which throw the OpenemsNamedException
	 * when using ".setNextWriteValue(...)". If only read channels are used the OpenemsNamedException does not need to
	 * be addressed.
	 *
	 */
	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
			case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
				try {
					this.updateChannels();
				} catch (OpenemsError.OpenemsNamedException e) {
					e.printStackTrace();
				}
				break;
		}
	}

	private void updateChannels() throws OpenemsError.OpenemsNamedException {

		// The counter is used to fill the channels with changing values and control the program flow.
		counter++;

		if (counter <= 5) {
			if (counter == 1) {
				this.logInfo(this.log, "--ReadChannel demonstration--");
				this.logInfo(this.log, "");
			}

			readChannelDemonstration();
		}

		if (counter > 5 && counter <= 10) {
			if (counter == 6) {
				this.logInfo(this.log, "--WriteChannel demonstration--");
				this.logInfo(this.log, "");
			}

			writeChannelDemonstration();
		}

		if (counter > 10 && counter <= 15) {
			if (counter == 11) {
				this.logInfo(this.log, "--WriteChannel demonstration 2: data format & null--");
				this.logInfo(this.log, "");
			}

			writeChannelDemonstration2();
		}

		if (counter > 15) {
			if (counter == 16) {
				this.logInfo(this.log, "--WriteChannel demonstration 3: multiple writes--");
				this.logInfo(this.log, "");
			}

			writeChannelDemonstration3();
		}

		if (counter == 20) {

			pastChannelValues();

			counter = 0;
		}
	}

	private void readChannelDemonstration() {

		/**
		 * -- Basic channel / ReadChannel read and write --
		 *
		 * Channel TEST1 and TEST2 are integer read channels. The standard method to put a value xxx in a channel is
		 * the ".setNextValue(xxx)" command. Before the command, you need to put the ChannelId.
		 * Usually, a channel has a method to shorten the Id call. This example uses the short call for channel TEST1.
		 * For channel TEST2, the full Id call is used. Both do exactly the same.
		 * The "this." in front is optional and used to emphasize that it is the channel of this module.
		 *
		 */

		this.channelTest1().setNextValue(counter);
		this.channel(ChannelId.TEST2).setNextValue(counter);
		this.logInfo(this.log, "Writing " + counter + " into [next] of channel Test1 and Test2.");

		/**
		 * We have now written the number in "counter" into the [next] container of both channels. As the program runs,
		 * "switch process image" happens and copies the content of [next] into [active]. Here is how to print out the
		 * contents:
		 *
		 */

		// Continue to use the full Id call for Test2.
		this.logInfo(this.log, "Channel Test1 [next]: " + this.channelTest1().getNextValue().get());
		this.logInfo(this.log, "Channel Test2 [next]: " + this.channel(ChannelId.TEST2).getNextValue().get());
		this.logInfo(this.log, "Channel Test1 [active]: " + this.channelTest1().value().get());
		this.logInfo(this.log, "Channel Test2 [active]: " + this.channel(ChannelId.TEST2).value().get());

		this.channelTest1().nextProcessImage();
		this.logInfo(this.log, "Executing \".nextProcessImage()\" on channel Test1.");

		// Now use the short Id call for Test2
		this.logInfo(this.log, "Channel Test1 [active]: " + this.channelTest1().value().get());
		this.logInfo(this.log, "Channel Test2 [active]: " + this.channelTest2().value().get());
		this.logInfo(this.log, "");

		/**
		 * The command to read [active] of channel Test1 is "this.channelTest1().value().get()". Lets dissect that:
		 * - "this.channelTest1()" is a method that returns the ChannelId.
		 * - ".value()" is a method that returns the [active] container. The container is of type "Value<T>", which is
		 *   a class defined in "io.openems.edge.common.channel.value".
		 * - ".getNextValue()" is used instead of ".value()" to return the [next] container. That is also a "Value<T>".
		 * - ".get()" is a method in Value<T> that returns the content.
		 *
		 * Things to note:
		 * - We don't have to worry about "null" in the channels here, since we just print the channel content and
		 *   "null" can be converted into a string without a null pointer exception.
		 * - We have called ".nextProcessImage()" on channel Test1. This demonstrates the effects of manually calling
		 *   "switch process image". Notice how Test2 is not affected by this.
		 * - Remember, the recommended method to read a channel is ".value().get()".
		 *
		 */
	}

	private void writeChannelDemonstration() throws OpenemsError.OpenemsNamedException {

		/**
		 * -- WriteChannel section 1: read and write --
		 *
		 * A WriteChannel is an extension of a ReadChannel. That means a WriteChannel can do everything a ReadChannel
		 * can do. The extension is that the WriteChannel also has the [write] data container with associated write and
		 * read methods.
		 * [write] is a bit different. While [next] and [active] are both "Value<T>", [write] is an "Optional<T>".
		 *
		 */

		this.exampleWriteChannel1().setNextWriteValue(counter * 10);
		this.exampleWriteChannel2().setNextWriteValue(counter * 10);
		this.logInfo(this.log, "Writing " + (counter * 10) + " into [write] of channel Write1 and Write2.");

		// Write nothing in [next] of Write2 to show it remains null.
		this.exampleWriteChannel1().setNextValue(counter);
		this.logInfo(this.log, "Writing " + counter + " into [next] of channel Write1.");

		/**
		 * A write channel then has three data containers that each can have a different value. [write] is NOT connected
		 * to [next] or [active]. To show that, different values are written in [write] and [next] of Write1, and
		 * nothing is written into [next] of Write2.
		 * The [write] container is not automatically copied into the [active] container at the process image switch
		 * event. Consequently, calling ".nextProcessImage()" does not do anything with the value in [write].
		 *
		 */

		this.logInfo(this.log, "Channel Write1 [write]: " + this.exampleWriteChannel1().getNextWriteValue().get());
		this.logInfo(this.log, "Channel Write2 [write]: " + this.exampleWriteChannel2().getNextWriteValue().get());
		this.logInfo(this.log, "Channel Write1 [next]: " + this.exampleWriteChannel1().getNextValue().get());
		this.logInfo(this.log, "Channel Write2 [next]: " + this.exampleWriteChannel2().getNextValue().get());
		this.logInfo(this.log, "Channel Write1 [active]: " + this.exampleWriteChannel1().value().get());
		this.logInfo(this.log, "Channel Write2 [active]: " + this.exampleWriteChannel2().value().get());

		this.exampleWriteChannel1().nextProcessImage();
		this.exampleWriteChannel2().nextProcessImage();
		this.logInfo(this.log, "Executing \".nextProcessImage()\" on channel Write1 and Write2.");

		this.logInfo(this.log, "Channel Write1 [active]: " + this.exampleWriteChannel1().value().get());
		this.logInfo(this.log, "Channel Write2 [active]: " + this.exampleWriteChannel2().value().get());
		this.logInfo(this.log, "");

		/**
		 * Putting completely different values in [write] and [next] as shown in this example for channel Write1 is very
		 * confusing and should be avoided. Using [write] and leaving [next] and [active] null as shown here for channel
		 * Write2 is also bad practice.
		 * A channel that is in use should have values in [active], since reading [active] is the recommended way to
		 * access a channel.
		 *
		 */
	}

	private void writeChannelDemonstration2() throws OpenemsError.OpenemsNamedException {

		/**
		 * -- WriteChannel section 2: data format & null pointer exceptions --
		 *
		 * A WriteChannel is supposed to handle inputs from external sources. We have no control over when a write
		 * occurs, nor can we be sure the written data is of a valid format. For the data format, the channels have
		 * inbuilt methods to handle that.
		 *
		 * A curious thing is that there is a big difference between [next] and [write]. As has been mentioned, [next]
		 * is a "Value<T>" while [write] is an "Optional<T>". "Value<T>" has a data converter, "Optional<T>" does not.
		 * The result is then this:
		 *
		 */

		if (runonce) {

			// The channel is of type integer. The compiler won't let you put a boolean in [write] that is an Optional<Integer>.
			// The following line would cause a compiler error:
			// this.exampleWriteChannel1().setNextWriteValue(true);

			// But you can put a boolean in [next] of the same channel, because that is a Value<Integer> and Value<T> has
			// a data converter:
			this.exampleWriteChannel1().setNextValue(true);
			// true is converted to 1
			this.logInfo(this.log, "Writing true into [next] of channel Write1.");
			this.logInfo(this.log, "Channel Write1 [next]: " + this.exampleWriteChannel1().getNextValue().get());
			this.exampleWriteChannel1().setNextValue(false);
			// false is converted to 0
			this.logInfo(this.log, "Writing false into [next] of channel Write1.");
			this.logInfo(this.log, "Channel Write1 [next]: " + this.exampleWriteChannel1().getNextValue().get());
			this.exampleWriteChannel1().setNextValue(10.9);
			// A float is converted to an integer and rounded correctly. 10.9 is rounded up to integer 11
			this.logInfo(this.log, "Writing 10.9 into [next] of channel Write1.");
			this.logInfo(this.log, "Channel Write1 [next]: " + this.exampleWriteChannel1().getNextValue().get());
			this.exampleWriteChannel1().setNextValue(10.3);
			// 10.3 is rounded down to integer 10
			this.logInfo(this.log, "Writing 10.3 into [next] of channel Write1.");
			this.logInfo(this.log, "Channel Write1 [next]: " + this.exampleWriteChannel1().getNextValue().get());
			this.logInfo(this.log, "");


			// Because Value<T> has a data converter, the compiler won't throw an error when non-fitting data is put in
			// a Value<T>. More precisely, the input for Value<T> accepts type "object" and the object is then converted.
			// If it can not be converted, you will get an error on runtime. Since everything is an object, the compiler
			// will allow anything as an input for Value<T>.
			// This would compile, but will throw an error when running:
			// this.exampleWriteChannel1().setNextValue('a');
			// this.exampleWriteChannel1().setNextValue("string");

			runonce = false;
		}


		/**
		 * The previous example for WriteChannels had several flaws:
		 * - [active] is empty or filled with values unrelated to the values in [write].
		 * - We do not account for the possibility of null in [write]
		 * In this section we address how to fix that.
		 *
		 * Note that null is a valid format for all channels and will not cause an error when it is written into
		 * a channel. So when reading a channel data container, we have to be careful that it might contain null.
		 *
		 * Pretty much any operation on channel data is prone to a null pointer exception!
		 * Things such as:
		 * - Using a channel in an if statement.
		 * - Putting channel data in a variable.
		 * - Doing math with channel data
		 *
		 * When you do any of these things, deploy safety measures to prevent a null pointer exception!
		 *
		 */

		// Every second cycle, write null in [write]
		if (counter%2 == 1) {
			this.exampleWriteChannel1().setNextWriteValue(null);
			this.exampleWriteChannel2().setNextWriteValue(null);
			this.exampleWriteChannel3().setNextWriteValue(null);
			this.logInfo(this.log, "Writing null into [write] of channel Write1, Write2 and Write3.");
		} else {
			this.exampleWriteChannel1().setNextWriteValue(counter);
			this.exampleWriteChannel2().setNextWriteValue(counter);
			this.exampleWriteChannel3().setNextWriteValue(counter);
			this.logInfo(this.log, "Writing " + counter + " into [write] of channel Write1, Write2 and Write3.");
		}

		// This copies the value in [write] over to [next] only if [write] is not null. If it is null, ".isPresent()"
		// evaluates to false and writing into [next] is not executed. Then [next] just keeps the value it had before.
		// [next] is never null with this code.
		if (this.exampleWriteChannel1().getNextWriteValue().isPresent()) {
			int thisIsNeverNull = this.exampleWriteChannel1().getNextWriteValue().get();
			this.exampleWriteChannel1().setNextValue(thisIsNeverNull);
		}

		// This also copies the value in [write] over to [next]. But if [write] is null, ".orElse(xxx)" returns xxx
		// instead of null. In this case, 0 was chosen to be returned instead of null.
		// This method is especially useful for a boolean channel.
		// Again, [next] is never null with this code.
		int wontThrowError = this.exampleWriteChannel2().getNextWriteValue().orElse(0);
		this.exampleWriteChannel2().setNextValue(wontThrowError);

		// The code that copies values from [write] to [next] for channel Write3 is in ChanneltestChannel.java
		// That code also copies null, so [next] can be null.


		/**
		 * Which of these methods to collect data from [write] you use depends on your code. The example for
		 * channel Write1 and Write2 let you decide when you update the value in [next], while the code for Write3
		 * updates [next] as soon as a write happens in [write].
		 *
		 */

		// Need to use ".orElse(...)" for returning [write]. If [write] is null, ".get()" will throw a null pointer exception.
		this.logInfo(this.log, "Channel Write1 [write]: " + this.exampleWriteChannel1().getNextWriteValue().orElse(null));
		this.logInfo(this.log, "Channel Write2 [write]: " + this.exampleWriteChannel2().getNextWriteValue().orElse(null));

		if (this.exampleWriteChannel3().getNextWriteValue().isPresent()) {  // <- ".isPresent()" because this is an Optional<T>
			// We tested that [write] is not null, so we can use ".get()".
			this.logInfo(this.log, "Channel Write3 [write]: " + this.exampleWriteChannel3().getNextWriteValue().get());
		} else {
			this.logInfo(this.log, "Channel Write3 [write]: null detected!");
		}

		this.logInfo(this.log, "Channel Write1 [next]: " + this.exampleWriteChannel1().getNextValue().get());
		this.logInfo(this.log, "Channel Write2 [next]: " + this.exampleWriteChannel2().getNextValue().get());
		this.logInfo(this.log, "Channel Write3 [next]: " + this.exampleWriteChannel3().getNextValue().get());
		this.logInfo(this.log, "Channel Write1 [active]: " + this.exampleWriteChannel1().value().get());
		this.logInfo(this.log, "Channel Write2 [active]: " + this.exampleWriteChannel2().value().get());

		if (this.exampleWriteChannel3().value().isDefined()) {  // <- ".isDefined()" because this is a Value<T>
			this.logInfo(this.log, "Channel Write3 [active]: " + this.exampleWriteChannel3().value().get());
		} else {
			this.logInfo(this.log, "Channel Write3 [active]: null detected!");
		}
		this.logInfo(this.log, "");

		/**
		 * As mentioned before, [write] is different to [next] and [active]. [next] and [active] are both
		 * "Value<T>", while [write] is an "Optional<T>".
		 * That means:
		 * - When the content is null, ".get()" on "Value<T>" will return null, while ".get()" on "Optional<T>" will
		 *   throw a null pointer exception!
		 * - The compiler knows that and will warn you if you use ".get()" on "Optional<T>" without first testing if
		 *   "Optional<T>" is null.
		 * - To test if "Optional<T>" is null, the command is ".isPresent()". For "Value<T>" the command is ".isDefined()".
		 *
		 */
	}

	private void writeChannelDemonstration3() throws OpenemsError.OpenemsNamedException {

		/**
		 * -- WriteChannel section 3: multiple writes --
		 *
		 * When you write multiple times in the same cycle into [write] or [next], the behavior is like with any
		 * other variable. The last written value overwrites the previous one.
		 *
		 */

		this.exampleWriteChannel1().setNextValue(counter + 4);
		this.exampleWriteChannel1().setNextValue(counter + 3);
		this.exampleWriteChannel1().setNextValue(counter + 2);
		this.exampleWriteChannel1().setNextValue(counter + 1);
		this.exampleWriteChannel1().setNextValue(counter);

		this.exampleWriteChannel3().setNextWriteValue(counter + 4);
		this.exampleWriteChannel3().setNextWriteValue(counter + 3);
		this.exampleWriteChannel3().setNextWriteValue(counter + 2);
		this.exampleWriteChannel3().setNextWriteValue(counter + 1);
		this.exampleWriteChannel3().setNextWriteValue(counter);

		this.logInfo(this.log, "Writing " + (counter + 4) + ", " + (counter + 3) + ", " + (counter + 2) + ", "
				+ (counter + 1) + "and " +  counter + " in [next] of Write1 and [write] of Write3.");

		this.logInfo(this.log, "Channel Write3 [write]: " + this.exampleWriteChannel3().getNextWriteValue().orElse(null));
		this.logInfo(this.log, "Channel Write1 [next]: " + this.exampleWriteChannel1().getNextValue().get());
		this.logInfo(this.log, "Channel Write3 [next]: " + this.exampleWriteChannel3().getNextValue().get());
		this.logInfo(this.log, "Channel Write1 [active]: " + this.exampleWriteChannel1().value().get());
		this.logInfo(this.log, "Channel Write3 [active]: " + this.exampleWriteChannel3().value().get());

		this.logInfo(this.log, "");


		/**
		 * It may be the case that you do not want this overwriting behavior.
		 * For a WriteChannel where you have no control when the write occurs, it may happen that several writes come in
		 * quick succession and you want to get all the values. This is possible by using the ".onSetNextWrite()" and
		 * ".onSetNextValue()" callbacks. The callback for channel Write4 was set in the activate() method.
		 *
		 */

		this.exampleWriteChannel4().setNextWriteValue(counter + 40);
		this.logInfo(this.log, "Channel Write4 [write]: " + this.exampleWriteChannel4().getNextWriteValue().orElse(null));
		this.exampleWriteChannel4().setNextWriteValue(counter + 30);
		this.logInfo(this.log, "Channel Write4 [write]: " + this.exampleWriteChannel4().getNextWriteValue().orElse(null));
		this.exampleWriteChannel4().setNextWriteValue(counter + 20);
		this.logInfo(this.log, "Channel Write4 [write]: " + this.exampleWriteChannel4().getNextWriteValue().orElse(null));
		this.exampleWriteChannel4().setNextWriteValue(counter + 10);
		this.logInfo(this.log, "Channel Write4 [write]: " + this.exampleWriteChannel4().getNextWriteValue().orElse(null));
		this.exampleWriteChannel4().setNextWriteValue(counter);
		this.logInfo(this.log, "Channel Write4 [write]: " + this.exampleWriteChannel4().getNextWriteValue().orElse(null));

		// Code to fill listOfWrittenValues with data is in "activate()", line 126.
		String writtenValues = "";
		for (Optional<Integer> entry : listOfWrittenValues) {
			writtenValues = writtenValues + entry.orElse(null) + ", ";
		}
		listOfWrittenValues.clear();

		this.logInfo(this.log, "Channel Write4 written values saved to list: " + writtenValues.substring(0, writtenValues.length()-2));
		this.logInfo(this.log, "");
	}

	private void pastChannelValues() {
		/**
		 * -- Past Channel Values --
		 *
		 * And finally, here is an example of how to access the past values of a channel. Only values that have been
		 * in [active] are saved. The maximum number of values saved is 100.
		 *
		 * The process is as follows:
		 * Values are saved in a CircularTreeMap, the keys are the timestamps. The timestamp for a value is created
		 * when it is written with ".setNextValue()". When "switch process image" happens, the old value in [active]
		 * is saved to the CircularTreeMap before it is replaced with the current value in [next]. Since no duplicate
		 * keys are allowed in a Map, if a value with an existing key is added it overwrites the value with the same
		 * key that is already in the Map. That means, if you execute ".setNextValue()" just once, only one value will
		 * be added to the past value list. It is added every "switch process image" event, but since is has the same
		 * timestamp it just overwrites itself in the list of past values over and over.
		 *
		 * So it is a bit tricky to say how far into the past the values in the CircularTreeMap reach. If "switch
		 * process image" is not called manually, the values reach at least 100 seconds into the past. Or 100
		 * executions of ".setNextValue()", where just one execution per second is counted.
		 *
		 */

		this.logInfo(this.log, "--Past Channel Values--");

		CircularTreeMap<LocalDateTime, Value<Integer>> collection = this.exampleWriteChannel4().getPastValues();
		for (Map.Entry<LocalDateTime, Value<Integer>> entry : collection.entrySet()) {
			this.logInfo(this.log, "Channel Write4 past [active] values: " + entry.getValue().get());
		}
		this.exampleWriteChannel4().getPastValues().clear();

		this.logInfo(this.log, "");
	}

	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		return new ModbusSlaveTable( //
				// Here is the start of Modbus address 220.
				OpenemsComponent.getModbusSlaveNatureTable(accessMode), //
				// Here is the start of Modbus address 300.
				ModbusSlaveNatureTable.of(ChannelTutorialImpl.class, accessMode, 200) // <- Number here is how many registers
						.channel(0, ChannelId.TEST1, ModbusType.UINT16) // <- Address 302
						.channel(1, ChannelId.TEST2, ModbusType.UINT16) // <- Address 303
						.channel(2, exampleWriteChannel1().channelId(), ModbusType.UINT16) // <- Address 304
						.channel(3, exampleWriteChannel2().channelId(), ModbusType.UINT16) // <- Address 305
						.channel(4, exampleWriteChannel3().channelId(), ModbusType.UINT16) // <- Address 306
						.channel(5, exampleWriteChannel4().channelId(), ModbusType.UINT16) // <- Address 307
						.build());
				// Modbus address for another entry here would start with 500.
	}

	/**
	 * -- Making channels available for Modbus --
	 *
	 * You can make your channels available to be read by Modbus. For the device to act as a Modbus Slave, use the
	 * "Controller Api Modbus/TCP" or "Controller Api Modbus/Serial" in Apache Felix. In the UI you need to enter
	 * the Id of your module. For this to work, your module must implement the "ModbusSlave" interface and then contain
	 * the "getModbusSlaveTable()" method. This method defines which channel is mapped to which Modbus register.
	 * The channels are mapped to both input and holding registers. Only write channels can accept writes from Modbus.
	 *
	 * The register address is dynamic and not static and depends on what modules where added before. The example given
	 * here assumes no other modules were added to the Modbus API.
	 *
	 * After the "OpenemsComponent..." entry, the Modbus address is at 300. The "length" argument in
	 * "ModbusSlaveNatureTable" defines how many registers this entry reserves.
	 *
	 * Declaring entries with "ModbusSlaveNatureTable" adds two entries at the start of the address field that contain
	 * meta information. The first entry at address 300 is the class name (ChannelTutorialImpl), the second entry at
	 * address 301 is number of registers reserved for this class (200). At address 302 is then the entry with offset 0,
	 * which is channel TEST1.
	 *
	 */
}