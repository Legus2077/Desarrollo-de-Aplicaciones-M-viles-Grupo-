package com.example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.models.TestResult;

import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private final List<TestResult> resultsList;

    public ResultsAdapter(List<TestResult> resultsList) {
        this.resultsList = resultsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_test_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestResult item = resultsList.get(position);
        holder.tvScore.setText(item.getScore());
        holder.tvTitle.setText(item.getTitle());
        holder.tvDetail.setText(item.getDetail());

        if (item.isLowScore()) {
            holder.tvScore.setBackgroundResource(R.drawable.bg_score_circle_danger);
            holder.tvScore.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.cq_danger));
        } else {
            holder.tvScore.setBackgroundResource(R.drawable.bg_score_circle);
            holder.tvScore.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.cq_text));
        }
    }

    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvScore;
        public final TextView tvTitle;
        public final TextView tvDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScore = itemView.findViewById(R.id.tvResultScore);
            tvTitle = itemView.findViewById(R.id.tvResultTitle);
            tvDetail = itemView.findViewById(R.id.tvResultDetail);
        }
    }
}
