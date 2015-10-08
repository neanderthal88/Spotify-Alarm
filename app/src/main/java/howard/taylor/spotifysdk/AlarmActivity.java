package howard.taylor.spotifysdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import java.util.ArrayList;
import java.util.Random;

public class AlarmActivity extends AppCompatActivity implements ConnectionStateCallback, PlayerNotificationCallback {
    private static final String CLIENT_ID = new Info().ClientID;
    private static final String REDIRECT_URI = new Info().REDIRECT_URI;
    private static final int REQUEST_CODE = 0;
    private MediaPlayer mMediaPlayer;
    private Player mPlayer;
    private String songURI;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_alarm);
        //songURI = getIntent().getStringExtra("songURI");
        ArrayList<String> songList  =(ArrayList<String>) getIntent().getSerializableExtra("songList");
        Random rand = new Random();
        songURI = songList.get(rand.nextInt(songList.size()));
        Log.d("song", "" +songURI);
        Button stopAlarm = (Button) findViewById(R.id.stopAlarm);
        stopAlarm.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                mPlayer.pause();
                finish();
                return false;
            }
        });
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);

        builder.setScopes(new String[]{"playlist-read-private", "user-read-private", "streaming", "user-read-email"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        //playSound(this, getAlarmUri());
    }


    private void playSong() {
        mPlayer.play(songURI);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Log.e("MainActivity", "TOKEN: " + response.getAccessToken());

                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer.addConnectionStateCallback(AlarmActivity.this);
                        mPlayer.addPlayerNotificationCallback(AlarmActivity.this);
                        playSong();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());

                    }
                });

            }
        }

    }

//    public void onFinish(View v) {
//        mMediaPlayer.stop();
//        finish();
//
//    }
//    private void playSound(Context context, Uri alert) {
//        mMediaPlayer = new MediaPlayer();
//        try {
//            mMediaPlayer.setDataSource(context, alert);
//            final AudioManager audioManager = (AudioManager) context
//                    .getSystemService(Context.AUDIO_SERVICE);
//            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
//                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
//                mMediaPlayer.prepare();
//                mMediaPlayer.start();
//            }
//        } catch (IOException e) {
//            System.out.println("OOPS");
//        }
//    }
//
//    //Get an alarm sound. Try for an alarm. If none set, try notification,
//    //Otherwise, ringtone.
//    private Uri getAlarmUri() {
//        Uri alert = RingtoneManager
//                .getDefaultUri(RingtoneManager.TYPE_ALARM);
//        if (alert == null) {
//            alert = RingtoneManager
//                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            if (alert == null) {
//                alert = RingtoneManager
//                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//            }
//        }
//        return alert;
//    }

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
            case TRACK_UNAVAILABLE:
                //TODO: ADD FUNCTINALITY IF IT FAILS
                break;
            case ERROR_PLAYBACK:
                //TODO: ADD FUNCTINALITY IF IT FAILS
                break;
            case ERROR_UNKNOWN:
                //TODO: ADD FUNCTINALITY IF IT FAILS
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }


}