package ia.information.collector;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

import sensor.information.collector.DatabaseHandler;
import sensor.information.collector.InformationCollectionService;
import sensor.information.collector.MainActivity;
import sensor.information.collector.MyApplication;

import static sensor.information.collector.MainActivity.mRetainedFragment;

public class Setting extends Activity {
    private DatabaseHandler dbclear = new DatabaseHandler(
            MyApplication.getAppContext());
    private String DeviceID;
    private static DatabaseReference mDatabase;

    public static SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        CheckBox UserIDCBox = (CheckBox) findViewById(R.id.UserIDCBox);
        CheckBox ServerCBox = (CheckBox) findViewById(R.id.ServerCB);
        UserIDCBox.setChecked(preferences.getBoolean("UserIDCBox", true));
        ServerCBox.setChecked(preferences.getBoolean("ServerCBox", false));
    }

    // Reset DB
    public void onClickCache_clr(View view) {
        dbclear.trunkContact();
        dbclear.close();
    }

    // Trunk ServerDB
    public void onClickServerDBReset(View view) {
        DeviceID = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //trunk only this user's data
        mDatabase.child(DeviceID).removeValue();
        //trunk all users' data
        //mDatabase.removeValue();
    }

    // Reset token distribution
    public void onClickHist_clr(View view) {
        File folder = Environment.getExternalStorageDirectory();
        String fileName = folder.getPath() + "/IACache/count.csv";
        File myFile = new File(fileName);
        if (myFile.exists()) {
            myFile.delete();
            Log.d(
                    "Deleting file: ",
                    "Success.");
        } else {
            Log.d("Deleting file: ",
                    "Deleting file failure.");
        }
        SharedPreferences.Editor editor = MainActivity.preferences_main.edit();
        editor.putBoolean("WindVaneCB", false);
        editor.putBoolean("UserIDCBox", true);
        editor.putString("preDirection", "L");
        editor.putString("Direction", "L");
        editor.commit();

        MainActivity.windvane.setPreDirection("L");
        MainActivity.windvane.setDirection("L");
        CheckBox UserIDCBox = (CheckBox) findViewById(R.id.UserIDCBox);
        UserIDCBox.setChecked(true);
    }

    // Reset system
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void onClickSysReset(View view) {
        dbclear.trunkContact();
        dbclear.close();
        File folder = Environment.getExternalStorageDirectory();
        String foldername = folder.getPath() + "/IACache/";
        File myfolder = new File(foldername);

        String[] files;
        files = myfolder.list();
        if (files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File myFile = new File(myfolder, files[i]);
                myFile.delete();
            }
            Log.d(
                    "Deleting file: ",
                    "Success.");
        } else {
            Log.d("Deleting file: ",
                    "Deleting file failure.");
        }

        SharedPreferences.Editor editor1 = MainActivity.preferences_main.edit();
        editor1.putBoolean("WindVaneCB", false);
        editor1.putString("edCache", "500");
        editor1.putString("preDirection", "L");
        editor1.putString("Direction", "L");
        editor1.putFloat("Strength", 0);
        editor1.putInt("Duration", 0);
        editor1.putInt("max", -1);
        editor1.putInt("min", -1);
        editor1.putFloat("js_divergence_large", -1);
        editor1.putFloat("js_divergence_middle", -1);
        editor1.putFloat("js_divergence_small", -1);
        editor1.putBoolean("StrideIniFlag",false);
        editor1.putInt("stride",50);
        editor1.apply();

        SharedPreferences.Editor editor2 = preferences.edit();
        editor2.putBoolean("UserIDCBox", true);
        editor2.putBoolean("ServerCBox", false);
        editor2.apply();

        MainActivity.windvane.setPreDirection("L");
        MainActivity.windvane.setDirection("L");
        CheckBox UserIDCBox = (CheckBox) findViewById(R.id.UserIDCBox);
        CheckBox ServerCBox = (CheckBox) findViewById(R.id.ServerCB);
        //CheckBox WindVaneCB =  (CheckBox) findViewById(R.id.WindVaneCB);
        UserIDCBox.setChecked(true);
        ServerCBox.setChecked(false);
        //WindVaneCB.setChecked(false);
        MainActivity.stride=50;

        //Another parameters - do not need store when app crashed
        InformationCollectionService.autoSampleInd = false;
        MainActivity.StrideIniFlag = false;

        if (mRetainedFragment != null) {
            mRetainedFragment=null;
        }
    }


    // User ID check box
    public void onCheckboxClickedUserIDCbox(View view) {
        CheckBox UserIDCBox = (CheckBox) findViewById(R.id.UserIDCBox);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("UserIDCBox", UserIDCBox.isChecked());
        editor.commit();
    }


    // Server upload check box
    public void onCheckboxClickedServerCbox(View view) {
        CheckBox ServerCBox = (CheckBox) findViewById(R.id.ServerCB);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("ServerCBox", ServerCBox.isChecked());
        editor.commit();
    }
}
