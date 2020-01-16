package com.hub.dairy.helpers;

import com.hub.dairy.models.Transaction;

import java.util.List;

public class TransactionEvent {

    private List<Transaction> mTransactions;

    public TransactionEvent() {
    }

    public List<Transaction> getTransactions() {
        return mTransactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        mTransactions = transactions;
    }
}
