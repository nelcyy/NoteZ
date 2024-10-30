package com.example.notez;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignUp extends AppCompatActivity {
    private EditText email, password, retypePassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        retypePassword = findViewById(R.id.retype_password);
        Button signUpButton = findViewById(R.id.sign_up_button);
        TextView signInText = findViewById(R.id.sign_in_text);
        TextView googleText = findViewById(R.id.googletext);
        ImageView googleIcon = findViewById(R.id.googleicon);

        auth = FirebaseAuth.getInstance();

        String text = "Already have an account? <b>LogIn</b>";
        signInText.setText(android.text.Html.fromHtml(text));

        signInText.setOnClickListener(v -> goToSignIn());

        signUpButton.setOnClickListener(v -> registerUser());
    }

    private void goToSignIn() {
        Intent intent = new Intent(this, SignIn.class);
        startActivity(intent);
    }

    private void registerUser() {
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String retypePass = retypePassword.getText().toString().trim();

        // Check if any fields are empty
        if (mail.isEmpty() || pass.isEmpty() || retypePass.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
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

        // Register user with Firebase
        auth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SignUp.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                goToSignIn();
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                Toast.makeText(SignUp.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
