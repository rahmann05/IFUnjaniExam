package com.rahman.ifunjaniexam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

public class KelasAdapter extends RecyclerView.Adapter<KelasAdapter.KelasViewHolder> {

    private JSONArray kelasList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(JSONObject kelasObj);
    }

    public KelasAdapter(JSONArray kelasList, OnItemClickListener listener) {
        this.kelasList = kelasList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public KelasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kelas, parent, false);
        return new KelasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KelasViewHolder holder, int position) {
        try {
            JSONObject kelasObj = kelasList.getJSONObject(position);
            JSONObject course = kelasObj.getJSONObject("course");
            JSONObject semester = kelasObj.getJSONObject("semester");

            holder.tvCourseName.setText(course.getString("name"));
            holder.tvClassName.setText("Kelas: " + kelasObj.getString("name") + " (" + kelasObj.optString("code", "-") + ")");
            holder.tvSemester.setText("Semester: " + semester.getString("name"));

            holder.itemView.setOnClickListener(v -> listener.onItemClick(kelasObj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return kelasList != null ? kelasList.length() : 0;
    }

    static class KelasViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvClassName, tvSemester;

        public KelasViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvSemester = itemView.findViewById(R.id.tvSemester);
        }
    }
}
