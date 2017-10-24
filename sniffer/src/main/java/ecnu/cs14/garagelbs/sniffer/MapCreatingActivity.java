package ecnu.cs14.garagelbs.sniffer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.MapData;
import ecnu.cs14.garagelbs.support.env.Environment;
import ecnu.cs14.garagelbs.support.info.FileBuilder;

import java.util.*;

public class MapCreatingActivity extends AppCompatActivity {
    private static final String TAG = MapCreatingActivity.class.getName();

    private Button addApButton;
    private Button buildButton;
    private ArrayList<Ap> apList = new ArrayList<>();
    private ListView apListView;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_creating);

        addApButton = (Button) findViewById(R.id.add_ap);
        addApButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddApDialog().show(getSupportFragmentManager(), "addApDialog");
            }
        });
        buildButton = (Button) findViewById(R.id.build);
        buildButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBuild();
            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, apList);
        apListView = (ListView) findViewById(R.id.ap_list);
        apListView.setAdapter(adapter);
    }

    public class ApListView extends ListView {
        public ApListView(Context context) {
            super(context);
            init();
        }

        public ApListView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();

        }

        public ApListView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private Environment environment;
        private ArrayAdapter<Ap> adapter;
        private ArrayAdapter<String> placeholderAdapter;
        public final List<Ap> apList = new ArrayList<>();

        private Handler handler = new Handler();

        public void init() {
            environment = Environment.getInstance(MapCreatingActivity.this);

            this.setChoiceMode(CHOICE_MODE_MULTIPLE);

            List<String> placeholderList = new ArrayList<>(1);
            placeholderList.add("请稍候...");
            placeholderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, placeholderList);
            this.setAdapter(placeholderAdapter);

            adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, apList);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final List<Ap> aps = environment.getAps();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            apList.clear();
                            apList.addAll(aps);
                            ApListView.this.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }).start();
        }
    }

    public static class AddApDialog extends DialogFragment {
        private MapCreatingActivity activity;
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            activity = (MapCreatingActivity) getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("AP");

            final ApListView apListView = activity.new ApListView(getActivity());
            builder.setView(apListView);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SparseBooleanArray choices = apListView.getCheckedItemPositions();
                    List<Ap> aps = new ArrayList<>();
                    for (int index = 0; index < choices.size(); index++) {
                        aps.add(apListView.apList.get(choices.keyAt(index)));
                    }
                    ((MapCreatingActivity) getActivity()).addAps(aps);
                }
            });

            builder.setCancelable(true);

            return builder.create();
        }
    }

    public void addAps(Collection<? extends Ap> aps) {
        for (Ap ap: aps) {
            boolean unique = true;
            for (Ap other: apList) {
                if (ap.equals(other)) {
                    unique = false;
                    break;
                }
            }
            if (unique) {
                apList.add(ap);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void startBuild() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setText("300 400");
        editText.selectAll();
        builder.setTitle("输入大小(width, height)")
                .setView(editText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String string = editText.getText().toString();
                        String[] stringPair = string.split("\\s");
                        if (stringPair.length == 2) {
                            build(
                                    Integer.valueOf(stringPair[0]),
                                    Integer.valueOf(stringPair[1])
                            );
                        } else {
                            Toast.makeText(MapCreatingActivity.this, "输入有误", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setCancelable(false)
                .show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask()   {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) MapCreatingActivity.this.getSystemService(INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, 500);
    }

    public void build(int width, int height) {
        MapData map = new MapData();
        map.aps = apList;
        map.width = width;
        map.height = height;
        FileBuilder fb = new FileBuilder(map);
        try {
            fb.build();
        } catch (Exception e) {
            Log.e(TAG, "build: failure", e);
            Toast.makeText(MapCreatingActivity.this, "创建失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(MapCreatingActivity.this, "创建完成，请重启", Toast.LENGTH_SHORT).show();
        finish();
    }
}
