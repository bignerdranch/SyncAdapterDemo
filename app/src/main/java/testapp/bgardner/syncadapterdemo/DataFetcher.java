package testapp.bgardner.syncadapterdemo;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class DataFetcher {
    private static final String TAG = "DataFetcher";
    private static final String ACCESS_TOKEN = "access_token";

    private static byte[] getUrlBytes(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public static String getUrl(String urlString) throws IOException {
        return new String(getUrlBytes(urlString));
    }

    public static HttpResponse postUrl(String urlString, List<NameValuePair> postParams) throws IOException, JSONException{
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(urlString);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");

        JSONObject jsonObject = new JSONObject();
        for (NameValuePair postParam : postParams) {
            jsonObject.put(postParam.getName(), postParam.getValue());
        }
        StringEntity entity = new StringEntity(jsonObject.toString(), "UTF-8");
        post.setEntity(entity);

        return client.execute(post);
    }

    public static HttpResponse putUrl(String urlString, JSONObject jsonObject, String accessToken) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpPut put = new HttpPut(urlString);
        put.setHeader("Content-Type", "application/json");
        put.setHeader("Accept", "application/json");

        try {
            jsonObject.put(ACCESS_TOKEN, accessToken);
            Log.d(TAG, "Put json object: " + jsonObject);

            StringEntity entity = new StringEntity(jsonObject.toString(), "UTF-8");
            put.setEntity(entity);

            return client.execute(put);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to add access token to json object", e);
            return null;
        }
    }

    public static HttpResponse deleteUrl(String urlString, JSONObject jsonObject, String accessToken) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(urlString);
        httpDelete.setHeader("Content-Type", "application/json");
        httpDelete.setHeader("Accept", "application/json");

        try {
            jsonObject.put(ACCESS_TOKEN, accessToken);

            StringEntity entity = new StringEntity(jsonObject.toString(), "UTF-8");
            httpDelete.setEntity(entity);

            return client.execute(httpDelete);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to add access token to json object", e);
            return null;
        }
    }
}
