/**
 * 
 */
package sensor.information.collector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author yin
 *
 */
public class MyStartServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Intent service=new Intent(context, InformationCollectionService.class);
		context.startService(service);
	}

	/**
	 * @param args
	 */

}
