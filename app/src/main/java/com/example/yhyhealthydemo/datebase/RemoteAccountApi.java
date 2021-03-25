package com.example.yhyhealthydemo.datebase;

import java.util.List;

public class RemoteAccountApi {

    /**
     * success : ["demo20","demo21","iostest3"]
     * errorCode : 0
     */

    private int errorCode;
    private List<String> success;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public List<String> getSuccess() {
        return success;
    }

    public void setSuccess(List<String> success) {
        this.success = success;
    }
}
