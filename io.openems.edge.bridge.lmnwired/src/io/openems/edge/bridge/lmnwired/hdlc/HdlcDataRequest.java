package io.openems.edge.bridge.lmnwired.hdlc;

import io.openems.edge.bridge.lmnwired.api.task.LMNWiredTask;

public class HdlcDataRequest {
    protected HdlcFrame hdlcFrame;
    protected long guardTime;
    public enum RequestSource {
        REGISTER,
        PRESENCE,
        DATA
    }
    protected RequestSource requestSource;
    protected LMNWiredTask lmnWiredTask;
    public HdlcDataRequest(HdlcFrame hdlcFrame, long guardTime, RequestSource requestSource) {
        this.hdlcFrame = hdlcFrame;
        this.guardTime = guardTime;
        this.requestSource = requestSource;
    }
    public HdlcDataRequest(HdlcFrame hdlcFrame, long guardTime, RequestSource requestSource, LMNWiredTask lmnWiredTask) {
        this.hdlcFrame = hdlcFrame;
        this.guardTime = guardTime;
        this.requestSource = requestSource;
        this.lmnWiredTask = lmnWiredTask;
    }
    public HdlcFrame getHdlcFrame() {
        return hdlcFrame;
    }
    public void setHdlcFrame(HdlcFrame hdlcFrame) {
        this.hdlcFrame = hdlcFrame;
    }
    public long getGuardTime() {
        return guardTime;
    }
    public void setGuardTime(long guardTime) {
        this.guardTime = guardTime;
    }
    public RequestSource getRequestSource() {
        return requestSource;
    }
    public void setRequestSource(RequestSource requestSource) {
        this.requestSource = requestSource;
    }
    public LMNWiredTask getLmnWiredTask() {
        return lmnWiredTask;
    }
    public void setLmnWiredTask(LMNWiredTask lmnWiredTask) {
        this.lmnWiredTask = lmnWiredTask;
    }

}
