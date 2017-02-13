package ecnu.cs14.garagelbs.support.info;

import android.content.Context;
import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.MapData;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link SpaceInfo} as well as an adapter of the whole module.
 * Created by K on 2017/2/1.
 */

final class SpaceInfoAdapter extends SpaceInfo {
    private FileSystem fileSystem;
    private MapSetSelector selector;
    private String mapSetFilename;
    private MapSet mapSet = null;

    SpaceInfoAdapter(Context context) {
        super(context);
        fileSystem = new FileSystem();
        selector = new MapSetSelector(fileSystem);
    }

    private void initMapSet(List<Ap> aps) throws IOException, JSONException {
        mapSetFilename = selector.selectFilename(aps);
        mapSet = new MapSet(fileSystem.getMapSetJson(mapSetFilename));
    }

    @Override
    public List<MapData> getAllMaps(List<Ap> aps) {
        try {
            if (mapSet == null) {
                initMapSet(aps);
            }
            return new ArrayList<>(mapSet.getMaps());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MapData autoSelectMap(List<Ap> aps) {
        try {
            if (mapSet == null) {
                initMapSet(aps);
            }
            return mapSet.getMap(aps);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MapData selectMap(int index) {
        if (mapSet != null) {
            return mapSet.getSelectedMap(index);
        }
        return null;
    }

    @Override
    public void updateMap(int index, MapData map) {
        mapSet.getMaps().set(index, map);
    }

    @Override
    public void saveAllMaps() {
        try {
            fileSystem.saveMapSet(mapSetFilename, mapSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
