package testapp.bgardner.syncadapterdemo.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;

import testapp.bgardner.syncadapterdemo.R;
import testapp.bgardner.syncadapterdemo.accounts.AuthenticationResult;
import testapp.bgardner.syncadapterdemo.contentprovider.ContentProviderContract;
import testapp.bgardner.syncadapterdemo.fragments.BookErrataFragment;
import testapp.bgardner.syncadapterdemo.interfaces.AuthenticatedInterface;
import testapp.bgardner.syncadapterdemo.models.BookErratum;

public class AuthenticatedActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, AuthenticatedInterface{
    private static final String TAG = "AuthenticatedActivity";
    public static final String AUTHENTICATION_RESULT_EXTRA = "AuthenticatedActivity.AuthenticationResultExtra";
    // Account type and auth token type
    public static final String ACCOUNT_TYPE = "com.bgardner.testapps.syncadapterdemo.USER_ACCOUNT";
    public static final String AUTH_TOKEN_TYPE = "com.bgardner.testapps.syncadapterdemo.FULL_ACCESS";
    // authority for sync adapter's content provider
    public static final String AUTHORITY = ContentProviderContract.AUTHORITY;
    // sync interval
    public static final long SYNC_INTERVAL = 10;
    // Book errata loader id
    private static final int BOOK_ERRATA_ID = 0;
    private TextView mCurrentUsernameTextView;
    private ListView mBookErrataListView;
    private Account mAccount;
    private BookErrataAdapter mBookErrataAdapter;
    private AccountManager mAccountManager;
    private String mAccessToken;

    public static Intent newIntent(Context context, AuthenticationResult authenticationResult) {
        Intent intent = new Intent(context, AuthenticatedActivity.class);
        intent.putExtra(AuthenticatedActivity.AUTHENTICATION_RESULT_EXTRA, authenticationResult);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountManager = AccountManager.get(this);
        AccountManagerFuture<Bundle> authTokenFuture = mAccountManager.getAuthTokenByFeatures(ACCOUNT_TYPE, AUTH_TOKEN_TYPE, null, this, null, null, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                Bundle bundle = null;
                try {
                    bundle = future.getResult();
                    mAccessToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    setupAccountInstance();
                    startPeriodicSync();
                    Log.d(TAG, "Got access token: " + mAccessToken);
                } catch (AuthenticatorException e) {
                    Log.e(TAG, "Got an authenticator exception", e);
                } catch (OperationCanceledException e) {
                    Log.e(TAG, "Got an operation canceled exception", e);
                    finish();
                } catch (IOException e) {
                    Log.e(TAG, "Got an IO Exception", e);
                }
            }
        }, null);

        setContentView(R.layout.activity_authenticated);

        mCurrentUsernameTextView = (TextView) findViewById(R.id.currentUsernameTextView);
        mBookErrataListView = (ListView) findViewById(R.id.book_errata_list_view);

        getLoaderManager().initLoader(BOOK_ERRATA_ID, null, this);
        mBookErrataAdapter = new BookErrataAdapter(getBaseContext(), null);
        mBookErrataListView.setAdapter(mBookErrataAdapter);
        mBookErrataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateBookErrataDetailView(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.authenticated_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_log_out:
                logoutUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateBookErrataDetailView(int position) {
        Cursor item = (Cursor) mBookErrataAdapter.getItem(position);
        BookErratum erratum = new BookErratum(item);
        BookErrataFragment fragment = BookErrataFragment.newInstance(erratum);
        getFragmentManager().beginTransaction().replace(R.id.book_errata_detail_view, fragment).commit();
    }

    private void setupAccountInstance() {
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        }
    }

    private void startPeriodicSync() {
        Log.d(TAG, "Have account: " + mAccount);
        if (mAccount != null) {
            ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);
            ContentResolver.addPeriodicSync(mAccount, AUTHORITY, new Bundle(), SYNC_INTERVAL);
            Log.d(TAG, "Added periodic sync to account");
        }
    }

    private void removePeriodicSync() {
        ContentResolver.removePeriodicSync(mAccount, AUTHORITY, new Bundle());
    }

    private String getUsernameText() {
        return getString(R.string.current_username_string, mAccount.name);
    }

    private void returnToAuthenticatorActivity() {
        Intent intent = AuthenticatorActivity.newIntent(AuthenticatedActivity.this);
        startActivity(intent);
        finish();
    }

    private void logoutUser() {
        removePeriodicSync();

        mAccountManager.removeAccount(mAccount, null, null);

        returnToAuthenticatorActivity();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case BOOK_ERRATA_ID:
                return new CursorLoader(getApplicationContext(), ContentProviderContract.BookErrata.CONTENT_URI, null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBookErrataAdapter.swapCursor(data);
        mBookErrataAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "Loader has been reset");
    }

    @Override
    public String getCurrentUsersUsername() {
        return mAccount.name;
    }


    private class BookErrataAdapter extends CursorAdapter {

        public BookErrataAdapter(Context context, Cursor cursor) {
            super(context, cursor, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return AuthenticatedActivity.this.getLayoutInflater().inflate(R.layout.book_errata_list_view_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView courseView = (TextView) view.findViewById(R.id.book_errata_course_text_view);
            TextView pageView = (TextView) view.findViewById(R.id.book_errata_page_text_view);
            TextView versionView = (TextView) view.findViewById(R.id.book_errata_version_text_view);
            TextView descriptionView = (TextView) view.findViewById(R.id.book_errata_description_text_view);

            int courseColumn = cursor.getColumnIndex(ContentProviderContract.BookErrata.COURSE_NAME);
            int pageColumn = cursor.getColumnIndex(ContentProviderContract.BookErrata.PAGE_NUMBER);
            int versionColumn = cursor.getColumnIndex(ContentProviderContract.BookErrata.BOOK_VERSION);
            int descriptionColumn = cursor.getColumnIndex(ContentProviderContract.BookErrata.ERROR_DESCRIPTION);

            courseView.setText(getString(R.string.book_errata_course_text, cursor.getString(courseColumn)));
            pageView.setText(getString(R.string.book_errata_page_text, cursor.getInt(pageColumn)));
            versionView.setText(getString(R.string.book_errata_version_text, cursor.getString(versionColumn)));
            descriptionView.setText(getString(R.string.book_errata_description_text, cursor.getString(descriptionColumn)));
        }
    }
}
