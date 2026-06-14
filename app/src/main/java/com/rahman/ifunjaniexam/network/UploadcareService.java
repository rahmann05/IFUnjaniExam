package com.rahman.ifunjaniexam.network;

import android.content.Context;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

public class UploadcareService {

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(Exception e);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                byte[] fileBytes = getBytes(inputStream);
                String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
                
                URL url = new URL("https://upload.uploadcare.com/base/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream os = conn.getOutputStream();
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"UPLOADCARE_PUB_KEY\"\r\n\r\n").getBytes());
                os.write(("c8b0d7bba3c5895d132b\r\n").getBytes());
                
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n").getBytes());
                os.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
                os.write(fileBytes);
                os.write(("\r\n--" + boundary + "--\r\n").getBytes());
                os.flush();
                os.close();

                InputStream responseStream = conn.getInputStream();
                String responseBody = new String(getBytes(responseStream));
                JSONObject json = new JSONObject(responseBody);
                String fileId = json.getString("file");

                String finalUrl = "https://ucarecdn.com/" + fileId + "/";
                callback.onSuccess(finalUrl);

            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    private static byte[] getBytes(InputStream is) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}
