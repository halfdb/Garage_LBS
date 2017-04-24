package ecnu.cs14.garagelbs.locator;

import android.os.Environment;
import ecnu.cs14.garagelbs.support.data.Fingerprint;
import ecnu.cs14.garagelbs.support.data.Pair;
import ecnu.cs14.garagelbs.support.data.Position;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void log(Fingerprint fingerprint, Position calculated, Position actual) {
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
        Position calculatedPosition;
        Position actualPosition;

        Test(Fingerprint fingerprint, Position calculated, Position actual) {
            this.fingerprint = fingerprint;
            calculatedPosition = calculated;
            actualPosition = actual;
        }

        double error() {
            double dx = calculatedPosition.x - actualPosition.x;
            double dy = calculatedPosition.y - actualPosition.y;
            double dz = calculatedPosition.z - actualPosition.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        double error2d() {
            double dx = calculatedPosition.x - actualPosition.x;
            double dy = calculatedPosition.y - actualPosition.y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        @Override
        public String toString() {
            return fingerprint.toString() + ' ' +
                    calculatedPosition.toString() + ' ' +
                    actualPosition.toString();
        }

        String pairToString(Pair<Integer, Integer> pair) {
            return '(' + pair.first.toString() + ", " + pair.second.toString() + ')';
        }
    }
}
