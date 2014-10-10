package testapp.bgardner.syncadapterdemo.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UsersDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "users.sqlite";
    private static final int VERSION = 1;

    public UsersDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create user database table
        db.execSQL("create table " + ContentProviderContract.User.TABLE_NAME + " (" +
                                    ContentProviderContract.User.USER_ID + " integer primary key autoincrement, " +
                                    ContentProviderContract.User.SERVER_ID + " integer, " +
                                    ContentProviderContract.User.USERNAME + " string, " +
                                    ContentProviderContract.User.DELETION_FLAG + " boolean default false," +
                                    "UNIQUE (" + ContentProviderContract.User.SERVER_ID + ") ON CONFLICT IGNORE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
