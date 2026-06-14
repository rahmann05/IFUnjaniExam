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

        // Load Logo Unjani dari Uploadcare menggunakan Glide
        com.bumptech.glide.Glide.with(this)
                .load("https://ucarecdn.com/ada08a9e-b241-4f5c-87f3-f0e6bbf4c272/Logo_Unjani.png")
                .into(ivLogo);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText() != null ? etUsername.getText().toString() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Silakan isi username dan password!", Toast.LENGTH_SHORT).show();
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

                            // Simpan token & role ke SharedPreferences
                            getSharedPreferences("AUTH_PREF", MODE_PRIVATE)
                                    .edit()
                                    .putString("jwt_token", token)
                                    .putString("role", role)
                                    .apply();

                            Toast.makeText(this, "Login Berhasil sebagai " + role, Toast.LENGTH_SHORT).show();

                            // Pindah ke MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = response.optString("message", "Login Gagal");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Format respon server tidak valid", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                }
        );

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }
}
