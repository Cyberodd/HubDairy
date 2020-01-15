package com.hub.dairy.fragments;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hub.dairy.R;
import com.hub.dairy.adapters.TransactionAdapter;
import com.hub.dairy.models.Transaction;

import java.util.ArrayList;
import java.util.List;

import static com.hub.dairy.helpers.Constants.ANIMALS;
import static com.hub.dairy.helpers.Constants.TIME;
import static com.hub.dairy.helpers.Constants.TRANSACTIONS;
import static com.hub.dairy.helpers.Constants.USER_ID;

public class TransactionFragment extends Fragment {

    private static final String TAG = "TransactionFragment";
    private ProgressBar mProgress;
    private RecyclerView mTransRv;
    private CollectionReference transRef;
    private String userId;
    private List<Transaction> mTransactions = new ArrayList<>();
    private TransactionAdapter mTransactionAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        transRef = database.collection(TRANSACTIONS);
        if (user != null) {
            userId = user.getUid();
        } else {
            Log.d(TAG, "onActivityCreated: User not logged in");
        }

        mTransactionAdapter = new TransactionAdapter(mTransactions);

        loadTransactions();
    }

    private void loadTransactions() {
        mProgress.setVisibility(View.VISIBLE);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mTransRv.setHasFixedSize(true);
        mTransRv.setLayoutManager(manager);

        Query query = transRef.whereEqualTo(USER_ID, userId);
        query.orderBy(TIME, Query.Direction.DESCENDING)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                mTransactions.clear();
                mTransactions.addAll(queryDocumentSnapshots.toObjects(Transaction.class));
                mTransRv.setAdapter(mTransactionAdapter);
                mTransactionAdapter.notifyDataSetChanged();
                mProgress.setVisibility(View.GONE);
            } else {
                mProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "No transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews(View view) {
        mProgress = view.findViewById(R.id.transProgress);
        mTransRv = view.findViewById(R.id.transRecycler);
    }
}
