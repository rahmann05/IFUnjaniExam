package com.rahman.ifunjaniexam;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import java.util.Calendar;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateExamActivity extends AppCompatActivity {

    private LinearLayout llQuestionsContainer;
    private int classId;
    private List<View> questionViews = new ArrayList<>();
    private View currentImageTargetView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_exam);

        classId = getIntent().getIntExtra("classId", -1);
        if (classId == -1) {
            Toast.makeText(this, "Kelas tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        llQuestionsContainer = findViewById(R.id.llQuestionsContainer);
        findViewById(R.id.btnAddQuestion).setOnClickListener(v -> {
            com.rahman.ifunjaniexam.utils.FeedbackUtils.clickAnim(v);
            addQuestionView();
        });
        findViewById(R.id.btnSaveExam).setOnClickListener(v -> {
            com.rahman.ifunjaniexam.utils.FeedbackUtils.clickAnim(v);
            saveExam();
        });

        android.widget.CheckBox cbManualStart = findViewById(R.id.cbManualStart);
        LinearLayout llTimePicker = findViewById(R.id.llTimePicker);
        cbManualStart.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                llTimePicker.setVisibility(View.GONE);
            } else {
                llTimePicker.setVisibility(View.VISIBLE);
            }
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadImageToUploadcare(imageUri);
                    }
                }
        );

        findViewById(R.id.etStartTime).setOnClickListener(v -> showDateTimePicker((EditText) v));
        findViewById(R.id.etEndTime).setOnClickListener(v -> showDateTimePicker((EditText) v));

        addQuestionView();
    }

    private void showDateTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                calendar.set(year, month, dayOfMonth, hourOfDay, minute);
                String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d", year, month + 1, dayOfMonth, hourOfDay, minute);
                editText.setText(formatted);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void addQuestionView() {
        View qView = LayoutInflater.from(this).inflate(R.layout.item_question_form, llQuestionsContainer, false);
        
        Spinner spinnerType = qView.findViewById(R.id.spinnerType);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Pilihan Ganda", "Essay"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        LinearLayout llOptionsContainer = qView.findViewById(R.id.llOptionsContainer);
        LinearLayout llEssayContainer = qView.findViewById(R.id.llEssayContainer);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // Pilihan Ganda
                    llOptionsContainer.setVisibility(View.VISIBLE);
                    llEssayContainer.setVisibility(View.GONE);
                } else { // Essay
                    llOptionsContainer.setVisibility(View.GONE);
                    llEssayContainer.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup RadioButtons exclusivity manually since they are inside nested LinearLayouts
        RadioButton[] rbOpts = {
                qView.findViewById(R.id.rbA),
                qView.findViewById(R.id.rbB),
                qView.findViewById(R.id.rbC),
                qView.findViewById(R.id.rbD)
        };
        for(RadioButton rb : rbOpts) {
            rb.setOnClickListener(v -> {
                for(RadioButton other : rbOpts) {
                    if(other != rb) other.setChecked(false);
                }
            });
        }

        qView.findViewById(R.id.btnAttachImage).setOnClickListener(v -> {
            currentImageTargetView = qView;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        qView.findViewById(R.id.btnRemoveQuestion).setOnClickListener(v -> {
            llQuestionsContainer.removeView(qView);
            questionViews.remove(qView);
        });

        qView.setTag(""); // store imageUrl

        llQuestionsContainer.addView(qView);
        questionViews.add(qView);
    }

    private void uploadImageToUploadcare(Uri imageUri) {
        if (currentImageTargetView == null) return;
        Toast.makeText(this, "Mengunggah gambar...", Toast.LENGTH_SHORT).show();
        
        com.rahman.ifunjaniexam.network.UploadcareService.uploadImage(this, imageUri, new com.rahman.ifunjaniexam.network.UploadcareService.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    currentImageTargetView.setTag(imageUrl);
                    ImageView iv = currentImageTargetView.findViewById(R.id.ivPreview);
                    iv.setVisibility(View.VISIBLE);
                    com.bumptech.glide.Glide.with(CreateExamActivity.this).load(imageUrl).into(iv);
                    Toast.makeText(CreateExamActivity.this, "Gambar berhasil diunggah", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(CreateExamActivity.this, "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveExam() {
        String title = ((EditText) findViewById(R.id.etExamTitle)).getText().toString();
        String desc = ((EditText) findViewById(R.id.etExamDesc)).getText().toString();
        
        android.widget.CheckBox cbManualStart = findViewById(R.id.cbManualStart);
        boolean isManual = cbManualStart.isChecked();
        
        String start = ((EditText) findViewById(R.id.etStartTime)).getText().toString();
        String end = ((EditText) findViewById(R.id.etEndTime)).getText().toString();
        String durationStr = ((EditText) findViewById(R.id.etDuration)).getText().toString();

        Spinner spCategory = findViewById(R.id.spCategory);
        String category = spCategory.getSelectedItem().toString();
        String weightStr = ((EditText) findViewById(R.id.etExamWeight)).getText().toString();

        if (title.isEmpty() || durationStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi detail ujian", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isManual && (start.isEmpty() || end.isEmpty())) {
            Toast.makeText(this, "Harap isi jadwal waktu ujian", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("title", title);
            payload.put("description", desc);
            payload.put("classId", classId);
            payload.put("category", category);
            payload.put("weight", Double.parseDouble(weightStr));
            if (!isManual) {
                payload.put("startTime", start.replace(" ", "T") + ":00Z");
                payload.put("endTime", end.replace(" ", "T") + ":00Z");
            }
            payload.put("durationMinutes", Integer.parseInt(durationStr));

            JSONArray questionsArray = new JSONArray();
            for (View qView : questionViews) {
                JSONObject qObj = new JSONObject();
                String qText = ((EditText) qView.findViewById(R.id.etQuestionText)).getText().toString();
                String marksStr = ((EditText) qView.findViewById(R.id.etQuestionMarks)).getText().toString();
                int marks = marksStr.isEmpty() ? 10 : Integer.parseInt(marksStr);
                
                String imageUrl = (String) qView.getTag();
                Spinner spinner = qView.findViewById(R.id.spinnerType);
                boolean isEssay = spinner.getSelectedItemPosition() == 1;

                qObj.put("text", qText);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    qObj.put("imageUrl", imageUrl);
                }
                qObj.put("marks", marks);
                qObj.put("type", isEssay ? "ESSAY" : "MULTIPLE_CHOICE");

                if (isEssay) {
                    String ans = ((EditText) qView.findViewById(R.id.etCorrectEssay)).getText().toString();
                    qObj.put("correctEssayAnswer", ans);
                } else {
                    JSONArray optionsArr = new JSONArray();
                    EditText[] etOpts = {
                            qView.findViewById(R.id.etOptA),
                            qView.findViewById(R.id.etOptB),
                            qView.findViewById(R.id.etOptC),
                            qView.findViewById(R.id.etOptD)
                    };
                    RadioButton[] rbOpts = {
                            qView.findViewById(R.id.rbA),
                            qView.findViewById(R.id.rbB),
                            qView.findViewById(R.id.rbC),
                            qView.findViewById(R.id.rbD)
                    };
                    for (int i=0; i<4; i++) {
                        JSONObject o = new JSONObject();
                        o.put("text", etOpts[i].getText().toString());
                        o.put("isCorrect", rbOpts[i].isChecked());
                        optionsArr.put(o);
                    }
                    qObj.put("options", optionsArr);
                }
                questionsArray.put(qObj);
            }
            payload.put("questions", questionsArray);

            findViewById(R.id.btnSaveExam).setEnabled(false);
            com.rahman.ifunjaniexam.network.ExamApiService.createExam(this, payload, new com.rahman.ifunjaniexam.network.ExamApiService.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Toast.makeText(CreateExamActivity.this, "Ujian berhasil disimpan!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(CreateExamActivity.this, "Gagal menyimpan ujian", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.btnSaveExam).setEnabled(true);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
