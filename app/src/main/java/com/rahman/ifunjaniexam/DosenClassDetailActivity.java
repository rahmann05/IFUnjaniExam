package com.rahman.ifunjaniexam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DosenClassDetailActivity extends AppCompatActivity {

    private TextView tvClassName, tvCourseName, tvSemester, tvStudentCount, tvEmptyExams;
    private RecyclerView rvExams;
    private ProgressBar progressBar;
    private int classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dosen_class_detail);

        classId = getIntent().getIntExtra("classId", -1);
        if (classId == -1) {
            Toast.makeText(this, "Kelas tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvClassName = findViewById(R.id.tvClassName);
        tvCourseName = findViewById(R.id.tvCourseName);
        tvSemester = findViewById(R.id.tvSemester);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        tvEmptyExams = findViewById(R.id.tvEmptyExams);
        rvExams = findViewById(R.id.rvExams);
        progressBar = findViewById(R.id.progressBar);

        rvExams.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnCreateExam).setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateExamActivity.class);
            intent.putExtra("classId", classId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClassDetails();
    }

    private void loadClassDetails() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://if-unjani-exam-api.vercel.app/api/kelas/" + classId;

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject kelas = response.getJSONObject("data");
                            JSONObject course = kelas.getJSONObject("course");
                            JSONObject semester = kelas.getJSONObject("semester");
                            JSONArray mahasiswa = kelas.getJSONArray("mahasiswa");
                            JSONArray exams = kelas.getJSONArray("exams");

                            tvClassName.setText(kelas.getString("name"));
                            tvCourseName.setText(course.getString("name") + " (" + course.getInt("sks") + " SKS)");
                            tvSemester.setText(semester.getString("name"));
                            tvStudentCount.setText(mahasiswa.length() + " Mahasiswa Terdaftar");

                            if (exams.length() == 0) {
                                tvEmptyExams.setVisibility(View.VISIBLE);
                                rvExams.setVisibility(View.GONE);
                            } else {
                                tvEmptyExams.setVisibility(View.GONE);
                                rvExams.setVisibility(View.VISIBLE);
                                
                                ExamListAdapter adapter = new ExamListAdapter(exams, true, examObj -> {
                                    try {
                                        Intent intent = new Intent(DosenClassDetailActivity.this, ExamResultsActivity.class);
                                        intent.putExtra("examId", examObj.getInt("id"));
                                        intent.putExtra("examTitle", examObj.getString("title"));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }, new ExamListAdapter.OnOptionClickListener() {
                                    @Override
                                    public void onEditClick(JSONObject exam) {
                                        showConfirmDialog(exam, "EDIT");
                                    }

                                    @Override
                                    public void onDeleteClick(JSONObject exam) {
                                        showConfirmDialog(exam, "DELETE");
                                    }

                                    @Override
                                    public void onUpdateStatusClick(JSONObject exam, String newStatus) {
                                        updateExamStatus(exam, newStatus);
                                    }
                                });
                                rvExams.setAdapter(adapter);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat detail kelas", Toast.LENGTH_SHORT).show();
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
    
    private void showConfirmDialog(JSONObject exam, String action) {
        try {
            int examId = exam.getInt("id");
            String title = exam.getString("title");
            String message = action.equals("DELETE") 
                ? "Apakah Anda yakin ingin menghapus ujian '" + title + "'?"
                : "Apakah Anda yakin ingin mengedit ujian '" + title + "'?";

            new android.app.AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage(message)
                .setPositiveButton("Ya", (dialog, which) -> {
                    if (action.equals("DELETE")) {
                        performDelete(examId, title);
                    } else {
                        Toast.makeText(this, "Fitur edit masih dikembangkan", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performDelete(int examId, String examTitle) {
        String url = "https://if-unjani-exam-api.vercel.app/api/exams/" + examId;
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(Request.Method.DELETE, url,
                responseStr -> {
                    try {
                        JSONObject response = new JSONObject(responseStr);
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Ujian berhasil dihapus", Toast.LENGTH_SHORT).show();
                            loadClassDetails(); // Reload
                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
                        try {
                            String res = new String(error.networkResponse.data, "utf-8");
                            JSONObject json = new JSONObject(res);
                            if (json.optBoolean("requiresApproval", false)) {
                                showRequestApprovalDialog(examId, examTitle, "DELETE");
                            } else {
                                Toast.makeText(this, "Gagal: " + json.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "Gagal menghapus ujian", Toast.LENGTH_SHORT).show();
                    }
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

    private void showRequestApprovalDialog(int examId, String examTitle, String requestType) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Persetujuan Dibutuhkan")
            .setMessage("Ujian '" + examTitle + "' sudah dikerjakan oleh mahasiswa. Anda perlu meminta persetujuan Admin untuk melanjutkan tindakan ini. Kirim permintaan?")
            .setPositiveButton("Kirim", (dialog, which) -> {
                sendApprovalRequest(examId, requestType);
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void sendApprovalRequest(int examId, String requestType) {
        String url = "https://if-unjani-exam-api.vercel.app/api/exams/" + examId + "/request-approval";
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        try {
            JSONObject body = new JSONObject();
            body.put("requestType", requestType);
            body.put("reason", "Meminta izin dari sistem");

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(this, "Permintaan berhasil dikirim ke Admin", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {}
                    },
                    error -> {
                        Toast.makeText(this, "Gagal mengirim permintaan", Toast.LENGTH_SHORT).show();
                    }) {
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

    private void updateExamStatus(JSONObject exam, String newStatus) {
        try {
            int examId = exam.getInt("id");
            String url = "https://if-unjani-exam-api.vercel.app/api/exams/" + examId + "/status";
            SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
            String token = prefs.getString("jwt_token", "");

            JSONObject body = new JSONObject();
            body.put("status", newStatus);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(this, "Status ujian diperbarui menjadi " + newStatus, Toast.LENGTH_SHORT).show();
                                loadClassDetails(); // Reload
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Gagal memperbarui status ujian", Toast.LENGTH_SHORT).show();
                    }) {
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
