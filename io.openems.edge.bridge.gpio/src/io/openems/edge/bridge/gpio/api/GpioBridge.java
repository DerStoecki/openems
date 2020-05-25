package io.openems.edge.bridge.gpio.api;

import java.util.Map;

import io.openems.edge.bridge.gpio.task.GpioBridgeReadTask;
import io.openems.edge.bridge.gpio.task.GpioBridgeWriteTask;
import org.osgi.service.cm.ConfigurationException;


public interface GpioBridge {

    void addGpioReadTask(String id, GpioBridgeReadTask task) throws ConfigurationException;

    void removeGpioReadTask(String id);

    void addGpioWriteTask(String id, GpioBridgeWriteTask task) throws ConfigurationException;

    void removeGpioWriteTask(String id);

    Map<String, GpioBridgeReadTask> getReadTasks();

    Map<String, GpioBridgeWriteTask> getWriteTasks();
}
