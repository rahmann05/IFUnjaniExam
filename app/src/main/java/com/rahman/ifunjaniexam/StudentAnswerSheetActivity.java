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
    private TextView tvStudentName;
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

        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentName.setText(studentName);

        rvAnswers = findViewById(R.id.rvAnswers);
        progressBar = findViewById(R.id.progressBar);

        rvAnswers.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fabEditScore).setOnClickListener(v -> {
            if (currentScore >= 0) showEditScoreDialog(currentScore);
        });

        loadAttemptDetail();
    }

    private double currentScore = -1;

    private void loadAttemptDetail() {
        progressBar.setVisibility(View.VISIBLE);
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/exams/attempts/" + attemptId;

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject attempt = response.getJSONObject("data");
                            JSONObject mahasiswa = attempt.getJSONObject("mahasiswa");
                            JSONArray answers = attempt.getJSONArray("answers");

                            currentScore = attempt.optDouble("score", 0.0);
                            String name = mahasiswa.getString("name");
                            tvStudentName.setText(name + " - Nilai: " + String.format("%.2f", currentScore));

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

    private void showEditScoreDialog(double scoreVal) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nilai Baru (0-100)");
        input.setText(String.valueOf(scoreVal));
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new android.app.AlertDialog.Builder(this)
            .setTitle("Ubah Nilai Akhir")
            .setMessage("Masukkan nilai baru untuk mahasiswa ini.")
            .setView(input)
            .setPositiveButton("Simpan", (dialog, which) -> {
                String val = input.getText().toString();
                if (!val.isEmpty()) updateScore(Double.parseDouble(val));
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void updateScore(double newScore) {
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/exams/attempts/" + attemptId + "/grade";
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        try {
            JSONObject body = new JSONObject();
            body.put("newScore", newScore);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body,
                    response -> {
                        Toast.makeText(this, "Nilai berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        loadAttemptDetail();
                    },
                    error -> Toast.makeText(this, "Gagal memperbarui nilai", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
