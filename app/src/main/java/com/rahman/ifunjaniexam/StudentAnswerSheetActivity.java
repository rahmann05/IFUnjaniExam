package com.rahman.ifunjaniexam;

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

public class StudentAnswerSheetActivity extends AppCompatActivity {

    private int attemptId;
    private String studentName;
    private RecyclerView rvAnswers;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_answer_sheet);

        attemptId = getIntent().getIntExtra("attemptId", -1);
        studentName = getIntent().getStringExtra("studentName");

        if (attemptId == -1) {
            Toast.makeText(this, "Data tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvName = findViewById(R.id.tvStudentName);
        tvName.setText(studentName);

        rvAnswers = findViewById(R.id.rvAnswers);
        progressBar = findViewById(R.id.progressBar);

        rvAnswers.setLayoutManager(new LinearLayoutManager(this));

        loadAttemptDetails();
    }

    private void loadAttemptDetails() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://if-unjani-exam-api.vercel.app/api/exams/attempts/" + attemptId;

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject attempt = response.getJSONObject("data");
                            JSONArray answers = attempt.getJSONArray("answers");
                            
                            StudentAnswerAdapter adapter = new StudentAnswerAdapter(answers);
                            rvAnswers.setAdapter(adapter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal memproses data lembar jawaban", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat lembar jawaban", Toast.LENGTH_SHORT).show();
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
