package com.rahman.ifunjaniexam;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdminManageClassActivity extends AppCompatActivity {

    private RecyclerView rvClasses;
    private ProgressBar progressBar;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_class);

        rvClasses = findViewById(R.id.rvClasses);
        progressBar = findViewById(R.id.progressBar);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddClass);

        rvClasses.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        token = prefs.getString("jwt_token", "");

        fabAdd.setOnClickListener(v -> showAddClassDialog());

        loadClasses();
    }

    private void loadClasses() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://if-unjani-exam-api.vercel.app/api/admin/classes";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray data = response.getJSONArray("data");
                            AdminClassAdapter adapter = new AdminClassAdapter(data, classObj -> {
                                showDeleteDialog(classObj);
                            });
                            rvClasses.setAdapter(adapter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat kelas", Toast.LENGTH_SHORT).show();
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

    private void showDeleteDialog(JSONObject classObj) {
        try {
            int classId = classObj.getInt("id");
            String name = classObj.getString("name");

            new AlertDialog.Builder(this)
                .setTitle("Hapus Kelas")
                .setMessage("Yakin ingin menghapus kelas '" + name + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteClass(classId))
                .setNegativeButton("Batal", null)
                .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteClass(int classId) {
        String url = "https://if-unjani-exam-api.vercel.app/api/admin/classes/" + classId;
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                responseStr -> {
                    try {
                        JSONObject response = new JSONObject(responseStr);
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Kelas berhasil dihapus", Toast.LENGTH_SHORT).show();
                            loadClasses();
                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Gagal menghapus kelas", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void showAddClassDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_class, null);
        EditText etName = view.findViewById(R.id.etClassName);
        EditText etCourseId = view.findViewById(R.id.etCourseId);
        EditText etSemesterId = view.findViewById(R.id.etSemesterId);
        EditText etDosenId = view.findViewById(R.id.etDosenId);

        new AlertDialog.Builder(this)
            .setTitle("Tambah Kelas")
            .setView(view)
            .setPositiveButton("Simpan", (dialog, which) -> {
                try {
                    String name = etName.getText().toString();
                    int courseId = Integer.parseInt(etCourseId.getText().toString());
                    int semesterId = Integer.parseInt(etSemesterId.getText().toString());
                    int dosenId = Integer.parseInt(etDosenId.getText().toString());
                    createClass(name, courseId, semesterId, dosenId);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "ID harus berupa angka", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void createClass(String name, int courseId, int semesterId, int dosenId) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Nama kelas tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://if-unjani-exam-api.vercel.app/api/admin/classes";
        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("courseId", courseId);
            body.put("semesterId", semesterId);
            body.put("dosenId", dosenId);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(this, "Kelas berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                loadClasses();
                            } else {
                                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, "Gagal menambah kelas (Mungkin ID tidak valid)", Toast.LENGTH_SHORT).show()) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
