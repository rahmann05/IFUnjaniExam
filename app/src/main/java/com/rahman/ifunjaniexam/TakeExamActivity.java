package com.rahman.ifunjaniexam;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakeExamActivity extends AppCompatActivity {

    private LinearLayout llQuestionsContainer;
    private ProgressBar progressBar;
    private TextView tvExamTitleHeader;
    private int examId;

    private List<View> questionViews = new ArrayList<>();
    private List<JSONObject> questionDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_exam);

        examId = getIntent().getIntExtra("examId", -1);
        String examTitle = getIntent().getStringExtra("examTitle");

        if (examId == -1) {
            Toast.makeText(this, "Ujian tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        llQuestionsContainer = findViewById(R.id.llQuestionsContainer);
        progressBar = findViewById(R.id.progressBar);
        tvExamTitleHeader = findViewById(R.id.tvExamTitleHeader);

        if (examTitle != null) {
            tvExamTitleHeader.setText(examTitle);
        }

        findViewById(R.id.btnSubmitExam).setOnClickListener(v -> submitExam());

        loadQuestions();
    }

    private void loadQuestions() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://if-unjani-exam-api.vercel.app/api/exams/" + examId + "/questions";

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject examObj = response.getJSONObject("data");
                            JSONArray questions = examObj.getJSONArray("questions");
                            
                            for (int i = 0; i < questions.length(); i++) {
                                JSONObject qObj = questions.getJSONObject(i);
                                addQuestionToUI(qObj, i + 1);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Format data salah", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat soal", Toast.LENGTH_SHORT).show();
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

    private void addQuestionToUI(JSONObject qObj, int index) throws Exception {
        View qView = LayoutInflater.from(this).inflate(R.layout.item_take_question, llQuestionsContainer, false);
        
        TextView tvNumber = qView.findViewById(R.id.tvQuestionNumber);
        TextView tvText = qView.findViewById(R.id.tvQuestionText);
        ImageView ivImage = qView.findViewById(R.id.ivQuestionImage);
        RadioGroup rgOptions = qView.findViewById(R.id.rgOptions);
        LinearLayout llEssay = qView.findViewById(R.id.llEssay);

        tvNumber.setText("Soal " + index);
        tvText.setText(qObj.getString("text"));

        String imageUrl = qObj.optString("imageUrl", null);
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
            ivImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).into(ivImage);
        }

        String type = qObj.optString("type", "MULTIPLE_CHOICE");
        if ("ESSAY".equals(type)) {
            rgOptions.setVisibility(View.GONE);
            llEssay.setVisibility(View.VISIBLE);
        } else {
            rgOptions.setVisibility(View.VISIBLE);
            llEssay.setVisibility(View.GONE);

            JSONArray options = qObj.getJSONArray("options");
            RadioButton[] rbs = {
                    qView.findViewById(R.id.rbOptA),
                    qView.findViewById(R.id.rbOptB),
                    qView.findViewById(R.id.rbOptC),
                    qView.findViewById(R.id.rbOptD)
            };

            for (int j = 0; j < 4; j++) {
                if (j < options.length()) {
                    JSONObject optObj = options.getJSONObject(j);
                    rbs[j].setText(optObj.getString("text"));
                    rbs[j].setTag(optObj.getInt("id"));
                } else {
                    rbs[j].setVisibility(View.GONE);
                }
            }
        }

        llQuestionsContainer.addView(qView);
        questionViews.add(qView);
        questionDataList.add(qObj);
    }

    private void submitExam() {
        try {
            int score = 0;
            int totalMarks = 0;

            JSONArray answersArray = new JSONArray();

            for (int i = 0; i < questionViews.size(); i++) {
                View qView = questionViews.get(i);
                JSONObject qData = questionDataList.get(i);

                int questionId = qData.getInt("id");
                int marks = qData.optInt("marks", 1);
                totalMarks += marks;

                String type = qData.optString("type", "MULTIPLE_CHOICE");
                JSONObject answerObj = new JSONObject();
                answerObj.put("questionId", questionId);

                if ("ESSAY".equals(type)) {
                    EditText etEssay = qView.findViewById(R.id.etEssayAnswer);
                    String studentAns = etEssay.getText().toString().trim();
                    answerObj.put("essayAnswer", studentAns);

                    String correctAns = qData.optString("correctEssayAnswer", "").trim();
                    if (studentAns.equalsIgnoreCase(correctAns)) {
                        score += marks;
                    }
                } else {
                    RadioGroup rg = qView.findViewById(R.id.rgOptions);
                    int checkedId = rg.getCheckedRadioButtonId();
                    
                    if (checkedId != -1) {
                        RadioButton rb = qView.findViewById(checkedId);
                        int selectedOptionId = (int) rb.getTag();
                        answerObj.put("selectedOptionId", selectedOptionId);

                        JSONArray options = qData.getJSONArray("options");
                        for (int j = 0; j < options.length(); j++) {
                            JSONObject opt = options.getJSONObject(j);
                            if (opt.getInt("id") == selectedOptionId && opt.optBoolean("isCorrect", false)) {
                                score += marks;
                                break;
                            }
                        }
                    }
                }
                answersArray.put(answerObj);
            }

            double finalScore = totalMarks > 0 ? ((double) score / totalMarks) * 100.0 : 0.0;

            sendResultsToServer(finalScore, answersArray);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menghitung skor", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendResultsToServer(double score, JSONArray answers) {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://if-unjani-exam-api.vercel.app/api/exams/" + examId + "/submit";

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JSONObject payload = new JSONObject();
        try {
            payload.put("score", score);
            payload.put("answers", answers);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ujian selesai! Skor Anda: " + String.format("%.2f", score), Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal mengirim jawaban", Toast.LENGTH_SHORT).show();
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
