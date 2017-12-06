package ecnu.cs14.garagelbs.support.env;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.util.Log;
import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.Fingerprint;

import java.util.*;

/**
 * An implementation of {@link Environment}.
 * Created by K on 2017/1/21.
 */

final class EnvironmentImpl extends Environment implements Wifi.ScanResultsReceiver {
    private final static String TAG = EnvironmentImpl.class.getName();

    private Wifi mWifi = null;
    private final List<ScanResult> mResults = new ArrayList<>();
    private boolean mResultsUpdated = false;

    private EnvironmentImpl(Context context) {
        super(context);
        mWifi = new Wifi(context);
        mWifi.registerScanResultsReceiver(this);
    }

    private static EnvironmentImpl sInstance;
    public static EnvironmentImpl getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new EnvironmentImpl(context);
        }
        return sInstance;
    }

    @Override
    public void receive(List<ScanResult> newResults) {
        synchronized (mResults) {
            mResults.clear();
            mResults.addAll(newResults);
            mResultsUpdated = true;
            mResults.notify();
        }
    }

    /**
     * Get all aps in the area. A timeout results in an empty return. Time consuming.
     * @return A {@link List} of {@link Ap} ordered by level.
     */
    @Override
    @NonNull
    public List<Ap> getAps() {
        List<Ap> list = new ArrayList<>();
        mWifi.scan();
        synchronized (mResults) {
            if (!mResultsUpdated) {
                try {
                    Log.i(TAG, "getAps: waiting for scan results");
                    mResults.wait(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!mResultsUpdated) {
                return new ArrayList<>();
            }
            Collections.sort(mResults, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult lhs, ScanResult rhs) {
                    return rhs.level - lhs.level;
                }
            });
            for (ScanResult sr :
                    mResults) {
                list.add(new Ap(sr.SSID, sr.BSSID));
            }
            mResultsUpdated = false;
        }
        Log.i(TAG, "getAps: aps got");
        Log.d(TAG, "getAps: aps: " + list.toString());
        return list;
    }

    /**
     * Generates a fingerprint according to the given base. A timeout results in an empty return. Time consuming.
     * @param base A {@link Set} of {@link Ap} on which the calculation is based.
     * @param sampleCount How many samples to take to make a single Fingerprint.
     * @return The {@link Fingerprint}.
     */
    @Override
    @NonNull
    public Fingerprint generateFingerprint(Collection<Ap> base, int sampleCount) {
        List[] scans = new List[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            synchronized (mResults) {
                mWifi.scan();
                if (!mResultsUpdated) {
                    try {
                        Log.i(TAG, "generateFingerprint: waiting for scan results");
                        mResults.wait(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!mResultsUpdated) {
//                    return new Fingerprint();
                    continue;
                }
                scans[i] = new ArrayList<>(mResults);
                mResultsUpdated = false;
            }
        }

        Fingerprint fingerprint = new Fingerprint();
        for (Ap ap :
                base) {
//            int signal = 0;
            ArrayList<Integer> signals = new ArrayList<>(sampleCount);
            for (int i = 0; i < sampleCount; i++) {
                List scan = scans[i];
                if (null == scan) {
                    continue;
                }
                boolean found = false;
                for (int j = 0; j < scan.size(); j++) {
                    ScanResult scanResult = (ScanResult) scan.get(j);
                    if (scanResult.BSSID.equals(ap.mac)) {
//                        signal += WifiManager.calculateSignalLevel(scanResult.level, SIGNAL_LEVEL_NUM);
//                        signals.add(WifiManager.calculateSignalLevel(scanResult.level, SIGNAL_LEVEL_NUM));
                        signals.add(scanResult.level);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    signals.add(SIGNAL_LEVEL_MIN);
                }
            }
//            fingerprint.put(ap, signal);
            fingerprint.put(ap, signals);
        }
        Log.i(TAG, "generateFingerprint: fingerprint made: " + fingerprint.toString());
        return fingerprint;
    }

    @Override
    public void destroy() {
        mWifi.destroy();
        sInstance = null;
    }
}
