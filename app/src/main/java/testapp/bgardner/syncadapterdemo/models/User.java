package testapp.bgardner.syncadapterdemo.models;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import testapp.bgardner.syncadapterdemo.contentprovider.ContentProviderContract;

public class User {
    private static final String TAG = "User";
    // Json fields
    private String SERVER_ID_FIELD = "id";
    private String USERNAME_FIELD = "username";

    private Integer mId;
    private int mServerId;
    private String mUsername;

    public User(Integer id, int serverId, String username) {
        mId = id;
        mServerId = serverId;
        mUsername = username;
    }

    public User(JSONObject jsonObject) {
        try {
            mServerId = jsonObject.getInt(SERVER_ID_FIELD);
            mUsername = jsonObject.getString(USERNAME_FIELD);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse user data", e);
        }
    }

    public ContentValues getUserContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ContentProviderContract.User.SERVER_ID, getServerId());
        cv.put(ContentProviderContract.User.USERNAME, getUsername());
        return cv;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public int getServerId() {
        return mServerId;
    }

    public void setServerId(int serverId) {
        mServerId = serverId;
    }
}
