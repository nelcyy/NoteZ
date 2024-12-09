package com.example.notez;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// For music
import android.media.MediaPlayer;
import android.net.Uri;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private List<NoteModel> notesList;
    private FirebaseFirestore firestore;

    private ImageView musicPlayerButton;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false; // declare as false, to make the music pause at first
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView logoutImageView = findViewById(R.id.logout);
        ImageView circleImageView = findViewById(R.id.circle);
        ImageView plusImageView = findViewById(R.id.plus);
        musicPlayerButton = findViewById(R.id.music_player_button);

        // Initialize the RecyclerView and Notes list
        notesRecyclerView = findViewById(R.id.rvListNote);
        notesList = new ArrayList<>();
        notesAdapter = new NotesAdapter(notesList);

        // Set up the RecyclerView with a GridLayoutManager
        notesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        notesRecyclerView.setAdapter(notesAdapter);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Use the Firebase client ID
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

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

        fetchNotes();
    }

    private void fetchNotes() {
        String userId = auth.getCurrentUser().getUid();

        firestore.collection("notes")
                .whereEqualTo("userId", userId)  // Get notes for the current user
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            // Clear the existing list of notes
                            notesList.clear();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                // Convert document to NoteModel
                                NoteModel note = document.toObject(NoteModel.class);
                                notesList.add(note);  // Add note to the list
                            }
                            notesAdapter.notifyDataSetChanged();  // Notify the adapter to refresh the UI
                        }
                    } else {
                        Toast.makeText(this, "Failed to load notes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void playMusic() {
        if (mediaPlayer == null) {
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.music);
            mediaPlayer = MediaPlayer.create(this, uri);
            mediaPlayer.setLooping(true);
        }
        mediaPlayer.start();
        isPlaying = true;
        musicPlayerButton.setImageResource(R.drawable.ic_pause);
        Toast.makeText(this, "Music Playing", Toast.LENGTH_SHORT).show();
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            musicPlayerButton.setImageResource(R.drawable.ic_play);
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
        Intent intent = new Intent(MainActivity.this, note_display.class);
        startActivity(intent);
    }

    private void logout() {
        // Sign out of Firebase
        auth.signOut();

        // Sign out of Google
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        });

        // Clear the login status in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Navigate back to the login screen
        Intent intent = new Intent(MainActivity.this, SignIn.class);
        startActivity(intent);
        finish();
    }
}
