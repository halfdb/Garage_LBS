package ecnu.cs14.garagelbs.locator;

import android.util.Pair;
import ecnu.cs14.garagelbs.support.data.Fingerprint;
import ecnu.cs14.garagelbs.support.data.MapData;

/**
 * An abstract locating algorithm.
 * Created by K on 2017/2/11.
 */

public abstract class Algorithm {
    public Algorithm(MapData map) {

    }
    public abstract Pair<Integer, Integer> locate(Fingerprint fingerprint);
    public abstract void close() throws Exception;
}
