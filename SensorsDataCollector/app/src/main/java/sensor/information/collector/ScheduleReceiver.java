/**
 * 
 */
package sensor.information.collector;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * @author yin
 * 
 */
public class ScheduleReceiver extends BroadcastReceiver {

	// restart service every 30 seconds
	public static long REPEAT_TIME = 1000 * 30;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		AlarmManager service = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, MyStartServiceReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Calendar cal = Calendar.getInstance();
		// start 10 second after boot complete
		cal.add(Calendar.SECOND, 10);
		// fetch every 10 seconds
		// InexactRepeating allows Android to optimize the energy consumption
		service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				cal.getTimeInMillis(), REPEAT_TIME, pending);
		// service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
		// REPEAT_TIME, pending);
	}

	/**
	 * @param args
	 */
}
