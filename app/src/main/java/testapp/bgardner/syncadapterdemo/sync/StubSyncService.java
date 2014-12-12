package testapp.bgardner.syncadapterdemo.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StubSyncService extends Service {
    private static final String TAG = "StubSyncService";
    // save the instance of sync adapter
    private static StubSyncAdapter sStubSyncAdapter = null;
    // object to use as thread-safe lock
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (sSyncAdapterLock) {
            if (sStubSyncAdapter == null) {
                sStubSyncAdapter = new StubSyncAdapter(getApplicationContext(), true);
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return sStubSyncAdapter.getSyncAdapterBinder();
    }
}
