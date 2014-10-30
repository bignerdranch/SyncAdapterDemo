package testapp.bgardner.syncadapterdemo.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import testapp.bgardner.syncadapterdemo.R;
import testapp.bgardner.syncadapterdemo.accounts.AccountSession;
import testapp.bgardner.syncadapterdemo.accounts.AuthenticationResult;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    private static final String TAG = "AuthenticatorActivity";
    // account type
    public static final String ACCOUNT_TYPE = "com.bgardner.testapps.syncadapterdemo";
    // Intent extra data
    public static final String EXTRA_ACCOUNT_TYPE = "com.bgardner.testapps.syncadapterdemo.ACCOUNT_TYPE";
    public static final String EXTRA_AUTH_TYPE = "com.bgardner.testapps.syncadapterdemo.AUTH_TYPE";
    public static final String EXTRA_ADD_NEW_ACCOUNT = "com.bgardner.testapps.syncadapterdemo.ADD_NEW_ACCOUNT";
    private EditText mAccountUsername;
    private EditText mAccountPassword;
    private Button mSignInButton;
    private TextView mErrorTextView;

    public static Intent newIntent(Context context) {
        return new Intent(context, AuthenticatorActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkCurrentLogInStatus();
        setContentView(R.layout.activity_authenticator);

        mAccountUsername = (EditText) findViewById(R.id.accountUsername);
        mAccountPassword = (EditText) findViewById(R.id.accountPassword);
        mSignInButton = (Button) findViewById(R.id.accountSignIn);
        mSignInButton.setOnClickListener(mSignInClickListener);
        mErrorTextView = (TextView) findViewById(R.id.errorTextView);
    }

    private void checkCurrentLogInStatus() {
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length != 0) {
            Account account = accounts[0];
            String username = account.name;
            String accessToken = manager.getPassword(account);
            AuthenticationResult authenticationResult = new AuthenticationResult(username, accessToken);
            Intent intent = AuthenticatedActivity.newIntent(AuthenticatorActivity.this, authenticationResult);
            startActivity(intent);
            finish();
        }
    }

    private View.OnClickListener mSignInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String accountUsername = mAccountUsername.getText().toString();
            String accountPassword = mAccountPassword.getText().toString();
            if (credentialsPresent(accountUsername, accountPassword)) {
                hideErrorText();
                disableSignInButton();
                new AuthenticateUserTask().execute(accountUsername, accountPassword);
            } else {
                displayMissingCredentialsError(accountUsername, accountPassword);
            }
        }
    };

    private AuthenticationResult attemptAccountAuthentication(String username, String password) {
        return new AccountSession().createSession(username, password);
    }

    private boolean credentialsPresent(String username, String password) {
        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password);
    }

    private void displayMissingCredentialsError(String username, String password) {
        String errorText = "";
        if (TextUtils.isEmpty(username) && TextUtils.isEmpty(password)) {
            errorText = getString(R.string.username_and_password_empty_error);
        } else if (TextUtils.isEmpty(username)) {
            errorText = getString(R.string.username_empty_error);
        } else if (TextUtils.isEmpty(password)) {
            errorText = getString(R.string.password_empty_error);
        }
        mErrorTextView.setText(errorText);
        showErrorText();
    }

    private void hideErrorText() {
        mErrorTextView.setVisibility(View.GONE);
    }

    private void showErrorText() {
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    private void disableSignInButton() {
        mSignInButton.setEnabled(false);
    }

    private void enableSignInButton() {
        mSignInButton.setEnabled(true);
    }

    private void displayAuthenticationError() {
        mErrorTextView.setText(R.string.authentication_failure);
        showErrorText();
    }

    private boolean storeAccountCredentials(AuthenticationResult authenticationResult) {
        Account account = new Account(authenticationResult.getUsername(), ACCOUNT_TYPE);
        AccountManager manager = AccountManager.get(this);
        return manager.addAccountExplicitly(account, authenticationResult.getAccessToken(), null);
    }

    private class AuthenticateUserTask extends AsyncTask<String, Void, AuthenticationResult> {

        @Override
        protected AuthenticationResult doInBackground(String... params) {
            return attemptAccountAuthentication(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(AuthenticationResult authenticationResult) {
            Log.d(TAG, "was authentication successful: " + authenticationResult.isSuccessfullyAuthenticated());
            if (authenticationResult.isSuccessfullyAuthenticated()) {
                boolean accountAdded = storeAccountCredentials(authenticationResult);
                if (accountAdded) {
                    //Start the authenticated activity
                    Intent intent = AuthenticatedActivity.newIntent(AuthenticatorActivity.this, authenticationResult);
                    startActivity(intent);
                    finish();
                } else {
                    Log.d(TAG, "Account has already been added");
                }
            } else {
                displayAuthenticationError();
                enableSignInButton();
            }
        }
    }
}
