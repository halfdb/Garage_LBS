package ecnu.cs14.garagelbs.support.locating.probability_distribution;

import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.Fingerprint;
import ecnu.cs14.garagelbs.support.data.Pair;

import java.util.*;

/**
 * Utilities for the package.
 * Created by K on 2017/2/14.
 */

final class Util {
    static HashMap<Integer, Integer> count(List<Integer> numbers) {
        HashMap<Integer, Integer> array = new HashMap<>();
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
        array.put(signal, count);
        return array;
    }

    static Fingerprint tailor(Fingerprint original, int count) {
        Fingerprint tailored = new Fingerprint();
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
