package com.opsc7311.catalog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    private static final String TAG = "AddCategoryActivity";
    private static final int GALLERY_CODE = 1 ;
    private String currentUserId;
    private String currentUserName;

    private EditText categoryNameEd;
    private EditText categoryAmountEd;
    private ImageView addPhotoButton;
    private ImageView imageView;

    private ProgressBar progressBar;
    private MaterialButton createCategoryButton;


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    // Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("Category");
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();

        imageView = findViewById(R.id.category_imageView);
        progressBar = findViewById(R.id.category_progressBar);
        categoryNameEd = findViewById(R.id.category_name_ed);
        categoryAmountEd = findViewById(R.id.category_amount_ed);

        createCategoryButton = findViewById(R.id.create_category_button);
        createCategoryButton.setOnClickListener(this);

        addPhotoButton = findViewById(R.id.categoryCameraButton);
        addPhotoButton.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);


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
     */
    private void saveCategory(){
        final String name = categoryNameEd.getText().toString().trim();
        final String amount = categoryAmountEd.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(name) &&
                !TextUtils.isEmpty(amount)) {

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
                                    //TODO: create a Catalog Object - model : COMPLETE
                                    Category category = new Category();
                                    category.setName(name);
                                    category.setAmount(amount);
                                    category.setImageUrl(imageUrl);
                                    category.setTimeAdded(new Timestamp(new Date()));
                                    category.setUserName(currentUserName);
                                    category.setUserId(currentUserId);

                                    //TODO: invoke our collectionReference
                                    collectionReference.add(category)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {

                                                    progressBar.setVisibility(View.INVISIBLE);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return  super.onCreateOptionsMenu(menu);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_category_button:
                // save catalog
                saveCategory();
                Toast.makeText(AddCategoryActivity.this,
                        "Category saved successfully!",
                        Toast.LENGTH_LONG).show();
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
                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(AddCategoryActivity.this, CatalogListActivity.class));
                }
                break;
            case R.id.action_category:

                startActivity(new Intent(AddCategoryActivity.this, AddCategoryActivity.class));

                break;

        }
        return super.onOptionsItemSelected(item);
    }
}