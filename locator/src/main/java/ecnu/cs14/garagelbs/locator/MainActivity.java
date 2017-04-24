package ecnu.cs14.garagelbs.locator;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ecnu.cs14.garagelbs.locator.probability_distribution.AlgorithmImpl;
import ecnu.cs14.garagelbs.support.data.Fingerprint;
import ecnu.cs14.garagelbs.support.data.Pair;
import ecnu.cs14.garagelbs.support.data.Position;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public final class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();

    private MapView mMapView;
    private ProgressDialog waitingDialog;
    private TextView mErrorTextView;
    private Locator mLocator;
    private TestLogger mLogger;
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
                case MSG_FINGERPRINT:
                {
                    mActivityRef.get().receiveFingerprint((Fingerprint) msg.obj);
                    break;
                }
                case MSG_LOCATOR:
                {
                    mActivityRef.get().receiveLocator((Locator) msg.obj);
                    break;
                }
                case MSG_POSITION_PAIR:
                {
                    mActivityRef.get().updatePosition((Position) msg.obj);
                    break;
                }
                case MSG_POSITION_STRING:
                {
                    mActivityRef.get().receivePositionString((String) msg.obj);
                    break;
                }
            }
        }
    }

    private long mLastUpdateTime = 0;
    private boolean mWorking = true;
    private void updatePosition(Position position) {
        long time = System.currentTimeMillis();
        if (mWorking && time - mLastUpdateTime > 3000) {
            if (position != null) {
                mMapView.setPositionDot(position);
                mLastUpdateTime = System.currentTimeMillis();
            }
        }
        // startUpdating();
    }

    public void startTesting(View v) {
        if (null == mLocator) {
            return;
        }
        waitingDialog.show();
        startUpdating();
        showPositionInputDialog();
    }

    private void showPositionInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setText("300 400 100");
        editText.selectAll();
        builder.setTitle("输入当前坐标")
                .setView(editText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Message msg = new Message();
                        msg.obj = editText.getText().toString();
                        msg.what = MainActivityHandler.MSG_POSITION_STRING;
                        mHandler.sendMessage(msg);
                    }
                })
                .setCancelable(false)
                .show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask()   {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) MainActivity.this.getSystemService(INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, 500);
    }

    private void startUpdating() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.obj = mLocator.getFingerprint();
                msg.what = MainActivityHandler.MSG_FINGERPRINT;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    private Position mActualPosition;
    private boolean mPositionUpdated = false;
    private synchronized void receivePositionString(String string) {
        String[] stringPair = string.split("\\s");
        if (stringPair.length == 3) {
            mActualPosition = new Position(
                    Integer.valueOf(stringPair[0]),
                    Integer.valueOf(stringPair[1]),
                    Integer.valueOf(stringPair[2])
            );
//            if (mActualPosition.x != null && mActualPosition.y != null) {
            mPositionUpdated = true;
//            }
        }
        if (!mPositionUpdated) {
            showPositionInputDialog();
        } else if (mFingerprintUpdated) {
            logTest();
        }
    }

    private boolean mFingerprintUpdated = false;
    private Fingerprint mFingerprint;
    private Position mCalculatedPosition;
    private synchronized void receiveFingerprint(Fingerprint fingerprint) {
        mFingerprint = fingerprint;
        mFingerprintUpdated = true;
        mCalculatedPosition = mLocator.locate(mFingerprint);
        updatePosition(mCalculatedPosition);
        if (mPositionUpdated) {
            logTest();
        }
    }

    private synchronized void logTest() {
        TestLogger.Test test = new TestLogger.Test(mFingerprint, mCalculatedPosition, mActualPosition);
        mLogger.log(test);
        mErrorTextView.setText("实际：" + mActualPosition.toString()
                + "\n计算：" + mCalculatedPosition.toString()
                + "\n误差: " + test.error()
                + "\n二维误差：" + test.error2d());
        mFingerprintUpdated = false;
        mPositionUpdated = false;
        waitingDialog.dismiss();
    }

    private void receiveLocator(final Locator locator) {
        if (locator == null) {
            Log.e(TAG, "receiveLocator: No locator available");
            Toast.makeText(this, "无法定位", Toast.LENGTH_LONG).show();
            return;
        }
        mLocator = locator;
        // startUpdating();
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
                    msg.obj = new Locator(MainActivity.this, AlgorithmImpl.class);
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

        try {
            mLogger = new TestLogger();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "无法建立测试记录", Toast.LENGTH_LONG).show();
            finish();
        }

        mErrorTextView = (TextView) findViewById(R.id.error_textview);
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
        mWorking = false;
        try {
            mLocator.finish();
        } catch (Exception e) {
           e.printStackTrace();
        }
        try {
            mLogger.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
