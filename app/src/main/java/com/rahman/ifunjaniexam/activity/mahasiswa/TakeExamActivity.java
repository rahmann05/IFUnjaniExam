package com.rahman.ifunjaniexam.activity.mahasiswa;

import com.rahman.ifunjaniexam.R;

import com.rahman.ifunjaniexam.utils.FeedbackUtils;
import com.rahman.ifunjaniexam.network.Config;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class TakeExamActivity extends AppCompatActivity {

    private FrameLayout flQuestionContainer;
    private LinearLayout llGridNav;
    private ProgressBar progressBar;
    private TextView tvExamTitleHeader, tvTimer;
    private Button btnPrev, btnNext, btnSubmitExam;
    
    private int examId;
    private int currentQuestionIndex = 0;
    
    private List<View> questionViews = new ArrayList<>();
    private List<JSONObject> questionDataList = new ArrayList<>();
    private List<Button> gridButtons = new ArrayList<>();
    
    private int warningCount = 0;
    private boolean isSubmitted = false;
    private CountDownTimer countDownTimer;
    
    private android.os.Handler statusCheckHandler = new android.os.Handler();
    private Runnable statusCheckRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_take_exam);

        examId = getIntent().getIntExtra("examId", -1);
        String examTitle = getIntent().getStringExtra("examTitle");

        if (examId == -1) {
            Toast.makeText(this, "Ujian tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        flQuestionContainer = findViewById(R.id.flQuestionContainer);
        llGridNav = findViewById(R.id.llGridNav);
        progressBar = findViewById(R.id.progressBar);
        tvExamTitleHeader = findViewById(R.id.tvExamTitleHeader);
        tvTimer = findViewById(R.id.tvTimer);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnSubmitExam = findViewById(R.id.btnSubmitExam);

        if (examTitle != null) {
            tvExamTitleHeader.setText(examTitle);
        }

        btnPrev.setOnClickListener(v -> {
            com.rahman.ifunjaniexam.utils.FeedbackUtils.clickAnim(v);
            navigateToQuestion(currentQuestionIndex - 1);
        });
        btnNext.setOnClickListener(v -> {
            com.rahman.ifunjaniexam.utils.FeedbackUtils.clickAnim(v);
            navigateToQuestion(currentQuestionIndex + 1);
        });
        
        btnSubmitExam.setOnClickListener(v -> {
            com.rahman.ifunjaniexam.utils.FeedbackUtils.clickAnim(v);
            showSubmitDialog();
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(TakeExamActivity.this, "Tidak diperbolehkan kembali selama ujian berlangsung!", Toast.LENGTH_SHORT).show();
            }
        });

        loadQuestions();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (!isSubmitted && !isFinishing()) {
            warningCount++;
            if (warningCount >= 3) {
                Toast.makeText(this, "Pelanggaran maksimal! Ujian otomatis dikumpulkan.", Toast.LENGTH_LONG).show();
                submitExam();
            } else {
                Toast.makeText(this, "Peringatan " + warningCount + "/3: Jangan keluar aplikasi saat ujian!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (statusCheckHandler != null && statusCheckRunnable != null) {
            statusCheckHandler.removeCallbacks(statusCheckRunnable);
        }
    }

    private void showSubmitDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Kumpulkan Ujian")
            .setMessage("Apakah Anda yakin ingin mengumpulkan ujian sekarang? Anda tidak bisa mengubah jawaban setelah ini.")
            .setPositiveButton("Ya, Kumpulkan", (dialog, which) -> submitExam())
            .setNegativeButton("Batal", null)
            .show();
    }

    private void loadQuestions() {
        progressBar.setVisibility(View.VISIBLE);
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/exams/" + examId + "/questions";

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject examObj = response.getJSONObject("data");
                            JSONArray questions = examObj.getJSONArray("questions");
                            // Clear lists to prevent duplicate questions bug
                            flQuestionContainer.removeAllViews();
                            llGridNav.removeAllViews();
                            questionViews.clear();
                            questionDataList.clear();
                            gridButtons.clear();

                            String endTimeStr = examObj.getString("endTime");
                            int durationMin = examObj.getInt("durationMinutes");
                            setupTimer(endTimeStr, durationMin);

                            for (int i = 0; i < questions.length(); i++) {
                                JSONObject qObj = questions.getJSONObject(i);
                                addQuestionToUI(qObj, i);
                            }
                            
                            if (questions.length() > 0) {
                                navigateToQuestion(0);
                            }
                            
                            startStatusChecker();
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

    private void setupTimer(String endTimeStr, int durationMin) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date end = format.parse(endTimeStr);
            long now = System.currentTimeMillis();
            
            long timeLeft = end.getTime() - now;
            long maxDuration = durationMin * 60 * 1000L;
            if (timeLeft > maxDuration) timeLeft = maxDuration;
            
            if (timeLeft <= 0) {
                Toast.makeText(this, "Waktu ujian sudah habis!", Toast.LENGTH_LONG).show();
                submitExam();
                return;
            }

            countDownTimer = new CountDownTimer(timeLeft, 1000) {
                public void onTick(long millisUntilFinished) {
                    long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                    long mins = (millisUntilFinished / (1000 * 60)) % 60;
                    long secs = (millisUntilFinished / 1000) % 60;
                    tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, mins, secs));
                }

                public void onFinish() {
                    tvTimer.setText("00:00:00");
                    Toast.makeText(TakeExamActivity.this, "Waktu habis! Mengumpulkan otomatis...", Toast.LENGTH_LONG).show();
                    submitExam();
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startStatusChecker() {
        statusCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSubmitted) return;
                checkExamStatus();
                statusCheckHandler.postDelayed(this, 15000); // Check every 15 seconds
            }
        };
        statusCheckHandler.postDelayed(statusCheckRunnable, 15000);
    }
    
    private void checkExamStatus() {
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/exams/" + examId + "/status";
        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject data = response.getJSONObject("data");
                            String status = data.optString("status", "PUBLISHED");
                            if ("FINISHED".equals(status)) {
                                Toast.makeText(TakeExamActivity.this, "Dosen telah menghentikan ujian ini. Mengumpulkan otomatis...", Toast.LENGTH_LONG).show();
                                submitExam();
                            }
                        }
                    } catch (Exception e) {}
                },
                error -> {}) {
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
        View qView = LayoutInflater.from(this).inflate(R.layout.item_take_question, flQuestionContainer, false);
        
        TextView tvNumber = qView.findViewById(R.id.tvQuestionNumber);
        TextView tvText = qView.findViewById(R.id.tvQuestionText);
        ImageView ivImage = qView.findViewById(R.id.ivQuestionImage);
        RadioGroup rgOptions = qView.findViewById(R.id.rgOptions);
        LinearLayout llEssay = qView.findViewById(R.id.llEssay);
        EditText etEssay = qView.findViewById(R.id.etEssayAnswer);

        tvNumber.setText("Soal " + (index + 1));
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
            etEssay.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateGridColor(index); }
                @Override public void afterTextChanged(Editable s) {}
            });
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
                    rbs[j].setChecked(false); // Default unchecked
                } else {
                    View parentCard = (View) rbs[j].getParent();
                    if (parentCard != null) parentCard.setVisibility(View.GONE);
                }
            }

            // Manual exclusivity and event trigger since they are inside MaterialCardViews
            for (RadioButton rb : rbs) {
                rb.setOnClickListener(v -> {
                    for (RadioButton other : rbs) {
                        if (other != rb) other.setChecked(false);
                    }
                    updateGridColor(index);
                });
                
                // Allow clicking the card itself
                View parentCard = (View) rb.getParent();
                if (parentCard != null) {
                    parentCard.setOnClickListener(v -> {
                        rb.setChecked(true);
                        for (RadioButton other : rbs) {
                            if (other != rb) other.setChecked(false);
                        }
                        updateGridColor(index);
                    });
                }
            }
        }

        qView.setVisibility(View.GONE);
        flQuestionContainer.addView(qView);
        questionViews.add(qView);
        questionDataList.add(qObj);

        // Add to Grid Nav
        Button gridBtn = new Button(this);
        gridBtn.setText(String.valueOf(index + 1));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(120, 120);
        lp.setMargins(8, 0, 8, 0);
        gridBtn.setLayoutParams(lp);
        gridBtn.setBackgroundColor(Color.parseColor("#bdc3c7")); // Default grey
        gridBtn.setTextColor(Color.WHITE);
        gridBtn.setOnClickListener(v -> {
            com.rahman.ifunjaniexam.utils.FeedbackUtils.clickAnim(v);
            navigateToQuestion(index);
        });
        
        llGridNav.addView(gridBtn);
        gridButtons.add(gridBtn);
    }

    private void updateGridColor(int index) {
        View qView = questionViews.get(index);
        JSONObject qData = questionDataList.get(index);
        String type = qData.optString("type", "MULTIPLE_CHOICE");
        boolean answered = false;
        
        if ("ESSAY".equals(type)) {
            EditText etEssay = qView.findViewById(R.id.etEssayAnswer);
            answered = !etEssay.getText().toString().trim().isEmpty();
        } else {
            RadioButton[] rbs = {
                    qView.findViewById(R.id.rbOptA),
                    qView.findViewById(R.id.rbOptB),
                    qView.findViewById(R.id.rbOptC),
                    qView.findViewById(R.id.rbOptD)
            };
            for (RadioButton rb : rbs) {
                if (rb.isChecked()) {
                    answered = true;
                    break;
                }
            }
        }

        if (index == currentQuestionIndex) {
            gridButtons.get(index).setBackgroundColor(Color.parseColor("#3498db")); // Active blue
        } else {
            gridButtons.get(index).setBackgroundColor(answered ? Color.parseColor("#2ecc71") : Color.parseColor("#bdc3c7")); // Green or grey
        }
    }

    private void navigateToQuestion(int index) {
        if (index < 0 || index >= questionViews.size()) return;
        
        int oldIndex = currentQuestionIndex;
        currentQuestionIndex = index;

        // Hide all, show current
        for (int i = 0; i < questionViews.size(); i++) {
            questionViews.get(i).setVisibility(i == currentQuestionIndex ? View.VISIBLE : View.GONE);
            updateGridColor(i);
        }

        btnPrev.setEnabled(currentQuestionIndex > 0);
        
        if (currentQuestionIndex == questionViews.size() - 1) {
            btnNext.setVisibility(View.GONE);
            btnSubmitExam.setVisibility(View.VISIBLE);
        } else {
            btnNext.setVisibility(View.VISIBLE);
            btnSubmitExam.setVisibility(View.GONE);
        }
    }

    private void submitExam() {
        if (isSubmitted) return;
        isSubmitted = true;
        if (countDownTimer != null) countDownTimer.cancel();

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
                    if (!studentAns.isEmpty() && studentAns.equalsIgnoreCase(correctAns)) {
                        score += marks;
                    }
                } else {
                    RadioButton[] rbs = {
                            qView.findViewById(R.id.rbOptA),
                            qView.findViewById(R.id.rbOptB),
                            qView.findViewById(R.id.rbOptC),
                            qView.findViewById(R.id.rbOptD)
                    };
                    
                    RadioButton checkedRb = null;
                    for (RadioButton rb : rbs) {
                        if (rb.isChecked()) {
                            checkedRb = rb;
                            break;
                        }
                    }
                    
                    if (checkedRb != null) {
                        int selectedOptionId = (int) checkedRb.getTag();
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
            isSubmitted = false;
        }
    }

    private void sendResultsToServer(double score, JSONArray answers) {
        progressBar.setVisibility(View.VISIBLE);
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/exams/" + examId + "/submit";

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
                    double actualScore = score;
                    try {
                        if (response.has("data") && !response.isNull("data")) {
                            actualScore = response.getJSONObject("data").optDouble("score", score);
                        }
                    } catch (Exception e) {}
                    com.rahman.ifunjaniexam.utils.FeedbackUtils.showToast(this, "Ujian selesai! Skor Anda: " + String.format("%.2f", actualScore));
                    finish();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    // Tangani 409: Sudah dikumpulkan
                    if (error.networkResponse != null && error.networkResponse.statusCode == 409) {
                        try {
                            String body = new String(error.networkResponse.data, "utf-8");
                            org.json.JSONObject json = new org.json.JSONObject(body);
                            double prevScore = json.optJSONObject("data") != null
                                    ? json.optJSONObject("data").optDouble("score", score)
                                    : score;
                            com.rahman.ifunjaniexam.utils.FeedbackUtils.showToast(this,
                                    "Ujian sudah dikumpulkan sebelumnya. Skor: " + String.format("%.2f", prevScore));
                        } catch (Exception ignored) {
                            com.rahman.ifunjaniexam.utils.FeedbackUtils.showToast(this, "Ujian sudah dikumpulkan sebelumnya.");
                        }
                        finish();
                        return;
                    }
                    com.rahman.ifunjaniexam.utils.FeedbackUtils.showToast(this, "Gagal mengirim jawaban. Coba lagi.");
                    isSubmitted = false;
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                15000, // 15 seconds timeout
                1,     // 1 retry
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(request);
    }
}
