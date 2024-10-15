package com.example.notez;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private ImageView musicPlayerButton;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    private ImageView logoutImageView;
    private ImageView circleImageView;
    private ImageView plusImageView;

    private static final int REQUEST_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the views
        logoutImageView = findViewById(R.id.logout);
        circleImageView = findViewById(R.id.circle);
        plusImageView = findViewById(R.id.plus);

        // Set up the click listener for logout
        logoutImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // Set up the click listener for the circle icon
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoteDisplay();
            }
        });

        // Set up the click listener for the plus icon
        plusImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoteDisplay();
            }
        });

        musicPlayerButton = findViewById(R.id.music_player_button);

        // Request permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }

        musicPlayerButton.setOnClickListener(v -> {
            if (isPlaying) {
                pauseMusic();
            } else {
                playMusic();
            }
        });
    }

    private void playMusic() {
        if (mediaPlayer == null) {
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.music); // Replace with your audio file
            mediaPlayer = MediaPlayer.create(this, uri);
        }
        mediaPlayer.start();
        isPlaying = true;
        musicPlayerButton.setImageResource(R.drawable.ic_pause); // Change icon to pause
        Toast.makeText(this, "Music Playing", Toast.LENGTH_SHORT).show();
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            musicPlayerButton.setImageResource(R.drawable.ic_play); // Change icon to play
            Toast.makeText(this, "Music Paused", Toast.LENGTH_SHORT).show();
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
        // Navigate to the note_display activity
        Intent intent = new Intent(MainActivity.this, note_display.class);
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
