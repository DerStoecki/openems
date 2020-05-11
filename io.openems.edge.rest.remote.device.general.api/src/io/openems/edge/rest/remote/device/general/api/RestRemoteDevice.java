package io.openems.edge.rest.remote.device.general.api;

public interface RestRemoteDevice extends RestRemoteChannel {

    boolean setValue(float value);

    boolean setValue(int value);

    boolean setValue(boolean value);

    boolean setValue(String value);

    //returns the Correct value as a string
    String getValue();

    String getType();

}
