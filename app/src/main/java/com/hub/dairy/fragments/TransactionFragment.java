package com.hub.dairy.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hub.dairy.R;
import com.hub.dairy.adapters.TransactionAdapter;
import com.hub.dairy.helpers.TransactionEvent;
import com.hub.dairy.models.Transaction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static com.hub.dairy.helpers.Constants.TIME;
import static com.hub.dairy.helpers.Constants.TRANSACTIONS;
import static com.hub.dairy.helpers.Constants.USER_ID;

public class TransactionFragment extends Fragment {

    public TransactionFragment() {
    }

    private static final String TAG = "TransactionFragment";
    private ProgressBar mProgress;
    private RecyclerView mTransRv;
    private CollectionReference transRef;
    private String userId;
    private List<Transaction> mTransactions = new ArrayList<>();
    private TransactionAdapter mTransactionAdapter;
    private DocumentSnapshot mLastQueriedDocument;

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

        getTransactions();
    }

    private void getTransactions() {
        Query query;
        if (mLastQueriedDocument != null) {
            query = transRef.whereEqualTo(USER_ID, userId)
                    .orderBy(TIME, Query.Direction.DESCENDING)
                    .endBefore(mLastQueriedDocument);
        } else {
            query = transRef.whereEqualTo(USER_ID, userId).orderBy(TIME, Query.Direction.DESCENDING);
        }
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                Transaction transaction = snapshot.toObject(Transaction.class);
                mTransactions.add(transaction);
            }
            if (queryDocumentSnapshots.size() != 0) {
                mLastQueriedDocument = queryDocumentSnapshots.getDocuments()
                        .get(queryDocumentSnapshots.size() - 1);
            }
            populateRecycler(mTransactions);
        });
    }

    private void populateRecycler(List<Transaction> transactions) {
        mTransactionAdapter = new TransactionAdapter(transactions);
        mProgress.setVisibility(View.VISIBLE);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mTransRv.setHasFixedSize(true);
        mTransRv.setLayoutManager(manager);
        mTransRv.setAdapter(mTransactionAdapter);
        mProgress.setVisibility(View.GONE);
        mTransactionAdapter.notifyDataSetChanged();
    }

    private void initViews(View view) {
        mProgress = view.findViewById(R.id.transProgress);
        mTransRv = view.findViewById(R.id.transRecycler);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getTransactions(TransactionEvent event){
        if (event != null){
            mTransactions = event.getTransactions();
            populateRecycler(mTransactions);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

