package com.rahman.ifunjaniexam.adapters;

import com.rahman.ifunjaniexam.R;

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
            JSONObject studentObj = attempts.getJSONObject(position);

            holder.tvStudentName.setText(studentObj.getString("name"));
            holder.tvNim.setText(studentObj.getString("nim"));
            
            if (studentObj.has("attempt") && !studentObj.isNull("attempt")) {
                JSONObject attempt = studentObj.getJSONObject("attempt");
                double score = attempt.optDouble("score", 0.0);
                holder.tvScore.setText(String.format("%.1f", score));
                holder.tvScore.setVisibility(View.VISIBLE);

                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(attempt);
                });
                holder.itemView.setClickable(true);
            } else {
                holder.tvScore.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(null);
                holder.itemView.setClickable(false);
            }
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
