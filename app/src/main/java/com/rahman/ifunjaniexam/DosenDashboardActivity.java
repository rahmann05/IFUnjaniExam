package com.rahman.ifunjaniexam;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DosenDashboardActivity extends AppCompatActivity {

    private TextView tvNipHeader, tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_dosen);

        tvNipHeader = findViewById(R.id.tvNipHeader);
        tvWelcome = findViewById(R.id.tvWelcome);
        View cardBuatSoal = findViewById(R.id.cardBuatSoal);
        View cardKelolaUjian = findViewById(R.id.cardKelolaUjian);

        // Fetch name/nip from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String username = prefs.getString("username", "Dosen");
        
        tvNipHeader.setText(username);
        tvWelcome.setText("Selamat Datang,\n" + username);

        View.OnClickListener goToClassSelection = v -> {
            android.content.Intent intent = new android.content.Intent(DosenDashboardActivity.this, ClassSelectionActivity.class);
            startActivity(intent);
        };

        cardBuatSoal.setOnClickListener(goToClassSelection);
        cardKelolaUjian.setOnClickListener(goToClassSelection);
    }
}
