package howard.taylor.spotifysdk;

import android.content.*;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by taylo_000 on 10/5/2015.
 */
public class MyBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String songURI = intent.getStringExtra("songURI");
        Log.d("song", songURI + "");
        Intent i = new Intent(context.getApplicationContext(),AlarmActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("songURI", songURI);
        context.startActivity(i);
    }
}
