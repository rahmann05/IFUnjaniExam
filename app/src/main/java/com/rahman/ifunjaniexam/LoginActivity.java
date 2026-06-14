package com.rahman.ifunjaniexam;

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
        // Menggunakan Endpoint API dari Vercel
        String url = "https://if-unjani-exam-api.vercel.app/api/auth/login";

        org.json.JSONObject postData = new org.json.JSONObject();
        try {
            postData.put("username", username);
            postData.put("password", password);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.POST,
                url,
                postData,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            String token = response.getString("token");
                            org.json.JSONObject data = response.getJSONObject("data");
                            String role = data.getString("role");
                            int profileId = data.optInt("profileId", -1);

                            // Simpan token, role, dan username ke SharedPreferences
                            getSharedPreferences("AUTH_PREF", MODE_PRIVATE)
                                    .edit()
                                    .putString("jwt_token", token)
                                    .putString("role", role)
                                    .putString("username", username)
                                    .putInt("profileId", profileId)
                                    .apply();

                            com.rahman.ifunjaniexam.utils.FeedbackUtils.showToast(this, "Login Berhasil");

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
                },
                error -> {
                    String errorMsg = "Gagal terhubung ke server";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String res = new String(error.networkResponse.data, "UTF-8");
                            org.json.JSONObject errObj = new org.json.JSONObject(res);
                            errorMsg = errObj.optString("message", errorMsg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    com.rahman.ifunjaniexam.utils.FeedbackUtils.shakeView(btnLogin);
                    com.rahman.ifunjaniexam.utils.FeedbackUtils.showSnackbar(btnLogin, errorMsg);
                }
        );

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }
}
