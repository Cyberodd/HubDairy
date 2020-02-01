package com.hub.dairy.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hub.dairy.R;
import com.hub.dairy.adapters.TransactionAdapter;
import com.hub.dairy.helpers.TransactionEvent;
import com.hub.dairy.models.Transaction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class TransactionFragment extends Fragment {

    public TransactionFragment() { }

    private ProgressBar mProgress;
    private RecyclerView mTransRv;
    private List<Transaction> mTransactions = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        initViews(view);
        return view;
    }

    private void populateRecycler(List<Transaction> transactions) {
        TransactionAdapter transactionAdapter = new TransactionAdapter(transactions);
        mProgress.setVisibility(View.VISIBLE);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mTransRv.setHasFixedSize(true);
        mTransRv.setLayoutManager(manager);
        mTransRv.setAdapter(transactionAdapter);
        mProgress.setVisibility(View.GONE);
        transactionAdapter.notifyDataSetChanged();
    }

    private void initViews(View view) {
        mProgress = view.findViewById(R.id.transProgress);
        mTransRv = view.findViewById(R.id.transRecycler);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getTransactions(TransactionEvent event){
        if (event != null){
            mTransactions.clear();
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

