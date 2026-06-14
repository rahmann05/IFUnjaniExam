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

public class MahasiswaClassDetailActivity extends AppCompatActivity {

    private TextView tvClassName, tvCourseName, tvSemester, tvDosen, tvEmptyExams;
    private RecyclerView rvExams;
    private ProgressBar progressBar;
    private int classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mahasiswa_class_detail);

        classId = getIntent().getIntExtra("classId", -1);
        if (classId == -1) {
            Toast.makeText(this, "Kelas tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvClassName = findViewById(R.id.tvClassName);
        tvCourseName = findViewById(R.id.tvCourseName);
        tvSemester = findViewById(R.id.tvSemester);
        tvDosen = findViewById(R.id.tvDosen);
        tvEmptyExams = findViewById(R.id.tvEmptyExams);
        rvExams = findViewById(R.id.rvExams);
        progressBar = findViewById(R.id.progressBar);

        rvExams.setLayoutManager(new LinearLayoutManager(this));
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
                            JSONObject dosen = kelas.getJSONObject("dosen");
                            JSONArray exams = kelas.getJSONArray("exams");

                            tvClassName.setText(kelas.getString("name"));
                            tvCourseName.setText(course.getString("name") + " (" + course.getInt("sks") + " SKS)");
                            tvSemester.setText("Semester: " + semester.getString("name"));
                            tvDosen.setText("Dosen: " + dosen.getString("name"));

                            if (exams.length() == 0) {
                                tvEmptyExams.setVisibility(View.VISIBLE);
                                rvExams.setVisibility(View.GONE);
                            } else {
                                tvEmptyExams.setVisibility(View.GONE);
                                rvExams.setVisibility(View.VISIBLE);
                                
                                ExamListAdapter adapter = new ExamListAdapter(exams, false, examObj -> {
                                    try {
                                        String status = examObj.optString("status", "PUBLISHED");
                                        if ("PUBLISHED".equals(status)) {
                                            String startStr = examObj.optString("startTime", "null");
                                            if (!"null".equals(startStr) && !startStr.isEmpty()) {
                                                try {
                                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                                                    java.util.Date startDate = sdf.parse(startStr);
                                                    if (new java.util.Date().before(startDate)) {
                                                        Toast.makeText(MahasiswaClassDetailActivity.this, "Ujian belum dimulai (Terjadwal)", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                } catch (Exception e) {}
                                            } else {
                                                Toast.makeText(MahasiswaClassDetailActivity.this, "Ujian belum dimulai", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        } else if ("FINISHED".equals(status)) {
                                            Toast.makeText(MahasiswaClassDetailActivity.this, "Ujian sudah selesai", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        Intent intent = new Intent(MahasiswaClassDetailActivity.this, TakeExamActivity.class);
                                        intent.putExtra("examId", examObj.getInt("id"));
                                        intent.putExtra("examTitle", examObj.getString("title"));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }, null);
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
}
