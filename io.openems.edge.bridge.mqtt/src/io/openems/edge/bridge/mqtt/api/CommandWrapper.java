package io.openems.edge.bridge.mqtt.api;

public class CommandWrapper {

    private String value;
    private String expiration;
    private boolean infinite;

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

    public boolean isInfinite() {
        return infinite;
    }

    public void setExpiration(String expiration) {
        if (expiration.toUpperCase().trim().equals("INFINITE")) {
            this.infinite = true;
        } else {
            this.infinite = false;
        }

        this.expiration = expiration;
    }
}
