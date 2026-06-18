package com.rahman.ifunjaniexam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

public class AdminApprovalAdapter extends RecyclerView.Adapter<AdminApprovalAdapter.ViewHolder> {

    private JSONArray requests;
    private OnActionListener listener;

    public interface OnActionListener {
        void onAction(JSONObject request, String action);
    }

    public AdminApprovalAdapter(JSONArray requests, OnActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_approval_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject req = requests.getJSONObject(position);
            JSONObject exam = req.getJSONObject("exam");
            JSONObject dosen = req.getJSONObject("dosen");

            holder.tvExamTitle.setText(exam.getString("title"));
            holder.tvDosenName.setText("Pemohon: " + dosen.getString("name"));
            holder.tvRequestType.setText("Tipe: " + req.getString("requestType"));
            holder.tvReason.setText("Alasan: " + req.optString("reason", "Tidak ada"));

            holder.btnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onAction(req, "APPROVE");
            });

            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onAction(req, "REJECT");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return requests.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExamTitle, tvDosenName, tvRequestType, tvReason;
        Button btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExamTitle = itemView.findViewById(R.id.tvExamTitle);
            tvDosenName = itemView.findViewById(R.id.tvDosenName);
            tvRequestType = itemView.findViewById(R.id.tvRequestType);
            tvReason = itemView.findViewById(R.id.tvReason);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
