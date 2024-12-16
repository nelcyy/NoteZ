package com.example.notez;

// Android permissions and UI imports
import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

// Android OS imports
import android.os.Bundle; // Required for activity lifecycle management

// Java imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Camera-related imports
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.widget.EditText;
import android.widget.ImageView;

// Location-related imports
import android.location.Address;
import android.location.Geocoder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.location.Location;

// Bluetooth-related imports
import android.bluetooth.BluetoothAdapter;

public class note_display extends AppCompatActivity {

    private EditText noteEditText, titleEditText, subtitleEditText;
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int BLUETOOTH_REQUEST_CODE = 102;
    private FusedLocationProviderClient fusedLocationClient;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_display);

        titleEditText = findViewById(R.id.notesTitle); // Your title field ID
        subtitleEditText = findViewById(R.id.notesSubtitle); // Your subtitle field ID
        noteEditText = findViewById(R.id.notes); // Your note field ID
        ImageView cameraImageView = findViewById(R.id.camera); // Your camera icon ID
        ImageView backImageView = findViewById(R.id.back); // Your back arrow icon ID
        ImageView locationImageView = findViewById(R.id.location);  // Your location icon ID
        ImageView bluetoothImageView = findViewById(R.id.bluetooth); // Your Bluetooth icon ID
        ImageView doneImageView = findViewById(R.id.done); // Your done icon ID
        ImageView sendImageView = findViewById(R.id.send); // Your send icon ID
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // Initialize fusedLocationClient for location services
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize views
        TextView titleTextView = findViewById(R.id.notesTitle);
        TextView subtitleTextView = findViewById(R.id.notesSubtitle);
        TextView contentTextView = findViewById(R.id.notes);

        // Get the data passed via Intent
        String title = getIntent().getStringExtra("title");
        String subtitle = getIntent().getStringExtra("subtitle");
        String content = getIntent().getStringExtra("content");

        // Set the data to views
        titleTextView.setText(title);
        subtitleTextView.setText(subtitle);
        contentTextView.setText(content);

        backImageView.setOnClickListener(v -> goBackToMainPage());

        cameraImageView.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            } else {
                openCamera();
            }
        });

        locationImageView.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                fetchLocation();
            }
        });

        bluetoothImageView.setOnClickListener(v -> {
            // Check for location permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                // Check for Bluetooth permissions
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_REQUEST_CODE);
                } else {
                    // Toggle Bluetooth
                    toggleBluetooth();
                }
            }
        });

        doneImageView.setOnClickListener(v -> saveNote());

        sendImageView.setOnClickListener(view -> showEmailSelectionDialogAndSaveNote());

    }

    private void showEmailSelectionDialogAndSaveNote() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserEmail = currentUser.getEmail();

        // Fetch all registered emails
        firestore.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<String> emailList = new ArrayList<>();
                List<String> userIdList = new ArrayList<>();

                // Populate email and userId lists
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String email = document.getString("email");
                    if (!email.equals(currentUserEmail)) {
                        emailList.add(email);
                        userIdList.add(document.getId()); // Assuming userId is the document ID
                    }
                }

                if (!emailList.isEmpty()) {
                    // Convert email list to an array for the dialog
                    String[] emailArray = emailList.toArray(new String[0]);

                    // Show email list dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Select a User");
                    builder.setItems(emailArray, (dialog, which) -> {
                        String selectedEmail = emailArray[which];
                        String selectedUserId = userIdList.get(which); // Get the corresponding userId

                        // Collect note details
                        String title = titleEditText.getText().toString().trim();
                        String subtitle = subtitleEditText.getText().toString().trim();
                        String note = noteEditText.getText().toString().trim();

                        // Validate input fields
                        if (title.isEmpty() || subtitle.isEmpty() || note.isEmpty()) {
                            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get the first word or snippet
                        String firstWord = getFirstWord(note);

                        // Save the note for sending
                        saveNoteToFirebase(title, subtitle, note, firstWord, selectedUserId, true, selectedEmail);
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                    builder.show();
                } else {
                    Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show();
                }
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Failed to fetch users";
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNoteToFirebase(String title, String subtitle, String note, String firstWord, String userId, boolean isSending, @Nullable String selectedEmail) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference notesCollection = firestore.collection("notes");

        NoteModel noteModel = new NoteModel(title, subtitle, note, firstWord, userId);

        notesCollection.add(noteModel)
                .addOnSuccessListener(documentReference -> {
                    noteModel.setId(documentReference.getId());

                    if (isSending) {
                        // Handle null case for `selectedEmail`
                        String emailMessage = (selectedEmail != null) ? "to: " + selectedEmail : "to the recipient.";
                        Toast.makeText(this, "Note sent successfully " + emailMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Note saved successfully.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to save note";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", errorMessage, e);
                });
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String subtitle = subtitleEditText.getText().toString().trim();
        String note = noteEditText.getText().toString().trim();

        // Validate input fields
        if (title.isEmpty() || subtitle.isEmpty() || note.isEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the first word from the note (or a short snippet if needed)
        String firstWord = getFirstWord(note);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        Log.d("SaveNoteDebug", "Saving note for user: " + userId);

        // Call the save method with a specific message for saving
        saveNoteToFirebase(title, subtitle, note, firstWord, userId, false, null);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void insertImageIntoEditText(Bitmap photo) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(photo, 300, 400, true); // Scale the bitmap to fit better in the EditText

        ImageSpan imageSpan = new ImageSpan(this, scaledBitmap); // Scale the bitmap to fit better in the EditText

        // Get the EditText text as Spannable
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.insert(0, " "); // Insert a space for the image
        ssb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ssb.append("\n\n"); // Add a newline to separate the image from the text

        ssb.append(noteEditText.getText()); // Append the existing text below the image

        noteEditText.setText(ssb); // Set the modified text back to the EditText
        noteEditText.setSelection(ssb.length()); // Move the cursor to the end of the text
    }

    private void fetchLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        getAddressFromLocation(location);
                    } else {
                        Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = "Location: " + address.getAddressLine(0); // Full address with label

                // Create a SpannableStringBuilder to manage existing content
                SpannableStringBuilder spannableText = new SpannableStringBuilder(noteEditText.getText());

                // Remove any existing location text to avoid duplication, including any preceding or trailing newlines
                String currentText = spannableText.toString();
                int locationIndex = currentText.indexOf("Location: ");
                if (locationIndex != -1) {
                    int endOfLocation = currentText.indexOf("\n\n", locationIndex);

                    // Determine the range to delete, including surrounding newlines if found
                    if (endOfLocation == -1) {
                        endOfLocation = currentText.length();
                    } else {
                        endOfLocation += 2; // Include the "\n\n" after the location text
                    }

                    // Delete the location text along with any surrounding newlines
                    spannableText.delete(locationIndex, endOfLocation);
                }

                // Determine where to insert the updated location text
                ImageSpan[] imageSpans = spannableText.getSpans(0, spannableText.length(), ImageSpan.class);
                int insertPosition;

                if (imageSpans.length > 0) {
                    // If there's an image, insert location below the last image span
                    insertPosition = spannableText.getSpanEnd(imageSpans[imageSpans.length - 1]);
                    spannableText.insert(insertPosition, "\n\n" + addressText); // Insert with double newline after image
                } else if (spannableText.length() == 0) {
                    // No existing content; insert location at the start without any newline
                    spannableText.insert(0, addressText + "\n\n");
                } else {
                    // If there's text but no image, insert at the beginning with a newline for spacing
                    spannableText.insert(0, addressText + "\n\n");
                }

                // Set the updated text in the EditText
                noteEditText.setText(spannableText);
                noteEditText.setSelection(spannableText.length()); // Move cursor to the end

            } else {
                Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Geocoder service failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_CODE);
        } else {
            makeDiscoverable();
        }
    }

    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    private void goBackToMainPage() {
        finish(); // This will close the note_display activity and go back to MainActivity
    }

    private String getFirstWord(String note) {
        // Extract the first word or a snippet of the note
        String[] words = note.split("\\s+");
        return words.length > 0 ? words[0] : "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission is required to get current location", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == BLUETOOTH_REQUEST_CODE) {
            // Check if Bluetooth permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toggleBluetooth();
            } else {
                Toast.makeText(this, "Bluetooth permission is required to use Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = (Bitmap) extras.get("data");
                insertImageIntoEditText(photo);
            }
        } else if (requestCode == BLUETOOTH_REQUEST_CODE) {
            // Check if Bluetooth was enabled
            if (resultCode == RESULT_OK) {
                // Bluetooth has been enabled, now make the device discoverable
                makeDiscoverable();
            } else {
                Toast.makeText(this, "Bluetooth was not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}