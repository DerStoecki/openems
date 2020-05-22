package io.openems.edge.bridge.rest.communcation.task;

public interface RestRequest {
    //Return DeviceId + Channel
    String getRequest();

    /**
     * Returns String for AutoAdaptRequest, if the Device is "Relays" Type.
     *
     * @return String IsCloser yes or no If Yes --> no Inverse Logic
     */
    String getAutoAdaptRequest();

    String getDeviceId();

    String getRealDeviceId();

    String getDeviceType();

    boolean isAutoAdapt();

    /**
     * sets IsInverse depending if the relays is a closer or not.
     *
     * @param succ   success of the REST GET Request.
     * @param answer 1 or 0 for Relays --> IsCloser.
     *               <p> If Relays is not a Closer ---> answer == 0; Inverse logic is true.</p>
     * @return boolean true if either AutoAdapt was already set or successful setting was done.
     */
    boolean setAutoAdaptResponse(boolean succ, String answer);

    boolean isInverseSet();

    boolean unitWasSet();

    /**
     * Sets the Unit for a Read or Write Task.
     *
     * @param succ   Success of the REST GET Request for Unit.
     * @param answer complete GET String. Will be Split at "Unit".
     */

    void setUnit(boolean succ, String answer);


}
