package org.opencb.commons.bioformats.commons.exception;

public class exception extends Exception {

    private static final long serialVersionUID = 1L;

    public exception(String msg) {
        super(msg);
    }

    public exception(Exception e) {
        super(e.toString());
        this.setStackTrace(e.getStackTrace());
    }

    public exception(String msg, Exception e) {
        super(msg);
        this.setStackTrace(e.getStackTrace());
    }

}
