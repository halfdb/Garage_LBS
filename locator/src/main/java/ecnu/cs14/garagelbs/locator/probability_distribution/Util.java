package ecnu.cs14.garagelbs.locator.probability_distribution;

import android.util.Pair;
import android.util.SparseIntArray;
import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.Fingerprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utilities for the package.
 * Created by K on 2017/2/14.
 */

final class Util {
    static SparseIntArray count(List<Integer> numbers) {
        SparseIntArray array = new SparseIntArray();
        Collections.sort(numbers);
        int count = 1;
        Integer signal = -1;
        for (int i = 0; i < numbers.size(); i++) {
            if (numbers.get(i).equals(signal)) {
                count++;
            } else {
                if (!signal.equals(-1)) {
                    array.put(signal, count);
                    count = 1;
                }
                signal = numbers.get(i);
            }
        }
        return array;
    }

    static Fingerprint tailor(Fingerprint original, int count) {
        Fingerprint tailored = new Fingerprint();
        tailored.sampleCount = original.sampleCount;
        List<Pair<Ap, Integer>> signal = new ArrayList<>();
        for (Ap ap :
                original.keySet()) {
            int total = 0;
            for (int s :
                    original.get(ap)) {
                total += s;
            }
            signal.add(new Pair<>(ap, total));
        }
        Collections.sort(signal, new Comparator<Pair<Ap, Integer>>() {
            @Override
            public int compare(Pair<Ap, Integer> lhs, Pair<Ap, Integer> rhs) {
                return rhs.second - lhs.second;
            }
        });
        for (int i = 0; i < count && i < signal.size(); i++) {
            Pair<Ap, Integer> pair = signal.get(i);
            tailored.put(pair.first, original.get(pair.first));
        }
        return tailored;
    }
}
