package ecnu.cs14.garagelbs.support.info;

import android.content.Context;
import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.MapData;

import java.util.List;

/**
 * A map data provider. Also supports map updating.
 * Created by K on 2017/2/1.
 */

public abstract class SpaceInfo {
    SpaceInfo(Context context) {

    }

    public abstract List<MapData> getAllMaps(List<Ap> aps);
    public abstract MapData autoSelectMap(List<Ap> aps);
    public abstract MapData selectMap(int index);
    public abstract void updateMap(int index, MapData map);
    public abstract void saveAllMaps();

    public static SpaceInfo getInstance(Context context) {
        return new SpaceInfoAdapter(context);
    }
}
