package ecnu.cs14.garagelbs.guide.guide;

import android.content.Context;
import android.graphics.PointF;
import com.onlylemi.mapview.library.utils.MapMath;
import ecnu.cs14.garagelbs.support.data.Position;
import ecnu.cs14.garagelbs.support.locating.DummyAlgorithm;
import ecnu.cs14.garagelbs.support.locating.Locator;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Controlling.
 * Created by kel on 12/7/17.
 */

public class Guide {

    private Position location = null;
//    private Position destination = null;
    private int destinationNodeId = -1;

    private List<PointF> nodesList = null;

    private WeakReference<MainActivity> activityRef;

    final static int NOT_INITIALIZED_STATUS = -1;
    final static int STANDBY_STATUS = 0;
    final static int MARKED_STATUS = 1;
    final static int GUIDING_STATUS = 2;
    int status = NOT_INITIALIZED_STATUS;

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
            try {
                locator = new Locator(context, DummyAlgorithm.class);
//                locator = new Locator(context, AlgorithmImpl.class);
                context = null;
                running = true;
            } catch (Exception e) {
                running = false;
                e.printStackTrace();
            }
            Position position;
            while (running) {
                position = locator.locate();
//                synchronized (Guide.this) {
                    Guide.this.updateLocation(position);
//                }
            }
        }
    }

    private UpdatingThread thread = null;

    /**
     * Initializing.
     */
    void initialize(
            MainActivity activity
//            MapView mapView,
//            MapLayer mapLayer,
//            LocationLayer locationLayer,
//            MarkLayer markLayer,
//            BitmapLayer bitmapLayer,
//            RouteLayer routeLayer
    ) {
        activityRef = new WeakReference<>(activity);

        // start location updating
        thread = new UpdatingThread(activity);
        thread.start();

        nodesList = TestData.getNodesList();

        status = STANDBY_STATUS;
    }

    void finialize() {
        // stop location updating
        thread.running = false;
    }

    private int findClosestNode(PointF point) {
        float distance = Float.MAX_VALUE;
        int index = -1;
        for (int i=0; i < nodesList.size(); i++) {
            float d = MapMath.getDistanceBetweenTwoPoints(nodesList.get(i), point);
            if (d < distance) {
                distance = d;
                index = i;
            }
        }
        return index;
    }

    private int findClosestNode(Position position) {
        return findClosestNode(new PointF(position.x, position.y));
    }

    void updateLocation(Position position) {
        // invoked by the child thread to update
        this.location = position;
    }

    /**
     * Mark current destination.
     */
    void markDestination() {
        if (status != STANDBY_STATUS) {
            return;
        }
        status = MARKED_STATUS;
//        destination = location;
        updateDestination(findClosestNode(location));
    }

    /**
     * Change the destination to the given node.
     * @param nodeID The target node ID.
     */
    void updateDestination(int nodeID) {
        if (status != MARKED_STATUS) {
            return;
        }
        destinationNodeId = nodeID;
        activityRef.get().markDestination(nodesList.get(nodeID));
    }

    /**
     * Remove the destination. Note: It changes status.
     */
    void removeDestination() {

    }

    /**
     * Start guiding.
     */
    void startGuiding() {

    }

    /**
     * Finish guiding.
     */
    void finishGuiding() {

    }
}
