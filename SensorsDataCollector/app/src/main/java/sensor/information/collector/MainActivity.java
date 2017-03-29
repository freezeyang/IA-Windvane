package sensor.information.collector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opencsv.CSVWriter;

import ia.information.collector.R;
import ia.information.collector.Setting;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static ia.information.collector.Setting.preferences;

public class MainActivity extends Activity implements OnGestureListener,
        OnDoubleTapListener {
    // variables defined here
    private TextView tvLongitude;
    private TextView tvLatitude;
    private TextView myAddress;
    private TextView lightReading;
    private TextView timeStamp;
    public static TextView tx_js_dis;

    private TextView tvX;
    private TextView tvY;
    private TextView tvZ;

    public static ProgressBar disProgress;
    public static ProgressDialog expProgress;

    public static TextView tvDBR;
    public static EditText edCache;
    public static TextView tvSR;
    public CheckBox WindVaneCB;

    private String sLongitude;
    private String sLatitude;
    private String smyAddress;
    private String slightReading;
    private String stimeStamp;
    private String stvX;
    private String stvY;
    private String stvZ;
    private String sPhoneNumber;
    private String sDeviceID;
    private String DeviceID;
    private Boolean ServerUploadSwitch;
    private static DatabaseReference mDatabase;
    private static DatabaseReference newRef;
    private static final String TAG_RETAINED_FRAGMENT = "RetainedFragment";
    public static RetainedFragment mRetainedFragment;

    // create instance of InformationCollectionService
    private InformationCollectionService s;
    private GestureDetectorCompat mDetector;
    public DatabaseHandler db = new DatabaseHandler(this);
    public static String touchScreenText;

    // JS distance definition
    public static double js_dis = 1.0;
    public static final double log2 = Math.log(2);
    public static Handler progressHandler = new Handler();
    public static Handler checkHandler = new Handler();
    public static Handler commonHandler = new Handler();
    public static Handler exportHandler;
    public static String ctext = new String();
    public static List<Contact> input_contact = new ArrayList<Contact>();
    public static String TouchScreenMessage = new String();
    public static Contact ct = new Contact();
    public static CSVWriter csvWrite;
    public static boolean taskDone = false;
    public static HashMap<String, Integer> hash_map;
    public static File cfile;
    public static String root;
    public static SharedPreferences preferences_main;

    // Progress Dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;
    public static int stride = 50;
    public static MainActivity activity;
    public static String SampleRate;
    public static WindVane windvane;
    public static final double WindStrengthThreshold = 0.43;
    public static final int WindDurationThreshold = 4;
    public static boolean StrideIniFlag;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // call the super class onCreate to complete the creation of activity like
        // the view hierarchy
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_setting);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_main, false);
        setContentView(R.layout.activity_main);
        tvLongitude = (TextView) findViewById(R.id.tv_longitude);
        tvLatitude = (TextView) findViewById(R.id.tv_latitude);
        myAddress = (TextView) findViewById(R.id.myaddress);
        lightReading = (TextView) findViewById(R.id.light_reading);
        timeStamp = (TextView) findViewById(R.id.timestamp);
        tvX = (TextView) findViewById(R.id.x_axis);
        tvY = (TextView) findViewById(R.id.y_axis);
        tvZ = (TextView) findViewById(R.id.z_axis);
        tx_js_dis = (TextView) findViewById(R.id.JSdis);
        tvDBR = (TextView) findViewById(R.id.DatabaseRow);
        edCache = (EditText) findViewById(R.id.CacheSize);
        WindVaneCB = (CheckBox) findViewById(R.id.WindVaneCB);
        disProgress = (ProgressBar) findViewById(R.id.distance_progress);
        disProgress.setMax(10);
        tvSR = (TextView) findViewById(R.id.samplerate);

        //setting
        /*preferences_main = this.getSharedPreferences("preferences_main", 0);
        preferences = this.getSharedPreferences("preferences", 0);*/
        preferences_main = this.getSharedPreferences("preferences_main", 0);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Create New Service
        Intent intent = new Intent(this, InformationCollectionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mDetector = new GestureDetectorCompat(this, this);

        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);
        activity = MainActivity.this;
        //preference setting
        edCache.setText(preferences_main.getString("edCache", "500"));
        WindVaneCB.setChecked(preferences_main.getBoolean("WindVaneCB", false));
        edCache.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                SharedPreferences.Editor editor = preferences_main.edit();
                editor.putString("edCache", edCache.getText().toString().trim());
                editor.commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }
        });

        /* SetUp the wind vane */
        if (preferences_main == null) {
            SharedPreferences.Editor editor = preferences_main.edit();
            editor.putString("preDirection", "L");
            editor.putString("Direction", "L");
            editor.putBoolean("StrideIniFlag", false);
            editor.putFloat("Strength", 0);
            editor.putInt("Duration", 0);
            editor.putInt("max", -1);
            editor.putInt("min", -1);
            editor.putInt("stride", 50);
            editor.putFloat("js_divergence_large", -1);
            editor.putFloat("js_divergence_small", -1);
            editor.putFloat("js_divergence_middle", -1);
            editor.commit();
        }
        //windvane = new WindVane("L", 0, 0, -1, -1);
        windvane = new WindVane(preferences_main.getString("Direction", "L"),
                preferences_main.getFloat("Strength", 0), preferences_main.getInt("Duration", 0),
                preferences_main.getInt("max", -1), preferences_main.getInt("min", -1));
        /*if (windvane.getPreDirection() == null) {
            windvane.setPreDirection("L");// initialization pre-direction
        }*/
        windvane.setPreDirection(preferences_main.getString("preDirection", "L"));// initialization pre-direction
        //StrideIniFlag = false;
        StrideIniFlag = preferences_main.getBoolean("StrideIniFlag", false);

        windvane.setJs_divergence_large(preferences_main.getFloat("js_divergence_large", -1));
        windvane.setJs_divergence_middle(preferences_main.getFloat("js_divergence_middle", -1));
        windvane.setJs_divergence_small(preferences_main.getFloat("js_divergence_small", -1));
        this.stride = preferences_main.getInt("stride", 50);

        // Is the view now checked?
        CheckBox WindVaneCB = (CheckBox) findViewById(R.id.WindVaneCB);
        boolean checked = WindVaneCB.isChecked();
        // Check which checkbox was clicked
        if (checked) {
            Toast.makeText(getBaseContext(), "Auto Export Enabled",
                    Toast.LENGTH_SHORT).show();

            if (MainActivity.isExternalStorageWritable() == false) {
                Toast.makeText(getBaseContext(),
                        "The External Storage Is Not Available",
                        Toast.LENGTH_SHORT).show();
            } else {
                String root = Environment.getExternalStorageDirectory()
                        .toString();
                File myDir = new File(root + "/IACache/");
                String cname = "count.csv";
                File cfile = new File(myDir, cname);
                if (!cfile.exists()) {
                    Toast.makeText(
                            getBaseContext(),
                            "First time calculate the JS-distance, please use Export button first to set up the rule.",
                            Toast.LENGTH_LONG).show();
                    WindVaneCB.setChecked(false);
                    InformationCollectionService.autoSampleInd = false;
                } else {
                    InformationCollectionService.autoSampleInd = true;
                }
            }
        } else {
            Toast.makeText(getBaseContext(), "Auto Export Disabled",
                    Toast.LENGTH_SHORT).show();
            InformationCollectionService.autoSampleInd = false;
        }


        // Store current fragment
        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        mRetainedFragment = (RetainedFragment) fm.findFragmentByTag(TAG_RETAINED_FRAGMENT);
        // create the fragment and data the first time
        if (mRetainedFragment == null) {
            // add the fragment
            mRetainedFragment = new RetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, TAG_RETAINED_FRAGMENT).commit();
            // load data from a data source or perform any calculation
            mRetainedFragment.setData(windvane);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // Sensor Accelerometer Information Defined Here
    @Override
    protected void onResume() {
        super.onResume();
        // registerReceiver(receiver, new IntentFilter(
        // InformationCollectionService.ADDRESSINFO));
        Intent intent = new Intent(this, InformationCollectionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
        // unregisterReceiver(receiver);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            InformationCollectionService.MyBinder b = (InformationCollectionService.MyBinder) binder;
            s = b.getService();
            // connected information given here
            // Toast.makeText(MainActivity.this, "Connected",
            // Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            s = null;
        }
    };

    public void onClick(View view) {
        new String();
        sLongitude = new String();
        sLatitude = new String();
        smyAddress = new String();
        slightReading = new String();
        stimeStamp = new String();
        stvX = new String();
        stvY = new String();
        stvZ = new String();
        sPhoneNumber = new String();
        sDeviceID = new String();

        // Bundle bundle = intent.getExtras();
        // if (bundle != null) {

        sLongitude = s.getLongitude();
        sLatitude = s.getLatitude();
        smyAddress = s.getMyAddress();
        slightReading = s.getlightReading();
        stimeStamp = s.getTimeStamp();
        stvX = s.getXText();
        stvY = s.getYText();
        stvZ = s.getZText();
        sPhoneNumber = s.getPhoneNumber();
        sDeviceID = s.getDeviceID();
        s.gettouchScreenText();

        tvLongitude.setText(sLongitude);
        tvLatitude.setText(sLatitude);
        myAddress.setText(smyAddress);
        tvX.setText(stvX);
        tvY.setText(stvY);
        tvZ.setText(stvZ);
        lightReading.setText(slightReading);
        timeStamp.setText(stimeStamp);

    }

    // Export button defined here
    public void onClickExport(View view) throws IOException {
        DeviceID = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        ExportFileTask exportFileTask = new ExportFileTask(MainActivity.this);
        exportFileTask.execute();

        // Choose to update to remote server or not
        newRef = mDatabase.child(DeviceID).push();
        if (preferences == null) {
            ServerUploadSwitch = false;
        } else {
            ServerUploadSwitch = preferences.getBoolean("ServerCBox", false);
        }
        if (ServerUploadSwitch) {
            newRef.setValue(db.getAllContacts());
        }
    }

    // Start Service
    public void onClickStart(View view) {
        Intent service = new Intent(this, InformationCollectionService.class);
        this.startService(service);
    }

    // Reset
    public void onClickReset(View view) {
        Intent intent = new Intent(this, Setting.class);
        startActivity(intent);
    }

    // Auto Export
    public void onCheckboxClicked(View view) {
        // Set preference
        WindVaneCB = (CheckBox) findViewById(R.id.WindVaneCB);
        SharedPreferences.Editor editor = preferences_main.edit();
        editor.putBoolean("WindVaneCB", WindVaneCB.isChecked());
        editor.commit();

        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.WindVaneCB:
                if (checked) {
                    Toast.makeText(getBaseContext(), "Auto Export Enabled",
                            Toast.LENGTH_SHORT).show();

                    if (MainActivity.isExternalStorageWritable() == false) {
                        Toast.makeText(getBaseContext(),
                                "The External Storage Is Not Available",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        String root = Environment.getExternalStorageDirectory()
                                .toString();
                        File myDir = new File(root + "/IACache/");
                        String cname = "count.csv";
                        File cfile = new File(myDir, cname);
                        if (!cfile.exists()) {
                            Toast.makeText(
                                    getBaseContext(),
                                    "First time calculate the JS-distance, please use export button first to set up the rule.",
                                    Toast.LENGTH_LONG).show();
                            checkHandler.post(new Runnable() {
                                public void run() {
                                    WindVaneCB.setChecked(false);
                                }
                            });
                            InformationCollectionService.autoSampleInd = false;
                        } else {
                            InformationCollectionService.autoSampleInd = true;
                        }
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Auto Export Disabled",
                            Toast.LENGTH_SHORT).show();
                    InformationCollectionService.autoSampleInd = false;
                }

                break;

            // TODO: Veggie sandwich
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // TODO Auto-generated method stub
        touchScreenText = touchScreenText + " onSingleTapConfirmed: "
                + e.toString();
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // TODO Auto-generated method stub
        // keep adding the touch screen text data
        touchScreenText = touchScreenText + " onDoubleTap: " + e.toString();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // TODO Auto-generated method stub
        touchScreenText = touchScreenText + " onDoubleTapEvent: "
                + e.toString();
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        touchScreenText = touchScreenText + " onDown: " + e.toString();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
        touchScreenText = touchScreenText + " onShowPress: " + e.toString();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        touchScreenText = touchScreenText + " onSingleTapUp: " + e.toString();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        // TODO Auto-generated method stub
        touchScreenText = touchScreenText + " onScroll: " + e1.toString() + " "
                + e2.toString();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
        touchScreenText = touchScreenText + " onLongPress: " + e.toString();

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        // TODO Auto-generated method stub
        touchScreenText = touchScreenText + " onFling: " + e1.toString() + " "
                + e2.toString();

        return true;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    /* Count the frequency of words */
    public static HashMap<String, Integer> makeWordList(String input_string)
            throws FileNotFoundException {
        // Scanner scan = new Scanner(input_File);
        HashMap<String, Integer> listOfWords = new HashMap<String, Integer>();
        @SuppressWarnings("resource")
        Scanner scan = new Scanner(input_string);
        while (scan.hasNext()) {
            String word = scan.next(); // scanner automatically uses " " as a
            // delimeter
            int countWord = 0;
            if (!listOfWords.containsKey(word)) { // add word if it isn't added
                // already
                listOfWords.put(word, 1); // first occurance of this word
            } else {
                countWord = listOfWords.get(word) + 1; // get current count and
                // increment
                // now put the new value back in the HashMap
                listOfWords.remove(word); // first remove it (can't have
                // duplicate keys)
                listOfWords.put(word, countWord); // now put it back with new
                // value
            }
        }
        return listOfWords; // return the HashMap you made of distinct words
    }

    /* Convert objectarray to String Array */
    public static String[] convertToString(Object[] objectArray) {
        String[] strArray = new String[objectArray.length];
        for (int i = 0; i < objectArray.length; i++) {
            strArray[i] = new String((String) objectArray[i].toString());
        }
        return strArray;
    }

    /* Convert CSV file to hashmap */
    public static HashMap<String, Integer> convertToHashMap(String input_file)
            throws IOException {
        String key_string = new String();
        String value_string = new String();
        BufferedReader br = new BufferedReader(new FileReader(input_file));
        String line = null;
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        try {
            int count = 0;
            /* Normally it only has two iterations */
            while ((line = br.readLine()) != null) {
                if (count == 0) {
                    key_string = line;
                    count++;
                } else if (count == 1) {
                    value_string = line;
                    break;
                }
            }
            String arr_key[] = key_string.trim().split(",");
            String arr_value[] = value_string.trim().split(",");
            for (int i = 0; i < arr_key.length; i++) {
                map.put(arr_key[i],
                        Integer.parseInt(arr_value[i].replace("\"", "")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /* Iterate through the count hashmap */
    public static Double[] countProbability(
            HashMap<String, Integer> input_hash,
            HashMap<String, Integer> historical_hash) {
        if (input_hash.size() == 0) {
            System.out
                    .println("The current hashmap is empty! please check it input");
        } else {
            Double[] feature_probability = new Double[historical_hash.size()];
            int index = 0;
            int count = 0;
            // Count the total number of words in the hashmap
            for (Map.Entry<String, Integer> entry : input_hash.entrySet()) {
                count = count + entry.getValue();
            }

            for (Map.Entry<String, Integer> entry : historical_hash.entrySet()) {
                if (input_hash.containsKey(entry.getKey())) {
                    /* if the input_hash already has such string key */
                    feature_probability[index] = (double) input_hash.get(entry
                            .getKey()) / (double) count;
                } else {
                    feature_probability[index] = 0.0;
                }

				/* the belowing is testing */
				/*
				 * System.out.println("Key = " + entry.getKey() + ", Value = " +
				 * entry.getValue());
				 */
                index++;
            }
            return feature_probability;
        }
        return null;
    }

    // Add values of two hashmap together based on their key

    public static HashMap<String, Integer> AddTwoHash(
            HashMap<String, Integer> input_hash,
            HashMap<String, Integer> historical_hash) {
        if (input_hash.size() == 0) {
            System.out
                    .println("The current hashmap is empty! please check it input");
        } else {
            HashMap<String, Integer> result_hashmap = new HashMap<String, Integer>();
            // first, copy the input_hash to the result hashmap
            result_hashmap.putAll(input_hash);
            if (historical_hash != null) {
                for (Map.Entry<String, Integer> entry : historical_hash
                        .entrySet()) {
                    if (input_hash.containsKey(entry.getKey())) {
						/*
						 * if the input_hash already has such string key then,
						 * add input hash map and historical hash map together
						 */
                        result_hashmap.put(entry.getKey(), entry.getValue()
                                + input_hash.get(entry.getKey()));
                    } else {
						/* keep the historical hashmap value unchanged */
                        result_hashmap.put(entry.getKey(), entry.getValue());
                    }

					/* the belowing is testing */
					/*
					 * System.out.println("Key = " + entry.getKey() +
					 * ", Value = " + entry.getValue());
					 */
                }
            } else {
                // do nothing
            }
            return result_hashmap;
        }
        return null;
    }

    /**
     * Returns the KL divergence, K(p1 || p2).
     * <p/>
     * The log is w.r.t. base 2.
     * <p/>
     * <p/>
     * *Note*: If any value in <tt>p2</tt> is <tt>0.0</tt> then the
     * KL-divergence is <tt>infinite</tt>. Limin changes it to zero instead of
     * infinite.
     */
    public static Double klDivergence(Double[] p1, Double[] p2) {

        Double klDiv = 0.0;

        for (int i = 0; i < p1.length; ++i) {
            if (p1[i] == 0) {
                continue;
            }
            if (p2[i] == 0.0) {
                continue;
            } // Limin

            klDiv += p1[i].doubleValue()
                    * Math.log(p1[i].doubleValue() / p2[i].doubleValue());
        }

        return klDiv / log2; // moved this division out of the loop -DM
    }

    /**
     * Returns the Jensen-Shannon divergence.
     */
    public static Double jensenShannonDivergence(Double[] p1, Double[] p2) {
        assert (p1.length == p2.length);
        Double[] average = new Double[p1.length];
        for (int i = 0; i < p1.length; ++i) {
            average[i] = (p1[i] + p2[i]) / 2;
        }
        return (klDivergence(p1, average) + klDivergence(p2, average)) / 2;
    }

}