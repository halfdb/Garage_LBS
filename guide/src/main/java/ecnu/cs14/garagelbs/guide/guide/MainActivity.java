package ecnu.cs14.garagelbs.guide.guide;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.onlylemi.mapview.library.MapView;
import com.onlylemi.mapview.library.MapViewListener;
import com.onlylemi.mapview.library.layer.BitmapLayer;
import com.onlylemi.mapview.library.layer.LocationLayer;
import com.onlylemi.mapview.library.layer.MarkLayer;
import com.onlylemi.mapview.library.layer.RouteLayer;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BitmapLayer.OnBitmapClickListener, MarkLayer.MarkIsClickListener {
    private final static String TAG = MainActivity.class.getCanonicalName();

    private Guide guide = new Guide();
    private MapView mapView = null;
    private BitmapLayer bitmapLayer = null;
    private LocationLayer locationLayer = null;
    private RouteLayer routeLayer = null;
    private MarkLayer markLayer = null;

    private FloatingActionButton fab = null;
    private List<PointF> marks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        setFabDrawable(R.drawable.ic_add_location_white_24px);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (guide.status) {
                    case Guide.STANDBY_STATUS:
                        guide.markDestination();
                        setFabDrawable(R.drawable.ic_directions_white_24px);
                        break;
                    case Guide.MARKED_STATUS:
                        guide.startGuiding();
                        setFabDrawable(R.drawable.ic_done_white_24px);
                        break;
                    case Guide.GUIDING_STATUS:
                        guide.finishGuiding();
                        setFabDrawable(R.drawable.ic_add_location_white_24px);
                        break;
                    case Guide.NOT_INITIALIZED_STATUS:
                    default:
                        break;
                }
            }
        });

        final List<PointF> nodesList = TestData.getNodesList();
        List<PointF> nodesContactList = TestData.getNodesContactList();
        marks = TestData.getMarks();
        final List<String> marksName = TestData.getMarksName();

        mapView = findViewById(R.id.mapview);
        mapView.setMapViewListener(new MapViewListener() {
            @Override
            public void onMapLoadSuccess() {
                Bitmap bmp = null;
                try {
                    bmp = BitmapFactory.decodeStream(getAssets().open("car.png"));
                } catch (IOException e) {
                    Log.e(TAG, "onMapLoadSuccess: loading bitmap mark failed", e);
                    e.printStackTrace();
                }
                bitmapLayer = new BitmapLayer(mapView, bmp);
                SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                bitmapLayer.isVisible = preferences.getBoolean(KEY_BMP_VISIBLE, false);
                bitmapLayer.setLocation(new PointF(
                        preferences.getFloat(KEY_BMP_X, 0),
                        preferences.getFloat(KEY_BMP_Y, 0)
                ));
                bitmapLayer.setOnBitmapClickListener(MainActivity.this);
                mapView.addLayer(bitmapLayer);

                locationLayer = new LocationLayer(mapView);
                locationLayer.setOpenCompass(false);
                mapView.addLayer(locationLayer);

                routeLayer = new RouteLayer(mapView, nodesList, null);
                mapView.addLayer(routeLayer);

                markLayer = new MarkLayer(mapView, marks, marksName);
                markLayer.isVisible = false;
                markLayer.setMarkIsClickListener(MainActivity.this);
                mapView.addLayer(markLayer);

                mapView.refresh();
            }

            @Override
            public void onMapLoadFail() {

            }
        });
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(getAssets().open("map.png"));
            mapView.loadMap(bitmap);
        } catch (IOException e) {
            Log.e(TAG, "onCreate: loading map image failed", e);
            e.printStackTrace();
        }

        guide.initialize(this, nodesList, nodesContactList, marks);
    }

    private static final String KEY_STATUS = "status";
    private static final String KEY_DEST_ID = "destId";
    private static final String KEY_BMP_VISIBLE = "bmpVisible";
    private static final String KEY_BMP_X = "bmpX";
    private static final String KEY_BMP_Y = "bmpY";

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putInt(KEY_STATUS, guide.status);
        editor.putInt(KEY_DEST_ID, guide.destinationMarkId);
        editor.putBoolean(KEY_BMP_VISIBLE, bitmapLayer.isVisible);
        PointF point = bitmapLayer.getLocation();
        editor.putFloat(KEY_BMP_X, point.x);
        editor.putFloat(KEY_BMP_Y, point.y);
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        guide.status = preferences.getInt(KEY_STATUS, Guide.STANDBY_STATUS);
        switch (guide.status) {
            case Guide.STANDBY_STATUS:
                setFabDrawable(R.drawable.ic_add_location_white_24px);
                break;
            case Guide.MARKED_STATUS:
                setFabDrawable(R.drawable.ic_directions_white_24px);
                break;
            case Guide.GUIDING_STATUS:
                setFabDrawable(R.drawable.ic_done_white_24px);
                break;
            case Guide.NOT_INITIALIZED_STATUS:
            default:
                break;
        }
        guide.destinationMarkId = preferences.getInt(KEY_DEST_ID, -1);
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putInt(KEY_STATUS, guide.status);
//        outState.putInt(KEY_DEST_ID, guide.destinationMarkId);
//        outState.putBoolean(KEY_BMP_VISIBLE, bitmapLayer.isVisible);
//        PointF point = bitmapLayer.getLocation();
//        outState.putFloat(KEY_BMP_X, point.x);
//        outState.putFloat(KEY_BMP_Y, point.y);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        guide.status = savedInstanceState.getInt(KEY_STATUS);
//        switch (guide.status) {
//            case Guide.STANDBY_STATUS:
//                setFabDrawable(R.drawable.ic_add_location_white_24px);
//                break;
//            case Guide.MARKED_STATUS:
//                setFabDrawable(R.drawable.ic_directions_white_24px);
//                break;
//            case Guide.GUIDING_STATUS:
//                setFabDrawable(R.drawable.ic_done_white_24px);
//                break;
//            case Guide.NOT_INITIALIZED_STATUS:
//            default:
//                break;
//        }
//        guide.destinationMarkId = savedInstanceState.getInt(KEY_DEST_ID);
//        bitmapLayer.isVisible = savedInstanceState.getBoolean(KEY_BMP_VISIBLE);
//        bitmapLayer.setLocation(new PointF(
//                savedInstanceState.getFloat(KEY_BMP_X),
//                savedInstanceState.getFloat(KEY_BMP_Y)
//        ));
//    }

    private void setFabDrawable(@DrawableRes int id) {
        fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(), id, null));
    }

    public void updateLocation(PointF location) {
        if (locationLayer != null) {
            locationLayer.setCurrentPosition(location);
            mapView.refresh();
        }
    }

    public void markDestination(PointF destination) {
        bitmapLayer.setLocation(destination);
        bitmapLayer.isVisible = true;
        mapView.refresh();
    }

    void removeDestination() {
        bitmapLayer.isVisible = false;
        setFabDrawable(R.drawable.ic_add_location_white_24px);
        mapView.refresh();
    }

    void startGuiding(List<Integer> path) {
        setGuidingPath(path);
    }

    void setGuidingPath(List<Integer> path) {
        routeLayer.setRouteList(path);
        mapView.refresh();
    }

    void finishGuiding() {
        removeDestination();
    }

    @Override
    protected void onPause() {
        super.onPause();
        guide.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        guide.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        guide.destroy();
    }

    @Override
    public void onBitmapClick(BitmapLayer bitmapLayer) {
        if (guide.status != Guide.MARKED_STATUS) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改或删除标记位置")
               .setPositiveButton("修改", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       markLayer.isVisible = true;
                       mapView.refresh();
                       Toast.makeText(MainActivity.this, "请选择所在车位", Toast.LENGTH_SHORT).show();
                   }
               })
               .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       guide.removeDestination();
                   }
               })
               .setCancelable(true)
               .show();
    }

    @Override
    public void markIsClick(int i) {
        if (markLayer.isVisible) {
            markLayer.isVisible = false;
            guide.updateDestination(i);
        }
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
