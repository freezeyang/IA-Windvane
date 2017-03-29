package sensor.information.collector;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

/**
 * Created by yin on 3/25/17.
 */

public class RetainedFragment extends Fragment {

    // data object we want to retain
    private WindVane data;

    // this method is only called once for this fragment
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setData(WindVane data) {
        this.data = data;
    }

    public WindVane getData() {
        return data;
    }
}
