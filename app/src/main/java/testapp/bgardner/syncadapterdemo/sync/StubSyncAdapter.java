package testapp.bgardner.syncadapterdemo.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

import testapp.bgardner.syncadapterdemo.DataFetcher;
import testapp.bgardner.syncadapterdemo.activities.BaseActivity;

public class StubSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "StubSyncAdapter";
    private static final String COUNT_FIELD = "count";
    private static final String ENDPOINT = "http://syncadapterdemo.herokuapp.com/";
    private static final String WELCOME_PATH = "welcome/index.json";
    public static final String BROADCAST_ACTION = "testapp.bgardner.syncadapterdemo.PAGE_VIEW_ACTION";
    public static final String BROADCAST_PERMISSION = "testapp.bgardner.syncadapterdemo.PRIVATE";

    private ContentResolver mContentResolver;

    public StubSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        try {
            Log.d(TAG, "Fetch the page view data");
            fetchPageViewData();
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch page view data from server", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse page view data from server", e);
        }
    }

    private void fetchPageViewData() throws IOException, JSONException {
        String outputData = DataFetcher.getUrl(getWelcomeUrl());
        JSONObject jsonObject = getPageViewJsonData(outputData);
        int pageViewCount = jsonObject.getInt(COUNT_FIELD);
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(BaseActivity.PAGE_VIEW_EXTRA, pageViewCount);
        getContext().sendBroadcast(intent, BROADCAST_PERMISSION);
    }

    private String getWelcomeUrl() {
        return ENDPOINT + WELCOME_PATH;
    }

    private JSONObject getPageViewJsonData(String jsonData) {
        try {
            return (JSONObject) new JSONTokener(jsonData).nextValue();
        } catch (JSONException exception) {
            Log.e(TAG, "Failed to parse json data", exception);
            return null;
        }
    }
}
