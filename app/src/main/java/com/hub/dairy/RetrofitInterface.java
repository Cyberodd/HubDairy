package com.hub.dairy;

import com.google.firebase.firestore.Transaction;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitInterface {
    @POST("/transactions")
    Call<Transaction> executeTransaction (@Body HashMap<String, String> map);

}
