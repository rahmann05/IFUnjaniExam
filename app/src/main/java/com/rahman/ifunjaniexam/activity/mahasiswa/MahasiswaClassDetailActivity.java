package com.rahman.ifunjaniexam.activity.mahasiswa;

import com.rahman.ifunjaniexam.adapters.ExamListAdapter;
import com.rahman.ifunjaniexam.activity.dosen.StudentAnswerSheetActivity;
import com.rahman.ifunjaniexam.network.Config;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private TextView tvClassName, tvCourseName, tvSemester, tvDosen;
    private TextView tvEmptyExams, tvEmptyHistory, tvEmptyStudents;
    private RecyclerView rvExams, rvExamHistory, rvStudents;
    private ProgressBar progressBar;
    private View scrollContent;
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

        tvClassName     = findViewById(R.id.tvClassName);
        tvCourseName    = findViewById(R.id.tvCourseName);
        tvSemester      = findViewById(R.id.tvSemester);
        tvDosen         = findViewById(R.id.tvDosen);
        tvEmptyExams    = findViewById(R.id.tvEmptyExams);
        tvEmptyHistory  = findViewById(R.id.tvEmptyHistory);
        tvEmptyStudents = findViewById(R.id.tvEmptyStudents);
        rvExams         = findViewById(R.id.rvExams);
        rvExamHistory   = findViewById(R.id.rvExamHistory);
        rvStudents      = findViewById(R.id.rvStudents);
        progressBar     = findViewById(R.id.progressBar);
        scrollContent   = findViewById(R.id.scrollContent);

        rvExams.setLayoutManager(new LinearLayoutManager(this));
        rvExamHistory.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        // Back button
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClassDetails();
    }

    private void loadClassDetails() {
        progressBar.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/kelas/" + classId;

        SharedPreferences prefs = getSharedPreferences("AUTH_PREF", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    scrollContent.setVisibility(View.VISIBLE);
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject kelas    = response.getJSONObject("data");
                            JSONObject course   = kelas.getJSONObject("course");
                            JSONObject semester = kelas.getJSONObject("semester");
                            JSONObject dosen    = kelas.getJSONObject("dosen");
                            JSONArray  exams    = kelas.optJSONArray("exams");
                            JSONArray  mahasiswaList = kelas.optJSONArray("mahasiswa");

                            tvClassName.setText(kelas.getString("name") + " (" + kelas.optString("code", "") + ")");
                            tvCourseName.setText(course.getString("name") + " (" + course.getInt("sks") + " SKS)");
                            tvSemester.setText("Semester: " + semester.getString("name"));
                            tvDosen.setText("Dosen: " + dosen.getString("name"));

                            // ── Split exams into active and history ──────────────
                            JSONArray activeExams  = new JSONArray();
                            JSONArray historyExams = new JSONArray();

                            if (exams != null) {
                                for (int i = 0; i < exams.length(); i++) {
                                    JSONObject exam = exams.getJSONObject(i);
                                    String status = exam.optString("status", "DRAFT");
                                    if ("FINISHED".equals(status)) {
                                        historyExams.put(exam);
                                    } else if (!"DRAFT".equals(status)) {
                                        activeExams.put(exam);
                                    }
                                }
                            }

                            // Active exams list
                            if (activeExams.length() == 0) {
                                tvEmptyExams.setVisibility(View.VISIBLE);
                                rvExams.setVisibility(View.GONE);
                            } else {
                                tvEmptyExams.setVisibility(View.GONE);
                                rvExams.setVisibility(View.VISIBLE);
                                rvExams.setAdapter(buildExamAdapter(activeExams));
                            }

                            // History exams list
                            if (historyExams.length() == 0) {
                                tvEmptyHistory.setVisibility(View.VISIBLE);
                                rvExamHistory.setVisibility(View.GONE);
                            } else {
                                tvEmptyHistory.setVisibility(View.GONE);
                                rvExamHistory.setVisibility(View.VISIBLE);
                                rvExamHistory.setAdapter(buildExamAdapter(historyExams));
                            }

                            // ── Student list ──────────────────────────────────────
                            if (mahasiswaList == null || mahasiswaList.length() == 0) {
                                tvEmptyStudents.setVisibility(View.VISIBLE);
                                rvStudents.setVisibility(View.GONE);
                            } else {
                                tvEmptyStudents.setVisibility(View.GONE);
                                rvStudents.setVisibility(View.VISIBLE);
                                rvStudents.setAdapter(new StudentClassAdapter(mahasiswaList));
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

    /** Build an ExamListAdapter for a given exam array with click logic. */
    private ExamListAdapter buildExamAdapter(JSONArray exams) {
        return new ExamListAdapter(exams, false, examObj -> {
            try {
                JSONArray attempts = examObj.optJSONArray("attempts");
                if (attempts != null && attempts.length() > 0) {
                    JSONObject attempt = attempts.getJSONObject(0);
                    int attemptId = attempt.getInt("id");
                    String username = getSharedPreferences("AUTH_PREF", MODE_PRIVATE)
                            .getString("username", "Mahasiswa");
                    Intent intent = new Intent(MahasiswaClassDetailActivity.this, StudentAnswerSheetActivity.class);
                    intent.putExtra("attemptId", attemptId);
                    intent.putExtra("studentName", username);
                    startActivity(intent);
                    return;
                }

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
                        } catch (Exception ignored) {}
                    } else {
                        Toast.makeText(MahasiswaClassDetailActivity.this, "Ujian belum dimulai", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else if ("FINISHED".equals(status)) {
                    Toast.makeText(MahasiswaClassDetailActivity.this, "Ujian sudah selesai (belum diikuti)", Toast.LENGTH_SHORT).show();
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
    }

    // ── Inner Adapter: Student list ───────────────────────────────────────────
    private static class StudentClassAdapter extends RecyclerView.Adapter<StudentClassAdapter.VH> {
        private final JSONArray data;
        StudentClassAdapter(JSONArray data) { this.data = data; }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_student_class, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            try {
                // The mahasiswa array is: [{ mahasiswa: { name, nim } }]
                JSONObject entry = data.getJSONObject(position);
                JSONObject mhs = entry.optJSONObject("mahasiswa");
                if (mhs != null) {
                    holder.tvName.setText(mhs.optString("name", "-"));
                    holder.tvNim.setText("NIM: " + mhs.optString("nim", "-"));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        @Override public int getItemCount() { return data != null ? data.length() : 0; }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvNim;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvStudentName);
                tvNim  = v.findViewById(R.id.tvStudentNim);
            }
        }
    }
}
