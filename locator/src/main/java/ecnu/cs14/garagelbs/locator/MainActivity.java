package ecnu.cs14.garagelbs.locator;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public final class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();

    private MapView mMapView;
    private ProgressDialog waitingDialog;
    private Locator mLocator;
    private final MainActivityHandler mHandler = new MainActivityHandler(this);


    private static final class MainActivityHandler extends Handler {
        private final WeakReference<MainActivity> mActivityRef;

        final static int MSG_SNIFFER = 0;
        final static int MSG_FINGERPRINT = 1;
        final static int MSG_POSITION_STRING = 2;
        final static int MSG_LOCATOR = 3;
        final static int MSG_POSITION_PAIR = 4;

        MainActivityHandler(MainActivity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_LOCATOR:
                {
                    mActivityRef.get().receiveLocator((Locator) msg.obj);
                    break;
                }
                case MSG_POSITION_PAIR:
                {
                    mActivityRef.get().updatePosition((Pair<Integer, Integer>) msg.obj);
                    break;
                }
            }
        }
    }

    private long mLastUpdateTime = 0;
    private boolean mPositionNeedsUpdating = true;
    private void updatePosition(Pair<Integer, Integer> position) {
        long time = System.currentTimeMillis();
        if (mPositionNeedsUpdating && time - mLastUpdateTime > 3000) {
            if (position != null) {
                mMapView.setPositionDot(position);
                mLastUpdateTime = System.currentTimeMillis();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.obj = mLocator.locate();
                    msg.what = MainActivityHandler.MSG_POSITION_PAIR;
                    mHandler.sendMessage(msg);
                }
            }).start();
        }
    }

    private void receiveLocator(final Locator locator) {
        if (locator == null) {
            Log.e(TAG, "receiveLocator: No locator available");
            Toast.makeText(this, "无法定位", Toast.LENGTH_LONG).show();
            return;
        }
        mLocator = locator;
        updatePosition(null);
        mMapView.setVisibility(View.VISIBLE);
        mMapView.setMap(locator.getMaps().get(locator.getMapIndex()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    msg.obj = new Locator(MainActivity.this, DummyAlgorithm.class);
                } catch (Throwable e) {
                    msg.obj = null;
                }
                msg.what = MainActivityHandler.MSG_LOCATOR;
                mHandler.sendMessage(msg);
            }
        }).start();
        tryRequestPermissions();

        waitingDialog = new ProgressDialog(this);
        waitingDialog.setMessage("请稍候");
        waitingDialog.setCancelable(false);
        waitingDialog.setIndeterminate(true);

        mMapView = (MapView) findViewById(R.id.map_view);
        if (mMapView == null) {
            finish();
        }
        View progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            mMapView.setEmptyView(progressBar);
        }
        mMapView.setVisibility(View.GONE);
    }

    private void tryRequestPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        boolean requestNeeded = false;

        for (String permission :
                permissions) {
            requestNeeded |= (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED);
        }
        if (requestNeeded) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result :
                grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "未获得权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPositionNeedsUpdating = false;
    }
}
