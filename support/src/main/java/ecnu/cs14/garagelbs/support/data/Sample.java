package ecnu.cs14.garagelbs.support.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Sample data structure.
 * Created by K on 2017/1/23.
 */

public final class Sample extends Pair<Pair<Integer, Integer>, Fingerprint> {
    public Sample(List<Ap> base, JSONObject json) throws JSONException{
        super(new Pair<>(json.getInt("x"), json.getInt("y")),
                new Fingerprint(base, json.getJSONArray("fingerprint"))
        );
    }

    public Sample(Pair<Integer, Integer> position, Fingerprint fingerprint) {
        super(position, fingerprint);
    }

    public JSONObject toJson(List<Ap> base) throws JSONException {
        return new JSONObject().put("x", first.first).put("y", first.second).put("fingerprint", second.toJson(base));
    }
}
