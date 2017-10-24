package ecnu.cs14.garagelbs.support.info;

import ecnu.cs14.garagelbs.support.data.MapData;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Build a new map file.
 * Created by K on 17/9/27.
 */

public final class FileBuilder {
    public MapData targetMap = new MapData();

    public FileBuilder() {

    }

    public FileBuilder(MapData map) {
        setMap(map);
    }

    public void setMap(MapData map) {
        targetMap = map;
    }

    public void build() throws IOException, JSONException {
        List<MapData> list = new ArrayList<>(1);
        list.add(targetMap);
        MapSet targetMapSet = new MapSet(list);
        FileSystem fs = new FileSystem();
        fs.saveMapSet(targetMapSet);
    }
}
