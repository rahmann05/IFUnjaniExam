package com.rahman.ifunjaniexam;

import android.content.Intent;
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

public class DosenDashboardActivity extends AppCompatActivity {

    private TextView tvNipHeader, tvWelcome, tvEmptyClasses;
    private RecyclerView rvKelas;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_dosen);

        tvNipHeader = findViewById(R.id.tvNipHeader);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmptyClasses = findViewById(R.id.tvEmptyClasses);
        rvKelas = findViewById(R.id.rvKelas);
        progressBar = findViewById(R.id.progressBar);

        rvKelas.setLayoutManager(new LinearLayoutManager(this));

        // Fetch name/nip from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String name = prefs.getString("name", "Dosen");
        
        tvNipHeader.setText(username);
        tvWelcome.setText("Selamat Datang,\n" + name);

        android.widget.ImageView ivPerson = findViewById(R.id.ivPerson);
        ivPerson.setOnClickListener(v -> showProfileMenu(v));

        loadClasses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses();
    }

    private void loadClasses() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyClasses.setVisibility(View.GONE);
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/kelas";

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray data = response.getJSONArray("data");
                            if (data.length() == 0) {
                                tvEmptyClasses.setVisibility(View.VISIBLE);
                                rvKelas.setVisibility(View.GONE);
                            } else {
                                tvEmptyClasses.setVisibility(View.GONE);
                                rvKelas.setVisibility(View.VISIBLE);
                                KelasAdapter adapter = new KelasAdapter(data, kelasObj -> {
                                    try {
                                        Intent intent = new Intent(DosenDashboardActivity.this, DosenClassDetailActivity.class);
                                        intent.putExtra("classId", kelasObj.getInt("id"));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                                rvKelas.setAdapter(adapter);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat daftar kelas", Toast.LENGTH_SHORT).show();
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

    private void showProfileMenu(View v) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
        popup.getMenu().add(0, 1, 0, "Ganti Password");
        popup.getMenu().add(0, 2, 0, "Logout");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                showChangePasswordDialog();
                return true;
            } else if (item.getItemId() == 2) {
                getSharedPreferences("AUTH_PREF", MODE_PRIVATE).edit().clear().apply();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showChangePasswordDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        android.widget.EditText etOld = new android.widget.EditText(this);
        etOld.setHint("Password Lama");
        etOld.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etOld);

        android.widget.EditText etNew = new android.widget.EditText(this);
        etNew.setHint("Password Baru");
        etNew.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNew);

        new android.app.AlertDialog.Builder(this)
            .setTitle("Ganti Password")
            .setView(layout)
            .setPositiveButton("Simpan", (dialog, which) -> {
                String oldPass = etOld.getText().toString();
                String newPass = etNew.getText().toString();
                if (!oldPass.isEmpty() && !newPass.isEmpty()) {
                    changePassword(oldPass, newPass);
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void changePassword(String oldPass, String newPass) {
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/auth/change-password";
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        try {
            org.json.JSONObject body = new org.json.JSONObject();
            body.put("oldPassword", oldPass);
            body.put("newPassword", newPass);

            com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                    com.android.volley.Request.Method.PUT, url, body,
                    response -> Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show(),
                    error -> Toast.makeText(this, "Gagal mengubah password", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
