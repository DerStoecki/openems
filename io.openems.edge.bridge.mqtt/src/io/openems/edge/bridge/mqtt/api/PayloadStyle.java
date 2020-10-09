package io.openems.edge.bridge.mqtt.api;

/**
 * Payloadstyles.
 * <p>
 * "STANDARD" Payloadstyle is:
 * {
 * TimeStamp : TIME ,
 * ID : Id -Of-The-Component,
 * "metrics" : {
 * NameForBroker: Value,
 * NAME : VALUE,
 * }
 * }
 * </p>
 * If you need different Payloadstyles add here an enum and add them to pub and sub task.
 */
public enum PayloadStyle {
    STANDARD
}
