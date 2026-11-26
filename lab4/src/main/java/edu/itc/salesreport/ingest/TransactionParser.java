package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;

@FunctionalInterface
public interface TransactionParser {
    String HEADER = "branch,date,receipt_no,sku,product_name,category,"
            + "quantity,unit_price,discount,payment_method";

    SaleTransaction parse(String line) throws InvalidRowException;
}
