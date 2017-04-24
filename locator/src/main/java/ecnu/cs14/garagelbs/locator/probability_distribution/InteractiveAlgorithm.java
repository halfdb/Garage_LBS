package ecnu.cs14.garagelbs.locator.probability_distribution;

import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.Fingerprint;
import ecnu.cs14.garagelbs.support.data.MapData;
import ecnu.cs14.garagelbs.support.info.MapSet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * An interface to apply the algorithm.
 * Created by K on 2017/3/15.
 */

public class InteractiveAlgorithm {

    private static String streamToString(InputStream stream) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(stream);
        for (; ; ) {
            int readSize = in.read(buffer, 0, buffer.length);
            if (readSize < 0)
                break;
            out.append(buffer, 0, readSize);
        }
        return out.toString();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Unexpected arguments.");
            return;
        }
        FileInputStream mapFile;
        try {
            mapFile = new FileInputStream(new File(args[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        MapData mapData;
        try {
            String json = streamToString(mapFile);
            MapSet mapSet = new MapSet(new JSONObject(json));
            mapData = mapSet.getSelectedMap(0);
        } catch (IOException e) {
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        AlgorithmImpl algorithm = new AlgorithmImpl(mapData);
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            // restore the fingerprint
            Fingerprint f = new Fingerprint();
            String line = in.nextLine();
            if ("".equals(line)) {
                break;
            }
            // a line is made up by 'AP .* [signal1, signal2...]
            for (Ap ap :
                    mapData.aps) {
                List<Integer> signals = new ArrayList<>();
                int start = line.indexOf(ap.mac) + ap.mac.length();
                start = line.indexOf('[', start) + 1;
//                while (true) {
//                    int end = line.indexOf(',', start);
//                    if (line.indexOf(']', start) < end || end == -1) {
//                        end = line.indexOf(']', start);
//                        signals.add(Integer.valueOf(line.substring(start, end).replaceAll("[,\\s]", "")));
//                        break;
//                    }
//                    signals.add(Integer.valueOf(line.substring(start, end).replaceAll("[,\\s]", "")));
//                    start = end + 1;
//                }
                int end = line.indexOf(']', start);
                String[] signalStrings = line.substring(start, end).split(",");
                for (String s:
                     signalStrings) {
                    signals.add(Integer.valueOf(s.replaceAll("\\s", "")));
                }
                f.put(ap, signals);
                f.sampleCount = signals.size();
            }
            System.out.println(algorithm.locate(f));
        }
    }
}
