package edu.itc.salesreport.ingest;

public class InvalidRowException extends RuntimeException {
    public InvalidRowException(String message) {
        super(message);
    }

    public InvalidRowException(String message, Throwable cause) {
        super(message, cause);
    }
}

