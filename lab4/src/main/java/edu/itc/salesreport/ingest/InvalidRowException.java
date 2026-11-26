package edu.itc.salesreport.ingest;

/** A row that fails validation; reported as an anomaly, never fatal. */
public class InvalidRowException extends Exception {
    public InvalidRowException(String message) { super(message); }
}
