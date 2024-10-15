package com.example.notez;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.PackageManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class note_display extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private ImageView cameraImageView;
    private ImageView backImageView;
    private EditText noteEditText;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private ImageView locationImageView;

    private BluetoothAdapter bluetoothAdapter;
    private ImageView bluetoothImageView; // Add a Bluetooth ImageView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_display); // Use your actual layout file

        // Initialize camera icon, back arrow icon, and note field
        cameraImageView = findViewById(R.id.imageView5); // Your camera icon ID
        backImageView = findViewById(R.id.imageView); // Your back arrow icon ID
        noteEditText = findViewById(R.id.textView5); // Your note field ID

        // Set up the camera click listener
        cameraImageView.setOnClickListener(v -> checkCameraPermission());

        // Set up the back arrow click listener
        backImageView.setOnClickListener(v -> goBackToMainPage());

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Find location icon and set listener
        locationImageView = findViewById(R.id.imageView7);  // Your location icon ID
        noteEditText = findViewById(R.id.textView5); // The EditText to display location

        // Set click listener for the location icon
        locationImageView.setOnClickListener(v -> getLocation());

        // Initialize Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothImageView = findViewById(R.id.imageView10); // Your Bluetooth icon ID

        // Set up Bluetooth click listener
        bluetoothImageView.setOnClickListener(v -> toggleBluetooth());
    }

    private void toggleBluetooth() {
        Log.d("BluetoothCheck", "Toggle Bluetooth called");
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if Bluetooth permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {

            Log.d("BluetoothCheck", "Bluetooth permissions not granted");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
            }, 1);
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Log.d("BluetoothCheck", "Bluetooth is not enabled, requesting enable");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        } else {
            Log.d("BluetoothCheck", "Bluetooth is already enabled, making discoverable");
            makeDiscoverable();
        }
    }

    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300); // 300 seconds
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(discoverableIntent);
    }


    // Method to go back to the MainActivity
    private void goBackToMainPage() {
        finish(); // This will close the note_display activity and go back to MainActivity
    }

    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private void openCamera() {
        if (!hasCamera()) {
            Toast.makeText(this, "No camera found on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            Log.d("CameraCheck", "Starting camera intent");
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Log.d("CameraCheck", "No app available to handle camera intent");
            Toast.makeText(this, "Camera not supported", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, open the camera
            openCamera();
        } else {
            // Permission is not granted, request for permission
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            Log.d("CameraCheck", "Camera permission not granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            // Handle camera permission result
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                openCamera();
            } else {
                // Permission denied, show a toast message
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Handle location permission result
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch location
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1) { // Replace 1 with your actual request code for Bluetooth
            // Handle Bluetooth permission result
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, attempt to enable Bluetooth again
                toggleBluetooth();
            } else {
                Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show();
            }
        }
        // Additional permissions can be handled here as needed
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle camera result
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            if (photo != null) {
                // Create a drawable from the captured Bitmap
                Drawable drawable = new BitmapDrawable(getResources(), photo);

                // Set the bounds for the drawable (resize as necessary)
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                // Create an ImageSpan
                ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);

                // Get the existing text in the EditText
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(noteEditText.getText());

                // Insert the image at the start of the text
                spannableStringBuilder.insert(0, " "); // This space will hold the image
                spannableStringBuilder.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Check if there are already double newlines after the image
                if (!spannableStringBuilder.toString().startsWith("\n\n", 1)) {
                    spannableStringBuilder.insert(1, "\n\n"); // Add newlines only if they don't exist
                }

                // Set the updated text with the image in the EditText
                noteEditText.setText(spannableStringBuilder);
            }
        }
        // Handle Bluetooth enable result
        else if (requestCode == 1) { // This should match the request code used in toggleBluetooth
            if (resultCode == RESULT_OK) {
                // Bluetooth has been enabled, now make it discoverable
                makeDiscoverable();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions already granted, fetch location
            fetchLocation();
        }
    }

    // Fetch location details and update the note with address information
    private void fetchLocation() {
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            updateLocationInNoteField(latitude, longitude);
                        } else {
                            Toast.makeText(note_display.this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateLocationInNoteField(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(note_display.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                String country = address.getCountryName();
                String city = address.getLocality();
                String state = address.getAdminArea();
                String road = address.getThoroughfare();

                // Create the location text
                String locationText = "Address: " + addressLine + "\n" +
                        "Country: " + country + "\n" +
                        "City: " + city + "\n" +
                        "State: " + state + "\n" +
                        "Road: " + road + "\n\n";

                // Get the existing text as Spannable (preserving images)
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(noteEditText.getText());

                // Find and remove the old location (if it exists)
                String oldLocationMarker = "Address:";
                int startIndex = spannableStringBuilder.toString().indexOf(oldLocationMarker);
                if (startIndex != -1) {
                    int endIndex = spannableStringBuilder.toString().indexOf("\n\n", startIndex);
                    if (endIndex != -1) {
                        spannableStringBuilder.delete(startIndex, endIndex + 2); // +2 to remove double newlines
                    } else {
                        spannableStringBuilder.delete(startIndex, spannableStringBuilder.length());
                    }
                }

                // Find the position of the ImageSpan
                ImageSpan[] imageSpans = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), ImageSpan.class);

                if (imageSpans.length > 0) {
                    // Get the end position of the first image span
                    int imageEndPosition = spannableStringBuilder.getSpanEnd(imageSpans[0]);

                    // Check if there are already double newlines after the image
                    String textAfterImage = spannableStringBuilder.subSequence(imageEndPosition, spannableStringBuilder.length()).toString();
                    if (!textAfterImage.startsWith("\n\n")) {
                        // If there aren't double newlines after the image, add them
                        spannableStringBuilder.insert(imageEndPosition, "\n\n");
                    }

                    // Insert the new location text right after the image span
                    spannableStringBuilder.insert(imageEndPosition + 2, locationText); // +2 to account for the newlines
                } else {
                    // If no image span is found, insert location at the very start of the notes
                    spannableStringBuilder.insert(0, locationText);
                }

                // Set the updated text with the image (if any) and new location
                noteEditText.setText(spannableStringBuilder);
            } else {
                // If no address is found, append coordinates
                noteEditText.append("\nLocation: " + latitude + ", " + longitude + " (Address not found)");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(note_display.this, "Error fetching address", Toast.LENGTH_SHORT).show();
        }
    }
}
