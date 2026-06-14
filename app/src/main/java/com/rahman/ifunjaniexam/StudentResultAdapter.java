package com.rahman.ifunjaniexam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

public class StudentResultAdapter extends RecyclerView.Adapter<StudentResultAdapter.ViewHolder> {

    private JSONArray attempts;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(JSONObject attempt);
    }

    public StudentResultAdapter(JSONArray attempts, OnItemClickListener listener) {
        this.attempts = attempts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject attempt = attempts.getJSONObject(position);
            JSONObject mhs = attempt.getJSONObject("mahasiswa");

            holder.tvStudentName.setText(mhs.getString("name"));
            holder.tvNim.setText(mhs.getString("nim"));
            
            double score = attempt.getDouble("score");
            holder.tvScore.setText(String.format("%.1f", score));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(attempt);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return attempts.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvNim, tvScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvNim = itemView.findViewById(R.id.tvNim);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}
