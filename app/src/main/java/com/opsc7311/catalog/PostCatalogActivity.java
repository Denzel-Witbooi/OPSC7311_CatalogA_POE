package com.opsc7311.catalog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.opsc7311.catalog.model.Catalog;
import com.opsc7311.catalog.util.CatalogApi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class PostCatalogActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1;
    private static final String TAG = "PostCatalogActivity";

    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton;
    private EditText titleEditText;
    private EditText categoryEditText;

    private Spinner categoryItems;

    private EditText descriptionEditText;
    private TextView currentUserTextView;
    private ImageView imageView;

    private String currentUserId;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    // Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("Catalog");
    FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    CollectionReference categoryRef = rootRef.collection("Category");

    private Uri imageUri;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_post);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.category_progressBar);
        titleEditText = findViewById(R.id.post_title_et);

        categoryItems = findViewById(R.id.category_items_spinner);

        descriptionEditText = findViewById(R.id.post_description_et);
        currentUserTextView = findViewById(R.id.post_username_textview);

        imageView = findViewById(R.id.post_imageView);
        saveButton = findViewById(R.id.post_save_catalog_button);
        saveButton.setOnClickListener(this);
        addPhotoButton = findViewById(R.id.postCameraButton);
        addPhotoButton.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);

        if (CatalogApi.getInstance() != null) {
            currentUserId = CatalogApi.getInstance().getUserId();
            currentUserName = CatalogApi.getInstance().getUsername();

            currentUserTextView.setText(currentUserName);
        }

        Spinner spinner = findViewById(R.id.category_items_spinner);
        List<String> categories = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        categoryRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String subject = document.getString("name");
                        if (Objects.equals(currentUserName, document.getString("userName"))) {
                            categories.add(subject);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {

                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return  super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_add:
                // Take users to add Catalog
                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(PostCatalogActivity.this, PostCatalogActivity.class));
//                    finish();
                }
                break;
            case R.id.action_signout:
                // sign user out
                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    startActivity(new Intent(PostCatalogActivity.this, MainActivity.class));
//                    finish();
                }
                break;
            case R.id.action_home:
                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(PostCatalogActivity.this, CatalogListActivity.class));
                }
                break;
            case R.id.action_category:

                startActivity(new Intent(PostCatalogActivity.this, AddCategoryActivity.class));

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.post_save_catalog_button:
                // save catalog
                saveCatalog();
                Toast.makeText(PostCatalogActivity.this,
                        "Catalog saved successfully!",
                        Toast.LENGTH_LONG).show();
                break;
            case R.id.postCameraButton:
                // get image from gallery/phone
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
        }
    }

    /**
     * Method to save a catalog
     */
    private void saveCatalog(){
        final String title = titleEditText.getText().toString().trim();
        final String category = categoryEditText.getText().toString().trim();
        final String description = descriptionEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(title) &&
            !TextUtils.isEmpty(category) &&
            !TextUtils.isEmpty(description)
            && imageUri != null) {

            final StorageReference filepath = storageReference //.../catalog_images/our_image.jpeg
                    .child("catalog_images")
                    .child("my_image_" + Timestamp.now().getSeconds()); // my_image_887474737

            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String imageUrl = uri.toString();
                                    //TODO: create a Catalog Object - model : COMPLETE
                                    Catalog catalog = new Catalog();
                                    catalog.setTitle(title);
                                    catalog.setCategory(category);
                                    catalog.setDescription(description);
                                    catalog.setImageUrl(imageUrl);
                                    catalog.setTimeAdded(new Timestamp(new Date()));
                                    catalog.setUserName(currentUserName);
                                    catalog.setUserId(currentUserId);

                                    //TODO: invoke our collectionReference
                                    collectionReference.add(catalog)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {

                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(PostCatalogActivity.this,
                                                            CatalogListActivity.class));
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                                }
                                            });
                                        //TODO: and save a catalog instance
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

            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData(); // we have the actual path to the image
                imageView.setImageURI(imageUri); //show image
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
