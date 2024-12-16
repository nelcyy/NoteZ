package com.example.notez;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SignUp extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1001;
    private EditText email, password, retypePassword;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private FirebaseFirestore firestore;

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
        firestore = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Replace with actual client ID
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        String text = "Already have an account? <b>Login</b>";
        signInText.setText(android.text.Html.fromHtml(text));

        signInText.setOnClickListener(v -> goToSignIn());

        signUpButton.setOnClickListener(v -> registerUser());

        googleText.setOnClickListener(v -> signInWithGoogle());

        googleIcon.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                Toast.makeText(SignUp.this, "Google sign-in successful!", Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                Toast.makeText(SignUp.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
            }
        });
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

        saveUserToMySQL(mail, pass);

        saveUserToFirebase(mail, pass);
    }

    private void saveUserToFirebase(String mail, String pass) {
        auth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();

                    // Create a HashMap to store user data
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", mail);
                    userData.put("password", pass); // Storing password is not recommended; hash it if necessary.
                    userData.put("userId", userId); // Add userId to Firestore document.

                    // Firestore Collection and Document Reference
                    CollectionReference collections = firestore.collection("users");
                    DocumentReference doc = collections.document(userId);

                    // Save the user data
                    doc.set(userData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(SignUp.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                                goToSignIn();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(SignUp.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                Toast.makeText(SignUp.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveUserToMySQL(String email, String pass) {
        // URL of the PHP script
        String url = "http://192.168.56.1/notez/add.php";

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        // Create a StringRequest with POST method
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if ("Success".equals(response)) {

                    } else {
                        Toast.makeText(SignUp.this, "Failed to save user", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Log.e("SignUp", "Volley Error: " + error.getLocalizedMessage())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("pass", pass);
                return params;
            }
        };

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }

    private void goToSignIn() {
        Intent intent = new Intent(this, SignIn.class);
        startActivity(intent);
    }

    private void goToMain() {
        Intent intent = new Intent(SignUp.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
