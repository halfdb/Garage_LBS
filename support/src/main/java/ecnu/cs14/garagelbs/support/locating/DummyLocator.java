package ecnu.cs14.garagelbs.support.locating;

import ecnu.cs14.garagelbs.support.data.Fingerprint;
import ecnu.cs14.garagelbs.support.data.Position;

import java.util.Random;

/**
 * Created by kel on 2/21/18.
 */

public final class DummyLocator extends Locator {
    private Random random;
    private int xRange;
    private int yRange;
    private int zRange;
    /**
     * A time-consuming constructor.
     */
    public DummyLocator(int xRange, int yRange, int zRange) {
        super();
        random = new Random(System.currentTimeMillis());
        this.xRange = xRange;
        this.yRange = yRange;
        this.zRange = zRange;
    }

    @Override
    public Fingerprint getFingerprint() {
        return null;
    }

    @Override
    public Position locate(Fingerprint fingerprint) {
        return new Position(random.nextInt(xRange), random.nextInt(yRange), random.nextInt(zRange));
    }
}
