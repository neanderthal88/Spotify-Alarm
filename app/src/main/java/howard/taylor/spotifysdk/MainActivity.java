package howard.taylor.spotifysdk;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;

public class MainActivity extends AppCompatActivity implements ConnectionStateCallback, PlayerNotificationCallback {
    private static final String CLIENT_ID = new Info().ClientID;
    private static final String REDIRECT_URI = new Info().REDIRECT_URI;
    private static final int REQUEST_CODE = 0;
    TimePicker timePicker;
    private Player mPlayer;
    BroadcastReceiver _broadcastReceiver;
    private final SimpleDateFormat _sdfWatchTime = new SimpleDateFormat("h:mm a");
    private TextView _tvTime;
    private SpotifyApi api;
    private SpotifyService spotify;
    private ProgressDialog progDailog;
    private int alarmHour, alarmMinute;
    AlarmDBHelper mydb = new AlarmDBHelper(this);
    DateFormat timeFormat = new SimpleDateFormat("hh:mm a");


    ArrayList<String> listOfPlaylists = new ArrayList<>();
    List<PlaylistSimple> playlists = new ArrayList<>();
    private String userID;
    private Pager<PlaylistTrack> songList;
    int playlistID = 0;
    String songURI;
    ArrayList<PlaylistTrack> songArrayList = new ArrayList<>();
    ArrayList<String> stringSongArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupTimeList();
        setupTimeDate();
        setupSpotify();

    }

    private void setupTimeList() {
        final ArrayList allAlarmTimes = mydb.getAllAlarms();
        final ArrayList<Integer> allAlarmIDs = mydb.getAllIDs();

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.listview, allAlarmTimes);
        ListView dbView = (ListView) findViewById(R.id.dbScrollView);
        dbView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

                Toast.makeText(getApplicationContext(), ""+position, Toast.LENGTH_SHORT).show();
            }
        });
        dbView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mydb.removeAlarm(allAlarmIDs.get(position));
                setupTimeList();
                onCancelAlarm();
                return true;
            }
        });
        dbView.setAdapter(arrayAdapter);
    }

    private void setupTimeDate() {
        Calendar cal = Calendar.getInstance();
        TextView date = (TextView) findViewById(R.id.date);
        String month = this.getApplicationContext().getResources().getStringArray(R.array.month_names)[cal.get(Calendar.MONTH)];
        String day_of_week = this.getApplicationContext().getResources().getStringArray(R.array.day_names)[cal.get(Calendar.DAY_OF_WEEK) - 1];
        Integer day_of_month = cal.get(Calendar.DAY_OF_MONTH);
        date.setText(day_of_week + ", " + month + " " + day_of_month.toString());
    }

    private void setupSpotify() {
        _tvTime = (TextView) findViewById(R.id.time);
        _tvTime.setText(_sdfWatchTime.format(new Date()));

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);

        builder.setScopes(new String[]{"playlist-read-private", "user-read-private", "streaming", "user-read-email"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        api = new SpotifyApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        listOfPlaylists.clear();
        _tvTime = (TextView) findViewById(R.id.time);
        _tvTime.setText(_sdfWatchTime.format(new Date()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Log.e("MainActivity", "TOKEN: " + response.getAccessToken());
                api.setAccessToken(response.getAccessToken());
                spotify = api.getService();
//                For testing purpose only
//                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
//                mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
//                    @Override
//                    public void onInitialized(Player player) {
//                        mPlayer.addConnectionStateCallback(MainActivity.this);
//                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
//
//                    }
//                });
//
            }
        }
        if (requestCode == 1) {
            if (intent != null) {
                playlistID = intent.getIntExtra("id", -1);
            }
            new getPlaylistSongs().execute(playlistID);

        }
    }

    public void getPlaylistButtonClick(View view) {
        new getUserIDSync(this.getApplicationContext()).execute();

    }

    public void onAlarmCreateClick(View view) {
        new getUserIDSync(this.getApplicationContext()).execute();
        Toast.makeText(this, "Please select a playlist", Toast.LENGTH_SHORT).show();
//        if (stringSongArrayList.isEmpty()) {
//            new getUserIDSync(this.getApplicationContext()).execute();
//            Toast.makeText(this, "Please select a playlist", Toast.LENGTH_SHORT).show();
//        }
//        else{
//            showDialog(0);
//            //onCreateAlarm();
//        }


    }

    @Override
    protected Dialog onCreateDialog(int playlistID) {
        return new TimePickerDialog(this, timePickerListener, alarmHour, alarmMinute, false);
    }


    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            alarmHour = hourOfDay;
            alarmMinute = minute;
            onCreateAlarm();
            Log.d("alarm", "Hour: " + alarmHour + "Minute: " + alarmMinute);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        _broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0)
                    _tvTime.setText(_sdfWatchTime.format(new Date()));
            }
        };

        registerReceiver(_broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (_broadcastReceiver != null)
            unregisterReceiver(_broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.add_playlist) {
            new getUserIDSync(this.getApplicationContext()).execute();
        }

        if (id == R.id.logout) {
            mydb.dropTable();
            logout();
        }

        return super.onOptionsItemSelected(item);
    }


    public void logout() {
        AuthenticationClient.clearCookies(this.getApplicationContext());
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    public boolean testFiveSeconds(View view) {
        //Create an offset from the current time in which the alarm will go off.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 5);
        if (stringSongArrayList.isEmpty() || songArrayList.isEmpty()) {
            Toast.makeText(this, "Please select a playlist", Toast.LENGTH_LONG).show();
            return false;
        }
        //Create a new PendingIntent and add it to the AlarmManager
        Intent intent = new Intent(this, MyBroadcast.class);
//        intent.putExtra("songURI", songURI);
        intent.putExtra("songList", stringSongArrayList);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),
                12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am =
                (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                pendingIntent);
        return true;
    }

    public boolean onCreateAlarm() {
        //Create an offset from the current time in which the alarm will go off.
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, alarmHour);
        cal.set(Calendar.MINUTE, alarmMinute);
        cal.set(Calendar.SECOND, 0);
        if (!(cal.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())) {
            cal.add(Calendar.DATE, 1);
        }
        if (stringSongArrayList.isEmpty() || songArrayList.isEmpty()) {
            Toast.makeText(this, "Please select a playlist", Toast.LENGTH_LONG).show();
            return false;
        }

        Intent intent = new Intent(this, MyBroadcast.class);
        intent.putExtra("songList", stringSongArrayList);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),
                (int)cal.getTimeInMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am =
                (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                pendingIntent);
//        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        mydb.insertAlarm((int)cal.getTimeInMillis(), 1, "day", timeFormat.format(cal.getTime()), "");


        Toast.makeText(this, "Alarm is set for " + cal.getTime().toString(), Toast.LENGTH_SHORT).show();
        setupTimeList();
        return true;
    }

    public boolean onCancelAlarm() {
        Intent intent = new Intent(this, MyBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),
                12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent.cancel();
        AlarmManager am =
                (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pendingIntent);
        return true;
    }
    class getPlaylistSongs extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(MainActivity.this);
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
            progDailog.dismiss();

        }

        @Override
        protected Integer doInBackground(Integer... params) {
            PlaylistSimple playlist = playlists.get(params[0]);
            String playlistID = playlist.id;
            songList = spotify.getPlaylistTracks(playlist.owner.id, playlistID);
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {

            songArrayList.clear();
            stringSongArrayList.clear();

            for (PlaylistTrack song : songList.items) {
                songArrayList.add(song);
                stringSongArrayList.add(song.track.uri);
            }
            Random rand = new Random();
            songURI = songArrayList.get(rand.nextInt(songArrayList.size())).track.uri;

        }
    }

    class getUserIDSync extends AsyncTask<Void, Void, Integer> {

        private Context context;

        public getUserIDSync(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(MainActivity.this);
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            userID = spotify.getMe().id;
            return 1;
        }

        @Override
        protected void onPostExecute(Integer i) {
            new getPlayListSync(context).execute();
        }

    }

    class getPlayListSync extends AsyncTask<Void, Void, Integer> {
        private Context context;

        public getPlayListSync(Context context) {
            this.context = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            playlists = spotify.getPlaylists(userID).items;
            return 1;
        }

        protected void onPostExecute(Integer integer) {
            for (PlaylistSimple list : playlists) {
                listOfPlaylists.add(list.name);
            }
            Intent intent = new Intent(context, ListOfPlaylists.class);
            Bundle b = new Bundle();
            b.putStringArrayList("playlist", listOfPlaylists);
            intent.putExtras(b);
            progDailog.dismiss();
            startActivityForResult(intent, 1);
            showDialog(0);
        }


    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
        switch (eventType) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
        switch (errorType) {
            // Handle error type as necessary
            default:
                break;
        }
    }

}
