package com.rahman.ifunjaniexam.activity.auth;

import com.rahman.ifunjaniexam.R;

import com.rahman.ifunjaniexam.activity.admin.AdminDashboardActivity;
import com.rahman.ifunjaniexam.network.AuthApiService;
import com.rahman.ifunjaniexam.utils.FeedbackUtils;
import com.rahman.ifunjaniexam.activity.dosen.DosenDashboardActivity;
import com.rahman.ifunjaniexam.activity.mahasiswa.MahasiswaDashboardActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private android.widget.ImageView ivLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ivLogo = findViewById(R.id.ivLogo);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            com.rahman.ifunjaniexam.utils.FeedbackUtils.clickAnim(v);
            String username = etUsername.getText() != null ? etUsername.getText().toString() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

            if (username.isEmpty() || password.isEmpty()) {
                com.rahman.ifunjaniexam.utils.FeedbackUtils.shakeView(btnLogin);
                com.rahman.ifunjaniexam.utils.FeedbackUtils.showSnackbar(v, "Silakan isi username dan password!");
            } else {
                // REST API Login using Volley
                loginUser(username, password);
            }
        });
    }

    private void loginUser(String username, String password) {
        com.rahman.ifunjaniexam.network.AuthApiService.login(this, username, password, new com.rahman.ifunjaniexam.network.AuthApiService.AuthCallback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                try {
                    boolean success = response.getBoolean("success");
                    if (success) {
                        String token = response.getString("token");
                        org.json.JSONObject data = response.getJSONObject("data");
                        String role = data.getString("role");
                        int profileId = -1;
                        String name = username;
                        if (data.has("profile") && !data.isNull("profile")) {
                            org.json.JSONObject profile = data.getJSONObject("profile");
                            profileId = profile.optInt("id", -1);
                            name = profile.optString("name", username);
                        }

                        // Simpan token, role, dan username ke SharedPreferences
                        getSharedPreferences("AUTH_PREF", MODE_PRIVATE)
                                .edit()
                                .putString("jwt_token", token)
                                .putString("role", role)
                                .putString("username", username)
                                .putString("name", name)
                                .putInt("profileId", profileId)
                                .apply();

                        com.rahman.ifunjaniexam.utils.FeedbackUtils.showToast(LoginActivity.this, "Login Berhasil");

                        // Pindah ke Dashboard Sesuai Role
                        Intent intent;
                        if ("DOSEN".equals(role)) {
                            intent = new Intent(LoginActivity.this, DosenDashboardActivity.class);
                        } else if ("ADMIN".equals(role)) {
                            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, MahasiswaDashboardActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        String message = response.optString("message", "Login Gagal");
                        com.rahman.ifunjaniexam.utils.FeedbackUtils.shakeView(btnLogin);
                        com.rahman.ifunjaniexam.utils.FeedbackUtils.showSnackbar(btnLogin, message);
                    }
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                    com.rahman.ifunjaniexam.utils.FeedbackUtils.showSnackbar(btnLogin, "Format respon server tidak valid");
                }
            }

            @Override
            public void onError(Exception error, com.android.volley.VolleyError volleyError) {
                String errorMsg = "Gagal terhubung ke server";
                if (volleyError != null && volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    try {
                        String res = new String(volleyError.networkResponse.data, "UTF-8");
                        org.json.JSONObject errObj = new org.json.JSONObject(res);
                        errorMsg = errObj.optString("message", errorMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                com.rahman.ifunjaniexam.utils.FeedbackUtils.shakeView(btnLogin);
                com.rahman.ifunjaniexam.utils.FeedbackUtils.showSnackbar(btnLogin, errorMsg);
            }
        });
    }
}
