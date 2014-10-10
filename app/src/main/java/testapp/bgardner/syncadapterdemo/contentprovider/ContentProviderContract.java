package testapp.bgardner.syncadapterdemo.contentprovider;

import android.content.ContentResolver;
import android.net.Uri;

public final class ContentProviderContract {

    public static final String AUTHORITY = "com.testapp.bgardner.syncadapterdemo.provider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class BookErrata {
        // database table name
        public static final String TABLE_NAME = "book_errata";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContentProviderContract.CONTENT_URI, TABLE_NAME);

        // Book errata column names
        public static final String ERRATA_ID = "_id";
        public static final String SERVER_ID = "server_id";
        public static final String COURSE_NAME = "course_name";
        public static final String PAGE_NUMBER = "page_number";
        public static final String BOOK_VERSION = "book_version";
        public static final String ERROR_DESCRIPTION = "error_description";
        public static final String USER_ID = "user_id";

        // Other column names
        public static final String DELETION_FLAG = "should_delete";
        public static final String UPDATE_FLAG = "needs_update";

        // Mime types
        public static final String LIST_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "vnd." + AUTHORITY;
        public static final String SINGLE_CONTENT_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "vnd." + AUTHORITY;
    }

    public static final class User {
        // database table name
        public static final String TABLE_NAME = "users";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(ContentProviderContract.CONTENT_URI, TABLE_NAME);

        // User column names
        public static final String USER_ID = "_id";
        public static final String SERVER_ID = "server_id";
        public static final String USERNAME = "username";

        // Other column names
        public static final String DELETION_FLAG = "should_delete";

        // Mime types
        public static final String LIST_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "vnd." + AUTHORITY;
        public static final String SINGLE_CONTENT_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "vnd." + AUTHORITY;
    }
}
