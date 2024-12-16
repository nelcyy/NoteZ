package com.example.notez;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.OnNoteClickListener {

    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private List<NoteModel> notesList;
    private FirebaseFirestore firestore;

    private ImageView musicPlayerButton;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false; // to track music playing state
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    private EditText searchBar; // Search bar for filtering notes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        ImageView logoutImageView = findViewById(R.id.logout);
        ImageView circleImageView = findViewById(R.id.circle);
        ImageView plusImageView = findViewById(R.id.plus);
        musicPlayerButton = findViewById(R.id.music_player_button);
        searchBar = findViewById(R.id.searchBar);
        notesRecyclerView = findViewById(R.id.rvListNote);

        notesList = new ArrayList<>();
        notesAdapter = new NotesAdapter(notesList,this);

        notesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        notesRecyclerView.setAdapter(notesAdapter);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

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
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter notes based on the input
                filterNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        fetchNotes(); // Load notes from Firestore
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchNotes(); // Ensure notes are loaded each time the activity is resumed
    }

    private void fetchNotes() {
        String userId = auth.getCurrentUser().getUid();

        firestore.collection("notes")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to load notes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null) {
                        notesList.clear();  // Clear existing notes
                        for (QueryDocumentSnapshot document : snapshot) {
                            NoteModel note = document.toObject(NoteModel.class);
                            notesList.add(note);
                        }
                        notesAdapter.notifyDataSetChanged();  // Notify adapter to refresh UI
                    }
                });
    }

    private void filterNotes(String query) {
        List<NoteModel> filteredList = new ArrayList<>();

        // Convert the query to lowercase for case-insensitive comparison
        String lowercaseQuery = query.toLowerCase();

        // Separate lists for matches starting with the query and others containing the query
        List<NoteModel> startsWithQuery = new ArrayList<>();
        List<NoteModel> containsQuery = new ArrayList<>();

        for (NoteModel note : notesList) {
            String title = note.getTitle().toLowerCase();

            if (title.startsWith(lowercaseQuery)) {
                // Add to "starts with" list if title starts with the query
                startsWithQuery.add(note);
            } else if (title.contains(lowercaseQuery)) {
                // Add to "contains" list if title contains the query elsewhere
                containsQuery.add(note);
            }
        }

        // Sort both lists based on ASCII values of their titles
        startsWithQuery.sort((n1, n2) -> n1.getTitle().compareTo(n2.getTitle()));
        containsQuery.sort((n1, n2) -> n1.getTitle().compareTo(n2.getTitle()));

        // Combine the two lists: startsWithQuery first, followed by containsQuery
        filteredList.addAll(startsWithQuery);
        filteredList.addAll(containsQuery);

        // Update the RecyclerView adapter with the filtered and sorted list
        notesAdapter.updateData(filteredList);
    }

    public void onNoteClick(int position) {
        // Get the clicked note from the list
        NoteModel clickedNote = notesList.get(position);

        // Open the note display activity and pass the clicked note details
        openNoteDisplay(clickedNote);
    }

    private void openNoteDisplay(NoteModel note) {
        Intent intent = new Intent(MainActivity.this, note_display.class);

        // Pass the note details to the next activity
        intent.putExtra("title", note.getTitle());
        intent.putExtra("subtitle", note.getSubtitle());
        intent.putExtra("content", note.getNote());

        startActivity(intent);
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