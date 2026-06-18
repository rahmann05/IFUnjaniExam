package com.rahman.ifunjaniexam.activity.admin;

import com.rahman.ifunjaniexam.network.Config;
import com.rahman.ifunjaniexam.adapters.AdminUserAdapter;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

public class AdminManageUserActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private ProgressBar progressBar;
    private Spinner spRoleFilter;
    private String token;
    private String currentRole = "DOSEN"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_user);

        rvUsers = findViewById(R.id.rvUsers);
        progressBar = findViewById(R.id.progressBar);
        spRoleFilter = findViewById(R.id.spRoleFilter);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddUser);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        token = prefs.getString("jwt_token", "");

        String[] roles = {"DOSEN", "MAHASISWA"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRoleFilter.setAdapter(spinnerAdapter);

        spRoleFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentRole = roles[position];
                loadUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        fabAdd.setOnClickListener(v -> showAddUserDialog());
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/admin/users?role=" + currentRole;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray data = response.getJSONArray("data");
                            AdminUserAdapter adapter = new AdminUserAdapter(data, userObj -> {
                                showDeleteDialog(userObj);
                            });
                            rvUsers.setAdapter(adapter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat pengguna", Toast.LENGTH_SHORT).show();
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

    private void showDeleteDialog(JSONObject user) {
        try {
            int userId = user.getInt("id");
            String username = user.getString("username");

            new AlertDialog.Builder(this)
                .setTitle("Hapus Pengguna")
                .setMessage("Yakin ingin menghapus '" + username + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteUser(userId))
                .setNegativeButton("Batal", null)
                .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteUser(int userId) {
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/admin/users/" + userId;
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                responseStr -> {
                    try {
                        JSONObject response = new JSONObject(responseStr);
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Pengguna berhasil dihapus", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Gagal menghapus pengguna", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void showAddUserDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etName = view.findViewById(R.id.etName);
        EditText etIdentifier = view.findViewById(R.id.etIdentifier);
        
        if (currentRole.equals("DOSEN")) {
            etIdentifier.setHint("NIP");
        } else {
            etIdentifier.setHint("NIM");
        }

        new AlertDialog.Builder(this)
            .setTitle("Tambah " + currentRole)
            .setView(view)
            .setPositiveButton("Simpan", (dialog, which) -> {
                createUser(
                    etUsername.getText().toString(),
                    etPassword.getText().toString(),
                    etName.getText().toString(),
                    etIdentifier.getText().toString()
                );
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void createUser(String username, String password, String name, String identifier) {
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || identifier.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/admin/users";
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);
            body.put("role", currentRole);
            body.put("name", name);
            if (currentRole.equals("DOSEN")) {
                body.put("nip", identifier);
            } else {
                body.put("nim", identifier);
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(this, "Pengguna berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            } else {
                                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, "Gagal menambah pengguna (Mungkin duplikat)", Toast.LENGTH_SHORT).show()) {
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
