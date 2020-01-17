package com.hub.dairy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hub.dairy.R;
import com.hub.dairy.models.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionHolder> {

    private List<Transaction> mTransactions;

    public TransactionAdapter(List<Transaction> transactions) {
        mTransactions = transactions;
    }

    @NonNull
    @Override
    public TransactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.transaction_item, parent, false);
        return new TransactionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionHolder holder, int position) {
        Transaction transaction = mTransactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return mTransactions.size();
    }

    class TransactionHolder extends RecyclerView.ViewHolder{

        TextView transDate, transType, transQuantity, transAmount, txtAmount;

        TransactionHolder(@NonNull View itemView) {
            super(itemView);
            transDate = itemView.findViewById(R.id.transDate);
            transType = itemView.findViewById(R.id.transType);
            transQuantity = itemView.findViewById(R.id.transQuantity);
            transAmount = itemView.findViewById(R.id.transAmount);
            txtAmount = itemView.findViewById(R.id.txtAmount);
        }

        private void bind(Transaction transaction) {
            transDate.setText(transaction.getTime());
            transType.setText(transaction.getType());
            transQuantity.setText(transaction.getQuantity());
            transAmount.setText(transaction.getCash());

            if (transaction.getType().equals("Milk Sale")){
                txtAmount.setText(R.string.quantity_litres);
            } else {
                txtAmount.setText(R.string.quantity_kgs);
            }
        }
    }
}
