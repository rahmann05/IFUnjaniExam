package com.rahman.ifunjaniexam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

public class ExamListAdapter extends RecyclerView.Adapter<ExamListAdapter.ExamViewHolder> {

    private JSONArray examList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(JSONObject examObj);
    }

    public ExamListAdapter(JSONArray examList, OnItemClickListener listener) {
        this.examList = examList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        try {
            JSONObject examObj = examList.getJSONObject(position);
            holder.tvExamTitle.setText(examObj.getString("title"));
            holder.tvExamDesc.setText(examObj.optString("description", "Tidak ada deskripsi"));
            holder.tvDuration.setText(examObj.getString("durationMinutes") + " Menit");
            
            String startTime = examObj.getString("startTime").replace("T", " ").replace("Z", "");
            holder.tvTimeRange.setText("Mulai: " + startTime);

            holder.itemView.setOnClickListener(v -> listener.onItemClick(examObj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return examList != null ? examList.length() : 0;
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvExamTitle, tvExamDesc, tvDuration, tvTimeRange;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExamTitle = itemView.findViewById(R.id.tvExamTitle);
            tvExamDesc = itemView.findViewById(R.id.tvExamDesc);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
        }
    }
}
