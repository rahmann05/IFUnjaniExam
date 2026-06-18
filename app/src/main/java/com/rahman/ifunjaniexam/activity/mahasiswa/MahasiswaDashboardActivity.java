package com.rahman.ifunjaniexam.activity.mahasiswa;

import com.rahman.ifunjaniexam.network.ClassApiService;
import com.rahman.ifunjaniexam.adapters.KelasAdapter;
import com.rahman.ifunjaniexam.activity.auth.LoginActivity;
import com.rahman.ifunjaniexam.network.AuthApiService;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MahasiswaDashboardActivity extends AppCompatActivity {

    private TextView tvNimHeader, tvWelcome;
    private View tvEmptyClasses;
    private RecyclerView rvKelas;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_mahasiswa);

        tvNimHeader = findViewById(R.id.tvNimHeader);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmptyClasses = findViewById(R.id.tvEmptyClasses);
        rvKelas = findViewById(R.id.rvKelas);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        rvKelas.setLayoutManager(new LinearLayoutManager(this));

        swipeRefresh.setOnRefreshListener(this::loadClasses);

        // Fetch name/nim from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String name = prefs.getString("name", "Mahasiswa");
        
        tvNimHeader.setText(username);
        tvWelcome.setText(name);

        android.widget.ImageView ivPerson = findViewById(R.id.ivPerson);
        ivPerson.setOnClickListener(v -> showProfileMenu(v));

        findViewById(R.id.btnGabungKelas).setOnClickListener(v -> showJoinClassDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses();
    }

    private void loadClasses() {
        if (!swipeRefresh.isRefreshing()) progressBar.setVisibility(View.VISIBLE);
        tvEmptyClasses.setVisibility(View.GONE);

        com.rahman.ifunjaniexam.network.ClassApiService.getClasses(this, new com.rahman.ifunjaniexam.network.ClassApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
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
                                    Intent intent = new Intent(MahasiswaDashboardActivity.this, MahasiswaClassDetailActivity.class);
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
                    Toast.makeText(MahasiswaDashboardActivity.this, "Format data salah", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception error, com.android.volley.VolleyError volleyError) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(MahasiswaDashboardActivity.this, "Gagal memuat daftar kelas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showJoinClassDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Masukkan Kode Kelas (e.g. IF-331)");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
            .setTitle("Gabung Kelas")
            .setMessage("Silakan masukkan kode kelas untuk bergabung.")
            .setView(input)
            .setPositiveButton("Gabung", null) // Set null temporarily to override default closing behavior
            .setNegativeButton("Batal", null)
            .create();

        dialog.setOnShowListener(d -> {
            android.widget.Button button = ((android.app.AlertDialog) dialog).getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String val = input.getText().toString();
                if (val.isEmpty()) {
                    input.setError("Kode kelas tidak boleh kosong");
                    return;
                }
                button.setEnabled(false);
                button.setText("Loading...");
                joinClass(val, dialog, button);
            });
        });
        dialog.show();
    }

    private void joinClass(String classCode, android.app.AlertDialog dialog, android.widget.Button button) {
        try {
            org.json.JSONObject payload = new org.json.JSONObject();
            payload.put("classCode", classCode);

            com.rahman.ifunjaniexam.network.ClassApiService.joinClass(this, payload, new com.rahman.ifunjaniexam.network.ClassApiService.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(MahasiswaDashboardActivity.this, "Berhasil bergabung ke kelas", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadClasses(); // Reload classes
                        } else {
                            Toast.makeText(MahasiswaDashboardActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            button.setEnabled(true);
                            button.setText("Gabung");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        button.setEnabled(true);
                        button.setText("Gabung");
                    }
                }

                @Override
                public void onError(Exception error, com.android.volley.VolleyError volleyError) {
                    button.setEnabled(true);
                    button.setText("Gabung");
                    if (volleyError != null && volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                        try {
                            String res = new String(volleyError.networkResponse.data, "utf-8");
                            org.json.JSONObject json = new org.json.JSONObject(res);
                            String msg = json.optString("message", "Gagal bergabung ke kelas");
                            Toast.makeText(MahasiswaDashboardActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(MahasiswaDashboardActivity.this, "Gagal bergabung ke kelas", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MahasiswaDashboardActivity.this, "Gagal bergabung (ID Kelas salah / sudah bergabung)", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            button.setEnabled(true);
            button.setText("Gabung");
        }
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
                android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        com.rahman.ifunjaniexam.network.AuthApiService.changePassword(this, oldPass, newPass, new com.rahman.ifunjaniexam.network.AuthApiService.AuthCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(MahasiswaDashboardActivity.this, "Password berhasil diubah", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception error, com.android.volley.VolleyError volleyError) {
                Toast.makeText(MahasiswaDashboardActivity.this, "Gagal mengubah password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
