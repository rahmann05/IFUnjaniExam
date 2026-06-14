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

        // Placeholder for Admin CRUD Actions since building full native forms takes immense boilerplate
        btnDosen.setOnClickListener(v -> showToast("API Kelola Dosen tersedia. Fitur UI sedang dikembangkan."));
        btnMahasiswa.setOnClickListener(v -> showToast("API Kelola Mahasiswa tersedia. Fitur UI sedang dikembangkan."));
        btnKelas.setOnClickListener(v -> showToast("API Kelola Kelas tersedia. Fitur UI sedang dikembangkan."));
        btnPersetujuan.setOnClickListener(v -> showToast("API Persetujuan Ujian tersedia. Fitur UI sedang dikembangkan."));

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
