package com.hub.dairy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hub.dairy.R;
import com.hub.dairy.models.MilkProduce;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportHolder> {

    private List<MilkProduce> mMilkProduces;

    public ReportAdapter(List<MilkProduce> milkProduces) {
        mMilkProduces = milkProduces;
    }

    @NonNull
    @Override
    public ReportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.progress_report_item, parent, false);
        return new ReportHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportHolder holder, int position) {
        holder.bind(mMilkProduces.get(position));
    }

    @Override
    public int getItemCount() {
        return mMilkProduces != null ? mMilkProduces.size() : 0;
    }

    class ReportHolder extends RecyclerView.ViewHolder{

        TextView date, animalName, mrgQty, evgQty, totalQty;

        ReportHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.progressDate);
            animalName = itemView.findViewById(R.id.animalReportName);
            mrgQty = itemView.findViewById(R.id.mrgQty);
            evgQty = itemView.findViewById(R.id.evnQty);
            totalQty = itemView.findViewById(R.id.totalQty);
        }

        private void bind(MilkProduce milkProduce) {
            date.setText(milkProduce.getDate());
            animalName.setText(milkProduce.getAnimalName());
            totalQty.setText(milkProduce.getTotalQty());

            if (!milkProduce.getMrgQty().equals("")){
                mrgQty.setText(milkProduce.getMrgQty());
            } else {
                mrgQty.setText("0");
            }

            if (!milkProduce.getEvnQty().equals("")){
                evgQty.setText(milkProduce.getEvnQty());
            } else {
                evgQty.setText("0");
            }
        }
    }
}
