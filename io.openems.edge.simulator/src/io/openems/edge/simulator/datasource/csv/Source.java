package io.openems.edge.simulator.datasource.csv;

public enum Source {
	ZERO("zero.csv"), //
	H0_HOUSEHOLD_SUMMER_WEEKDAY_STANDARD_LOAD_PROFILE("h0-summer-weekday-standard-load-profile.csv"), //
	H0_HOUSEHOLD_SUMMER_WEEKDAY_PV_PRODUCTION("h0-summer-weekday-pv-production.csv"), //
	H0_HOUSEHOLD_SUMMER_WEEKDAY_NON_REGULATED_CONSUMPTION("h0-summer-weekday-non-regulated-consumption.csv"), //
	H0_HOUSEHOLD_SUMMER_WEEKDAY_PV_PRODUCTION2("h0-summer-weekday-pv-production2.csv"),
	TEMPERATURE_STANDARD_LOAD_PROFILE_PRIMARY_FORWARD("temperature-standard-load-profile-primary-Forward.csv"),
    TEMPERATURE_STANDARD_LOAD_PROFILE_PRIMARY_REWIND("temperature-standard-load-profile-primary-Rewind.csv"),
    TEMPERATURE_STANDARD_LOAD_PROFILE_SECUNDARY_FORWARD("temperature-standard-load-profile-secundary-Forward.csv"),
    TEMPERATURE_STANDARD_LOAD_PROFILE_SECUNDARY_REWIND("temperature-standard-load-profile-secundary-Rewind.csv"),
    TEMPERATURE_STANDARD_LOAD_PROFILE_OVERSEER_TEMPERATURE("temperature-standard-load-profile-overseer-temperature.csv");
	public final String filename;

	private Source(String filename) {
		this.filename = filename;
	}
}
