package testapp.bgardner.syncadapterdemo.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.sql.SQLException;

public class SyncAdapterContentProvider extends ContentProvider {
    private static final String TAG = "SyncAdapterContentProvider";
    private BookErrataDatabaseHelper mBookErrataDatabaseHelper;
    private UsersDatabaseHelper mUsersDatabaseHelper;
    private static final int BOOK_ERRATA_LIST = 1;
    private static final int BOOK_ERRATA_ID = 2;
    private static final int USERS_LIST = 3;
    private static final int USER_ID = 4;
    private static final UriMatcher sUriMatcher;

    // Setup the uri matcher with the needed paths
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Book errata table matchers
        sUriMatcher.addURI(ContentProviderContract.AUTHORITY, "book_errata", BOOK_ERRATA_LIST);
        sUriMatcher.addURI(ContentProviderContract.AUTHORITY, "book_errata/#", BOOK_ERRATA_ID);
        // User table matchers
        sUriMatcher.addURI(ContentProviderContract.AUTHORITY, "users", USERS_LIST);
        sUriMatcher.addURI(ContentProviderContract.AUTHORITY, "users/#", USER_ID);
    }


    @Override
    public boolean onCreate() {
        mBookErrataDatabaseHelper = new BookErrataDatabaseHelper(getContext());
        mUsersDatabaseHelper = new UsersDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tableName;
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case BOOK_ERRATA_LIST:
                tableName = ContentProviderContract.BookErrata.TABLE_NAME;
                if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
                if (TextUtils.isEmpty(selection) || !selection.contains(ContentProviderContract.BookErrata.DELETION_FLAG)) {
                    selection = updateBookErrataSelectionToHideDeletedRows(selection);
                }
                cursor = mBookErrataDatabaseHelper.getReadableDatabase().query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case BOOK_ERRATA_ID:
                tableName = ContentProviderContract.BookErrata.TABLE_NAME;
                if (TextUtils.isEmpty(selection)) {
                    selection = "_ID = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " _ID = " + uri.getLastPathSegment();
                }
                selection = updateBookErrataSelectionToHideDeletedRows(selection);
                cursor = mBookErrataDatabaseHelper.getReadableDatabase().query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case USERS_LIST:
                tableName = ContentProviderContract.User.TABLE_NAME;
                if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
                selection = updateUserSelectionToHideDeletedRows(selection);
                cursor = mUsersDatabaseHelper.getReadableDatabase().query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case USER_ID:
                tableName = ContentProviderContract.User.TABLE_NAME;
                if (TextUtils.isEmpty(selection)) {
                    selection = "_ID = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " _ID = " + uri.getLastPathSegment();
                }
                selection = updateUserSelectionToHideDeletedRows(selection);
                cursor = mUsersDatabaseHelper.getReadableDatabase().query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case BOOK_ERRATA_LIST:
                return ContentProviderContract.BookErrata.LIST_CONTENT_TYPE;
            case BOOK_ERRATA_ID:
                return ContentProviderContract.BookErrata.SINGLE_CONTENT_TYPE;
            case USERS_LIST:
                return ContentProviderContract.User.LIST_CONTENT_TYPE;
            case USER_ID:
                return ContentProviderContract.User.SINGLE_CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long insertedRowId = 0;
        switch (sUriMatcher.match(uri)) {
            case BOOK_ERRATA_LIST:
                insertedRowId = mBookErrataDatabaseHelper.getWritableDatabase().insert(ContentProviderContract.BookErrata.TABLE_NAME, null, values);
                break;
            case USERS_LIST:
                insertedRowId = mUsersDatabaseHelper.getWritableDatabase().insertOrThrow(ContentProviderContract.User.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        if (insertedRowId > 0) {
            notifyUriChange(uri);
            return ContentUris.withAppendedId(uri, insertedRowId);
        } else {
            return null;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deletedCount;
        String where;
        switch (sUriMatcher.match(uri)) {
            case BOOK_ERRATA_LIST:
                deletedCount = mBookErrataDatabaseHelper.getWritableDatabase().delete(ContentProviderContract.BookErrata.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ERRATA_ID:
                String errataId = uri.getLastPathSegment();
                where = ContentProviderContract.BookErrata.ERRATA_ID + " = " + errataId;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deletedCount = mBookErrataDatabaseHelper.getWritableDatabase().delete(ContentProviderContract.BookErrata.TABLE_NAME, where, selectionArgs);
                break;
            case USERS_LIST:
                deletedCount = mUsersDatabaseHelper.getWritableDatabase().delete(ContentProviderContract.User.TABLE_NAME, selection, selectionArgs);
                break;
            case USER_ID:
                String userID = uri.getLastPathSegment();
                where = ContentProviderContract.User.USER_ID + " = " + userID;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deletedCount = mUsersDatabaseHelper.getWritableDatabase().delete(ContentProviderContract.User.TABLE_NAME, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (deletedCount > 0) {
            notifyUriChange(uri);
        }
        return deletedCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updateCount = 0;
        String where;
        switch (sUriMatcher.match(uri)) {
            case BOOK_ERRATA_LIST:
                updateCount = mBookErrataDatabaseHelper.getWritableDatabase().update(ContentProviderContract.BookErrata.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BOOK_ERRATA_ID:
                String errataID = uri.getLastPathSegment();
                where = ContentProviderContract.BookErrata.ERRATA_ID + " = " + errataID;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = mBookErrataDatabaseHelper.getWritableDatabase().update(ContentProviderContract.BookErrata.TABLE_NAME, values, where, selectionArgs);
                break;
            case USERS_LIST:
                updateCount = mUsersDatabaseHelper.getWritableDatabase().update(ContentProviderContract.User.TABLE_NAME, values, selection, selectionArgs);
                break;
            case USER_ID:
                String userID = uri.getLastPathSegment();
                where = ContentProviderContract.User.USER_ID + " = " + userID;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = mUsersDatabaseHelper.getWritableDatabase().update(ContentProviderContract.User.TABLE_NAME, values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (updateCount > 0) {
            notifyUriChange(uri);
        }
        return updateCount;
    }


    // private methods
    private void notifyUriChange(Uri updatedUri) {
        getContext().getContentResolver().notifyChange(updatedUri, null);
    }

    private ContentValues getBookErrataDeleteContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ContentProviderContract.BookErrata.DELETION_FLAG, true);
        return cv;
    }

    private ContentValues getUsersDeleteContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ContentProviderContract.User.DELETION_FLAG, true);
        return cv;
    }

    private String updateBookErrataSelectionToHideDeletedRows(String selection) {
        if (TextUtils.isEmpty(selection)) {
            selection = ContentProviderContract.BookErrata.DELETION_FLAG + " = 'false'";
        } else {
            selection += " AND " + ContentProviderContract.BookErrata.DELETION_FLAG + " = 'false'";
        }
        return selection;
    }

    private String updateUserSelectionToHideDeletedRows(String selection) {
        if (TextUtils.isEmpty(selection)) {
            selection = ContentProviderContract.User.DELETION_FLAG + " = 'false'";
        } else {
            selection += " AND " + ContentProviderContract.User.DELETION_FLAG + " = 'false'";
        }
        return selection;
    }
}
