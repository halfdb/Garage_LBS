package ecnu.cs14.garagelbs.sniffer;

import android.content.Context;
import ecnu.cs14.garagelbs.support.data.*;
import ecnu.cs14.garagelbs.support.env.Environment;
import ecnu.cs14.garagelbs.support.info.SpaceInfo;

import java.util.HashSet;
import java.util.List;

/**
 * Provides integrated and stateful use of the packages.
 * Created by K on 2017/1/29.
 */

public final class Sniffer {
    private Environment environment;
    private List<MapData> mapDatas;
    private int mapIndex;
    private MapData mapData;
    private SpaceInfo spaceInfo;
    private boolean isChanged;

    /**
     * A time-consuming constructor.
     * @param context Context.
     */
    public Sniffer(Context context) {
        environment = Environment.getInstance(context);
        List<Ap> aps = environment.getAps();
        spaceInfo = SpaceInfo.getInstance(context);

        mapDatas = spaceInfo.getAllMaps(aps);
        MapData originalMap = spaceInfo.autoSelectMap(aps);
        mapData = copyMapBase(originalMap);
        mapIndex = mapDatas.indexOf(originalMap);
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
        return mapDatas;
    }

    /**
     * Changes to another map.
     * @param index The index of the MapData in the {@link List} given by {@code getMaps()}.
     * @param storePrevious Whether previous chosen {@link MapData} should be saved.
     */
    public void changeMap(int index, boolean storePrevious) {
        if (storePrevious) {
            save();
        }
        mapIndex = index;
        mapData = copyMapBase(spaceInfo.selectMap(index));
    }

    /**
     * Get the index of the current MapData in the {@link List} given by {@code getMaps()}.
     * @return The index.
     */
    public int getMapIndex() {
        return mapIndex;
    }

    private static final int sampleCount = 20;
    /**
     * Get the fingerprint at this position. Time-consuming.
     * @return The fingerprint.
     */
    public Fingerprint getFingerprint() {
        return environment.generateFingerprint(mapData.aps, sampleCount);
    }

    /**
     * Store the Sample given.
     * @param position A {@link Pair} of {@link Integer} indicating the position.
     * @param fingerprint The fingerprint.
     */
    public void storeSample(Pair<Integer, Integer> position, Fingerprint fingerprint) {
        mapData.samples.add(new Sample(position, fingerprint));
        isChanged = true;
    }

    public boolean needsSaving() {
        return isChanged;
    }

    public void save() {
        if (!needsSaving()) {
            return;
        }
        spaceInfo.updateMap(mapIndex, mapData);
        spaceInfo.saveAllMaps();
        isChanged = false;
    }

    /**
     * Finish the work.
     */
    public void finish() {
        environment.destroy();
    }
}
