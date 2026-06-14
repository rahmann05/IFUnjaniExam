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

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private JSONArray users;
    private OnUserDeleteListener listener;

    public interface OnUserDeleteListener {
        void onDeleteClick(JSONObject user);
    }

    public AdminUserAdapter(JSONArray users, OnUserDeleteListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject user = users.getJSONObject(position);
            String role = user.getString("role");
            holder.tvUsername.setText("Username: " + user.getString("username"));

            if ("DOSEN".equals(role) && !user.isNull("dosen")) {
                JSONObject dosen = user.getJSONObject("dosen");
                holder.tvName.setText(dosen.getString("name"));
                holder.tvIdentifier.setText("NIP: " + dosen.getString("nip"));
            } else if ("MAHASISWA".equals(role) && !user.isNull("mahasiswa")) {
                JSONObject mhs = user.getJSONObject("mahasiswa");
                holder.tvName.setText(mhs.getString("name"));
                holder.tvIdentifier.setText("NIM: " + mhs.getString("nim"));
            } else {
                holder.tvName.setText("Admin / Tidak diketahui");
                holder.tvIdentifier.setText("");
            }

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(user);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return users.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvIdentifier, tvUsername;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvIdentifier = itemView.findViewById(R.id.tvIdentifier);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
