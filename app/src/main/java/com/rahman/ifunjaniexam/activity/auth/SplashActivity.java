package com.rahman.ifunjaniexam.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.rahman.ifunjaniexam.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Pengecekan token secara instan, tanpa delay buatan sama sekali
        android.content.SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        String role = prefs.getString("role", "");

        Intent intent;
        if (token != null && !token.isEmpty()) {
            // Token ada, auto-login instan sesuai role
            if ("DOSEN".equals(role)) {
                intent = new Intent(SplashActivity.this, com.rahman.ifunjaniexam.activity.dosen.DosenDashboardActivity.class);
            } else if ("ADMIN".equals(role)) {
                intent = new Intent(SplashActivity.this, com.rahman.ifunjaniexam.activity.admin.AdminDashboardActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, com.rahman.ifunjaniexam.activity.mahasiswa.MahasiswaDashboardActivity.class);
            }
        } else {
            // Tidak ada token, langsung lempar ke Login
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
