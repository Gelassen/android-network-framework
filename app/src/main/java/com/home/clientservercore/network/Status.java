package com.home.clientservercore.network;


import android.os.Bundle;

import org.apache.http.HttpStatus;

public class Status {

    public static final int FAILED_NETWORK = 0;
    public static final int FAILED_TO_EXECUTE_REQUEST = 1;

    private static final String NAMESPACE = Status.class.getName();

    private static final String CMD_STATUS_MSG = NAMESPACE.concat(".CMD_STATUS_MSG");
    private static final String CMD_STATUS_CODE = NAMESPACE.concat(".CMD_STATUS_CODE");

    private Bundle status;

    public Status() {
        this.status = new Bundle();
    }

    private Status(Bundle extras) {
        this.status = extras;
    }

    public static Status from(Bundle bundle) {
        return new Status(bundle);
    }

    public Status add(int statusCode) {
        status.putInt(CMD_STATUS_CODE, statusCode);
        return this;
    }

    public Status add(String statusMessage) {
        status.putString(CMD_STATUS_MSG, statusMessage);
        return this;
    }

    public Status add(Bundle extras) {
        status.putAll(extras);
        return this;
    }

    public int getStatusCode() {
        return status.getInt(CMD_STATUS_CODE);
    }

    public boolean isSuccess() {
        // extend this if you expect others status code as valid
        return HttpStatus.SC_OK == getStatusCode();
    }

    public Object getExtra(final String extraField) {
        return status.get(extraField);
    }

    /*package*/ Bundle getBundle() {
        return status;
    }
}
