package sensor.information.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Environment;
import android.util.Log;

public class WindVane {
    private String w_direction;
    private String w_pre_direction;
    private double w_strength;
    private int w_duration;
    private int w_max;
    private int w_min;

    private double js_divergence_large;
    private double js_divergence_small;
    private double js_divergence_middle;

    private final double delta = 0.01;

    WindVane(String direction, double strength, int duriation, int max, int min) {
        this.w_direction = direction;
        this.w_strength = strength;
        this.w_duration = duriation;
        this.w_max = max;
        this.w_min = min;
    }

    public String getDirection() {
        return this.w_direction;
    }

    public void setDirection(String direction) {
        this.w_direction = direction;
    }

    public String getPreDirection() {
        return this.w_pre_direction;
    }

    public void setPreDirection(String pre_direction) {
        this.w_pre_direction = pre_direction;
    }

    public double getStrength() {
        return this.w_strength;
    }

    public void setStrength(double strength) {
        this.w_strength = strength;
    }

    public int getDuration() {
        return this.w_duration;
    }

    public void setDuration(int duration) {
        this.w_duration = duration;
    }

    public int getMax() {
        return this.w_max;
    }

    public void setMax(int max) {
        this.w_max = max;
    }

    public int getMin() {
        return this.w_min;
    }

    public void setMin(int min) {
        this.w_min = min;
    }

    public double getJs_divergence_large() {
        return this.js_divergence_large;
    }

    public void setJs_divergence_large(double JS_Divergence_large){
        this.js_divergence_large=JS_Divergence_large;
    }

    public double getJs_divergence_small(){
        return this.js_divergence_small;
    }

    public void setJs_divergence_small(double JS_Divergence_small){
        this.js_divergence_small=JS_Divergence_small;
    }

    public double getJs_divergence_middle(){
        return this.js_divergence_middle;
    }

    public void setJs_divergence_middle(double JS_Divergence_middle){
        this.js_divergence_middle=JS_Divergence_middle;
    }

    public int StrideIni(int stride) throws IOException {
        int newStride = stride;// should be equal to the MainActivity.stride
        int tem_stride_large;
        int tem_stride_small;
        int tem_stride_middle;
        WindVane windvane = this;
        /* Check the direction of wind vane */
        if (windvane.w_direction.equals("L")) {
			/* We need a comparably smaller divergence (wind points A->L) */
            if (windvane.w_max == -1) {
				/* find the upper bound */
                tem_stride_large = stride * 2;
                js_divergence_large = JSdivCalculation(tem_stride_large);

                if (js_divergence_large == -1) {
					/* Wait */
                    Log.d("Stride Change: ",
                            "L: Waiting for current user to perform more action.");
                } else {
                    if (windvane.w_min == -1) {
						/* First time measurement */
                        tem_stride_small = stride / 2;
                    } else {
                        tem_stride_small = windvane.w_min;
                    }
                    js_divergence_middle = JSdivCalculation(stride);
                    js_divergence_small = JSdivCalculation(tem_stride_small);

                    if ((Math.abs(js_divergence_large - js_divergence_middle) < delta)
                            && (Math.abs(js_divergence_small
                            - js_divergence_middle) < delta)) {
                        // converged, done
                        MainActivity.StrideIniFlag = false;
                        newStride = tem_stride_small;
                        windvane.w_max = -1;
                        windvane.w_min = -1;
                        Log.d("Stride Change: ", "L: done.");
                    } else if (js_divergence_large < js_divergence_middle) {
						/*
						 * Need continuously enlarge the stride, already find
						 * the lower bound, still need to find the upper bound
						 */
                        windvane.w_min = stride;// MUST
                        // first
                        // set
                        // to the current stride
                        newStride = tem_stride_large;// return the new stride
                        // windvane.w_min=newStride;
                        Log.d("Stride Change: ",
                                "L: still need to find the upper bound");
                    } else if (js_divergence_middle > js_divergence_small) {
						/* Already find the upper bound */
                        windvane.w_max = stride;// MUST
                        // first
                        // set
                        // this
                        newStride = tem_stride_small;// return the new stride
                        // windvane.w_max=newStride;
                        Log.d("Stride Change: ",
                                "L: Already find the upper bound.");
                    } else if ((js_divergence_large > js_divergence_middle)
                            && (js_divergence_middle < js_divergence_small)) {
						/*
						 * None of the directions are correct, the correction
						 * stride must near the current stride
						 */
                        windvane.w_max = tem_stride_large;
                        windvane.w_min = tem_stride_small;
                        newStride = stride;
                        Log.d("Stride Change: ",
                                "L: None of the directions are correct.");
                    }
                }
            } else if (windvane.w_min == -1) {
				/*
				 * already find the upper bound need to find the LOWER bound,
				 * using the upper bound JS div compares with the new lower bound
				 * JS div
				 */
                tem_stride_large = windvane.w_max;
                tem_stride_small = stride / 2;// current stride divided by two
                js_divergence_small = JSdivCalculation(tem_stride_small);
                js_divergence_large = JSdivCalculation(tem_stride_large);

                if (Math.abs(js_divergence_small - js_divergence_large) < delta) {
                    // converged, done
                    MainActivity.StrideIniFlag = false;
                    newStride = tem_stride_small;
                    windvane.w_max = -1;
                    windvane.w_min = -1;
                    Log.d("Stride Change: ", "L: done.");
                } else if (js_divergence_small > js_divergence_large) {
					/* already find the lower bound */
                    windvane.w_min = tem_stride_small;
                    newStride = tem_stride_large;
                    Log.d("Stride Change: ", "L: already find the lower bound.");
                } else if (js_divergence_small < js_divergence_large) {
					/*
					 * does not find the lower bound, need further shrink the
					 * stride
					 */
                    windvane.w_max = tem_stride_small;
                    newStride = tem_stride_small;
                    Log.d("Stride Change: ",
                            "L: need further shrink the stride.");
                }

            } else {
				/* Already known the upper and lower bounds */
                tem_stride_middle = (windvane.w_max - windvane.w_min) / 2
                        + windvane.w_min;
                js_divergence_large = JSdivCalculation(windvane.w_max);
                js_divergence_middle = JSdivCalculation(tem_stride_middle);
                js_divergence_small = JSdivCalculation(windvane.w_min);

                if ((Math.abs(js_divergence_large - js_divergence_middle) < delta)
                        && (Math.abs(js_divergence_small - js_divergence_middle) < delta)) {
					/* Converged, done */
                    MainActivity.StrideIniFlag = false;
                    newStride = windvane.w_min;
                    windvane.w_max = -1;
                    windvane.w_min = -1;
                }
				/* further compare these values */
                else if (js_divergence_large < js_divergence_middle) {
					/* Need reset the lower bound */
                    windvane.w_min = tem_stride_middle;
					/* And further adjust the stride */
                    newStride = windvane.w_max;

                } else if (js_divergence_small < js_divergence_middle) {
					/* Need reset the upper bound */
                    windvane.w_max = tem_stride_middle;
					/* And further adjust the stride */
                    newStride = windvane.w_min;
                } else if ((js_divergence_large > js_divergence_middle)
                        && (js_divergence_small > js_divergence_middle)) {
					/*
					 * Neither the directions are correct, need further shrink
					 * the upper bound a little bit.
					 */
                    windvane.w_max--;
                    newStride = stride;
                }

            }
        } else if (windvane.w_direction.equals("A")) {
			/* We need a comparably larger divergence (wind points L->A) */
            if (windvane.w_max == -1) {
				/* find the upper bound */
                tem_stride_large = stride * 2;
                js_divergence_large = JSdivCalculation(tem_stride_large);

                if (js_divergence_large == -1) {
					/* Wait */
                    Log.d("Stride Change: ",
                            "A: Waiting for current user to perform more action.");
                } else {
                    if (windvane.w_min == -1) {
						/* First time measurement */
                        tem_stride_small = stride / 2;
                    } else {
                        tem_stride_small = windvane.w_min;
                    }
                    js_divergence_middle = JSdivCalculation(stride);
                    js_divergence_small = JSdivCalculation(tem_stride_small);

                    if ((Math.abs(js_divergence_large - js_divergence_middle) < delta)
                            && (Math.abs(js_divergence_small
                            - js_divergence_middle) < delta)) {
                        // converged, done
                        MainActivity.StrideIniFlag = false;
                        newStride = tem_stride_small;
                        windvane.w_max = -1;
                        windvane.w_min = -1;
                    } else if (js_divergence_large > js_divergence_middle) {// changed
						/*
						 * Need continuously enlarge the stride, already find
						 * the lower bound, still need to find the upper bound
						 */
                        windvane.w_min = stride;// MUST
                        // first
                        // set
                        // to the current stride
                        newStride = tem_stride_large;// return the new stride
                        // windvane.w_min=newStride;
                    } else if (js_divergence_middle < js_divergence_small) {// changed
						/* Already find the upper bound */
                        windvane.w_max = stride;// MUST
                        // first
                        // set
                        // this
                        newStride = tem_stride_small;// return the new stride
                        // windvane.w_max=newStride;
                    } else if ((js_divergence_large < js_divergence_middle)
                            && (js_divergence_middle > js_divergence_small)) {// changed
						/*
						 * None of the directions are correct, the correction
						 * stride must near the current stride
						 */
                        windvane.w_max = tem_stride_large;
                        windvane.w_min = tem_stride_small;
                        newStride = stride;

                    }
                }
            } else if (windvane.w_min == -1) {
				/*
				 * already find the upper bound need to find the LOWER bound,
				 * use the upper bound JS div comparing with the new lower bound
				 * JS div
				 */
                tem_stride_large = windvane.w_max;
                tem_stride_small = stride / 2;// current stride divided by two
                js_divergence_small = JSdivCalculation(tem_stride_small);
                js_divergence_large = JSdivCalculation(tem_stride_large);
                if (Math.abs(js_divergence_small - js_divergence_large) < delta) {
                    // converged, done
                    MainActivity.StrideIniFlag = false;
                    newStride = tem_stride_small;
                    windvane.w_max = -1;
                    windvane.w_min = -1;
                } else if (js_divergence_small < js_divergence_large) {// changed
					/* already find the lower bound */
                    windvane.w_min = tem_stride_small;
                    newStride = tem_stride_large;
                } else if (js_divergence_small > js_divergence_large) {// changed
					/*
					 * does not find the lower bound, need further shrink the
					 * stride
					 */
                    windvane.w_max = tem_stride_small;
                    newStride = tem_stride_small;
                }
            } else {
				/* Already known the upper and lower bound */
                tem_stride_middle = (windvane.w_max - windvane.w_min) / 2
                        + windvane.w_min;
                js_divergence_large = JSdivCalculation(windvane.w_max);
                js_divergence_middle = JSdivCalculation(tem_stride_middle);
                js_divergence_small = JSdivCalculation(windvane.w_min);

                if ((Math.abs(js_divergence_large - js_divergence_middle) < delta)
                        && (Math.abs(js_divergence_small - js_divergence_middle) < delta)) {
					/* Converged, done */
                    MainActivity.StrideIniFlag = false;
                    newStride = windvane.w_min;
                    windvane.w_max = -1;
                    windvane.w_min = -1;
                }
				/* further compare these values */
                else if (js_divergence_large > js_divergence_middle) {// changed
					/* Need reset the lower bound */
                    windvane.w_min = tem_stride_middle;
					/* And further adjust the stride */
                    newStride = windvane.w_max;
                } else if (js_divergence_small > js_divergence_middle) {// changed
					/* Need reset the upper bound */
                    windvane.w_max = tem_stride_middle;
					/* And further adjust the stride */
                    newStride = windvane.w_min;
                } else if ((js_divergence_large < js_divergence_middle)
                        && (js_divergence_small < js_divergence_middle)) {// changed
					/*
					 * Neither the directions are correct, need further shrink
					 * the upper bound a little bit.
					 */
                    windvane.w_max--;
                    newStride = stride;
                }

            }
        } else {
			/* Error occurs */
            return 0;
        }

        Log.d("js_divergence_large: ",Double.toString(js_divergence_large));
        Log.d("js_divergence_middle: ",Double.toString(js_divergence_middle));
        Log.d("js_divergence_small: ",Double.toString(js_divergence_small));

        return newStride;

    }

    public double JSdivCalculation(int tem_stride) throws IOException {
        String root;
        Contact ct;
        String TouchScreenMessage = new String();
        String ctext;
        HashMap<String, Integer> hash_map = null;
        HashMap<String, Integer> historical_hashmap = new HashMap<String, Integer>();
        DatabaseHandler db = new DatabaseHandler(MyApplication.getAppContext());
        List<Contact> input_contact = new ArrayList<Contact>();
        input_contact = db.getAllContacts();
        int dbSize = input_contact.size();
        root = Environment.getExternalStorageDirectory().toString();
        double jsDiv;

        if (dbSize >= tem_stride) {
			/* Do we have enough cache data? */
            for (int index = dbSize - tem_stride; index < dbSize; index++) {
                ct = input_contact.get(index);
				/* Combine the result to a single string */
                if (ct.getTouchScreen() != null) {
                    TouchScreenMessage = ct.getTouchScreen()
                            .replaceAll(",", "");
                }

                ctext = //ct.getName()
                        //+ " "
                        //+ ct.getPhoneNumber()
                        //+ " "
                         ct.getAddress().replaceAll("\n", " ")
                        .replaceAll(",", "") + " " + ct.getXText()
                        + " " + ct.getYText() + " " + ct.getZText() + " "
                        + ct.getLightReading() + " " + TouchScreenMessage + " "
                        //+ ct.getTimeStamp()
                ;
                hash_map = MainActivity.AddTwoHash(
                        MainActivity.makeWordList(ctext), hash_map);
            }
            historical_hashmap = MainActivity.convertToHashMap(root
                    + "/IACache/" + "count.csv");
			/* Calculate the current distribution */
            Double[] current_distribution = MainActivity.countProbability(
                    hash_map, historical_hashmap);
			/* Calculate the historical distribution */
            Double[] historical_ditribution = MainActivity.countProbability(
                    historical_hashmap, historical_hashmap);
            // Calculate the JS-distance between two
            // distributions
            jsDiv = MainActivity.jensenShannonDivergence(
                    historical_ditribution, current_distribution);
            return jsDiv;
        } else {
			/* We do not have enough cache data */
			/* Wait */
            return -1;
        }

    }
}
