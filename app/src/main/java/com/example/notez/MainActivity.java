package com.example.notez;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

//For music
import android.media.MediaPlayer;
import android.net.Uri;

public class MainActivity extends AppCompatActivity {

    private ImageView musicPlayerButton;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false; // declare as false, to make the music pause at first

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView logoutImageView = findViewById(R.id.logout);
        ImageView circleImageView = findViewById(R.id.circle);
        ImageView plusImageView = findViewById(R.id.plus);
        musicPlayerButton = findViewById(R.id.music_player_button);

        logoutImageView.setOnClickListener(v -> logout());

        circleImageView.setOnClickListener(v -> openNoteDisplay());

        plusImageView.setOnClickListener(v -> openNoteDisplay());

        musicPlayerButton.setOnClickListener(v -> {
            if (isPlaying) {
                pauseMusic();
            } else {
                playMusic();
            }
        });
    }

    private void playMusic() {
        if (mediaPlayer == null) { // mediaplayer doesnt been set up first
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.music); // specify the location of media files (music/mp3)
            mediaPlayer = MediaPlayer.create(this, uri); // now set up mediaPlayer with the music file URI
            mediaPlayer.setLooping(true); // set into loop, so it will play the music looping/not stop by itself
        }
        mediaPlayer.start(); // start playing the music file
        isPlaying = true; // make the boolean into true, means the music is playing
        musicPlayerButton.setImageResource(R.drawable.ic_pause); // Change icon play to pause
        Toast.makeText(this, "Music Playing", Toast.LENGTH_SHORT).show(); // show a message to the user saying "Music Playing"
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // pause playing the music file
            isPlaying = false; // make the boolean into false, means the music is stop now
            musicPlayerButton.setImageResource(R.drawable.ic_play); // Change icon to play
            Toast.makeText(this, "Music Paused", Toast.LENGTH_SHORT).show(); // show a message to the user saying "Music Playing"
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void openNoteDisplay() {
        Intent intent = new Intent(MainActivity.this, note_display.class); // Navigate to the note_display activity
        startActivity(intent);
    }

    private void logout() {
        // Clear shared preferences (use "user_prefs" here to match SignIn.java)
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // This will clear the login state
        editor.apply();

        // Navigate back to the login screen
        Intent intent = new Intent(MainActivity.this, SignIn.class);
        startActivity(intent);
        finish(); // Close the current activity
    }
}