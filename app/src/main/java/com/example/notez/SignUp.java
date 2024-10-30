package com.example.notez;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignUp extends AppCompatActivity {
    private EditText username, email, password, retypePassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        databaseHelper = new DatabaseHelper(this);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        retypePassword = findViewById(R.id.retype_password);
        Button signUpButton = findViewById(R.id.sign_up_button);
        TextView signInText = findViewById(R.id.sign_in_text);

        String text = "Already have an account? <b>Sign In</b>";
        signInText.setText(android.text.Html.fromHtml(text));

        signInText.setOnClickListener(v -> goToSignIn());

        signUpButton.setOnClickListener(v -> registerUser());
    }

    private void goToSignIn() {
        Intent intent = new Intent(this, SignIn.class); // Create intent to navigate to SignIn
        startActivity(intent); // Start the SignIn activity
    }

    private void registerUser() {
        String user = username.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String retypePass = retypePassword.getText().toString().trim();

        // Check if any fields are empty
        if (user.isEmpty() || mail.isEmpty() || pass.isEmpty() || retypePass.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate username length
        if (user.length() < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format with a regex pattern directly in the if statement
        if (!mail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
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
        boolean isAdded = databaseHelper.addUser(user, mail, pass);
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
