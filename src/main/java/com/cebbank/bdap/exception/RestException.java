package com.cebbank.bdap.exception;

/**
 * Created by yilong on 2017/10/18.
 */
public class RestException extends Exception {
    public final static int  ERR_CODE_BAD_HEADER = 1;
    public final static int  ERR_CODE_INTERNAL_ERROR = 2;
    public final static int  ERR_CODE_AUTHENTICATION_EXCEPTION = 3;

    private int errcode;
    private String errmsg;
    public RestException(int code, String msg) {
        super("code:"+code+";reason:"+msg);
        errcode = code;
        errmsg = msg;
    }

    public int getErrCode() { return errcode; }
    public void setErrcode(int code) {
        this.errcode = code;
    }

    public String getErrMsg() { return errmsg; }
    public void setErrmsg(String msg) {
        this.errmsg = msg;
    }
}
