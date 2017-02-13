package ecnu.cs14.garagelbs.support.data;

import android.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * AP data structure.
 * Created by K on 2017/1/22.
 */

public final class Ap extends Pair<String, String> {
    public Ap(String ssid, String mac) {
        super(ssid, mac);
    }
    public Ap(JSONObject json) throws JSONException {
        super(json.optString("ssid", ""), json.getString("mac"));
    }

    public final String ssid = first;
    public final String mac = second;

    public JSONObject toJson() throws JSONException {
        return new JSONObject().putOpt("ssid", ssid).put("mac", mac);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Ap) {
            return mac.equals(((Ap) o).mac);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return mac.hashCode();
    }
}
