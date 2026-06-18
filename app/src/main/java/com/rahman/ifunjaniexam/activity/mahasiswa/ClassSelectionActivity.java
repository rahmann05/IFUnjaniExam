package com.rahman.ifunjaniexam.activity.mahasiswa;

import com.rahman.ifunjaniexam.R;

import com.rahman.ifunjaniexam.network.Config;
import com.rahman.ifunjaniexam.adapters.KelasAdapter;
import com.rahman.ifunjaniexam.activity.dosen.DosenClassDetailActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class ClassSelectionActivity extends AppCompatActivity {

    private RecyclerView rvKelas;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_selection);

        rvKelas = findViewById(R.id.rvKelas);
        progressBar = findViewById(R.id.progressBar);

        rvKelas.setLayoutManager(new LinearLayoutManager(this));
        
        loadClasses();
    }

    private void loadClasses() {
        progressBar.setVisibility(View.VISIBLE);
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/kelas";

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray data = response.getJSONArray("data");
                            String role = prefs.getString("role", "MAHASISWA");

                            KelasAdapter adapter = new KelasAdapter(data, kelasObj -> {
                                try {
                                    Toast.makeText(this, "Memilih kelas: " + kelasObj.getString("name"), Toast.LENGTH_SHORT).show();
                                    
                                    android.content.Intent intent;
                                    if ("DOSEN".equals(role)) {
                                        intent = new android.content.Intent(this, DosenClassDetailActivity.class);
                                    } else {
                                        intent = new android.content.Intent(this, MahasiswaClassDetailActivity.class);
                                    }
                                    intent.putExtra("classId", kelasObj.getInt("id"));
                                    startActivity(intent);
                                    
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            rvKelas.setAdapter(adapter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat daftar kelas", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
