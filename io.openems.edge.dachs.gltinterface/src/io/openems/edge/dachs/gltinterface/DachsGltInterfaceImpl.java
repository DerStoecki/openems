package io.openems.edge.dachs.gltinterface;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.dachs.gltinterface.api.DachsGltInterfaceChannel;
import io.openems.edge.chp.device.api.ChpBasic;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;


@Designate(ocd = Config.class, factory = true)
@Component(name = "DachsGltInterfaceImpl",
		configurationPolicy = ConfigurationPolicy.REQUIRE,
		immediate = true)
public class DachsGltInterfaceImpl extends AbstractOpenemsComponent implements OpenemsComponent, ChpBasic, DachsGltInterfaceChannel, Controller {

	private final Logger log = LoggerFactory.getLogger(DachsGltInterfaceImpl.class);
	private InputStream is = null;
	private String urlBuilderIP;
	private String basicAuth;
	private int interval;
	private LocalDateTime timestamp;

	public DachsGltInterfaceImpl() {

		super(OpenemsComponent.ChannelId.values(),
				DachsGltInterfaceChannel.ChannelId.values(),
				ChpBasic.ChannelId.values(),
				Controller.ChannelId.values());
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		interval = config.interval();
		timestamp = LocalDateTime.now().minusSeconds(interval-2);	//Shift timing a bit, may help to avoid executing at the same time as other demanding controllers.
		urlBuilderIP = config.address();
		String gltpass = config.username()+":"+config.password();
		basicAuth = "Basic " + new String(Base64.getEncoder().encode(gltpass.getBytes()));
		getSerialAndPartsNumber();

	}

	@Deactivate
	public void deactivate() {super.deactivate();}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		if (ChronoUnit.SECONDS.between(timestamp, LocalDateTime.now()) >= interval){
			updateChannels();
			timestamp = LocalDateTime.now();

			if (this.setOnOff().getNextWriteValue().isPresent()){
				if (this.setOnOff().getNextWriteValue().get()) {
					activateDachs();
				}
			}
		}


	}

	protected void updateChannels() {
		String temp = getKeyDachs(		//For a description of these commands, look in DachsGltInterfaceChannel
                        "k=Hka_Bd.ulAnzahlStarts&" +
                        "k=Hka_Bd.ulBetriebssekunden&" +
                        "k=Hka_Bd.ulArbeitThermKon&" +
                        "k=Hka_Bd.UHka_Anf.usAnforderung&" +
                        "k=Hka_Bd.UHka_Anf.Anforderung.fStrom&" +
                        "k=Hka_Bd.bWarnung&" +
                        "k=Hka_Bd.UStromF_Frei.bFreigabe&" +
                        "k=Hka_Bd.Anforderung.UStromF_Anf.bFlagSF&" +
                        "k=Hka_Bd.Anforderung.ModulAnzahl&" +
                        "k=Wartung_Cache.fStehtAn&" +
                        "k=Hka_Mw1.usDrehzahl&" +
                        "k=Hka_Bd.ulArbeitElektr&" +
                        "k=Hka_Bd.ulArbeitThermHka&" +
						"k=Hka_Bd.UHka_Frei.usFreigabe&" +
                        "k=Hka_Mw1.Temp.sbRuecklauf&" +
                        "k=Hka_Mw1.Temp.sbVorlauf&" +
						"k=Hka_Mw1.sWirkleistung&" +
						"k=Hka_Bd.bWarnung&" +
                        "k=Hka_Bd.bStoerung");
		if (temp.contains("Hka_Bd.bStoerung=")) {

			String stoerung = readEntryAfterString(temp, "Hka_Bd.bStoerung=");
			if (stoerung.equals("0")) {
				this.isError().setNextValue(false);
			} else {
				this.isError().setNextValue(true);
				this.getErrorMessages().setNextValue(stoerung);

				//more code here to decipher error code

			}
			this.logInfo(this.log, "isError: " + this.isError().getNextValue().get().toString());


			String warnung = readEntryAfterString(temp, "Hka_Bd.bWarnung=");
			if (warnung.equals("0")) {
				this.isWarning().setNextValue(false);
			} else {
				this.isWarning().setNextValue(true);
				this.getWarningMessages().setNextValue(warnung);

				//more code here to decipher warning code

			}
			this.logInfo(this.log, "isWarning: " + this.isWarning().getNextValue().get().toString());


			String wirkleistung = "";	//to make sure there is no null exception when parsing
			wirkleistung = readEntryAfterString(temp, "Hka_Mw1.sWirkleistung=");
			try {
				this.getElectricalPower().setNextValue(Float.parseFloat(wirkleistung));
			} catch (NumberFormatException e) {
				this.logInfo(this.log, "Error, can't parse electrical power (Wirkleistung): " + e.getMessage());
				this.getElectricalPower().setNextValue(0);	//To avoid null exception when printing this channel to the log
			}
			this.logInfo(this.log, "getElectricalPower: " + this.getElectricalPower().getNextValue().get().toString());


			String forwardTemp = "";   //to make sure there is no null exception when parsing
			forwardTemp = forwardTemp + readEntryAfterString(temp, "Hka_Mw1.Temp.sbVorlauf=");
			try {
				this.getForwardTemp().setNextValue(Integer.parseInt(forwardTemp.trim())*10);	//convert to dezidegree
			} catch (NumberFormatException e) {
				this.logInfo(this.log, "Error, can't parse forward temperature (Vorlauf): " + e.getMessage());
				this.getForwardTemp().setNextValue(0);
			}
			this.logInfo(this.log, "getForwardTemp: " + this.getForwardTemp().getNextValue().get());


			String rewindTemp = "";
			rewindTemp = rewindTemp + readEntryAfterString(temp, "Hka_Mw1.Temp.sbRuecklauf=");
			try {
				this.getRewindTemp().setNextValue(Integer.parseInt(rewindTemp.trim())*10);	//convert to dezidegree
			} catch (NumberFormatException e) {
				this.logInfo(this.log, "Error, can't parse rewind temperature (Ruecklauf): " + e.getMessage());
				this.getRewindTemp().setNextValue(0);
			}
			this.logInfo(this.log, "getRewindTemp: " + this.getRewindTemp().getNextValue().get());


			String freigabe = readEntryAfterString(temp, "Hka_Bd.UHka_Frei.usFreigabe=");
			if (freigabe.equals("65535")) {	//This is the int equivalent of hex FFFF. Manual discusses freigabe code in hex.
				this.isReady().setNextValue(true);
			} else {
				this.isReady().setNextValue(false);
				this.getNotReadyCode().setNextValue(freigabe);

				//more code here to decipher freigabe code

			}
			this.logInfo(this.log, "isReady: " + this.isReady().getNextValue().get().toString());



			String drehzahl = "";
			drehzahl = drehzahl + readEntryAfterString(temp, "Hka_Mw1.usDrehzahl=");
            try {
                this.getRpm().setNextValue(Integer.parseInt(drehzahl.trim()));
            } catch (NumberFormatException e) {
                this.logInfo(this.log, "Error, can't parse RPM: " + e.getMessage());
				this.getRpm().setNextValue(0);
            }
			this.logInfo(this.log, "getRpm: " + this.getRpm().getNextValue().get());


		} else {
		    this.logInfo(this.log, "Error: Couldn't read data from GLT interface.");
		}
	}


	//Seperate method for these as they don't change and only need to be requested once.
    protected void getSerialAndPartsNumber() {
        String temp = getKeyDachs("k=Hka_Bd_Stat.uchSeriennummer&k=Hka_Bd_Stat.uchTeilenummer");
        if (temp.contains("Hka_Bd_Stat.uchSeriennummer=")) {
			this.getSerialNumber().setNextValue(readEntryAfterString(temp, "Hka_Bd_Stat.uchSeriennummer="));
            this.logInfo(this.log, "Seriennummer: " + this.getSerialNumber().getNextValue().get());
			this.getPartsNumber().setNextValue(readEntryAfterString(temp, "Hka_Bd_Stat.uchTeilenummer="));
            this.logInfo(this.log, "Teilenummer: " + this.getPartsNumber().getNextValue().get());
        } else {
            this.logInfo(this.log, "Error: Couldn't read data from GLT interface.");
        }
    }

    //Extract a value from the server return message. "stuff" is the return message from the server. "marker" is the value
	//after which you want to read. Reads until the end of the line.
    protected String readEntryAfterString(String stuff, String marker) {
        return stuff.substring(stuff.indexOf(marker)+marker.length(), stuff.indexOf("/n",stuff.indexOf(marker)));
	}


	//Send read request to server
	protected String getKeyDachs(String key) {
		String message = "";
		try {
            URL url = new URL("http://"+urlBuilderIP+":8081/getKey?"+key);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty ("Authorization", basicAuth);
            is = connection.getInputStream();

            // read text returned by server
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                this.logInfo(this.log, line);
                message = message + line + "/n";
            }
            reader.close();

		} catch (MalformedURLException e) {
			this.logInfo(this.log, "Malformed URL: " + e.getMessage());
		} catch (IOException e) {
			this.logInfo(this.log, "I/O Error: " + e.getMessage());
            if (e.getMessage().contains("code: 401")) {
                this.logInfo(this.log, "Wrong user/password. Access refused.");
            } else if (e.getMessage().contains("code: 404") || e.getMessage().contains("Connection refused")) {
                this.logInfo(this.log, "No GLT interface at specified address.");
            }
		} finally {
			if ( is != null )
				try { is.close(); } catch ( IOException e ) { this.logInfo(this.log, "I/O Error: " + e.getMessage()); }
		}

		return message;
	}


	//Send write request to server
	protected String setKeysDachs(String key) {
		String message = "";
		try {
			String body = key;

			URL url = new URL( "http://"+urlBuilderIP+":8081/setKeys" );
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty ("Authorization", basicAuth);
			connection.setRequestMethod( "POST" );
			connection.setDoInput( true );
			connection.setDoOutput( true );
			connection.setUseCaches( false );
			connection.setRequestProperty( "Content-Type",
					"application/x-www-form-urlencoded" );
			connection.setRequestProperty( "Content-Length", String.valueOf(body.length()) );

			OutputStreamWriter writer = new OutputStreamWriter( connection.getOutputStream() );
			writer.write(body);
			writer.flush();
			writer.close();

			is = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is) );
			String line;
			while ((line = reader.readLine()) != null) {
				this.logInfo(this.log, line);
				message = message + line + "/n";
			}
			reader.close();

		} catch (MalformedURLException e) {
			this.logInfo(this.log, "Malformed URL: " + e.getMessage());
		} catch (IOException e) {
			this.logInfo(this.log, "I/O Error: " + e.getMessage());
		} finally {
			if ( is != null )
				try { is.close(); } catch ( IOException e ) { this.logInfo(this.log, "I/O Error: " + e.getMessage()); }
		}

		return message;
	}

	protected void activateDachs() {
		//return setKeysDachs("Stromf_Ew.Anforderung_GLT.bAktiv=1&Stromf_Ew.Anforderung_GLT.bAnzahlModule=1");
		this.logInfo(this.log, setKeysDachs("Stromf_Ew.Anforderung_GLT.bAktiv=1"));
		return;
	}

}
