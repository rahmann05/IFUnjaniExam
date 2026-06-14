package com.rahman.ifunjaniexam;

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

        loadRequests();
    }

    private void loadRequests() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://if-unjani-exam-api.vercel.app/api/admin/requests";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
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
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat permintaan", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void processApproval(int reqId, String action) {
        String url = "https://if-unjani-exam-api.vercel.app/api/admin/requests/" + reqId;
        try {
            JSONObject body = new JSONObject();
            body.put("action", action);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(this, "Aksi berhasil", Toast.LENGTH_SHORT).show();
                                loadRequests();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Gagal memproses aksi", Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
