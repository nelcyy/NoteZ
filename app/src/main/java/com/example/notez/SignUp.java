package com.example.notez;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignUp extends AppCompatActivity {
    private EditText username;
    private EditText nickname;
    private EditText password;
    private EditText retypePassword;
    private Button signUpButton;
    private TextView signInText; // TextView to navigate to SignIn
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up); // Set the layout for SignUp activity

        username = findViewById(R.id.username); // Initialize the username EditText
        nickname = findViewById(R.id.nickname); // Initialize the nickname EditText
        password = findViewById(R.id.password); // Initialize the password EditText
        retypePassword = findViewById(R.id.retype_password); // Initialize the retype password EditText
        signUpButton = findViewById(R.id.sign_up_button); // Initialize the SignUp button
        signInText = findViewById(R.id.sign_in_text); // Initialize the SignIn text view
        databaseHelper = new DatabaseHelper(this); // Initialize the DatabaseHelper

        // Handle the SignIn text click
        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSignIn(); // Navigate to SignIn activity
            }
        });

        // Handle the SignUp button click
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(); // Register the new user
            }
        });
    }

    private void goToSignIn() {
        Intent intent = new Intent(this, SignIn.class); // Create intent to navigate to SignIn
        startActivity(intent); // Start the SignIn activity
    }

    private void registerUser() {
        String user = username.getText().toString().trim();
        String nick = nickname.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String retypePass = retypePassword.getText().toString().trim();

        // Check if any fields are empty
        if (user.isEmpty() || nick.isEmpty() || pass.isEmpty() || retypePass.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate username length
        if (user.length() < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password length
        if (pass.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if passwords match
        if (!pass.equals(retypePass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username already exists
        if (databaseHelper.isUsernameExists(user)) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user to database
        boolean isAdded = databaseHelper.addUser(user, nick, pass);
        if (isAdded) {
            Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show();
            // Optionally navigate to sign-in screen
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish(); // Finish the SignUp activity
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
}
