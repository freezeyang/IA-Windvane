/**
 *
 */
package sensor.information.collector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opencsv.CSVWriter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.support.v4.view.GestureDetectorCompat;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;


import static ia.information.collector.Setting.*;
import static sensor.information.collector.MainActivity.preferences_main;

/**
 * @author yin
 */
public class InformationCollectionService extends Service implements
        LocationListener, SensorEventListener, OnGestureListener,
        OnDoubleTapListener, OnTouchListener {

    // variables defined here
    LocationManager locationManager;
    String provider;

    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float NOISE = (float) 2.0;
    private GestureDetectorCompat mDetector;
    // create database as private object
    private DatabaseHandler db = new DatabaseHandler(this);
    private String UserIDTrue;
    private String UserIDWindvane;
    private Boolean ServerUploadSwitch;
    private int dbCount;


    // private GoogleMap mGoogleMap;
    // light meter read
    // ProgressBar lightMeter;
    // WakeLock wakelock;

    // public cariable defined here
    public long lastTime = 0;
    public CSVWriter csvWrite;
    public File cfile;
    public String root;

    // @SuppressWarnings("deprecation")
    // Notification for broadcasting
    // public static final String ADDRESSINFO =
    // "in.wptrafficanalyzer.locationformgps";
    // public static final String LONGITUDE = "tvLongitude";
    // public static final String LATITUDE = "tvLatitude";
    // public static final String MYADDRESS = "myaddress";
    // public static final String TIMESTAMP = "timeStamp";
    // public static final String DEVICEID = "deviceid";
    // public static final String PHONENUMBER = "phonenumber";
    // public static final String XTEXT = "tvxtext";
    // public static final String YTEXT = "tvytext";
    // public static final String ZTEXT = "tvztext";
    // public static final String TOUCHSCREEN = "touchScreentext";
    // public static final String LIGHTREADING = "lightReading";

    // Broadcast value defined here
    private String tvLongitude;
    private String tvLatitude;
    private String myAddress;
    private String timeStamp;
    private String tvXText, tvYText, tvZText;
    private String device_id = new String();
    private String mPhoneNumber = new String();
    private String lightReading = new String();
    private String touchScreenText = new String();
    private final IBinder mBinder = new MyBinder();
    private static DatabaseReference mDatabase;
    private static DatabaseReference newRef;

    // touch layout
    private LinearLayout touchLayout;
    // window manager

    private WindowManager mWindowManager;

    private String TAG = this.getClass().getSimpleName();

    public static boolean autoSampleInd = false;
    public static boolean export_flag = false;

    public static double sampleRate;
    public static double sampleConstantRate;
    public static final double parameter_Gama = 1.5;

    public InformationCollectionService() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // create linear layout
        try {
            // initializing the sample rate
            sampleConstantRate = 1000;
            touchLayout = new LinearLayout(this);
            LayoutParams lp = new LayoutParams(30, LayoutParams.MATCH_PARENT);
            touchLayout.setLayoutParams(lp);

            // fetch window manager object

            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            // set layout parameter of window manager

            WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                    30,
                    // width of layout 30 px

                    WindowManager.LayoutParams.MATCH_PARENT, // height is equal
                    // to
                    // full screen

                    WindowManager.LayoutParams.TYPE_PHONE, // Type Phone, These
                    // are
                    // non-application
                    // windows providing
                    // user interaction
                    // with
                    // the phone (in
                    // particular
                    // incoming
                    // calls).

                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this
                    // window
                    // won't
                    // ever
                    // get key
                    // input
                    // focus

                    PixelFormat.TRANSLUCENT);

            mParams.gravity = Gravity.LEFT | Gravity.TOP;

            Log.i(TAG, "add View");

            mWindowManager.addView(touchLayout, mParams);
            touchLayout.setOnTouchListener(this);
            mDatabase = FirebaseDatabase.getInstance().getReference();

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // TODO Auto-generated method stub
        //	onSingleTapConfirmed
        touchScreenText = touchScreenText + " oSTC"
                + e.toString();
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // TODO Auto-generated method stub
        //	onDoubleTap
        touchScreenText = touchScreenText + " oDT" + e.toString();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // TODO Auto-generated method stub
        //	onDoubleTapEvent
        touchScreenText = touchScreenText + " oDTE"
                + e.toString();
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        //	onDown
        touchScreenText = touchScreenText + " oD" + e.toString();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
        //	onShowPress
        touchScreenText = touchScreenText + " oSP" + e.toString();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        //	onSingleTapUp
        touchScreenText = touchScreenText + " oSTU: " + e.toString();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        // TODO Auto-generated method stub
        //	onScroll
        touchScreenText = touchScreenText + " oS" + e1.toString() + " oS"
                + e2.toString();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
        //	onLongPress
        touchScreenText = touchScreenText + " oLP" + e.toString();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        // TODO Auto-generated method stub
        //	onFling
        touchScreenText = touchScreenText + " oF" + e1.toString() + " "
                + e2.toString();
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        // lightReading = new String();
        tvXText = new String();
        tvYText = new String();
        tvZText = new String();
        timeStamp = new String();
        SharedPreferences.Editor editor = MainActivity.preferences_main.edit();
        // mPhoneNumber = new String();
        // device_id = new String();
        // mPhoneNumber = new String();
        if (preferences == null) {
            // first time usage
            UserIDTrue = "L";
        } else if (preferences.getBoolean("UserIDCBox", true)) {
            UserIDTrue = "L";
        } else {
            UserIDTrue = "A";
        }
        if (MainActivity.windvane == null) {
            // first time usage
            UserIDWindvane = "L";
        } else {
            UserIDWindvane = MainActivity.windvane.getDirection();
        }

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float currentReading = Math.round(event.values[0] * 1000) / 1000;
            // lightMeter.setProgress((int)currentReading);
            /* Current Light Strength Reading: */
            lightReading = " " + String.valueOf(currentReading);
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            tvXText = "0.0";
            tvYText = "0.0";
            tvZText = "0.0";
            mInitialized = true;
        } else {
            float deltaX = Math.abs(mLastX - x);
            float deltaY = Math.abs(mLastY - y);
            float deltaZ = Math.abs(mLastZ - z);
            if (deltaX < NOISE)
                deltaX = (float) 0.0;
            if (deltaY < NOISE)
                deltaY = (float) 0.0;
            if (deltaZ < NOISE)
                deltaZ = (float) 0.0;
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            tvXText = Float.toString(deltaX);
            tvYText = Float.toString(deltaY);
            tvZText = Float.toString(deltaZ);

            // setTimestamp
            timeStamp = (DateFormat.format("dd-MM-yyyy hh:mm:ss",
                    new java.util.Date()).toString());

            // get device id
            device_id = Secure.getString(getContentResolver(),
                    Secure.ANDROID_ID);

            // read phone number
            TelephonyManager tMgr = (TelephonyManager) this
                    .getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneNumber = tMgr.getLine1Number();

        }

        //The following code provides app-centric IA
        touchScreenText = MainActivity.touchScreenText;
        /* Database row number count */
        dbCount = db.getContactsCount();
        MainActivity.commonHandler.post(new Runnable() {
            public void run() {
                MainActivity.tvDBR.setText(Integer.toString(dbCount));
            }
        });
        /**
         * CRUD Operations
         * */
        /*
         * Inserting Contacts, the default frequency is defined as 10000 but not
		 * update UI with progress bar and textview
		 */
        /*
         * sampleRate = sampleConstantRate / (MainActivity.js_dis *
		 * parameter_Gama);
		 */
        sampleRate = sampleConstantRate
                / (Math.pow(MainActivity.js_dis, parameter_Gama));
        if (!autoSampleInd) {
            if (System.currentTimeMillis() - lastTime > sampleConstantRate) {
                Log.d("Insert: ", "Inserting ..");
                db.addContact(new Contact(device_id, mPhoneNumber, tvLongitude.replace("Longitude: ", ""),
                        tvLatitude.replace("Latitude: ", ""), myAddress, tvXText, tvYText, tvZText, lightReading,
                        touchScreenText, UserIDTrue, UserIDWindvane, timeStamp));
                // cleaning up the touch screen data
                touchScreenText = "";
                MainActivity.touchScreenText = "";
                lastTime = System.currentTimeMillis();
                dbCount++;
                MainActivity.commonHandler.post(new Runnable() {
                    public void run() {
                        MainActivity.tvDBR.setText(Integer.toString(dbCount));
                    }
                });

            }
        }
        // autoSampling enabled (checkbox checked)
        else {
            if (System.currentTimeMillis() - lastTime > sampleRate) {
                Log.d("Insert: ", "Inserting ..");
                db.addContact(new Contact(device_id, mPhoneNumber, tvLongitude,
                        tvLatitude, myAddress, tvXText, tvYText, tvZText, lightReading,
                        touchScreenText, UserIDTrue, UserIDWindvane, timeStamp));
                // cleaning up the touch screen data
                touchScreenText = "";
                MainActivity.touchScreenText = "";
                lastTime = System.currentTimeMillis();
                dbCount++;
                MainActivity.commonHandler.post(new Runnable() {
                    public void run() {
                        MainActivity.tvDBR.setText(Integer.toString(dbCount));
                    }
                });
                int cache_size = Integer.parseInt(MainActivity.edCache
                        .getText().toString().trim());

                if (MainActivity.isExternalStorageWritable() == false) {
                    Toast.makeText(getBaseContext(),
                            "The External Storage Is Not Available",
                            Toast.LENGTH_SHORT).show();
                } else {
                    root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/IACache/");
                    String fname = "samplerate.txt";
                    String cname = "count.csv";
                    String ctext = new String();
                    String TouchScreenMessage = new String();

                    File file = new File(myDir, fname);

                    if (file.exists()) {
                        // do nothing
                    } else {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    cfile = new File(myDir, cname);
                    HashMap<String, Integer> hash_map = new HashMap<String, Integer>();
                    if (!cfile.exists()) {
                        Toast.makeText(
                                getBaseContext(),
                                "First time calculate the JS-distance, please use export button first to set up the rule.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            List<Contact> input_contact = new ArrayList<Contact>();
                            input_contact = db.getAllContacts();
                            Contact ct = null;
                            if (input_contact.size() > 0) {
                                if ((dbCount >= cache_size)
                                        && (export_flag == false)) {
                                    /*
                                     * export_flag indicate if the export thread
									 * is executing or not, otherwise, it may
									 * exulst the app by creating many threads
									 * at the same time.
									 */
                                    ExportFileTask exportFileTask = new ExportFileTask(
                                            MainActivity.activity);
                                    exportFileTask.execute();
                                    export_flag = true;

                                    // Choose to update to remote server or not
                                    newRef = mDatabase.child(device_id).push();
                                    if (preferences == null) {
                                        ServerUploadSwitch = false;
                                    } else {
                                        ServerUploadSwitch = preferences.getBoolean("ServerCBox", false);
                                    }
                                    if (ServerUploadSwitch) {
                                        newRef.setValue(db.getAllContacts());
                                    }

									/*
                                     * // Create new CSVWriter ready for
									 * updating csvWrite = new CSVWriter(new
									 * FileWriter( file)); // Need Export
									 * export_flag = true; for (int index = 0;
									 * index < input_contact .size(); index++) {
									 * ct = input_contact.get(index); int srNo =
									 * index; String arrStr[] = {
									 * String.valueOf(srNo + 1), ct.getName(),
									 * ct.getPhoneNumber(),
									 * ct.getAddress().replaceAll( "\n", " "),
									 * ct.getXText(), ct.getYText(),
									 * ct.getZText(), ct.getLightReading(),
									 * ct.getTouchScreen(), ct.getTimeStamp() };
									 * csvWrite.writeNext(arrStr, false); // The
									 * actually db will be truncked at // the
									 * end of this step dbCount--;
									 * MainActivity.commonHandler .post(new
									 * Runnable() { public void run() {
									 * MainActivity.tvDBR .setText(Integer
									 * .toString(dbCount)); } }); Combine the
									 * result to a single string if
									 * (ct.getTouchScreen() != null) {
									 * TouchScreenMessage = ct .getTouchScreen()
									 * .replaceAll(",", ""); }
									 * 
									 * ctext = ct.getName() + " " +
									 * ct.getPhoneNumber() + " " +
									 * ct.getAddress() .replaceAll("\n", " ")
									 * .replaceAll(",", "") + " " +
									 * ct.getXText() + " " + ct.getYText() + " "
									 * + ct.getZText() + " " +
									 * ct.getLightReading() + " " +
									 * TouchScreenMessage + " " +
									 * ct.getTimeStamp();
									 * 
									 * Recursively add the element to hash map
									 * 
									 * hash_map = MainActivity.AddTwoHash(
									 * MainActivity .makeWordList(ctext),
									 * hash_map); } csvWrite.close();
									 */
                                } else if ((dbCount < cache_size)
                                        && (export_flag == false)) {
                                    // Does not need export
                                    for (int index = 0; index < input_contact
                                            .size(); index++) {
                                        ct = input_contact.get(index);
                                        /* Combine the result to a single string */
                                        if (ct.getTouchScreen() != null) {
                                            TouchScreenMessage = ct
                                                    .getTouchScreen()
                                                    .replaceAll(",", "");
                                        }
                                        if (MainActivity.stride >= input_contact
                                                .size()
                                                || (MainActivity.stride < input_contact
                                                .size() && (index - (input_contact
                                                .size() - MainActivity.stride)) >= 0)) {
                                            ctext = //ct.getName()
                                                    //+ " "
                                                    //+ ct.getPhoneNumber()
                                                    //+ " "
                                                    ct.getAddress()
                                                            .replaceAll("\n",
                                                                    " ")
                                                            .replaceAll(",", "")
                                                            + " " + ct.getXText() + " "
                                                            + ct.getYText() + " "
                                                            + ct.getZText() + " "
                                                            + ct.getLightReading()
                                                            + " " + TouchScreenMessage
                                            //+ " " + ct.getTimeStamp()
                                            ;
                                            /*
                                             * Recursively add the element to
											 * hash map
											 */
                                            hash_map = MainActivity
                                                    .AddTwoHash(
                                                            MainActivity
                                                                    .makeWordList(ctext),
                                                            hash_map);
                                        }
                                    }

									/*
                                     * Write the word count information to the
									 * count.csv file
									 */
									/*
									 * HashMap<String, Integer> hash_map =
									 * MainActivity .makeWordList(ctext);
									 */
                                    // The historical count is already existing
                                    Toast.makeText(getBaseContext(),
                                            "Reading count file: count.csv",
                                            Toast.LENGTH_SHORT).show();
                                    HashMap<String, Integer> historical_hashmap = new HashMap<String, Integer>();
									/*
									 * Convert the count.csv files to hashmap
									 * where stores the histrocial distribution
									 */
                                    historical_hashmap = MainActivity
                                            .convertToHashMap(root
                                                    + "/IACache/" + "count.csv");
									/* Calculate the current distribution */
                                    Double[] current_distribution = MainActivity
                                            .countProbability(hash_map,
                                                    historical_hashmap);

									/* Calculate the historical distribution */
                                    Double[] historical_ditribution = MainActivity
                                            .countProbability(
                                                    historical_hashmap,
                                                    historical_hashmap);

                                    // Calculate the JS-distance between two
                                    // distributions, this is new js distance
                                    MainActivity.js_dis = MainActivity
                                            .jensenShannonDivergence(
                                                    historical_ditribution,
                                                    current_distribution);
									/*
									 * Set up the wind strength of wind vane to
									 * JS divergence and further decide the
									 * stride size if necessary
									 */
                                    MainActivity.windvane
                                            .setStrength(MainActivity.js_dis);
                                    editor.putFloat("Strength", (float) MainActivity.js_dis);
                                    editor.apply();
									/*
									 * Checking if in the initialization stage
									 * or no
									 */
                                    if (MainActivity.StrideIniFlag == false) {
										/* Check the direction of wind vane */
                                        if (MainActivity.windvane
                                                .getDirection().equals("L")) {
											/* Duration resetting if need */
                                            if (MainActivity.windvane
                                                    .getStrength() > MainActivity.WindStrengthThreshold) {
												/*
												 * Abnormal Behavioral Patterns
												 * detected Adversary Direction
												 * (+)
												 */
                                                MainActivity.windvane
                                                        .setDuration(MainActivity.windvane
                                                                .getDuration() + 1);
                                                editor.putInt("Duration", MainActivity.windvane
                                                        .getDuration());
                                                editor.apply();
                                                if (MainActivity.windvane
                                                        .getDuration() >= MainActivity.WindDurationThreshold) {
													/*
													 * Changing the wind vane
													 * direction L->A
													 */
                                                    MainActivity.windvane
                                                            .setPreDirection(MainActivity.windvane
                                                                    .getDirection());
                                                    // store pre-direction
                                                    editor.putString("preDirection", MainActivity.windvane
                                                            .getDirection());
                                                    MainActivity.windvane
                                                            .setDirection("A");
                                                    editor.putString("Direction", "A");
                                                    editor.putBoolean("StrideIniFlag", true);
                                                    editor.commit();
													/*
													 * Need re-initialize the
													 * stride size
													 */
                                                    MainActivity.StrideIniFlag = true;
                                                }

                                            } else if (MainActivity.windvane
                                                    .getStrength() <= MainActivity.WindStrengthThreshold) {
												/* Back to Normal again */
                                                MainActivity.windvane
                                                        .setDuration(0);
                                                editor.putInt("Duration", MainActivity.windvane
                                                        .getDuration());
                                                editor.apply();
                                            }
                                        } else if (MainActivity.windvane
                                                .getDirection().equals("A")) {
											/* Duration resetting if need */
                                            if (MainActivity.windvane
                                                    .getStrength() <= MainActivity.WindStrengthThreshold) {
												/*
												 * Abnormal Behavioral Patterns
												 * detected Adversary Direction
												 * (-)
												 */
                                                MainActivity.windvane
                                                        .setDuration(MainActivity.windvane
                                                                .getDuration() - 1);
                                                editor.putInt("Duration", MainActivity.windvane
                                                        .getDuration());
                                                editor.apply();
                                                if (MainActivity.windvane
                                                        .getDuration() <= -(MainActivity.WindDurationThreshold)) {
													/*
													 * Changing the wind vane
													 * direction A->L
													 */
                                                    MainActivity.windvane
                                                            .setPreDirection(MainActivity.windvane
                                                                    .getDirection());
                                                    editor.putString("preDirection", MainActivity.windvane
                                                            .getDirection());
                                                    MainActivity.windvane
                                                            .setDirection("L");
                                                    editor.putString("Direction", "L");
                                                    editor.putBoolean("StrideIniFlag", true);
                                                    editor.commit();
													/*
													 * Need re-initialize the
													 * stride size
													 */
                                                    MainActivity.StrideIniFlag = true;
                                                }

                                            } else if (MainActivity.windvane
                                                    .getStrength() > MainActivity.WindStrengthThreshold) {
												/* Back to Normal again */
                                                MainActivity.windvane
                                                        .setDuration(0);
                                                editor.putInt("Duration", MainActivity.windvane
                                                        .getDuration());
                                                editor.apply();
                                            }

                                        } else {
											/* Error occur */
                                            Log.d("Stride Change: ",
                                                    "Windvane setting error.");
                                        }
                                    } else if (MainActivity.StrideIniFlag) {
										/*
										 * Need re-initialize the stride size
										 */
                                        int new_stride = MainActivity.windvane
                                                .StrideIni(MainActivity.stride);
                                        if (new_stride == 0) {
                                            Log.d("Stride Change: ",
                                                    "Initialization error, result is 0");
                                        } else {
											/*
											 * Need re-initialize the stride
											 * size
											 */
                                            MainActivity.stride = new_stride;
                                        }
                                    }
                                    Log.d("Stride Change: ",
                                            MainActivity.windvane
                                                    .getPreDirection()
                                                    + "->"
                                                    + MainActivity.windvane
                                                    .getDirection()
                                                    + " Stride Num: "
                                                    + MainActivity.stride
                                                    + " Strength: "
                                                    + MainActivity.windvane
                                                    .getStrength()
                                                    + " Duration: "
                                                    + MainActivity.windvane
                                                    .getDuration()
                                                    + " "
                                                    + Boolean
                                                    .toString(MainActivity.StrideIniFlag)
                                                    + " Min:"
                                                    + MainActivity.windvane
                                                    .getMin()
                                                    + " Max:"
                                                    + MainActivity.windvane
                                                    .getMax()
                                                    + " StrideIniFlag: "
                                                    + MainActivity.StrideIniFlag);
                                    editor.putInt("max", MainActivity.windvane
                                            .getMax());
                                    editor.putInt("min", MainActivity.windvane
                                            .getMin());
                                    editor.putFloat("js_divergence_large",
                                            (float) MainActivity.windvane.getJs_divergence_large());
                                    editor.putFloat("js_divergence_middle",
                                            (float) MainActivity.windvane.getJs_divergence_middle());
                                    editor.putFloat("js_divergence_small",
                                            (float) MainActivity.windvane.getJs_divergence_small());
                                    editor.putInt("stride", MainActivity.stride);
                                    editor.putBoolean("StrideIniFlag", MainActivity.StrideIniFlag);
                                    editor.apply();

                                    // Updating Sample rate
									/*
									 * sampleRate = sampleConstantRate /
									 * (MainActivity.js_dis * parameter_Gama);
									 */
                                    sampleRate = sampleConstantRate
                                            / (Math.pow(MainActivity.js_dis,
                                            parameter_Gama));
									/*
									 * MainActivity.progressHandler .post(new
									 * Runnable() { public void run() {
									 * MainActivity.disProgress
									 * .setProgress((int) (MainActivity.js_dis *
									 * 10)); MainActivity.tvSR.setText(Double
									 * .toString(sampleRate)); } });
									 */
                                    MainActivity.progressHandler
                                            .post(new Runnable() {
                                                public void run() {
                                                    MainActivity.disProgress
                                                            .setProgress((int) (MainActivity.js_dis * 10));
                                                    MainActivity.tvSR.setText(new DecimalFormat(
                                                            "#0.0")
                                                            .format(sampleRate)
                                                            + " "
                                                            + MainActivity.windvane
                                                            .getPreDirection()
                                                            + "->"
                                                            + MainActivity.windvane
                                                            .getDirection());
                                                }
                                            });
                                    MainActivity.commonHandler
                                            .post(new Runnable() {
                                                public void run() {
                                                    MainActivity.tx_js_dis.setText(Double
                                                            .toString(MainActivity.js_dis));
                                                }
                                            });

                                    try {
                                        FileWriter fwSample = new FileWriter(
                                                file, true);
                                        fwSample.write(" "
                                                + Double.toString(sampleRate));
                                        fwSample.close();
                                    } catch (IOException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }

								/*
								 * Checking if the export method triggered or
								 * not
								 */
								/*
								 * if (export_flag) { // We know it is exported
								 * and we must update // the count.csv
								 * 
								 * Add two hashmap together HashMap<String,
								 * Integer> add_result_hashmap = new
								 * HashMap<String, Integer>();
								 * add_result_hashmap = MainActivity
								 * .AddTwoHash(hash_map, historical_hashmap); //
								 * hash_map.putAll(historical_hashmap); //
								 * refresh cfile cfile.delete(); Output the new
								 * hashmap List<HashMap<String, Integer>>
								 * myArrList = new ArrayList<HashMap<String,
								 * Integer>>(); myArrList.add(hash_map);
								 * CSVWriter csvWrite_count;
								 * 
								 * csvWrite_count = new CSVWriter( new
								 * FileWriter(cfile));
								 * 
								 * Object[] keyArray = add_result_hashmap
								 * .keySet().toArray(); String[] str_keyArray =
								 * MainActivity .convertToString(keyArray);
								 * Object[] valueArray = add_result_hashmap
								 * .values().toArray(); String[] str_valueArray
								 * = MainActivity .convertToString(valueArray);
								 * 
								 * if (myArrList.size() > 0) {
								 * csvWrite_count.writeNext(str_keyArray,
								 * false); csvWrite_count.writeNext(
								 * str_valueArray, false); } // Remember to
								 * close the csvWrite after the // writing
								 * csvWrite_count.close();
								 * 
								 * // Trunk database db.trunkContact();
								 */
                                // }

                            } else {
                                Toast.makeText(
                                        getBaseContext(),
                                        "The cahce database is empty, please try again later.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            Log.e("Information Export Err", e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        tvLongitude = new String();
        tvLongitude = "Longitude: " + location.getLongitude();
        tvLatitude = new String();
        tvLatitude = "Latitude: " + location.getLatitude();
        myAddress = new String();

        // set address
        Geocoder geocoder = new Geocoder(this, Locale.US);
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null) {
                Address returnAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder(
				/* Address text appears here */
                        "");
                for (int i = 0; i < returnAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnAddress.getAddressLine(i))
                            .append("\n");
                }
                myAddress = strReturnedAddress.toString();
            } else {
                myAddress = "No Address returned!";
            }

        } catch (IOException e) {
            e.printStackTrace();
            myAddress = "Cannot get Address!";
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    // @Override
    // protected void onHandleIntent(Intent intent) {
    // // TODO Auto-generated method stub
    //
    //
    // }


    // private void publishResults() {
    // Intent intent = new Intent(ADDRESSINFO);
    // intent.putExtra(LONGITUDE, tvLongitude);
    // intent.putExtra(LATITUDE, tvLatitude);
    // intent.putExtra(MYADDRESS, myAddress);
    // intent.putExtra(TIMESTAMP, timeStamp);
    // intent.putExtra(DEVICEID, device_id);
    // intent.putExtra(PHONENUMBER, mPhoneNumber);
    // intent.putExtra(XTEXT, tvXText);
    // intent.putExtra(YTEXT, tvYText);
    // intent.putExtra(ZTEXT, tvZText);
    // intent.putExtra(LIGHTREADING, lightReading);
    // intent.putExtra(TOUCHSCREEN, touchScreenText);
    // sendBroadcast(intent);
    // }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // define a window to catch up touch event

        // Getting Gesture Information
        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.OnGestureListener
        mDetector = new GestureDetectorCompat(this, this);
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);

        // Getting LocationManager object
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Creating an empty criteria object
        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria
        provider = locationManager.getBestProvider(criteria, false);

        // getting GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        // getting network status
        boolean isNWEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        // Upate location if location is null

        // locationManager.requestLocationUpdates(provider, 0, 0, this);
        // Changed it to be or logic
        try {
            if (!isGPSEnabled || !isNWEnabled) {
                // no network provider is enabled
                Log.e("Service Enable Info:",
                        "Current network privider or gps provider is not provided.");
            } else if (provider != null && !provider.equals("")) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 0, 0,
                        new LocationListener() {
                            @Override
                            public void onStatusChanged(String provider,
                                                        int status, Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                            }

                            @Override
                            public void onLocationChanged(
                                    final Location location) {
                            }
                        });

                // Get the location from the given provider
                // Location location =
                // locationManager.getLastKnownLocation(provider);
                // Use network provider first
                Location location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                locationManager
                        .requestLocationUpdates(provider, 20000, 1, this);

                if (location != null)
                    onLocationChanged(location);
                else {
                    // if networking privider is not available , use gps
                    // provider
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
						/*
						 * Toast.makeText(getBaseContext(), location.toString(),
						 * Toast.LENGTH_SHORT) .show();
						 */
                    } else {
                        Toast.makeText(
                                getBaseContext(),
                                "Location can't be retrieved, please make sure you have GPS permission enableda",
                                Toast.LENGTH_SHORT).show();
                    }

                }

            } else {
                Toast.makeText(getBaseContext(), "No Provider Found",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException ne) {
            Log.e("Current Location:", ne.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get the location from the given provider
		/*
		 * try{ mGoogleMap.getMyLocation(); Location location =
		 * mGoogleMap.getMyLocation();
		 * 
		 * if (location != null) onLocationChanged(location); else
		 * Toast.makeText(getBaseContext(), "Location can't be retrieved",
		 * Toast.LENGTH_SHORT).show(); }catch (NullPointerException ne) {
		 * Log.e("Current Location:", ne.toString()); //return new LatLng(0, 0);
		 * }catch (Exception e) { e.printStackTrace(); //return new LatLng(0,
		 * 0); Log.e("Trace Information:", e.toString()); }
		 */

        // light meter read SensorManager Created here
        // lightMeter = (ProgressBar) findViewById(R.id.lightmeter);
        SensorManager LightSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor = LightSensorManager
                .getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            Toast.makeText(InformationCollectionService.this,
                    "No Light Sensor! quit-", Toast.LENGTH_LONG).show();
        } else {
            // float max = lightSensor.getMaximumRange();
            // lightMeter.setMax((int)max);
            LightSensorManager.registerListener(this, lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        // accelerometer information defined here
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Registers a SensorEventListener for the given sensor

        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);

        // try {
        // publishResults();
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        return Service.START_NOT_STICKY;

    }

    public class MyBinder extends Binder {
        InformationCollectionService getService() {
            return InformationCollectionService.this;
        }
    }

    // class HUDView extends ViewGroup {
    // private Paint mLoadPaint;
    //
    // public HUDView(Context context) {
    // super(context);
    // Toast.makeText(getContext(),"HUDView", Toast.LENGTH_LONG).show();
    //
    // mLoadPaint = new Paint();
    // mLoadPaint.setAntiAlias(true);
    // mLoadPaint.setTextSize(10);
    // mLoadPaint.setARGB(255, 255, 0, 0);
    // }
    //
    // @Override
    // protected void onLayout(boolean changed, int l, int t, int r, int b) {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public boolean onTouchEvent(MotionEvent event) {
    //
    // // ATTENTION: GET THE X,Y OF EVENT FROM THE PARAMETER
    // // THEN CHECK IF THAT IS INSIDE YOUR DESIRED AREA
    //
    //
    // Toast.makeText(getContext(),"onTouchEvent", Toast.LENGTH_LONG).show();
    // return true;
    // }
    // }

    // Set get parameter to let outside class extract variable

    public String getLongitude() {
        return this.tvLongitude;
    }

    public String getLatitude() {
        return this.tvLatitude;
    }

    public String getMyAddress() {
        return this.myAddress;

    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public String getDeviceID() {
        return this.device_id;

    }

    public String getPhoneNumber() {
        return this.mPhoneNumber;
    }

    public String getXText() {
        return this.tvXText;
    }

    public String getYText() {
        return this.tvYText;
    }

    public String getZText() {
        return this.tvZText;
    }

    public String getlightReading() {
        return this.lightReading;
    }

    // Get touch screen gesture text info

    public String gettouchScreenText() {
        return this.touchScreenText;
    }

    // Set touch screen gesture text info

    public void settouchScreenText(String touchScreenText) {
        this.touchScreenText = touchScreenText;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_UP)

            touchScreenText = "Action :" + event.getAction() + "\t X :"
                    + event.getRawX() + "\t Y :" + event.getRawY();

        return true;

    }

    public void cancelAlarmIfExists(Context mContext, int requestCode,
                                    Intent intent) {
        try {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                    requestCode, intent, 0);
            AlarmManager am = (AlarmManager) mContext
                    .getSystemService(Context.ALARM_SERVICE);
            am.cancel(pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

}
