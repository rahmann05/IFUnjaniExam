package com.rahman.ifunjaniexam.activity.admin;

import com.rahman.ifunjaniexam.network.AdminApiService;
import com.rahman.ifunjaniexam.adapters.AdminApprovalAdapter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdminApprovalActivity extends AppCompatActivity {

    private RecyclerView rvApprovals;
    private ProgressBar progressBar;
    private TextView tvEmptyApprovals;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_approval);

        rvApprovals = findViewById(R.id.rvApprovals);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyApprovals = findViewById(R.id.tvEmptyApprovals);

        rvApprovals.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        token = prefs.getString("jwt_token", "");

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadRequests();
    }

    private void loadRequests() {
        progressBar.setVisibility(View.VISIBLE);

        com.rahman.ifunjaniexam.network.AdminApiService.getApprovalRequests(this, new com.rahman.ifunjaniexam.network.AdminApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                try {
                    if (response.getBoolean("success")) {
                        JSONArray data = response.getJSONArray("data");
                        if (data.length() == 0) {
                            tvEmptyApprovals.setVisibility(View.VISIBLE);
                            rvApprovals.setVisibility(View.GONE);
                        } else {
                            tvEmptyApprovals.setVisibility(View.GONE);
                            rvApprovals.setVisibility(View.VISIBLE);

                            AdminApprovalAdapter adapter = new AdminApprovalAdapter(data, (reqObj, action) -> {
                                try {
                                    processApproval(reqObj.getInt("id"), action);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            rvApprovals.setAdapter(adapter);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception error, com.android.volley.VolleyError volleyError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminApprovalActivity.this, "Gagal memuat permintaan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processApproval(int reqId, String action) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Anda yakin ingin " + (action.equals("APPROVE") ? "menyetujui" : "menolak") + " permintaan ini?")
            .setPositiveButton("Ya", (dialog, which) -> {
                com.rahman.ifunjaniexam.network.AdminApiService.processApproval(this, reqId, action, new com.rahman.ifunjaniexam.network.AdminApiService.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(AdminApprovalActivity.this, "Aksi berhasil", Toast.LENGTH_SHORT).show();
                                loadRequests();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Exception error, com.android.volley.VolleyError volleyError) {
                        Toast.makeText(AdminApprovalActivity.this, "Gagal memproses aksi", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Batal", null)
            .show();
    }
}
