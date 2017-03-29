package sensor.information.collector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVWriter;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.format.DateFormat;
import android.text.method.Touch;
import android.util.Log;

public class ExportFileTask extends AsyncTask<Void, Void, Double[]> {

	public String ctext = new String();
	public List<Contact> input_contact = new ArrayList<Contact>();
	public String TouchScreenMessage = new String();
	public Contact ct = new Contact();
	public CSVWriter csvWrite;
	// public HashMap<String, Integer> hash_map;
	public File cfile;
	public String root;
	public DatabaseHandler db = new DatabaseHandler(
			MyApplication.getAppContext());

	public ProgressDialog expProgress;
	public MainActivity context;

	public ExportFileTask(MainActivity activity) {
		expProgress = new ProgressDialog(activity);
	}

	// Show Progress bar before downloading Music
	@Override
	protected void onPreExecute() {
		super.onPreExecute(); // Shows
		// Progress Bar Dialog and then call doInBackground method
		// expProgress = new ProgressDialog(MyApplication.getAppContext());
		expProgress.setTitle("Export Data ...");
		expProgress.setMessage("Export in progress ...");
		expProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		expProgress.setProgress(0);
		// int max=db.getContactsCount();
		expProgress.setMax(db.getContactsCount());
		expProgress.show();

	}

	@Override
	protected Double[] doInBackground(Void... params) {
		// TODO Auto-generated method stub
		Double result[] = new Double[2];
		if (MainActivity.isExternalStorageWritable() == false) {
			Log.e("OutsideStorage disabled",
					"Must enable the outside storage before exporting.");
		} else {
			root = Environment.getExternalStorageDirectory().toString();
			File myDir = new File(root + "/IACache/");
			String fname = DateFormat.format("dd.MM.yyyy.hh.mm.ss",
					new java.util.Date()).toString();
			String cname = "count.csv";
			String srate = "sampleRate.txt";
			MainActivity.ctext = new String();
			TouchScreenMessage = new String();
			MainActivity.hash_map = null;
			File file = new File(myDir, fname + ".csv");
			File sfile = new File(myDir, srate);
			cfile = new File(myDir, cname);
			TokenFilter tf;

			if (!file.exists() || !cfile.exists()) {
				myDir.mkdirs();
			}

			try {
				input_contact = db.getAllContacts();
				ct = null;
				if (input_contact.size() > 0) {
					// file.createNewFile();
					csvWrite = new CSVWriter(new FileWriter(file));

					for (int index = 0; index < input_contact.size(); index++) {
						ct = input_contact.get(index);
						int srNo = index;
						String arrStr[] = { String.valueOf(srNo + 1),
								ct.getName(), ct.getPhoneNumber(),ct.getLongitude(),
								ct.getLatitude(),
								ct.getAddress().replaceAll("\n", " "),
								ct.getXText(), ct.getYText(), ct.getZText(),
								ct.getLightReading(), ct.getTouchScreen(), ct.getUserIDTrue(), ct.getUserIDWindVane(),
								ct.getTimeStamp() };
						csvWrite.writeNext(arrStr, false);
						/* Combine the result to a single string */

						if (ct.getTouchScreen() != null) {
							TouchScreenMessage = ct.getTouchScreen()
									.replaceAll(",", "");
							tf=new TokenFilter(TouchScreenMessage);
							TouchScreenMessage=tf.CoarseGrained();
						}
						// only choose the last stride to calculate the
						// JS-distance
						if (MainActivity.stride >= input_contact.size()
								|| (MainActivity.stride < input_contact.size() && (index - (input_contact
										.size() - MainActivity.stride)) >= 0)) {
							/* int size=MainActivity.ctext.length(); */
							MainActivity.ctext = //ct.getName()
									//+ " "
									//+ ct.getPhoneNumber()
									//+ " "
									 ct.getAddress().replaceAll("\n", " ")
											.replaceAll(",", "") + " long"
                                    + ct.getLongitude() + " lat"
                                    + ct.getLatitude() + " ax"
									+ ct.getXText() + " ay" + ct.getYText() + " az"
									+ ct.getZText() + " l"
									+ ct.getLightReading().trim() + " "
									+ TouchScreenMessage
//									+ " " + ct.getTimeStamp()
							;

							MainActivity.hash_map = MainActivity.AddTwoHash(
									MainActivity
											.makeWordList(MainActivity.ctext),
									MainActivity.hash_map);
						}
						publishProgress();
					}
					csvWrite.close();

					/*
					 * Write the word count information to the count.csv file
					 */
					// HashMap<String, Integer> hash_map =
					// makeWordList(ctext);

					/* Check if the directory contains the count.csv */
					if (!cfile.exists()) {
						// First time export
						Log.d("Creating File...",
								"Creating count file: count.csv");
						/*
						 * Toast.makeText(MyApplication.getAppContext(),
						 * "Creating count file: count.csv",
						 * Toast.LENGTH_SHORT).show();
						 */
						List<HashMap<String, Integer>> myArrList = new ArrayList<HashMap<String, Integer>>();
						myArrList.add(MainActivity.hash_map);
						try {
							CSVWriter csvWrite_count = new CSVWriter(
									new FileWriter(cfile));
							Object[] keyArray = MainActivity.hash_map.keySet()
									.toArray();
							String[] str_keyArray = MainActivity
									.convertToString(keyArray);
							Object[] valueArray = MainActivity.hash_map
									.values().toArray();
							String[] str_valueArray = MainActivity
									.convertToString(valueArray);

							if (myArrList.size() > 0) {
								csvWrite_count.writeNext(str_keyArray, false);
								csvWrite_count.writeNext(str_valueArray, false);
							}
							csvWrite_count.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else {
						try {
							// The historical count is already existing
							Log.d("Reading File...",
									"Reading count file: count.csv");
							/*
							 * Toast.makeText(MyApplication.getAppContext(),
							 * "Reading count file: count.csv",
							 * Toast.LENGTH_SHORT).show();
							 */
							HashMap<String, Integer> historical_hashmap = new HashMap<String, Integer>();
							/*
							 * String key_string = new String(); String
							 * value_string = new String();
							 */
							/*
							 * BufferedReader br = new BufferedReader( new
							 * FileReader(root + "/IACache/" + "count.csv"));
							 */
							// String line = null;
							// line = br.readLine();

							historical_hashmap = MainActivity
									.convertToHashMap(root + "/IACache/"
											+ "count.csv");
							/* Calculate the current distribution */
							Double[] current_distribution = MainActivity
									.countProbability(MainActivity.hash_map,
											historical_hashmap);

							/* Calculate the historical distribution */
							Double[] historical_ditribution = MainActivity
									.countProbability(historical_hashmap,
											historical_hashmap);

							// Calculate the JS-distance between two
							// distributions
							MainActivity.js_dis = MainActivity
									.jensenShannonDivergence(
											historical_ditribution,
											current_distribution);
							// update the sample rate
							/*
							 * InformationCollectionService.sampleRate =
							 * InformationCollectionService.sampleConstantRate /
							 * (MainActivity.js_dis *
							 * InformationCollectionService.parameter_Gama);
							 */
							InformationCollectionService.sampleRate = InformationCollectionService.sampleConstantRate
									/ (Math.pow(
											MainActivity.js_dis,
											InformationCollectionService.parameter_Gama));

							/* Add two hashmap together */
							HashMap<String, Integer> add_result_hashmap = new HashMap<String, Integer>();
							add_result_hashmap = MainActivity.AddTwoHash(
									MainActivity.hash_map, historical_hashmap);
							// hash_map.putAll(historical_hashmap);
							// refresh cfile
							cfile.delete();
							/* Output the new hashmap */
							List<HashMap<String, Integer>> myArrList = new ArrayList<HashMap<String, Integer>>();
							myArrList.add(MainActivity.hash_map);
							CSVWriter csvWrite_count;

							csvWrite_count = new CSVWriter(
									new FileWriter(cfile));

							Object[] keyArray = add_result_hashmap.keySet()
									.toArray();
							String[] str_keyArray = MainActivity
									.convertToString(keyArray);
							Object[] valueArray = add_result_hashmap.values()
									.toArray();
							String[] str_valueArray = MainActivity
									.convertToString(valueArray);

							if (myArrList.size() > 0) {
								csvWrite_count.writeNext(str_keyArray, false);
								csvWrite_count.writeNext(str_valueArray, false);
							}
							csvWrite_count.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				} else {

					Log.e("Cache Database Error",
							"The cahce database is empty, please try again later.");
					/*
					 * Toast.makeText( MyApplication.getAppContext(),
					 * "The cahce database is empty, please try again later.",
					 * Toast.LENGTH_SHORT).show();
					 */
				}

			} catch (IOException e) {
				Log.e("SearchResultActivity", e.getMessage(), e);
			}
			// write to sample rate count.txt
			try {
				FileWriter fwSample = new FileWriter(sfile, true);
				fwSample.write(" "
						+ Double.toString(InformationCollectionService.sampleRate));
				fwSample.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		result[0] = MainActivity.js_dis;
		result[1] = (double) db.getContactsCount();
		return result;
	}

	protected void onProgressUpdate(Void... progress) {
		expProgress.incrementProgressBy(1);
	}

	@Override
	protected void onPostExecute(Double[] result) {

		MainActivity.disProgress.setProgress((int) (result[0] * 10));
		MainActivity.tx_js_dis.setText(Double.toString(result[0]));

		expProgress.dismiss();
		db.trunkContact();
		MainActivity.tvDBR.setText(Double.toString(result[1]));
		/*
		 * MainActivity.tvSR.setText(Double
		 * .toString(InformationCollectionService.sampleRate));
		 */
		MainActivity.tvSR.setText(new DecimalFormat("#0.0")
				.format(InformationCollectionService.sampleRate)
				+ " "
				+ MainActivity.windvane.getPreDirection()
				+ "->"
				+ MainActivity.windvane.getDirection());
		InformationCollectionService.export_flag = false;
	}

}
