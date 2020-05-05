package io.openems.edge.dachs.gltinterface;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.dachs.gltinterface.api.DachsGltInterfaceChannel;
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
import java.util.Scanner;


@Designate(ocd = Config.class, factory = true)
@Component(name = "DachsGltInterfaceImpl",
		configurationPolicy = ConfigurationPolicy.REQUIRE,
		immediate = true)
public class DachsGltInterfaceImpl extends AbstractOpenemsComponent implements OpenemsComponent, DachsGltInterfaceChannel, Controller {

	private final Logger log = LoggerFactory.getLogger(DachsGltInterfaceImpl.class);
	private InputStream is = null;
	private String urlBuilderIP;
	private int testcounter = 0;

	public DachsGltInterfaceImpl() {

		super(OpenemsComponent.ChannelId.values(),
				DachsGltInterfaceChannel.ChannelId.values(),
				Controller.ChannelId.values());
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		this.noError().setNextValue(true);
		urlBuilderIP = config.address();
		Authenticator.setDefault(new Authenticator()
		{
			@Override protected PasswordAuthentication getPasswordAuthentication()
			{

				//System.out.printf( "url=%s, host=%s, ip=%s, port=%s%n", getRequestingURL(), getRequestingHost(),	getRequestingSite(), getRequestingPort() );

				return new PasswordAuthentication( config.username(), config.password().toCharArray() );
			}
		} );

	}

	@Deactivate
	public void deactivate() {super.deactivate();}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		if (testcounter == 2){

			this.logInfo(this.log, getKeyDachs("Hka_Bd.ulArbeitElektr"));

			try {
				URL url = new URL("http://"+urlBuilderIP+":8081/getKey?k=Hka_Bd.ulArbeitElektr");
				is = url.openStream();

				// read text returned by server
				BufferedReader in = new BufferedReader(new InputStreamReader(is));

				String line;
				while ((line = in.readLine()) != null) {
					this.logInfo(this.log, line);
				}
				in.close();

			} catch (MalformedURLException e) {
				this.logInfo(this.log, "Malformed URL: " + e.getMessage());
			} catch (IOException e) {
				this.logInfo(this.log, "I/O Error: " + e.getMessage());
			} finally {
				if ( is != null )
					try { is.close(); } catch ( IOException e ) { this.logInfo(this.log, "I/O Error: " + e.getMessage()); }
			}


		}


		testcounter++;

	}

	protected String getKeyDachs(String key) {
		String message = "";
		try {
			URL url = new URL("http://"+urlBuilderIP+":8081/getKey?k="+key);
			is = url.openStream();

			// read text returned by server
			BufferedReader in = new BufferedReader(new InputStreamReader(is));

			String line;
			while ((line = in.readLine()) != null) {
				this.logInfo(this.log, line);
				message = message + line + "/n";
			}
			in.close();

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

	protected String setKeysDachs(String key) {
		String message = "";
		try {
			String body = key;

			URL url = new URL( "http://"+urlBuilderIP+":8081/setKeys" );
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod( "POST" );
			connection.setDoInput( true );
			connection.setDoOutput( true );
			connection.setUseCaches( false );
			connection.setRequestProperty( "Content-Type",
					"application/x-www-form-urlencoded" );
			connection.setRequestProperty( "Content-Length", String.valueOf(body.length()) );

			OutputStreamWriter writer = new OutputStreamWriter( connection.getOutputStream() );
			writer.write( body );
			writer.flush();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()) );

			String line;
			while ((line = reader.readLine()) != null) {
				this.logInfo(this.log, line);
				message = message + line + "/n";
			}

			writer.close();
			reader.close();

		} catch (MalformedURLException e) {
			this.logInfo(this.log, "Malformed URL: " + e.getMessage());
		} catch (IOException e) {
			this.logInfo(this.log, "I/O Error: " + e.getMessage());
		}

		return message;
	}

	protected String activateDachs() {
		return setKeysDachs("Stromf_Ew.Anforderung_GLT.bAktiv=1&Stromf_Ew.Anforderung_GLT.bAnzahlModule=1");
	}

}
