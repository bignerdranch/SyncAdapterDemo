package testapp.bgardner.syncadapterdemo.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import testapp.bgardner.syncadapterdemo.accounts.AuthenticationResult;
import testapp.bgardner.syncadapterdemo.activities.AuthenticatedActivity;
import testapp.bgardner.syncadapterdemo.activities.AuthenticatorActivity;
import testapp.bgardner.syncadapterdemo.activities.BaseActivity;
import testapp.bgardner.syncadapterdemo.DataFetcher;
import testapp.bgardner.syncadapterdemo.contentprovider.ContentProviderContract;
import testapp.bgardner.syncadapterdemo.models.BookErratum;
import testapp.bgardner.syncadapterdemo.models.User;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";
    private static final String ENDPOINT = "http://syncadapterdemo.herokuapp.com/";
    private static final String USERS_INDEX_PATH = "users.json";
    private static final String BOOK_ERRATA_INDEX_PATH = "book_errata.json";
    private static final String BOOK_ERRATA_POST_PATH = "book_errata/";

    ContentResolver mContentResolver;
    String mAccessToken;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        // background data fetching here
        Log.d(TAG, "Perform the background sync");
        AccountManager manager = AccountManager.get(getContext());

        mAccessToken = manager.peekAuthToken(account, AuthenticatedActivity.AUTH_TOKEN_TYPE);
        Log.d(TAG, "Have access token: " + mAccessToken);
        try {
            pushBookErrataData();
            fetchUsersData();
            fetchBookErrataData();
        } catch (IOException exception) {
            Log.e(TAG, "Failed to sync data from server", exception);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to get value from json object", e);
        }
    }

    private void pushBookErrataData() {
        updateBookErrataData();
        deleteBookErrataData();
    }

    private void updateBookErrataData() {
        String updatedSelection = ContentProviderContract.BookErrata.UPDATE_FLAG + " = 1";
        Cursor updatedErrataCursor = getContext().getContentResolver().query(ContentProviderContract.BookErrata.CONTENT_URI, null, updatedSelection, null, null);
        Log.d(TAG, "Have this many items that need to be updated: " + updatedErrataCursor.getCount());
        updatedErrataCursor.moveToFirst();
        while (!updatedErrataCursor.isAfterLast()) {
            Log.d(TAG, "Update server with erratum data");
            BookErratum erratum = new BookErratum(updatedErrataCursor);
            Log.d(TAG, "Send this erratum json: " + erratum.toJSON());
            String erratumUpdatePath = getErratumUpdatePath(erratum);
            Log.d(TAG, "Have erratum update path: " + erratumUpdatePath);
            try {
                HttpResponse response = DataFetcher.putUrl(erratumUpdatePath, erratum.toJSON(), mAccessToken);
                Log.d(TAG, "Have response code: " + response.getStatusLine());
            } catch (IOException e) {
                Log.e(TAG, "Failed to put new erratum data", e);
            }
            updatedErrataCursor.moveToNext();
        }
    }

    private void deleteBookErrataData() {
        String deletedSelection = ContentProviderContract.BookErrata.DELETION_FLAG + " = 1";
        Cursor deletedErrataCursor = getContext().getContentResolver().query(ContentProviderContract.BookErrata.CONTENT_URI, null, deletedSelection, null, null);
        deletedErrataCursor.moveToFirst();
        while (!deletedErrataCursor.isAfterLast()) {
            BookErratum erratum = new BookErratum(deletedErrataCursor);
            String erratumDeletePath = getErratumDeletePath(erratum);
            try {
                HttpResponse response = DataFetcher.deleteUrl(erratumDeletePath, erratum.toJSON(), mAccessToken);
                int responseCode = response.getStatusLine().getStatusCode();
                Log.d(TAG, "Have response code: " + responseCode);
                if (responseCode == 200 || responseCode == 404) {
                    // no record on the server so just delete it from the local database
                    String selection = ContentProviderContract.BookErrata.ERRATA_ID + " = ?";
                    String[] selectionParams = {Integer.toString(erratum.getId())};
                    getContext().getContentResolver().delete(ContentProviderContract.BookErrata.CONTENT_URI, selection, selectionParams);
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to delete erratum data", e);
            }
            deletedErrataCursor.moveToNext();
        }
    }

    private void fetchUsersData() throws IOException, JSONException {
        String outputData = DataFetcher.getUrl(getUsersIndexUrl());
        JSONArray jsonArray = getUsersJsonArray(outputData);
        parseUsersJsonData(jsonArray);
    }

    private void fetchBookErrataData() throws IOException, JSONException {
        String outputData = DataFetcher.getUrl(getBookErrataIndexUrl());
        JSONArray jsonArray = getBookErrataJsonArray(outputData);
        parseBookErrataJsonData(jsonArray);
    }

    private String getUsersIndexUrl() {
        return ENDPOINT + USERS_INDEX_PATH;
    }

    private String getBookErrataIndexUrl() {
        return ENDPOINT + BOOK_ERRATA_INDEX_PATH;
    }

    private String getErratumUpdatePath(BookErratum erratum) {
        return ENDPOINT + BOOK_ERRATA_POST_PATH + erratum.getWebServerId() + ".json";
    }

    private String getErratumDeletePath(BookErratum erratum) {
        return ENDPOINT + BOOK_ERRATA_POST_PATH + erratum.getWebServerId() + ".json";
    }

    private JSONArray getUsersJsonArray(String userJsonData) {
        try {
            return new JSONArray(userJsonData);
        } catch (JSONException exception) {
            Log.e(TAG, "Failed to parse user json data", exception);
            return null;
        }
    }

    private JSONArray getBookErrataJsonArray(String bookErrataJsonData) {
        try {
            return new JSONArray(bookErrataJsonData);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse book errata json data", e);
            return null;
        }
    }

    private void parseUsersJsonData(JSONArray userData) {
        for (int i=0; i < userData.length(); i++) {
            try {
                JSONObject jsonObject = userData.getJSONObject(i);
                User user = new User(jsonObject);
                // user's server id is unique in the app database
                if (userRecordAlreadyExists(user.getServerId())) {
                    updateUserRecord(user);
                } else {
                    createNewUserRecord(user);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to get json object from user data", e);
            }
        }
    }

    private boolean userRecordAlreadyExists(int userServerId) {
        String selectionQuery = ContentProviderContract.User.SERVER_ID + " = ?";
        String[] selectionParams = {Integer.toString(userServerId)};
        Cursor userCursor = getContext().getContentResolver().query(ContentProviderContract.User.CONTENT_URI, null, selectionQuery, selectionParams, null);
        return userCursor.getCount() > 0;
    }

    private void createNewUserRecord(User user) {
        getContext().getContentResolver().insert(ContentProviderContract.User.CONTENT_URI, user.getUserContentValues());
    }

    private void updateUserRecord(User user) {
        String selectionQuery = ContentProviderContract.User.SERVER_ID + " = ?";
        String[] selectionParams = {Integer.toString(user.getServerId())};
        getContext().getContentResolver().update(ContentProviderContract.User.CONTENT_URI, user.getUserContentValues(), selectionQuery, selectionParams);
    }

    private void parseBookErrataJsonData(JSONArray bookErrataData) {
        ArrayList<Integer> bookErrataServerIds = new ArrayList<Integer>();
        ArrayList<Integer> currentBookErrataServerIds = (ArrayList<Integer>) getCurrentBookErrataServerIds();
        for (int i=0; i < bookErrataData.length(); i++) {
            try {
                JSONObject errataData = bookErrataData.getJSONObject(i);
                BookErratum bookErratum = new BookErratum(errataData);
                bookErrataServerIds.add(bookErratum.getWebServerId());
                if (bookErrataRecordAlreadyExists(bookErratum.getWebServerId())) {
                    updateBookErrataRecord(bookErratum);
                } else {
                    createNewBookErrataRecord(bookErratum);
                }
            } catch (JSONException e) {
                Log.d(TAG, "Failed to parse book errata data", e);
            }
        }
        // Remove book errata records that have been deleted off the server
        ArrayList<Integer> deletedServerIds = new ArrayList<Integer>();
        deletedServerIds.addAll(currentBookErrataServerIds);
        deletedServerIds.removeAll(bookErrataServerIds);
        for (Integer deletedId : deletedServerIds) {
            deleteBookErrataRecord(deletedId);
        }
    }

    private List<Integer> getCurrentBookErrataServerIds() {
        ArrayList<Integer> serverIds = new ArrayList<Integer>();
        String[] columns = {ContentProviderContract.BookErrata.SERVER_ID};
        Cursor errataCursor = getContext().getContentResolver().query(ContentProviderContract.BookErrata.CONTENT_URI, columns, null, null, null);
        while (errataCursor.moveToNext()) {
            serverIds.add(errataCursor.getInt(0));
        }
        errataCursor.close();
        return serverIds;
    }

    private boolean bookErrataRecordAlreadyExists(int bookErrataServerId) {
        String selectionQuery = ContentProviderContract.BookErrata.SERVER_ID + " = ?";
        String[] selectionParams = {Integer.toString(bookErrataServerId)};
        Cursor errataCursor = getContext().getContentResolver().query(ContentProviderContract.BookErrata.CONTENT_URI, null, selectionQuery, selectionParams, null);
        return errataCursor.getCount() > 0;
    }

    private void updateBookErrataRecord(BookErratum bookErratum) {
        String selectionQuery = ContentProviderContract.BookErrata.SERVER_ID + " = ?";
        String[] selectionParams = {Integer.toString(bookErratum.getWebServerId())};
        getContext().getContentResolver().update(ContentProviderContract.BookErrata.CONTENT_URI, bookErratum.getContentValues(), selectionQuery, selectionParams);
    }

    private void createNewBookErrataRecord(BookErratum bookErratum) {
        getContext().getContentResolver().insert(ContentProviderContract.BookErrata.CONTENT_URI, bookErratum.getContentValues());
    }

    private void deleteBookErrataRecord(int bookErrataServerId) {
        String selectionQuery = ContentProviderContract.BookErrata.SERVER_ID + " = ?";
        String[] selectionParams = {Integer.toString(bookErrataServerId)};
        getContext().getContentResolver().delete(ContentProviderContract.BookErrata.CONTENT_URI, selectionQuery, selectionParams);
    }
}
