package io.openems.edge.bridge.mqtt.api;

public class CommandWrapper {

    private String value;
    private String expiration;

    public CommandWrapper(String value, String expiration) {
        this.value = value;
        this.expiration = expiration;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }
}
