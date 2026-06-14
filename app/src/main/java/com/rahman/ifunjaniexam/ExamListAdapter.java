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
            
            holder.tvExamDesc.setText("[" + category + " - Bobot: " + weight + "%]\n" + desc);
            holder.tvDuration.setText(examObj.getString("durationMinutes") + " Menit");
            
            String startTime = examObj.getString("startTime").replace("T", " ").replace("Z", "");
            holder.tvTime.setText(startTime);

            if (isDosen) {
                holder.btnOptions.setVisibility(View.VISIBLE);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExamTitle = itemView.findViewById(R.id.tvExamTitle);
            tvExamDesc = itemView.findViewById(R.id.tvExamDesc);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnOptions = itemView.findViewById(R.id.btnOptions);
        }
    }
}
