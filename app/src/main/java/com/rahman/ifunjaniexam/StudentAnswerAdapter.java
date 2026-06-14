package com.rahman.ifunjaniexam;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

public class StudentAnswerAdapter extends RecyclerView.Adapter<StudentAnswerAdapter.ViewHolder> {

    private JSONArray answers;

    public StudentAnswerAdapter(JSONArray answers) {
        this.answers = answers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_answer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject answerObj = answers.getJSONObject(position);
            JSONObject question = answerObj.getJSONObject("question");
            
            String qText = question.getString("text");
            int marks = question.optInt("marks", 10);
            String type = question.getString("type");
            String imageUrl = question.optString("imageUrl", "");

            holder.tvQuestionText.setText((position + 1) + ". " + qText);

            if (!imageUrl.isEmpty() && !imageUrl.equals("null")) {
                holder.ivQuestionImage.setVisibility(View.VISIBLE);
                Glide.with(holder.itemView.getContext()).load(imageUrl).into(holder.ivQuestionImage);
            } else {
                holder.ivQuestionImage.setVisibility(View.GONE);
            }

            boolean isCorrect = false;
            String studentAnswerText = "";
            String correctAnswerText = "";

            if (type.equals("MULTIPLE_CHOICE")) {
                if (!answerObj.isNull("selectedOptionId") && answerObj.has("selectedOption") && !answerObj.isNull("selectedOption")) {
                    JSONObject selectedOpt = answerObj.getJSONObject("selectedOption");
                    studentAnswerText = selectedOpt.getString("text");
                    isCorrect = selectedOpt.getBoolean("isCorrect");
                } else {
                    studentAnswerText = "(Tidak Dijawab)";
                }

                // Find correct option
                JSONArray options = question.getJSONArray("options");
                for (int i = 0; i < options.length(); i++) {
                    JSONObject opt = options.getJSONObject(i);
                    if (opt.getBoolean("isCorrect")) {
                        correctAnswerText = opt.getString("text");
                        break;
                    }
                }
            } else {
                // Essay
                studentAnswerText = answerObj.optString("essayAnswer", "(Tidak Dijawab)");
                if (studentAnswerText.equals("null")) studentAnswerText = "(Tidak Dijawab)";

                correctAnswerText = question.optString("correctEssayAnswer", "");
                if (correctAnswerText.equals("null")) correctAnswerText = "";
                
                if (studentAnswerText.trim().equalsIgnoreCase(correctAnswerText.trim()) && !correctAnswerText.isEmpty()) {
                    isCorrect = true;
                }
            }

            holder.tvStudentAnswer.setText(studentAnswerText);

            if (isCorrect) {
                holder.tvStatus.setText("BENAR (+" + marks + " Poin)");
                holder.tvStatus.setTextColor(Color.parseColor("#27ae60")); // Green
                holder.tvCorrectAnswerLabel.setVisibility(View.GONE);
                holder.tvCorrectAnswer.setVisibility(View.GONE);
            } else {
                holder.tvStatus.setText("SALAH (0 Poin)");
                holder.tvStatus.setTextColor(Color.parseColor("#e74c3c")); // Red
                holder.tvCorrectAnswerLabel.setVisibility(View.VISIBLE);
                holder.tvCorrectAnswer.setVisibility(View.VISIBLE);
                holder.tvCorrectAnswer.setText(correctAnswerText);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return answers.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionText, tvStudentAnswer, tvCorrectAnswerLabel, tvCorrectAnswer, tvStatus;
        ImageView ivQuestionImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            tvStudentAnswer = itemView.findViewById(R.id.tvStudentAnswer);
            tvCorrectAnswerLabel = itemView.findViewById(R.id.tvCorrectAnswerLabel);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivQuestionImage = itemView.findViewById(R.id.ivQuestionImage);
        }
    }
}
