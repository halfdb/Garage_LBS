package ecnu.cs14.garagelbs.support.env;

import android.content.Context;
import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.Fingerprint;

import java.util.Collection;
import java.util.List;

/**
 * Provides all APs and generates fingerprint according to a base.
 * Created by K on 2017/1/21.
 */

public abstract class Environment {
    final static int SIGNAL_LEVEL_NUM = 10;
    Environment(Context context) {

    }
    public abstract List<Ap> getAps();
    public abstract Fingerprint generateFingerprint(Collection<Ap> base);
    public abstract void destroy();

    public static Environment getInstance(Context context) {
        return new EnvironmentImpl(context);
    }
}
