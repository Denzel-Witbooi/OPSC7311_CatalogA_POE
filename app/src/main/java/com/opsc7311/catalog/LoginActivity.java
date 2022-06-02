package com.opsc7311.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.opsc7311.catalog.util.CatalogApi;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    //Button Vars
    private ImageButton BackButton;
    private Button registerButton;
    private Button loginButton;

    //EditText and AutoCompleteTextView vars
    private AutoCompleteTextView emailAddress;
    private EditText password;

    private ProgressBar progressBar;

    // Firebase vars
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Firestore instance var and collection ref to users collection var
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).hide();
        firebaseAuth = FirebaseAuth.getInstance();

        BackButton = findViewById(R.id.img_back);

        //Sends user back to splash screen
        BackButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            openMainActivity();
        }
    });

        registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        }
    });


        emailAddress = findViewById(R.id.login_email_atw);
        password = findViewById(R.id.login_password_ed);
        loginButton = findViewById(R.id.login_user_button);

        progressBar = findViewById(R.id.login_acct_progress);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEmailPasswordUser(emailAddress.getText().toString().trim(),
                        password.getText().toString().trim());
            }
        });
    }

    /**
     * Method to log in user with email and password
     * Author: Google - Firebase
     * URL: https://firebase.google.com/docs/auth/android/manage-users
     * Code:
     * mAuth.signInWithEmailAndPassword(email, password)
     *         .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
     *             @Override
     *             public void onComplete(@NonNull Task<AuthResult> task) {
     *                 if (task.isSuccessful()) {
     *                     // Sign in success, update UI with the signed-in user's information
     *                     Log.d(TAG, "signInWithEmail:success");
     *                     FirebaseUser user = mAuth.getCurrentUser();
     *                     updateUI(user);
     *                 } else {
     *                     // If sign in fails, display a message to the user.
     *                     Log.w(TAG, "signInWithEmail:failure", task.getException());
     *                     Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
     *                             Toast.LENGTH_SHORT).show();
     *                     updateUI(null);
     *                 }
     *             }
     *         });
     *  Code Modified to use catalogApi (singleton) -to set instance of user details
     * @param email
     * @param pwd
     */
    private void loginEmailPasswordUser(String email, String pwd) {
        progressBar.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(pwd)) {
            firebaseAuth.signInWithEmailAndPassword(email,pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            assert user != null;
                            String currentUserId = user.getUid();

                            //Invoke collection Reference
                            collectionReference
                                    .whereEqualTo("userId", currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                return;
                                            }
                                            assert queryDocumentSnapshots != null;
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                    CatalogApi catalogApi = CatalogApi.getInstance();
                                                    catalogApi.setUsername(snapshot.getString("username"));
                                                    catalogApi.setUserId(snapshot.getString("userId"));

                                                    //Go to ListActivity
                                                    startActivity(new Intent(LoginActivity.this,
                                                            CatalogListActivity.class));

                                                }

                                            }
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
        } else {

            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this,
                    "Please enter email and password",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void openMainActivity()
    {
        Intent intent = new Intent(this,MainActivity.class );
        startActivity(intent);
    }
}
