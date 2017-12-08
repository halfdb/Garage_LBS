package ecnu.cs14.garagelbs.support.locating;

import android.util.Log;
import ecnu.cs14.garagelbs.support.data.Fingerprint;
import ecnu.cs14.garagelbs.support.data.MapData;
import ecnu.cs14.garagelbs.support.data.Position;

/**
 * A dummy that helps debugging.
 * Created by K on 2017/2/11.
 */

public final class DummyAlgorithm extends Algorithm {
    private static final String TAG = DummyAlgorithm.class.getName();

    public DummyAlgorithm(MapData map) {
        super(map);
        Log.d(TAG, "DummyAlgorithm: ctor called");
    }

    @Override
    public Position locate(Fingerprint fingerprint) {
        Log.d(TAG, "locate: called");
        return new Position(0, 0, 0);
    }

    @Override
    public void close() throws Exception {
        Log.d(TAG, "close: called");
    }
}
