package com.rahman.ifunjaniexam.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class AuthApiService {

    public interface AuthCallback {
        void onSuccess(JSONObject response);
        void onError(Exception error, com.android.volley.VolleyError volleyError);
    }

    public static void login(Context context, String username, String password, AuthCallback callback) {
        String url = "https://if-unjani-exam-api.vercel.app/api/auth/login";

        JSONObject postData = new JSONObject();
        try {
            postData.put("username", username);
            postData.put("password", password);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                postData,
                response -> callback.onSuccess(response),
                error -> callback.onError(error, error)
        );

        Volley.newRequestQueue(context).add(request);
    }
}
