package ecnu.cs14.garagelbs.locator;

import android.content.Context;
import android.util.Log;
import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.Fingerprint;
import ecnu.cs14.garagelbs.support.data.MapData;
import ecnu.cs14.garagelbs.support.data.Pair;
import ecnu.cs14.garagelbs.support.env.Environment;
import ecnu.cs14.garagelbs.support.info.SpaceInfo;

import java.util.HashSet;
import java.util.List;

/**
 * A locator using a substitutable algorithm.
 * Created by K on 2017/2/10.
 */

public final class Locator {
    private static final String TAG = Locator.class.getName();

    private Environment environment;
    private List<MapData> maps;
    private int mapIndex;
    private MapData map;
    private SpaceInfo spaceInfo;
    private Algorithm algorithm;

    /**
     * A time-consuming constructor.
     * @param context Context.
     */
    public Locator(Context context, Class<? extends Algorithm> algorithmClass) throws Exception {
        environment = Environment.getInstance(context);
        List<Ap> aps = environment.getAps();
        spaceInfo = SpaceInfo.getInstance(context);

        maps = spaceInfo.getAllMaps(aps);
        map = spaceInfo.autoSelectMap(aps);
        mapIndex = maps.indexOf(map);

        try {
            algorithm = algorithmClass.getConstructor(MapData.class).newInstance(map);
        } catch (Exception e) {
            Log.e(TAG, "Locator: failed to instantiate the AlgorithmImpl: " + algorithmClass.getName(), e);
            throw e;
        }
    }

    private static MapData copyMapBase(MapData originalMap) {
        MapData map = new MapData();
        map.aps.addAll(originalMap.aps);
        map.height = originalMap.height;
        map.width = originalMap.width;
        map.name = originalMap.name;
        map.shapes = new HashSet<>(originalMap.shapes);
        return map;
    }

    /**
     * Get the maps available in the area.
     * @return A {@link List} of the available Maps.
     */
    public List<MapData> getMaps() {
        return maps;
    }

    /**
     * Changes to another map.
     * @param index The index of the map in the {@link List} given by {@code getMaps()}.
     *
     */
    public void changeMap(int index) {
        mapIndex = index;
        map = spaceInfo.selectMap(index);
    }

    /**
     * Get the index of the current map in the {@link List} given by {@code getMaps()}.
     * @return The index.
     */
    public int getMapIndex() {
        return mapIndex;
    }

    private static final int sampleCount = 5;
    /**
     * Get the fingerprint at this position. Time-consuming.
     * @return The fingerprint.
     */
    public Fingerprint getFingerprint() {
        return environment.generateFingerprint(map.aps, sampleCount);
    }

    public Pair<Integer, Integer> locate(Fingerprint fingerprint) {
        return algorithm.locate(fingerprint);
    }

    public Pair<Integer, Integer> locate() {
        return locate(getFingerprint());
    }

    /**
     * Finish the work.
     */
    public void finish() {
        environment.destroy();
        try {
            algorithm.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
