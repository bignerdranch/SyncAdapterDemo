package testapp.bgardner.syncadapterdemo.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import testapp.bgardner.syncadapterdemo.contentprovider.ContentProviderContract;

public class BookErratum implements Serializable{
    private static final String TAG = "BookErratum";
    // JSON fields
    private String SERVER_ID_FIELD = "id";
    private String COURSE_FIELD = "course";
    private String PAGE_FIELD = "page";
    private String VERSION_FIELD = "version";
    private String DESCRIPTION_FIELD = "description";
    private String USER_ID_FIELD = "user_id";

    private int mId;
    private int mWebServerId;
    private String mCourseName;
    private int mPageNumber;
    private String mBookVersion;
    private String mErrorDescription;
    private int mUserId;

    public BookErratum(int webServerId, String courseName, int pageNumber, String bookVersion, String errorDescription, int userId) {
        mWebServerId = webServerId;
        mCourseName = courseName;
        mPageNumber = pageNumber;
        mBookVersion = bookVersion;
        mErrorDescription = errorDescription;
        mUserId = userId;
    }

    public BookErratum(JSONObject jsonObject) {
        try {
            mWebServerId = jsonObject.getInt(SERVER_ID_FIELD);
            mCourseName = jsonObject.getString(COURSE_FIELD);
            mPageNumber = jsonObject.getInt(PAGE_FIELD);
            mBookVersion = jsonObject.getString(VERSION_FIELD);
            mErrorDescription = jsonObject.getString(DESCRIPTION_FIELD);
            mUserId = jsonObject.getInt(USER_ID_FIELD);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse book errata json", e);
        }
    }

    public BookErratum(Cursor cursor) {
        mId = cursor.getInt(cursor.getColumnIndex(ContentProviderContract.BookErrata.ERRATA_ID));
        mWebServerId = cursor.getInt(cursor.getColumnIndex(ContentProviderContract.BookErrata.SERVER_ID));
        mCourseName = cursor.getString(cursor.getColumnIndex(ContentProviderContract.BookErrata.COURSE_NAME));
        mPageNumber = cursor.getInt(cursor.getColumnIndex(ContentProviderContract.BookErrata.PAGE_NUMBER));
        mBookVersion = cursor.getString(cursor.getColumnIndex(ContentProviderContract.BookErrata.BOOK_VERSION));
        mErrorDescription = cursor.getString(cursor.getColumnIndex(ContentProviderContract.BookErrata.ERROR_DESCRIPTION));
        mUserId = cursor.getInt(cursor.getColumnIndex(ContentProviderContract.BookErrata.USER_ID));
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ContentProviderContract.BookErrata.SERVER_ID, getWebServerId());
        cv.put(ContentProviderContract.BookErrata.COURSE_NAME, getCourseName());
        cv.put(ContentProviderContract.BookErrata.PAGE_NUMBER, getPageNumber());
        cv.put(ContentProviderContract.BookErrata.BOOK_VERSION, getBookVersion());
        cv.put(ContentProviderContract.BookErrata.ERROR_DESCRIPTION, getErrorDescription());
        cv.put(ContentProviderContract.BookErrata.USER_ID, getUserId());
        return cv;
    }

    public ContentValues getUpdateContentValues() {
        ContentValues cv = getContentValues();
        cv.put(ContentProviderContract.BookErrata.UPDATE_FLAG, true);
        return cv;
    }

    public static ContentValues getDeleteContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ContentProviderContract.BookErrata.DELETION_FLAG, true);
        return cv;
    }

    public JSONObject toJSON() {
        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SERVER_ID_FIELD, getWebServerId());
            jsonObject.put(COURSE_FIELD, getCourseName());
            jsonObject.put(PAGE_FIELD, getPageNumber());
            jsonObject.put(VERSION_FIELD, getBookVersion());
            jsonObject.put(DESCRIPTION_FIELD, getErrorDescription());
            jsonObject.put(USER_ID_FIELD, getUserId());
            JSONObject bookErrataObject = new JSONObject();
            bookErrataObject.put("book_erratum", jsonObject);
            return bookErrataObject;
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse erratum to json", e);
            return null;
        }
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getWebServerId() {
        return mWebServerId;
    }

    public void setWebServerId(int webServerId) {
        mWebServerId = webServerId;
    }

    public String getCourseName() {
        return mCourseName;
    }

    public void setCourseName(String courseName) {
        mCourseName = courseName;
    }

    public int getPageNumber() {
        return mPageNumber;
    }

    public void setPageNumber(int pageNumber) {
        mPageNumber = pageNumber;
    }

    public String getBookVersion() {
        return mBookVersion;
    }

    public void setBookVersion(String bookVersion) {
        mBookVersion = bookVersion;
    }

    public String getErrorDescription() {
        return mErrorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        mErrorDescription = errorDescription;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        mUserId = userId;
    }
}
