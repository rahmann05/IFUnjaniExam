package com.rahman.ifunjaniexam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Button btnDosen = findViewById(R.id.btnKelolaDosen);
        Button btnMahasiswa = findViewById(R.id.btnKelolaMahasiswa);
        Button btnKelas = findViewById(R.id.btnKelolaKelas);
        Button btnPersetujuan = findViewById(R.id.btnPersetujuan);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnDosen.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminManageUserActivity.class));
        });
        btnMahasiswa.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminManageUserActivity.class));
        });
        btnKelas.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminManageClassActivity.class));
        });
        btnPersetujuan.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminApprovalActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("AUTH_PREF", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
