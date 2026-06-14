package com.rahman.ifunjaniexam.network;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ExamApiService {
    
    public interface ApiCallback {
        void onSuccess(JSONObject response);
        void onError(Exception error);
    }

    public static void createExam(Context context, JSONObject payload, ApiCallback callback) {
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/exams/create";
        SharedPreferences prefs = context.getSharedPreferences("AUTH_PREF", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload,
                response -> callback.onSuccess(response),
                error -> callback.onError(error)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }
}
