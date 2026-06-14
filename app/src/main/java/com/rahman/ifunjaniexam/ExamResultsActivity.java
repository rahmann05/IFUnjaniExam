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

public class ExamResultsActivity extends AppCompatActivity {

    private int examId;
    private String examTitle;
    private RecyclerView rvResults;
    private ProgressBar progressBar;
    private TextView tvEmptyResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_results);

        examId = getIntent().getIntExtra("examId", -1);
        examTitle = getIntent().getStringExtra("examTitle");

        if (examId == -1) {
            Toast.makeText(this, "Ujian tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tvExamTitle);
        tvTitle.setText(examTitle);

        rvResults = findViewById(R.id.rvResults);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyResults = findViewById(R.id.tvEmptyResults);

        rvResults.setLayoutManager(new LinearLayoutManager(this));

        loadResults();
    }

    private void loadResults() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://if-unjani-exam-api.vercel.app/api/exams/" + examId + "/results";

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray results = response.getJSONArray("data");
                            if (results.length() == 0) {
                                tvEmptyResults.setVisibility(View.VISIBLE);
                                rvResults.setVisibility(View.GONE);
                            } else {
                                tvEmptyResults.setVisibility(View.GONE);
                                rvResults.setVisibility(View.VISIBLE);
                                
                                StudentResultAdapter adapter = new StudentResultAdapter(results, attemptObj -> {
                                    try {
                                        int attemptId = attemptObj.getInt("id");
                                        Intent intent = new Intent(ExamResultsActivity.this, StudentAnswerSheetActivity.class);
                                        intent.putExtra("attemptId", attemptId);
                                        intent.putExtra("studentName", attemptObj.getJSONObject("mahasiswa").getString("name"));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                                rvResults.setAdapter(adapter);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat hasil ujian", Toast.LENGTH_SHORT).show();
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
