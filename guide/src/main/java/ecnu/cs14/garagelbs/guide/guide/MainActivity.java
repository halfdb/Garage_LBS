package ecnu.cs14.garagelbs.guide.guide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import com.onlylemi.mapview.library.MapView;
import com.onlylemi.mapview.library.MapViewListener;
import com.onlylemi.mapview.library.layer.BitmapLayer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getCanonicalName();

    private Guide guide = new Guide();
    private MapView mapView = null;
    private BitmapLayer bitmapLayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
//                markDestination(new PointF(200, 200));
            }
        });

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
                bitmapLayer.isVisible = false;
                mapView.addLayer(bitmapLayer);

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

        guide.initialize(this);
    }

    public void markDestination(PointF destination) {
        bitmapLayer.setLocation(destination);
        bitmapLayer.isVisible = true;
        mapView.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
