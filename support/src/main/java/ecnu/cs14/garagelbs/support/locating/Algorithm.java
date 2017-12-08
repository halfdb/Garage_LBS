package ecnu.cs14.garagelbs.support.locating;

import ecnu.cs14.garagelbs.support.data.Fingerprint;
import ecnu.cs14.garagelbs.support.data.MapData;
import ecnu.cs14.garagelbs.support.data.Pair;
import ecnu.cs14.garagelbs.support.data.Position;

/**
 * An abstract locating algorithm.
 * Created by K on 2017/2/11.
 */

public abstract class Algorithm {
    public Algorithm(MapData map) {

    }
    public abstract Position locate(Fingerprint fingerprint);
    public abstract void close() throws Exception;
}
