package testapp.bgardner.syncadapterdemo.accounts;

import java.io.Serializable;

public class AuthenticationResult implements Serializable {
    private boolean mSuccessfullyAuthenticated;
    private String mUsername;
    private String mAccessToken;

    public AuthenticationResult(String username) {
        mUsername = username;
        mSuccessfullyAuthenticated = false;
        mAccessToken = null;
    }

    public AuthenticationResult(String username, String accessToken) {
        mUsername = username;
        mAccessToken = accessToken;
        mSuccessfullyAuthenticated = true;
    }

    public boolean isSuccessfullyAuthenticated() {
        return mSuccessfullyAuthenticated;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getAccessToken() {
        return mAccessToken;
    }
}
