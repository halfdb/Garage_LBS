package ecnu.cs14.garagelbs.support.info;

import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.MapData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A set of MapDatas representing an area.
 * Created by K on 2017/1/25.
 */

final class MapSet {
    private List<MapData> mapDatas;
    private List<Ap> aps;
    private int selected = 0;

    MapSet(JSONObject json) throws JSONException {
        mapDatas = new ArrayList<>();
        aps = new ArrayList<>();
        JSONArray mapsJson = json.getJSONArray("maps");
        for (int i = 0; i < mapsJson.length(); i++) {
            mapDatas.add(new MapData(mapsJson.getJSONObject(i)));
        }
        JSONArray apsJson = json.getJSONArray("aps");
        for (int i = 0; i < apsJson.length(); i++) {
            aps.add(new Ap(apsJson.getJSONObject(i)));
        }
    }

    MapSet(List<MapData> maps) {
        this.mapDatas = new ArrayList<>(maps);
        for (MapData map:
             this.mapDatas) {
            aps.addAll(map.aps);
        }
    }

    JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        HashSet<Ap> aps = new HashSet<>();
        for (MapData map :
                mapDatas) {
            json.accumulate("maps", map.toJson());
            for (Ap ap :
                    map.aps) {
                aps.add(ap);
            }
        }
        for (Ap ap :
                aps) {
            json.accumulate("aps", ap.toJson());
        }
        return json;
    }

    /**
     * Get a suitable {@link MapData} according to present APs.
     * @param aps The {@link List} of {@link Ap}.
     * @return The {@link MapData}.
     */
    MapData getMap(List<Ap> aps) {
        int[] fitCount = new int[mapDatas.size()];
        int max = 0;
        int indexMax = 0;
        for (Ap ap :
                aps) {
            for (int i = 0; i < mapDatas.size(); i++) {
                if (mapDatas.get(i).aps.contains(ap)) {
                    if (++fitCount[i] > max) {
                        indexMax = i;
                        max = fitCount[i];
                    }
                }
            }
        }
        selected = indexMax;
        return mapDatas.get(selected);
    }

    MapData getSelectedMap(int index) {
        selected = index;
        return mapDatas.get(selected);
    }

    List<MapData> getMaps() {
        return mapDatas;
    }

    List<Ap> getAps() {
        return aps;
    }
}
