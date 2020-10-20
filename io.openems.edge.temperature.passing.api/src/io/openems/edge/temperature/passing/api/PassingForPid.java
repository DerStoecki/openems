package io.openems.edge.temperature.passing.api;

import io.openems.common.exceptions.OpenemsError;

public interface PassingForPid extends PassingChannel {

    boolean readyToChange() throws OpenemsError.OpenemsNamedException;

    boolean changeByPercentage(double percentage);

}
