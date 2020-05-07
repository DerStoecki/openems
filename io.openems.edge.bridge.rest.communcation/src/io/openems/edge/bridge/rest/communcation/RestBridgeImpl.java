package io.openems.edge.bridge.rest.communcation;

import io.openems.edge.bridge.rest.communcation.api.RestBridge;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.event.EventConstants;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Bridge.Rest",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)
public class RestBridgeImpl extends AbstractOpenemsComponent implements RestBridge, OpenemsComponent {


    public RestBridgeImpl() {
        super(OpenemsComponent.ChannelId.values());
    }

    /*
    * TODO! 1. Genibus ---> each controller adds Tasks with its id --> put together for Rest requests / responses
    *  --> POST or GET; Deactivate Method
    * TODO: Header etc via IP; Include BASE Encoder; etc etc
    *
    *
    *
    *
    *
    * */
}


/*
* private static final String DEFAULT_USERNAME = StringUtils.EMPTY;
private String username = DEFAULT_USERNAME;
@Property(label="username", description="user name", value=DEFAULT_USERNAME )
public static final String USERNAME = "xxx.username";

private static final String DEFAULT_PASSWORD = StringUtils.EMPTY;
private String password = DEFAULT_PASSWORD;
@Property(label="password", description="user password", passwordValue=DEFAULT_PASSWORD )
public static final String PASSWORD = "xxx.password";
*
* */

/*
* package io.openems.edge.dachs.gltinterface;

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
import java.util.Base64;


@Designate(ocd = Config.class, factory = true)
@Component(name = "DachsGltInterfaceImpl",
		configurationPolicy = ConfigurationPolicy.REQUIRE,
		immediate = true)
public class DachsGltInterfaceImpl extends AbstractOpenemsComponent implements OpenemsComponent, DachsGltInterfaceChannel, Controller {

	private final Logger log = LoggerFactory.getLogger(DachsGltInterfaceImpl.class);
	private InputStream is = null;
	private String urlBuilderIP;
	private String basicAuth;
	private int testcounter = 0;

	public DachsGltInterfaceImpl() {

		super(OpenemsComponent.ChannelId.values(),
				DachsGltInterfaceChannel.ChannelId.values(),
				Controller.ChannelId.values());
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		this.isError().setNextValue(false);
		urlBuilderIP = config.address();
		String gltpass = config.username()+":"+config.password();
		basicAuth = "Basic " + new String(Base64.getEncoder().encode(gltpass.getBytes()));

	}

	@Deactivate
	public void deactivate() {super.deactivate();}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		if (testcounter == 2){
			updateChannels();
		}


		testcounter++;

	}

	protected void updateChannels() {
		String temp = getKeyDachs("k=Hka_Bd_Stat.ulInbetriebnahmedatum&k=Hka_Bd.ulAnzahlStarts&k=Hka_Bd.ulBetriebssekunden&k=Hka_Bd.ulArbeitThermKon&k=Hka_Bd.UHka_Frei.usFreigabe&k=Hka_Bd.UHka_Anf.usAnforderung&k=Hka_Bd.UHka_Anf.Anforderung.fStrom&k=Hka_Bd.bWarnung&k=Hka_Bd.UStromF_Frei.bFreigabe&k=Hka_Bd.Anforderung.UStromF_Anf.bFlagSF&k=Hka_Bd.Anforderung.ModulAnzahl&k=Wartung_Cache.fStehtAn&k=Hka_Mw1.usDrehzahl&k=Hka_Mw1.sWirkleistung&k=Hka_Bd.ulArbeitElektr&k=Hka_Bd.ulArbeitThermHka&k=Hka_Mw1.Temp.sbRuecklauf&k=Hka_Mw1.Temp.sbVorlauf&k=Hka_Bd.bStoerung");
		if (temp.contains("Hka_Bd.bStoerung=")) {

			String wirkleistung = temp.substring(temp.indexOf("Hka_Mw1.sWirkleistung=")+"Hka_Mw1.sWirkleistung=".length(), temp.indexOf("/n",temp.indexOf("Hka_Mw1.sWirkleistung=")));
			this.logInfo(this.log, wirkleistung);

		} else {
		    this.logInfo(this.log, "Error: Couldn't read data from GLT interface.");
		}
	}


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

	protected String activateDachs() {
		return setKeysDachs("Stromf_Ew.Anforderung_GLT.bAktiv=1&Stromf_Ew.Anforderung_GLT.bAnzahlModule=1");
	}

}

*
* */