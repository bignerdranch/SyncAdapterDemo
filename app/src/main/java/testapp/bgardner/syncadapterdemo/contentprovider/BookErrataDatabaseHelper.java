package testapp.bgardner.syncadapterdemo.contentprovider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import testapp.bgardner.syncadapterdemo.models.BookErratum;

public class BookErrataDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "book_errata.sqlite";
    private static final int VERSION = 1;

    public BookErrataDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create book errata database table
        db.execSQL("create table " + ContentProviderContract.BookErrata.TABLE_NAME + " (" +
                    ContentProviderContract.BookErrata.ERRATA_ID + " integer primary key autoincrement, " +
                    ContentProviderContract.BookErrata.SERVER_ID + " integer, " +
                    ContentProviderContract.BookErrata.COURSE_NAME + " string, " +
                    ContentProviderContract.BookErrata.PAGE_NUMBER + " integer, " +
                    ContentProviderContract.BookErrata.BOOK_VERSION + " string, " +
                    ContentProviderContract.BookErrata.ERROR_DESCRIPTION + " string, " +
                    ContentProviderContract.BookErrata.USER_ID + " integer, " +
                    ContentProviderContract.BookErrata.DELETION_FLAG + " boolean default false," +
                    ContentProviderContract.BookErrata.UPDATE_FLAG + " boolean default false," +
                    "UNIQUE(" + ContentProviderContract.BookErrata.SERVER_ID + ") ON CONFLICT IGNORE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
