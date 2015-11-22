package io.keiji.asupdatechecker;

public class EndpointResult {
    public final UpdateState updateState;
    public final Exception exception;

    public EndpointResult(UpdateState updateState) {
        this.updateState = updateState;
        this.exception = null;
    }

    public EndpointResult(Exception exception) {
        this.updateState = null;
        this.exception = exception;
    }
}
