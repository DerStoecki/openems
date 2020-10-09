package io.openems.edge.bridge.mqtt.api;

/**
 * MqttEvents. Either published or received from broker.
 */
public enum MqttEventType {
    ERROR, MODBUS_CONNECTION_ERROR, I2C_ERROR, SPI_ERROR, M_BUS_CONNECTION_ERROR,
}
