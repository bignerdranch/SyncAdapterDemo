package testapp.bgardner.syncadapterdemo.accounts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StubAuthenticatorService extends Service {
    private StubAuthenticator mAuthenticator;

    public StubAuthenticatorService() {
        mAuthenticator = new StubAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
