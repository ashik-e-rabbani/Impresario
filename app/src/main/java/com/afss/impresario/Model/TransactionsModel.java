package com.afss.impresario.Model;

import androidx.annotation.Keep;

@Keep
public class TransactionsModel {

    private String txn_amount;
    private String txn_type;
    private String time_stamp;



    private String txn_description;

    public TransactionsModel() {
    }
    public String getTxn_description() {
        return txn_description;
    }

    public void setTxn_description(String txn_description) {
        this.txn_description = txn_description;
    }
    public String getTxn_amount() {
        return txn_amount;
    }

    public void setTxn_amount(String txn_amount) {
        this.txn_amount = txn_amount;
    }

    public String getTxn_type() {
        return txn_type;
    }

    public void setTxn_type(String txn_type) {
        this.txn_type = txn_type;
    }

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }
}
