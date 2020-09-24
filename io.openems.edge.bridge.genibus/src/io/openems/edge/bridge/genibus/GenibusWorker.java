package io.openems.edge.bridge.genibus;

import com.google.common.base.Stopwatch;
import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.genibus.api.PumpDevice;
import io.openems.edge.bridge.genibus.api.task.GenibusTask;
import io.openems.edge.bridge.genibus.api.task.GenibusWriteTask;
import io.openems.edge.bridge.genibus.protocol.ApplicationProgramDataUnit;
import io.openems.edge.bridge.genibus.protocol.Telegram;
import io.openems.edge.common.taskmanager.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

class GenibusWorker extends AbstractCycleWorker {

    private final Logger log = LoggerFactory.getLogger(GenibusWorker.class);
    // Measures the Cycle-Length between two consecutive BeforeProcessImage events
    private final Stopwatch cycleStopwatch = Stopwatch.createUnstarted();

    private final LinkedBlockingDeque<Telegram> telegramQueue = new LinkedBlockingDeque<>();

    private final ArrayList<PumpDevice> deviceList = new ArrayList<>();

    private int currentDeviceCounter = 0;

    private final GenibusImpl parent;

    // The measured duration between BeforeProcessImage event and ExecuteWrite event
    private long durationBetweenBeforeProcessImageTillExecuteWrite = 0;

    protected GenibusWorker(GenibusImpl parent) {
        this.parent = parent;
    }

    @Override
    public void activate(String name) {
        super.activate(name);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }


    // Creates a telegram for the current device (if possible) and increments the currentDeviceCounter (with a few exceptions).
    // The telegram is built from a task list of priority high, low and once tasks. This list is created by the module
    // that implements the pump device.
    // Picking tasks for the telegram works as follows: from the list of tasks, all high tasks are added to taskQueue.
    // Then a number of low tasks is added to tasksQueue. How many low tasks are added is defined by the pump device
    // module. Then as many tasks as possible are taken from tasksQueue and added to the telegram. Tasks added to the
    // telegram are removed from tasksQueue. If there are still tasks in taskQueue once the telegram is full, they
    // remain there and will be processed next time a telegram is created for this device. The queue is refilled as
    // needed and checks are in place to prevent the queue from getting too big.
    // All priority once tasks are added to the queue on the first run after the high tasks.
    //
    // If this method is called multiple times in a cycle for the same device, all possible tasks are only executed once.
    // Once the regularly scheduled tasks have been executed (high tasks + defined number of low tasks), a telegram with
    // the remaining (unscheduled) low tasks is created. Further calls of this method in the same cycle for the same
    // device will then not create a telegram but only switch to the next device (increase currentDeviceCounter).
    protected void createTelegram() {
        if (deviceList.isEmpty()) {
            return;
        }

        // This handles the different pumps
        if (deviceList.size() <= currentDeviceCounter) {
            currentDeviceCounter = 0;
        }
        PumpDevice currentDevice = deviceList.get(currentDeviceCounter);

        // Remaining bytes that can be put in this telegram. Calculated by getting the buffer length of the device
        // and subtracting the telegram header (4 bytes) and the crc (2 bytes).
        int telegramRemainingBytes = currentDevice.getDeviceByteBufferLength() - 6;

        // Estimate how big the telegram can be to still fit in this cycle.
        long remainingCycleTime = 940 - cycleStopwatch.elapsed(TimeUnit.MILLISECONDS);  // Include 60 ms buffer.
        if (remainingCycleTime < 0) {
            remainingCycleTime = 0;
        }
        // An telegram with no tasks takes ~33 ms to send and receive. Each additional byte in the send telegram adds ~3 ms to that.
        // A buffer of 60 ms was used in the time calculation. The 33 ms base time for a telegram is included in that buffer.
        int cycleRemainingBytes = (int) (remainingCycleTime / currentDevice.getMillisecondsPerByte());
        if (cycleRemainingBytes < 5) {
            // Not enough time left. Exit the method without changing the device, so that this device is not skipped.
            return;
        }
        // Reduce telegram byte count if time is too short
        if (cycleRemainingBytes < telegramRemainingBytes) {
            // If this telegram has a greatly reduced bytecount because of time constraints, don't switch to the next
            // device. So the device is guaranteed to have a full size telegram in the next cycle.
            if (cycleRemainingBytes / (telegramRemainingBytes * 1.0) < 0.3) {
                currentDeviceCounter--;
            }
            telegramRemainingBytes = cycleRemainingBytes;   // This assumes header and crc are not time consuming.
        }
        currentDeviceCounter++;

        long lastExecutionMillis = System.currentTimeMillis() - currentDevice.getTimestamp();
        // Rollover backup. Just in case this software actually runs that long...
        if (lastExecutionMillis < 0) {
            lastExecutionMillis = 1000;
        }

        // Get taskQueue from the device. Contains all tasks that couldn't fit in the last telegram.
        List<GenibusTask> tasksQueue = currentDevice.getTaskQueue();

        // Priority low tasks are added with .getOneTask(), which starts from 0 when the end of the list is reached.
        // Make sure the number of low tasks added per telegram is not longer than the list to prevent adding a task twice.
        int lowTasksToAdd = currentDevice.getLowPrioTasksPerCycle();
        int numberOfLowTasks = currentDevice.getTaskManager().getAllTasks(Priority.LOW).size();
        if (lowTasksToAdd <= 0) {
            lowTasksToAdd = 1;
        }
        if (lowTasksToAdd > numberOfLowTasks) {
            lowTasksToAdd = numberOfLowTasks;
        }

        if (lastExecutionMillis > 950) {
            // This checks if this was already executed this cycle. This part should execute once per cycle only.

            currentDevice.setTimestamp();
            currentDevice.setAllLowPrioTasksAdded(false);

            // Check content of taskQueue. If length is longer than numberOfHighTasks + lowTasksToAdd (=number of tasks
            // this method would add), all high tasks are already in the queue and don't need to be added again.
            int numberOfHighTasks = currentDevice.getTaskManager().getAllTasks(Priority.HIGH).size();
            if (tasksQueue.size() <= numberOfHighTasks + lowTasksToAdd) {
                // Add all high tasks
                tasksQueue.addAll(currentDevice.getTaskManager().getAllTasks(Priority.HIGH));

                // Add all once tasks that need a second execution because the first execution was only INFO. This list
                // should be empty after the second or third cycle and won't be refilled.
                List<GenibusTask> onceTasksWithInfo = currentDevice.getOnceTasksWithInfo();
                for (int i = 0; i < onceTasksWithInfo.size(); i++) {
                    GenibusTask currentTask = onceTasksWithInfo.get(i);
                    if (currentTask.InformationDataAvailable()) {
                        tasksQueue.add(currentTask);
                        onceTasksWithInfo.remove(i);
                        i--;
                    }
                }

                // Add all once Tasks. Only done on the first run, since .getOneTask(Priority.ONCE) will return null after
                // it has traversed the list once.
                boolean onceTasksAvailable = true;
                while (onceTasksAvailable) {
                    GenibusTask currentTask = currentDevice.getTaskManager().getOneTask(Priority.ONCE);
                    if (currentTask == null) {
                        onceTasksAvailable = false;
                    } else {
                        tasksQueue.add(currentTask);
                        switch (currentTask.getHeader()) {
                            case 2:
                            case 4:
                            case 5:
                                currentDevice.getOnceTasksWithInfo().add(currentTask);
                        }
                    }
                }
                // ToDo: Add a check to see if the once tasks were executed. Right now a once task can fail if there is
                //  an error while processing the telegram. Best way to do this is to check if the telegram was
                //  processed or not. Can't check the individual tasks since a task could have a wrong address etc.
                //  Could still do this by adding an execution counter. If a task was sent three times without success
                //  it is considered faulty.


                // Add a number of low tasks.
                for (int i = 0; i < lowTasksToAdd; i++) {
                    GenibusTask currentTask = currentDevice.getTaskManager().getOneTask(Priority.LOW);
                    if (currentTask == null) {
                        break;
                    } else {
                        tasksQueue.add(currentTask);
                    }
                }
            }

        } else {
            // This executes if a telegram was already sent to this pumpDevice in this cycle. If the taskQueue is empty,
            // fill the telegram with any remaining low priority tasks. If that was already done this cycle, exit the method.
            if (tasksQueue.isEmpty()) {
                if (currentDevice.isAllLowPrioTasksAdded() == false) {
                    currentDevice.setAllLowPrioTasksAdded(true);
                    // The amount lowTasksToAdd was already added in the normal cycle. The number of tasks before they
                    // repeat is then numberOfLowTasks - lowTasksToAdd.
                    int lowTaskFill = numberOfLowTasks - lowTasksToAdd;
                    // Compare that number with the bytes allowed in this telegram. We want to fill this telegram up
                    // with low tasks, but we don't want to add more tasks than can be sent with this telegram. Anything
                    // left in taskQueue is executed in the next telegram and reduces the space there for high tasks.
                    // One task = one byte
                    // The math is not exact but a good enough estimate. 1-2 low tasks remaining in taskQueue is not a big deal.
                    if ((telegramRemainingBytes - 2) < lowTaskFill) {    // -2 to account for at least one apdu header.
                        lowTaskFill = (telegramRemainingBytes - 2);
                    }

                    for (int i = 0; i < lowTaskFill; i++) {
                        GenibusTask currentTask = currentDevice.getTaskManager().getOneTask(Priority.LOW);
                        if (currentTask == null) {
                            // This should not happen, but checked just in case to prevent null pointer exception.
                            break;
                        } else {
                            tasksQueue.add(currentTask);
                        }
                    }
                }
            }
        }

        if (tasksQueue.isEmpty()) {
            // Nothing useful left to do. No tasks could be found that were not already executed this cycle.
            if (parent.getDebug()) {
                this.parent.logInfo(this.log, "No tasks left this cycle for pump number "
                        + currentDeviceCounter + ". Time since last timestamp: " + lastExecutionMillis + " ms.");
                // currentDeviceCounter has already been incremented at this point, but that is fine. First pump in the
                // list is now displayed as "pump number 1", while the deviceCounter number would be 0.
            }
            return;
        }

        if (parent.getDebug()) {
            this.parent.logInfo(this.log, "--Telegram Builder--");
            this.parent.logInfo(this.log, "Number of pumps in list: " + deviceList.size()
                    + ", current pump number: " + (currentDeviceCounter) + ", current pump id: " + currentDevice.getPumpDeviceId()
                    + ", GENIbus address: " + currentDevice.getGenibusAddress()
                    + ", Time since last timestamp: " + lastExecutionMillis + " ms.");
        }

        // This list contains all tasks for the current telegram. The key is a 3 digit decimal number called the
        // apduIdentifier. The 100 digit is the HeadClass of the apdu, the 10 digit is the operation
        // (0=get, 2=set, 3=info), the 1 digit is a counter starting at 0. The counter allows to have more than one apdu
        // of a given type. Since an apu (request and answer) is limited to 63 bytes, several apdu of the same type
        // might be needed to fit all tasks.
        Map<Integer, ArrayList<GenibusTask>> telegramTaskList = new HashMap<>();

        // Same as telegramTaskList, except this list contains the apdus with the tasks of telegramTaskList.
        Map<Integer, ApplicationProgramDataUnit> telegramApduList = new HashMap<>();

        if (parent.getDebug()) {
            this.parent.logInfo(this.log, "Bytes allowed: " + telegramRemainingBytes + ", task queue size: "
                    + tasksQueue.size() + ".");
            this.parent.logInfo(this.log, "Tasks are listed with \"apduIdentifier, address\". The apduIdentifier "
                    + "is a three digit number where the 100 digit is the HeadClass of the apdu, the 10 digit is the "
                    + "operation (0=get, 2=set, 3=info) and the 1 digit is a counter starting at 0 to track if more than "
                    + "one apdu of this type exists.");
        }

        while (tasksQueue.isEmpty() == false) {
            GenibusTask currentTask = tasksQueue.get(0);
            int byteSize = checkTaskByteSize(currentTask, telegramApduList);
            if (byteSize == 0) {
                // When byteSize is 0, this task does nothing (for example a command task with no command to send). Skip this task.
                checkGetOrRemove(tasksQueue, 0);
                if (parent.getDebug()) {
                    this.parent.logInfo(this.log, "Skipping task: " + currentTask.getApduIdentifier() + ", "
                            + Byte.toUnsignedInt(currentTask.getAddress()) + ". Task queue size: " + tasksQueue.size());
                }
                continue;
            }

            if (telegramRemainingBytes - byteSize >= 0) {
                telegramRemainingBytes = telegramRemainingBytes - byteSize;
                addTaskToApdu(currentTask, telegramTaskList, telegramApduList);
                checkGetOrRemove(tasksQueue, 0);
                if (parent.getDebug()) {
                    this.parent.logInfo(this.log, "Adding task: " + currentTask.getApduIdentifier() + ", "
                            + Byte.toUnsignedInt(currentTask.getAddress()) + " - bytes added: " + byteSize + " - bytes remaining: "
                            + telegramRemainingBytes + " - Task queue size: " + tasksQueue.size());
                }
                if (telegramRemainingBytes == 0) {
                    // Telegramm is full, but tasksQueue is probably not yet empty. Need break to escape the loop.
                    break;
                }
                continue;
            }

            if (telegramRemainingBytes >= 1) {
                // byteSize of currentTask is too big to fit. Try to find one more task with small byte count that might fit.
                for (int i = 1; i < tasksQueue.size(); i++) {
                    currentTask = tasksQueue.get(i);
                    int byteSizeSmall = checkTaskByteSize(currentTask, telegramApduList);
                    if (byteSizeSmall != 0 && telegramRemainingBytes - byteSizeSmall >= 0) {
                        telegramRemainingBytes = telegramRemainingBytes - byteSizeSmall;
                        addTaskToApdu(currentTask, telegramTaskList, telegramApduList);
                        checkGetOrRemove(tasksQueue, i);
                        if (parent.getDebug()) {
                            this.parent.logInfo(this.log, "Adding last small task: " + currentTask.getApduIdentifier() + ", "
                                    + Byte.toUnsignedInt(currentTask.getAddress()) + " - bytes added: " + byteSizeSmall + " - bytes remaining: "
                                    + telegramRemainingBytes + " - Task queue size: " + tasksQueue.size());
                        }
                        // Telegramm is full, but tasksQueue is probably not yet empty. Need break to escape the loop.
                        break;
                    }
                }
            }
            // Telegramm is full, but tasksQueue is probably not yet empty. Need break to escape the loop.
            break;
        }

        // Create the telegram and add it to the queue.
        Telegram telegram = new Telegram();
        telegram.setStartDelimiterDataRequest();
        telegram.setDestinationAddress(currentDevice.getGenibusAddress());
        telegram.setSourceAddress(0x01);
        telegram.setPumpDevice(currentDevice);

        telegramApduList.forEach((key, apdu) -> {
            telegram.getProtocolDataUnit().putAPDU(apdu);
        });
        telegram.setTelegramTaskList(telegramTaskList);

        telegramQueue.add(telegram);
    }

    // This is how GET for a writeTask (HeadClass 4 or 5) is handled. Checks if the task is a writeTask and if not the
    // task is removed. If it is a writeTask, it will set the sendGet boolean to true and leaves the task in the queue
    // so that it is added to a GET apdu. If sendGet is true that is reset to false and the task is removed from the
    // queue. That means every writeTask will perform a GET.
    private void checkGetOrRemove(List<GenibusTask> tasksQueue, int position) {
        GenibusTask currentTask = tasksQueue.get(position);
        if (currentTask instanceof GenibusWriteTask && currentTask.InformationDataAvailable()) {
            if (((GenibusWriteTask) currentTask).getSendGet() == false) {

                // ToDo: Add code here to check if a GET is needed. Right now GET is always done.

                ((GenibusWriteTask) currentTask).setSendGet(true);
                return;
            } else {
                ((GenibusWriteTask) currentTask).setSendGet(false);
            }
        }
        tasksQueue.remove(position);
    }

    // Returns how many bytes this task would need if it were added to the telegram. This means checking if a new apdu
    // is required or not, since a new apdu adds it's header of two bytes to the byte count. Since this method checks
    // in which apdu a task can be placed, the resulting key/identifier is saved to the task object so it can be used later
    // when the task is actually placed in an apdu.
    private int checkTaskByteSize(GenibusTask task, Map<Integer, ApplicationProgramDataUnit> telegramApduList) {
        int headClass = task.getHeader();
        int dataByteSize = task.getDataByteSize();
        switch (headClass) {
            case 0:
                task.setApduIdentifier(0);
                // HeadClass 0 only has three commands. No more than one HeadClass 0 apdu will exist, so don't need to
                // check if there is more than one or if apdu is full. Key is 0 since GET is 0.
                if (telegramApduList.containsKey(0)) {
                    return dataByteSize;
                }
                return 2 + dataByteSize;
            case 2:
                // Check if INFO has already been received. If yes this task is GET, if not this task is INFO.
                if (task.InformationDataAvailable()) {
                    // See if an apdu exists and if yes check remaining space.
                    // Key is 2*100 for HeadClass 2, 0*10 for GET and 0 for first apdu.
                    return checkAvailableApdu(task, 200, 63, telegramApduList);
                } else {

                    // Task is INFO. Search for apdu with key 2*100 for HeadClass 2, 3*10 for INFO and 0 for first apdu.
                    // For INFO, return message can be 4 byte per task and max is 63. So 15 is the maximum number of
                    // tasks where overflow is guaranteed to be avoided. Assume one byte per task, so 15 bytes maximum
                    // in a request INFO apdu.
                    return checkAvailableApdu(task, 230, 15, telegramApduList);
                }
            case 3:
                // HeadClass 3 is commands. Commands are boolean where true = send and false = no send. For true,
                // getRequest() returns 1. HeadClass 3 does allow INFO, but it is not needed.
                if (task.getRequest(0) != 0) {
                    // Task is SET. Search for apdu with key 3*100 for HeadClass 3, 2*10 for SET and 0 for first apdu.
                    return checkAvailableApdu(task, 320, 63, telegramApduList);
                } else {
                    // Set apduIdentifier for debug info.
                    task.setApduIdentifier(320);
                }
                // False in channel, so no send = no bytes.
                return 0;
            case 4:
            case 5:
                // Check if INFO has already been received. INFO is needed for both GET and SET.
                if (task.InformationDataAvailable()) {
                    if (task instanceof GenibusWriteTask) {
                        // Check boolean if this is GET or SET.
                        if (((GenibusWriteTask) task).getSendGet()) {
                            // Check apdu for GET. Key is HeadClass*100, 0*10 for GET and 0 for first apdu.
                            return checkAvailableApdu(task, headClass * 100, 63, telegramApduList);
                        } else {
                            // Task is SET. Check if there is a value to set. If not don't send anything.
                            int valueRequest = task.getRequest(0);  // This also works for 16bit. Will return something else than -256 in byte[0] if there is a value to set.
                            int setBytes = 0;
                            if (valueRequest > -256) {
                                // Check apdu for SET. Key is HeadClass*100, 2*10 for SET and 0 for first apdu.
                                setBytes = checkAvailableApdu(task, (headClass * 100) + 20, 63, telegramApduList);
                            } else {
                                // Set apduIdentifier for debug readout.
                                task.setApduIdentifier((headClass * 100) + 20);
                            }
                            return  setBytes;
                        }
                    } else {
                        this.parent.logError(this.log, "GENIbus error. Wrong headclass for task "
                                + task.getHeader() + ", " + task.getAddress() + ". Can't execute.");
                        return 0;
                    }
                } else {
                    // Task is INFO. Search for apdu with key 4*100 for HeadClass 4, 3*10 for INFO and 0 for first apdu.
                    // For INFO, return message can be 4 byte per task and max is 63. So 15 is the maximum number of
                    // tasks where overflow is guaranteed to be avoided. Assume one byte per task, so 15 bytes maximum
                    // in a request INFO apdu.
                    return checkAvailableApdu(task, (headClass * 100) + 30, 15, telegramApduList);
                }
            case 7:
                // HeadClass 7 is ASCII, which has only GET. Each ASCII task needs it's own apdu.
                // Check apdu for GET. Key is 700, 0*10 for GET and 0 for first apdu.
                // Further, the answer to an ASCII task can be so long that the send buffer of the device overflows.
                // So an ASCII should be sent in a telegram with nothing else in it. Add 61 to return byte count to make
                // enough space
                return checkAvailableApdu(task, 700, 1, telegramApduList) + 61;
        }

        return 0;
    }

    // Helper method for checkTaskByteSize()
    private int checkAvailableApdu(GenibusTask task, int key, int apduMaxBytes, Map<Integer, ApplicationProgramDataUnit> telegramApduList) {
        int dataByteSize = task.getDataByteSize();
        int nextFreeApdu = key;
        // nextFreeApdu is the key/identifier of the last existing apdu +1.
        while (telegramApduList.containsKey(nextFreeApdu)) {
            nextFreeApdu++;
        }
        // No apdu yet.
        if (nextFreeApdu == key) {
            task.setApduIdentifier(nextFreeApdu);
            return 2 + dataByteSize;
        }
        // Check remaining space in last apdu.
        int remainingBytes = apduMaxBytes - telegramApduList.get(nextFreeApdu - 1).getLength();
        if (remainingBytes >= dataByteSize) {
            task.setApduIdentifier(nextFreeApdu - 1);
            return dataByteSize;
        }
        task.setApduIdentifier(nextFreeApdu);
        return 2 + dataByteSize;
    }


    // Adds a task to the telegramTaskList and the telegramApduList.
    private void addTaskToApdu(GenibusTask currentTask, Map<Integer, ArrayList<GenibusTask>> telegramTaskList, Map<Integer, ApplicationProgramDataUnit> telegramApduList) {
        int apduIdentifier = currentTask.getApduIdentifier();

        // telegramApduList should have the same keys as telegramTaskList, so only need to check one.
        if (telegramTaskList.containsKey(apduIdentifier)) {
            telegramTaskList.get(apduIdentifier).add(currentTask);

            // For tasks with more than 8 bit, put more than one byte in the apdu.
            for (int i = 0; i < currentTask.getDataByteSize(); i++) {
                telegramApduList.get(apduIdentifier).putDataField(currentTask.getAddress() + i);
                // Add write value for write task.
                switch (currentTask.getHeader()) {
                    case 4:
                    case 5:
                        if (((apduIdentifier % 100) / 10) == 2) {
                            int valueRequest = currentTask.getRequest(i);
                            telegramApduList.get(apduIdentifier).putDataField((byte) valueRequest);
                        }
                }
            }
        } else {
            ArrayList<GenibusTask> apduTaskList = new ArrayList<GenibusTask>();
            apduTaskList.add(currentTask);
            telegramTaskList.put(apduIdentifier, apduTaskList);
            ApplicationProgramDataUnit newApdu = new ApplicationProgramDataUnit();
            newApdu.setHeadClass(apduIdentifier / 100);
            newApdu.setHeadOSACK((apduIdentifier % 100) / 10);

            // For tasks with more than 8 bit, put more than one byte in the apdu.
            for (int i = 0; i < currentTask.getDataByteSize(); i++) {
                newApdu.putDataField(currentTask.getAddress() + i);
                // Add write value for write task.
                switch (currentTask.getHeader()) {
                    case 4:
                    case 5:
                        if (((apduIdentifier % 100) / 10) == 2) {
                            int valueRequest = currentTask.getRequest(i);
                            newApdu.putDataField((byte) valueRequest);
                        }
                }
            }

            telegramApduList.put(apduIdentifier, newApdu);
        }
    }


    /**
     * Checks if the telegram queue is empty and if yes, creates a telegram (if possible) and puts it in the queue.
     * If a telegram is in the queue, the connection is checked and the telegram is sent. If the connection is not ok,
     * the telegram will stay in the waiting queue until it can be sent. This is important to ensure priority once tasks
     * are actually executed, since they are only added to one telegram. So far there is no check to see if a priority
     * once task has actually been executed.
     *
     */
    @Override
    protected void forever() {
        if (this.cycleStopwatch.isRunning()) {
            if (parent.getDebug()) {
                this.parent.logInfo(this.log, "Stopwatch 3: " + cycleStopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }
        cycleStopwatch.reset();
        cycleStopwatch.start();

        if (this.telegramQueue.isEmpty()) {
            createTelegram();
        }

        while (this.telegramQueue.isEmpty() == false) {
            // Check connection.
            if (!parent.handler.checkStatus()) {
                // If checkStatus() returns false, the connection is lost. Try to reconnect
                parent.connectionOk = parent.handler.start(parent.portName);
            }
            if (parent.connectionOk) {
                try {
                    Telegram telegram = telegramQueue.takeLast();
                    long timeCounterTimestamp = cycleStopwatch.elapsed(TimeUnit.MILLISECONDS);
                    if (parent.getDebug()) {
                        this.parent.logInfo(this.log, "Stopwatch 1: " + cycleStopwatch.elapsed(TimeUnit.MILLISECONDS));
                    }
                    parent.handleTelegram(telegram);
                    // Measure how long it took to execute that telegram and store it in the pump device. This value is
                    // later retrieved to check if a telegram for this pump could still fit into the remaining time of
                    // the cycle.
                    long executionDuration = cycleStopwatch.elapsed(TimeUnit.MILLISECONDS) - timeCounterTimestamp;
                    int telegramByteLength = Byte.toUnsignedInt(telegram.getLength()) - 2;  // Subtract crc
                    if (parent.getDebug()) {
                        this.parent.logInfo(this.log, "Stopwatch 2: " + cycleStopwatch.elapsed(TimeUnit.MILLISECONDS));
                        this.parent.logInfo(this.log, "Estimated telegram execution time was: "
                                + (33 + telegramByteLength * telegram.getPumpDevice().getMillisecondsPerByte())
                                + " ms. Actual time: " + executionDuration + " ms.");
                    }
                    telegram.getPumpDevice().setExecutionDuration(executionDuration);
                    // A telegram with no tasks takes ~33 ms to send and receive. Each additional byte in the send telegram adds ~3 ms.
                    telegram.getPumpDevice().setMillisecondsPerByte((executionDuration - 33) / (telegramByteLength * 1.0));
                } catch (InterruptedException e) {
                    this.parent.logWarn(this.log, "Couldn't get telegram. " + e);
                }
            }

            // Check if there is enough time for another telegram. The telegram length is dynamic and adjusts depending
            // on time left in the cycle. A short telegram can be sent and received in ~50 ms.
            if (cycleStopwatch.elapsed(TimeUnit.MILLISECONDS) < 900) {
                // There should be enough time. Create the telegram. The createTelegram() method checks if a telegram
                // has already been sent this cycle and will then only fill it with tasks that have not been executed
                // this cycle. If all tasks were already executed, no telegram is created.
                createTelegram();

                // If no telegram was created and put in the queue (the device had nothing to send), check if the other
                // devices still have tasks.
                if (this.telegramQueue.isEmpty()) {
                    for (int i = 0; i < deviceList.size() - 1; i++) {   // "deviceList.size() - 1" because we already checked one device.
                        createTelegram();
                        if (this.telegramQueue.isEmpty() == false) {
                            // Exit for-loop if a telegram was created.
                            break;
                        }
                    }
                }
            }
        }
    }

    public void addDevice(PumpDevice pumpDevice) {
        deviceList.add(pumpDevice);
    }

    public void removeDevice(String deviceId) {
        for (int counter = 0; counter < deviceList.size(); counter++) {
            if (deviceList.get(counter).getPumpDeviceId().equals(deviceId)) {
                deviceList.remove(counter);
                // decrease counter to not skip an entry.
                counter--;
            }
        }
    }
}
