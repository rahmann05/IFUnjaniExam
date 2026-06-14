package com.rahman.ifunjaniexam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

public class AdminClassAdapter extends RecyclerView.Adapter<AdminClassAdapter.ViewHolder> {

    private JSONArray classes;
    private OnClassDeleteListener listener;

    public interface OnClassDeleteListener {
        void onDeleteClick(JSONObject classObj);
    }

    public AdminClassAdapter(JSONArray classes, OnClassDeleteListener listener) {
        this.classes = classes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_class, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject classObj = classes.getJSONObject(position);
            JSONObject course = classObj.getJSONObject("course");
            JSONObject dosen = classObj.getJSONObject("dosen");

            holder.tvClassName.setText("Kelas: " + classObj.getString("name"));
            holder.tvCourseName.setText(course.getString("name"));
            holder.tvDosenName.setText("Dosen: " + dosen.getString("name"));

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(classObj);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return classes.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvCourseName, tvDosenName;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvDosenName = itemView.findViewById(R.id.tvDosenName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
