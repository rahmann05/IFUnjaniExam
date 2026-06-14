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
        android.widget.ImageView ivPerson = findViewById(R.id.ivPerson);

        // Fetch name/nim from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String username = prefs.getString("username", "Mahasiswa");
        
        tvNimHeader.setText(username);
        tvWelcome.setText("Selamat Datang,\n" + username);

        ivPerson.setOnClickListener(v -> showProfileMenu(v));

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
        String url = "https://if-unjani-exam-api.vercel.app/api/auth/change-password";
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        try {
            org.json.JSONObject body = new org.json.JSONObject();
            body.put("oldPassword", oldPass);
            body.put("newPassword", newPass);

            com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                    com.android.volley.Request.Method.PUT, url, body,
                    response -> android.widget.Toast.makeText(this, "Password berhasil diubah", android.widget.Toast.LENGTH_SHORT).show(),
                    error -> android.widget.Toast.makeText(this, "Gagal mengubah password", android.widget.Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
