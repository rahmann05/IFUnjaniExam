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

        rvCompleted = findViewById(R.id.rvCompleted);
        rvNotCompleted = findViewById(R.id.rvNotCompleted);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyCompleted = findViewById(R.id.tvEmptyCompleted);
        tvEmptyNotCompleted = findViewById(R.id.tvEmptyNotCompleted);

        rvCompleted.setLayoutManager(new LinearLayoutManager(this));
        rvNotCompleted.setLayoutManager(new LinearLayoutManager(this));

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadResults();
    }

    private RecyclerView rvCompleted, rvNotCompleted;
    private TextView tvEmptyCompleted, tvEmptyNotCompleted;

    private void loadResults() {
        progressBar.setVisibility(View.VISIBLE);
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/exams/" + examId + "/results";

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject data = response.getJSONObject("data");
                            JSONArray completed = data.getJSONArray("completed");
                            JSONArray notCompleted = data.getJSONArray("notCompleted");
                            
                            if (completed.length() == 0) {
                                tvEmptyCompleted.setVisibility(View.VISIBLE);
                                rvCompleted.setVisibility(View.GONE);
                            } else {
                                tvEmptyCompleted.setVisibility(View.GONE);
                                rvCompleted.setVisibility(View.VISIBLE);
                                
                                StudentResultAdapter adapter = new StudentResultAdapter(completed, attemptObj -> {
                                    try {
                                        int attemptId = attemptObj.getInt("id");
                                        Intent intent = new Intent(ExamResultsActivity.this, StudentAnswerSheetActivity.class);
                                        intent.putExtra("attemptId", attemptId);
                                        // fetch from student object instead of nested
                                        // Wait, adapter listener returns attemptObj, let's pass name through intent if we want, or remove it
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                                rvCompleted.setAdapter(adapter);
                            }
                            
                            if (notCompleted.length() == 0) {
                                tvEmptyNotCompleted.setVisibility(View.VISIBLE);
                                rvNotCompleted.setVisibility(View.GONE);
                            } else {
                                tvEmptyNotCompleted.setVisibility(View.GONE);
                                rvNotCompleted.setVisibility(View.VISIBLE);
                                
                                StudentResultAdapter adapterNot = new StudentResultAdapter(notCompleted, null);
                                rvNotCompleted.setAdapter(adapterNot);
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
