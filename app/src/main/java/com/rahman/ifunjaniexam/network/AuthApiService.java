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
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/auth/login";

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

    public static void changePassword(Context context, String oldPassword, String newPassword, AuthCallback callback) {
        String url = com.rahman.ifunjaniexam.network.Config.BASE_URL + "/auth/change-password";
        SharedPreferences prefs = context.getSharedPreferences("AUTH_PREF", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JSONObject postData = new JSONObject();
        try {
            postData.put("oldPassword", oldPassword);
            postData.put("newPassword", newPassword);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                postData,
                response -> callback.onSuccess(response),
                error -> callback.onError(error, error)
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }
}
