package io.openems.edge.bridge.gpio.api;

import java.util.Map;

import io.openems.edge.bridge.gpio.task.GpioBridgeTask;


public interface GpioBridge {

    void addGpioTask(String id, GpioBridgeTask task);

    void removeGpioTask(String id);

    Map<String, GpioBridgeTask> getTasks();
}
