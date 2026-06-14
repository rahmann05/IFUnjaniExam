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

        View cardGabungKelas = findViewById(R.id.cardGabungKelas);
        cardGabungKelas.setOnClickListener(v -> showJoinClassDialog());
    }

    private void showJoinClassDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Masukkan ID Kelas (angka)");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new android.app.AlertDialog.Builder(this)
            .setTitle("Gabung Kelas")
            .setMessage("Silakan masukkan ID kelas untuk bergabung.")
            .setView(input)
            .setPositiveButton("Gabung", (dialog, which) -> {
                String val = input.getText().toString();
                if (!val.isEmpty()) joinClass(val);
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void joinClass(String classId) {
        String url = "https://if-unjani-exam-api.vercel.app/api/kelas/join";
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        try {
            org.json.JSONObject body = new org.json.JSONObject();
            body.put("classId", Integer.parseInt(classId));

            com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                    com.android.volley.Request.Method.POST, url, body,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                android.widget.Toast.makeText(this, "Berhasil bergabung ke kelas", android.widget.Toast.LENGTH_SHORT).show();
                            } else {
                                android.widget.Toast.makeText(this, response.getString("message"), android.widget.Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> android.widget.Toast.makeText(this, "Gagal bergabung (ID Kelas salah / sudah bergabung)", android.widget.Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
