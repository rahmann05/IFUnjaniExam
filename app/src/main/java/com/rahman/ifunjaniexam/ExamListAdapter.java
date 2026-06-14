package com.rahman.ifunjaniexam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

public class ExamListAdapter extends RecyclerView.Adapter<ExamListAdapter.ViewHolder> {

    private JSONArray examList;
    private OnItemClickListener listener;
    private OnOptionClickListener optionListener;
    private boolean isDosen;

    public interface OnItemClickListener {
        void onItemClick(JSONObject exam);
    }

    public interface OnOptionClickListener {
        void onEditClick(JSONObject exam);
        void onDeleteClick(JSONObject exam);
        void onUpdateStatusClick(JSONObject exam, String newStatus);
    }

    public ExamListAdapter(JSONArray examList, boolean isDosen, OnItemClickListener listener, OnOptionClickListener optionListener) {
        this.examList = examList;
        this.isDosen = isDosen;
        this.listener = listener;
        this.optionListener = optionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject examObj = examList.getJSONObject(position);
            holder.tvExamTitle.setText(examObj.getString("title"));
            
            String category = examObj.optString("category", "OTHER");
            String weight = examObj.optString("weight", "100");
            String desc = examObj.optString("description", "Tidak ada deskripsi");

            JSONArray attempts = examObj.optJSONArray("attempts");
            boolean hasAttempt = (attempts != null && attempts.length() > 0);

            if (hasAttempt) {
                try {
                    JSONObject attempt = attempts.getJSONObject(0);
                    double score = attempt.optDouble("score", -1.0);
                    String scoreStr = (score >= 0) ? String.format("%.2f", score) : "Belum Dinilai";
                    holder.tvExamDesc.setText("[" + category + " - Bobot: " + weight + "%]\n" + desc + "\n\nNilai Anda: " + scoreStr);
                } catch (Exception e) {
                    holder.tvExamDesc.setText("[" + category + " - Bobot: " + weight + "%]\n" + desc + "\n\nSudah Dikerjakan");
                }
            } else {
                holder.tvExamDesc.setText("[" + category + " - Bobot: " + weight + "%]\n" + desc);
            }

            holder.tvDuration.setText(examObj.getString("durationMinutes") + " Menit");
            
            String startTime = examObj.optString("startTime", "Manual / Belum Diset").replace("T", " ").replace("Z", "");
            holder.tvTime.setText(startTime.equals("null") ? "Manual / Belum Diset" : startTime);

            if (isDosen) {
                holder.btnOptions.setVisibility(View.VISIBLE);
                holder.btnStatus.setVisibility(View.VISIBLE);
                
                String status = examObj.optString("status", "PUBLISHED");
                if ("PUBLISHED".equals(status)) {
                    holder.btnStatus.setText("Mulai Ujian");
                    holder.btnStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2ecc71"))); // Green
                    holder.btnStatus.setEnabled(true);
                    holder.btnStatus.setOnClickListener(v -> {
                        if (optionListener != null) optionListener.onUpdateStatusClick(examObj, "ONGOING");
                    });
                } else if ("ONGOING".equals(status)) {
                    holder.btnStatus.setText("Hentikan Ujian");
                    holder.btnStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#e74c3c"))); // Red
                    holder.btnStatus.setEnabled(true);
                    holder.btnStatus.setOnClickListener(v -> {
                        if (optionListener != null) optionListener.onUpdateStatusClick(examObj, "FINISHED");
                    });
                } else {
                    holder.btnStatus.setText("Selesai");
                    holder.btnStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#95a5a6"))); // Grey
                    holder.btnStatus.setEnabled(false);
                }

                holder.btnOptions.setOnClickListener(v -> {
                    android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), holder.btnOptions);
                    popup.getMenu().add("Edit");
                    popup.getMenu().add("Hapus");
                    popup.setOnMenuItemClickListener(item -> {
                        if (item.getTitle().equals("Edit") && optionListener != null) {
                            optionListener.onEditClick(examObj);
                        } else if (item.getTitle().equals("Hapus") && optionListener != null) {
                            optionListener.onDeleteClick(examObj);
                        }
                        return true;
                    });
                    popup.show();
                });
            } else {
                holder.btnOptions.setVisibility(View.GONE);
                holder.btnStatus.setVisibility(View.VISIBLE);
                if (hasAttempt) {
                    holder.btnStatus.setText("Lihat Hasil");
                    holder.btnStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3498db"))); // Blue
                    holder.btnStatus.setEnabled(true);
                    holder.btnStatus.setOnClickListener(v -> {
                        if (listener != null) listener.onItemClick(examObj);
                    });
                } else {
                    String status = examObj.optString("status", "PUBLISHED");
                    if ("PUBLISHED".equals(status)) {
                        holder.btnStatus.setText("Belum Mulai");
                        holder.btnStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#f1c40f"))); // Yellow
                    } else if ("ONGOING".equals(status)) {
                        holder.btnStatus.setText("Sedang Berlangsung");
                        holder.btnStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2ecc71"))); // Green
                    } else {
                        holder.btnStatus.setText("Selesai");
                        holder.btnStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#95a5a6"))); // Grey
                    }
                    holder.btnStatus.setEnabled(false);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(examObj);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return examList != null ? examList.length() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExamTitle, tvExamDesc, tvDuration, tvTime;
        android.widget.ImageView btnOptions;
        android.widget.Button btnStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExamTitle = itemView.findViewById(R.id.tvExamTitle);
            tvExamDesc = itemView.findViewById(R.id.tvExamDesc);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnOptions = itemView.findViewById(R.id.btnOptions);
            btnStatus = itemView.findViewById(R.id.btnStatus);
        }
    }
}
