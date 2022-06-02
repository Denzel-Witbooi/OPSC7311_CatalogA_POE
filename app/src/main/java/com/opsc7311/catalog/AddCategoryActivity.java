package com.opsc7311.catalog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.opsc7311.catalog.model.Catalog;
import com.opsc7311.catalog.model.Category;
import com.opsc7311.catalog.util.CatalogApi;

import java.util.Date;
import java.util.Objects;

public class AddCategoryActivity extends AppCompatActivity implements View.OnClickListener {
    // Tag for debugging
    private static final String TAG = "AddCategoryActivity";

    // Get's gallery code which is 1
    private static final int GALLERY_CODE = 1 ;

    // Current user's name and Id
    private String currentUserId;
    private String currentUserName;

    // Add Category vars
    private EditText categoryNameEd;
    private EditText categoryAmountEd;
    private ImageView addPhotoButton;
    private ImageView imageView;

    private ProgressBar progressBar;
    private MaterialButton createCategoryButton;

    // Firebase vars
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    // Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Category");
    // To store imageURI in and put file
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        // Set's app bar elevation to 0
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        // Initialize storage reference
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize firebaseAuth and assign auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize widgets
        imageView = findViewById(R.id.category_imageView);
        progressBar = findViewById(R.id.category_progressBar);
        categoryNameEd = findViewById(R.id.category_name_ed);
        categoryAmountEd = findViewById(R.id.category_amount_ed);

        createCategoryButton = findViewById(R.id.create_category_button);
        createCategoryButton.setOnClickListener(this);

        addPhotoButton = findViewById(R.id.categoryCameraButton);
        addPhotoButton.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);


        // Get current user id and name based if singleton instance not null
        if (CatalogApi.getInstance() != null) {
            currentUserId = CatalogApi.getInstance().getUserId();
            currentUserName = CatalogApi.getInstance().getUsername();
        }
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



    /**
     * Method to save a category
     * Code based of snippets from firebase fire-store documentation
     * URL: https://firebase.google.com/docs/firestore/manage-data/add-data
     * Upload image to storage reference
     * URL: https://firebase.google.com/docs/storage/web/upload-files
     */
    private void saveCategory(){
        final String name = categoryNameEd.getText().toString().trim();
        final String amount = categoryAmountEd.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(name) &&
                !TextUtils.isEmpty(amount) && imageUri != null) {

            final StorageReference filepath = storageReference //.../catalog_images/our_image.jpeg
                    .child("category_images")
                    .child("my_image_" + Timestamp.now().getSeconds()); // my_image_887474737

            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String imageUrl = uri.toString();
                                    // create a Catalog Object - model : COMPLETE
                                    Category category = new Category();
                                    category.setName(name);
                                    category.setAmount(amount);
                                    category.setImageUrl(imageUrl);
                                    category.setTimeAdded(new Timestamp(new Date()));
                                    category.setUserName(currentUserName);
                                    category.setUserId(currentUserId);

                                    // invoke our collectionReference
                                    collectionReference.add(category)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {

                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(AddCategoryActivity.this,
                                                            "Category saved successfully!",
                                                            Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(AddCategoryActivity.this,
                                                            PostCatalogActivity.class));
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                                }
                                            });
                                    // and save a catalog instance
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
            // Validation if fields empty
            TextView error_catName;
            TextView error_catAmt;
            error_catName = findViewById(R.id.error_cat_name);
            error_catAmt = findViewById(R.id.error_cat_amount);

            if (!isTextValid(name)) {
                error_catName.setText("Please enter more than 3 characters");
            } else {
                error_catName.setText(null); // Clear the error
            }

            if (!isAmountValid(amount)) {
                error_catAmt.setText("Please enter 1 or more item goal");
            } else {
                error_catAmt.setText(null); // Clear the error
            }

            if (imageUri == null){
                Toast.makeText(AddCategoryActivity.this,
                        "Please upload an Image!",
                        Toast.LENGTH_LONG).show();
            }

            /**
             * Listens to characters user enters in edit text field
             * Author: Android dev - Written by Gautham Sajith
             * URL: https://codelabs.developers.google.com/codelabs/mdc-101-java#2
             */
            categoryNameEd.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (isTextValid(String.valueOf(categoryNameEd.getText()))) {
                        error_catName.setText(null); // Clear the error
                    }
                    return false;
                }
            });

            categoryAmountEd.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (isTextValid(String.valueOf(categoryAmountEd.getText()))) {
                        error_catAmt.setText(null); // Clear the error
                    }
                    return false;
                }
            });

            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Method to check if text is null or larger than 3 characters
     * @param text
     * @return
     */
    private boolean isTextValid(@Nullable String text) {
        return text != null && text.length() >= 3;
    }

    /**
     * Method to check if text is null or larger than 1 character
     * @param text
     * @return
     */
    private boolean isAmountValid(@Nullable String text) {
        return text != null && text.length() >= 1;
    }

    /**
     * OnActivityResult is triggered when request to access gallery
     * by clicking the image icon
     * Checks if request = 1 for gallery and result code is = OK
     * Intent is checked if its not null
     * and gets the intent data (image uri)
     * stores it in imageUri variable.
     * and sets the imageView to show image
     * @param requestCode
     * @param resultCode
     * @param data
     * Author: Android
     * URL: https://developer.android.com/training/camera/photobasics
     */
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

    /**
     * Inflates the menu resource to add category activity.xml
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return  super.onCreateOptionsMenu(menu);
    }

    /**
     * Gets current user logged in on start
     * And initializes firebaseAuth -> listener
     * Acts as a session
     */
    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    /**
     * Ends session
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_category_button:
                // save catalog
                saveCategory();

                break;
            case R.id.categoryCameraButton:
                // get image from gallery/phone
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_add:
                // Take users to add Catalog
                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(AddCategoryActivity.this, PostCatalogActivity.class));
//                    finish();
                }
                break;
            case R.id.action_signout:
                // sign user out
                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    startActivity(new Intent(AddCategoryActivity.this, MainActivity.class));
//                    finish();
                }
                break;
            case R.id.action_home:
                // Take users to home page
                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(AddCategoryActivity.this, CatalogListActivity.class));
                }
                break;
            case R.id.action_category:
                // Take users to add Category
                startActivity(new Intent(AddCategoryActivity.this, AddCategoryActivity.class));

                break;

        }
        return super.onOptionsItemSelected(item);
    }
}