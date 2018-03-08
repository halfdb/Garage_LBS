package ecnu.cs14.garagelbs.guide.guide;

import android.content.Context;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.annotation.Nullable;
import android.util.Log;
import com.onlylemi.mapview.library.utils.MapMath;
import com.onlylemi.mapview.library.utils.MapUtils;
import ecnu.cs14.garagelbs.support.data.Position;
import ecnu.cs14.garagelbs.support.locating.DummyLocator;
import ecnu.cs14.garagelbs.support.locating.Locator;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Controlling.
 * Created by kel on 12/7/17.
 */

public class Guide {
    private final static String TAG = Guide.class.getName();

    private Position location = null;
//    private Position destination = null;
    int destinationMarkId = -1;

    private List<PointF> nodesList = null;
    private List<PointF> nodesContactList = null;
    private List<PointF> markList;

    private WeakReference<MainActivity> activityRef;

    final static int NOT_INITIALIZED_STATUS = -1;
    final static int STANDBY_STATUS = 0;
    final static int MARKED_STATUS = 1;
    final static int GUIDING_STATUS = 2;
    int status = NOT_INITIALIZED_STATUS;

    private boolean paused = false;

    private class UpdatingThread extends Thread {

        boolean running = false;
        Locator locator = null;
        private Context context;

        public UpdatingThread(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            super.run();
            Log.d(TAG, "run: UpdatingThread started running");
            try {
                locator = new DummyLocator(1200, 750, 500);
//                locator = new Locator(context, AlgorithmImpl.class);
                context = null;
                running = true;
            } catch (Exception e) {
                running = false;
                e.printStackTrace();
            }

            long time = 0;
            Position position;
            while (running) {
                position = locator.locate();
//                synchronized (Guide.this) {
                    Guide.this.updateLocation(position);
//                }
                long t;
                for (t = System.currentTimeMillis(); t - time < 1000; t = System.currentTimeMillis()) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                time = t;
            }
        }
    }

    private UpdatingThread thread = null;

    /**
     * Initializing.
     */
    void initialize(
            MainActivity activity,
//            MapView mapView,
//            MapLayer mapLayer,
//            LocationLayer locationLayer,
//            MarkLayer markLayer,
//            BitmapLayer bitmapLayer,
//            RouteLayer routeLayer
            List<PointF> nodesList,
            List<PointF> nodesContactList,
            List<PointF> markList
    ) {
        activityRef = new WeakReference<>(activity);

        // start location updating
        thread = new UpdatingThread(activity);
        thread.start();

        this.nodesList = nodesList;
        this.nodesContactList = nodesContactList;
        this.markList = markList;
        MapUtils.init(nodesList.size(), nodesContactList.size());

        status = STANDBY_STATUS;
    }

    void pause() {
        paused = true;
    }

    void resume() {
        paused = false;
    }

    void destroy() {
        // stop location updating
        thread.running = false;
    }

    private int findClosestPoint(List<PointF> list, PointF point) {
        float distance = Float.MAX_VALUE;
        int index = -1;
        for (int i=0; i < list.size(); i++) {
            float d = MapMath.getDistanceBetweenTwoPoints(list.get(i), point);
            if (d < distance) {
                distance = d;
                index = i;
            }
        }
        return index;
    }

    private int findClosestNode(PointF point) {
        return findClosestPoint(nodesList, point);
    }

    private int findClosestMark(Position position) {
        return findClosestPoint(markList, new PointF(position.x, position.y));
    }

    private int findClosestNode(Position position) {
        return findClosestNode(new PointF(position.x, position.y));
    }

    private int findCurrentClosestNode() {
        return findClosestNode(new PointF(location.x, location.y));
    }

    @Nullable
    private List<Integer> findPath() {
        if (destinationMarkId != -1) {
            return MapUtils.getShortestDistanceBetweenTwoPoints(
                    new PointF(location.x, location.y),
                    markList.get(destinationMarkId),
                    nodesList,
                    nodesContactList
            );
        } else {
            return null;
        }
    }

    void updateLocation(Position position) {
        if (paused) {
            return;
        }
        this.location = position;
        activityRef.get().updateLocation(new PointF(position.x, position.y));
        if (status == GUIDING_STATUS) {
            activityRef.get().setGuidingPath(findPath());
        }
        Log.i(TAG, "updateLocation: location updated: " + position.toString());
    }

    /**
     * Mark current destination. Changes status.
     */
    void markDestination() {
        if (status != STANDBY_STATUS) {
            return;
        }
        status = MARKED_STATUS;
//        destination = location;
        updateDestination(findClosestMark(location));
    }

    /**
     * Change the destination to the given node.
     * @param markId The target node ID.
     */
    void updateDestination(int markId) {
        if (status != MARKED_STATUS) {
            return;
        }
        destinationMarkId = markId;
        activityRef.get().markDestination(markList.get(markId));
    }

    /**
     * Remove the destination. Note: It changes status.
     */
    void removeDestination() {
        if (status != MARKED_STATUS) {
            return;
        }
        status = STANDBY_STATUS;
        destinationMarkId = -1;
        activityRef.get().removeDestination();
    }

    /**
     * Start guiding.
     */
    void startGuiding() {
        if (status != MARKED_STATUS) {
            return;
        }
        status = GUIDING_STATUS;

        activityRef.get().startGuiding(findPath());
    }

    /**
     * Finish guiding.
     */
    void finishGuiding() {
        if (status != GUIDING_STATUS) {
            return;
        }
        status = STANDBY_STATUS;
        destinationMarkId = -1;
        activityRef.get().finishGuiding();
        activityRef.get().setGuidingPath(null);
    }
}
