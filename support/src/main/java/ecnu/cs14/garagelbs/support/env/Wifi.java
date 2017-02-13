package ecnu.cs14.garagelbs.support.env;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;

/**
 * Handles wifi related affairs.
 * Provides scan results.
 * Created by K on 16/12/11.
 */

final class Wifi extends BroadcastReceiver {
    private final static String TAG = Wifi.class.getName();
    private Context mContext;
    private WifiManager mManager;
    private HashSet<WeakReference<ScanResultsReceiver>> mCallbacks;

    interface ScanResultsReceiver {
        void receive(List<ScanResult> results);
    }

    Wifi(Context context) {
        mContext = context;
        mManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mCallbacks = new HashSet<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: started");
        List<ScanResult> list = mManager.getScanResults();
        for (WeakReference<ScanResultsReceiver> r :
                mCallbacks) {
            r.get().receive(list);
        }
        mContext.unregisterReceiver(this);
    }

    void enable() {
        mManager.setWifiEnabled(true);
    }

    void scan() {
        Log.i(TAG, "scan: started");
        if (!mManager.isWifiEnabled()) {
            enable();
        }
        mContext.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mManager.startScan();
    }

    void registerScanResultsReceiver(ScanResultsReceiver receiver) {
        mCallbacks.add(new WeakReference<>(receiver));
    }

    void destroy() {
        try {
            mContext.unregisterReceiver(this);
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().startsWith("Receiver not registered")) {
                throw e;
            } else {
                Log.d(TAG, "destroy: Duplicate un-registration caught.");
            }
        }
    }
}
