package testapp.bgardner.syncadapterdemo.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import testapp.bgardner.syncadapterdemo.R;
import testapp.bgardner.syncadapterdemo.sync.SyncAdapter;


public class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";
    // authority for sync adapter's content provider
    public static final String AUTHORITY = "com.testapp.bgardner.syncadapterdemo.provider";
    // account type
    public static final String ACCOUNT_TYPE = "com.bgardner.testapps.syncadapterdemo";
    // account name
    public static final String ACCOUNT_NAME = "dummy_account";
    // sync interval
    public static final long SYNC_INTERVAL = 10;
    // broadcast view count extra
    public static final String PAGE_VIEW_EXTRA = "testapp.bgardner.syncadapterdemo.PAGE_VIEW_EXTRA";
    // instance fields
    Account mAccount;
    ContentResolver mContentResolver;

    private TextView mViewCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mViewCountTextView = (TextView) findViewById(R.id.viewCountTextView);

        mContentResolver = getContentResolver();
        mAccount = createSyncAccount(this);

        Log.d(TAG, "Add periodic sync with account: " + mAccount);
        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);
        ContentResolver.addPeriodicSync(mAccount, AUTHORITY, new Bundle(), SYNC_INTERVAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Remove periodic sync from account");
        ContentResolver.removePeriodicSync(mAccount, AUTHORITY, new Bundle());
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(SyncAdapter.BROADCAST_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    public static Account createSyncAccount(Context context) {
        // create the account object
        Account account = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        // get the android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

        // add the account and account type. No passwords or user data
        // if successful return the account, else throw an error
        if (accountManager.addAccountExplicitly(account, null, null)) {
            return account;
        } else {
            // Couldn't create account, one must already exist
            Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
            if (accounts.length > 0) {
                return accounts[0];
            }
            return null;
        }
    }

    private void requestImmediateSync() {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int viewCount = intent.getIntExtra(PAGE_VIEW_EXTRA, 0);
            if (viewCount > 0) {
                mViewCountTextView.setText(getString(R.string.view_count_string, viewCount));
            }
        }
    };
}
