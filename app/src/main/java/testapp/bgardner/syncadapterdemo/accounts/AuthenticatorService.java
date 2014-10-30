package testapp.bgardner.syncadapterdemo.accounts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
    private StubAuthenticator mAuthenticator;

    public AuthenticatorService() {
        mAuthenticator = new StubAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
