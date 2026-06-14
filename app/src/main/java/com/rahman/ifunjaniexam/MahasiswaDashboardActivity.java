package com.rahman.ifunjaniexam;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MahasiswaDashboardActivity extends AppCompatActivity {

    private TextView tvNimHeader, tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_mahasiswa);

        tvNimHeader = findViewById(R.id.tvNimHeader);
        tvWelcome = findViewById(R.id.tvWelcome);
        View cardDaftarUjian = findViewById(R.id.cardDaftarUjian);

        // Fetch name/nim from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String username = prefs.getString("username", "Mahasiswa");
        
        tvNimHeader.setText(username);
        tvWelcome.setText("Selamat Datang,\n" + username);

        cardDaftarUjian.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MahasiswaDashboardActivity.this, ClassSelectionActivity.class);
            startActivity(intent);
        });
    }
}
