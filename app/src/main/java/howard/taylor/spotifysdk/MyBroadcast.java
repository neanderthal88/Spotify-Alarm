package howard.taylor.spotifysdk;

import android.content.*;
import android.util.Log;

public class MyBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String songURI = intent.getStringExtra("songURI");

        Log.d("song", songURI + "");
        Intent i = new Intent(context.getApplicationContext(),AlarmActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        i.putExtra("songURI", songURI);
        i.putExtra("songList", intent.getSerializableExtra("songList"));
        context.startActivity(i);
    }
}
