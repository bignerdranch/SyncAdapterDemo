package testapp.bgardner.syncadapterdemo.accounts;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import testapp.bgardner.syncadapterdemo.DataFetcher;

public class AccountSession {
    public static final String TAG = "AccountSession";
    private static final String ENDPOINT = "http://syncadapterdemo.herokuapp.com/";
    private static final String SESSION_PATH = "sessions.json";
    private static final String ACCESS_TOKEN_FIELD = "access_token";

    public AuthenticationResult createSession(String username, String password) {
        List<NameValuePair> postParams = createSessionPostParams(username, password);
        Log.d(TAG, "Authenticate with username: " + username + ", and password: " + password);
        try {
            HttpResponse response = DataFetcher.postUrl(getSessionPathString(), postParams);
            Log.d(TAG, "Got status code: " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200) {
                InputStream is = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder out = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                JSONObject jsonObject = new JSONObject(out.toString());
                if (jsonObject.has(ACCESS_TOKEN_FIELD)) {
                    String accessToken = jsonObject.getString(ACCESS_TOKEN_FIELD);
                    Log.d(TAG, "Got access token: " + accessToken);
                    if (!TextUtils.isEmpty(accessToken)) {
                        // can't have successful authentication result without an access token
                        return new AuthenticationResult(username, accessToken);
                    }
                } else {
                    Log.d(TAG, "No access token included in response");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create session on server", e);
        }
        return new AuthenticationResult(username);
    }

    private String getSessionPathString() {
        return ENDPOINT + SESSION_PATH;
    }

    private List<NameValuePair> createSessionPostParams(String username, String password) {
        ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
        postParams.add(new BasicNameValuePair("username", username));
        postParams.add(new BasicNameValuePair("password", password));
        return postParams;
    }
}
