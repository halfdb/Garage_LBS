package ecnu.cs14.garagelbs.locator;

import android.os.Environment;
import android.util.Pair;
import ecnu.cs14.garagelbs.support.data.Fingerprint;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logs the test results.
 * Created by K on 2017/3/7.
 */

class TestLogger {
    private FileOutputStream out;
    TestLogger() throws FileNotFoundException {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        path = new File(path, "GarageLBSTest");
        path.mkdirs();
        path = new File(path, new SimpleDateFormat("MM-dd-HH-mm-ss").format(new Date()) + ".txt");
        out = new FileOutputStream(path);
    }

    void log(Test test) {
        try {
            out.write((test.toString() + '\n').getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void log(Fingerprint fingerprint, Pair<Integer, Integer> calculated, Pair<Integer, Integer> actual) {
        log(new Test(fingerprint, calculated, actual));
    }

    void close() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Test {
        Fingerprint fingerprint;
        Pair<Integer, Integer> calculatedPosition;
        Pair<Integer, Integer> actualPosition;

        Test(Fingerprint fingerprint, Pair<Integer, Integer> calculated, Pair<Integer, Integer> actual) {
            this.fingerprint = fingerprint;
            calculatedPosition = calculated;
            actualPosition = actual;
        }

        double error() {
            double dx = calculatedPosition.first - actualPosition.first;
            double dy = calculatedPosition.second - actualPosition.second;
            return Math.sqrt(dx * dx + dy * dy);
        }

        @Override
        public String toString() {
            return fingerprint.toString() + ' ' +
                    pairToString(calculatedPosition) + ' ' +
                    pairToString(actualPosition);
        }

        String pairToString(Pair<Integer, Integer> pair) {
            return '(' + pair.first.toString() + ", " + pair.second.toString() + ')';
        }
    }
}
