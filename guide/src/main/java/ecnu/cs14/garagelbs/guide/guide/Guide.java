package ecnu.cs14.garagelbs.guide.guide;

import android.content.Context;
import com.onlylemi.mapview.library.MapView;
import com.onlylemi.mapview.library.layer.*;
import ecnu.cs14.garagelbs.support.locating.Locator;

/**
 * Controlling.
 * Created by kel on 12/7/17.
 */

public class Guide {
    /**
     * Initializing.
     */
    void initialize(
            Context context,
            MapView mapView,
            MapLayer mapLayer,
            LocationLayer locationLayer,
            MarkLayer markLayer,
            BitmapLayer bitmapLayer,
            RouteLayer routeLayer
    ) {
        // start location updating
    }

    void finialize() {
        // stop location updating
    }

    void updateLocation() {
        // invoked by the child thread to update
    }

    /**
     * Mark current destination.
     */
    void markDestination() {

    }

    /**
     * Change the destination to the given node.
     * @param nodeID The target node ID.
     */
    void updateDestination(int nodeID) {

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
