package com.opsc7311.catalog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PostCatalogActivity extends AppCompatActivity implements View.OnClickListener {
    // Get's gallery code which is 2
    private static final int take_photo = 1;
    private static final int GALLERY_CODE = 2;

    // Tag for debugging
    private static final String TAG = "PostCatalogActivity";

    // Post catalog vars
    private MaterialButton saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton;
    private EditText titleEditText;

    private Spinner categoryItems;
    private EditText descriptionEditText;
    private TextView currentUserTextView;
    private ImageView imageView;

    // Current user's name and Id
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
    // To store imageURI in and put file
    private Uri imageUri;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_post);
        // Set's app bar elevation to 0
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        // Initialize storage reference
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize firebaseAuth and assign auth instance
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
        // Get current user id and name based if singleton instance not null
        if (CatalogApi.getInstance() != null) {
            currentUserId = CatalogApi.getInstance().getUserId();
            currentUserName = CatalogApi.getInstance().getUsername();

            currentUserTextView.setText(currentUserName);
        }

        categoryItems = findViewById(R.id.category_items_spinner);
        /**
         * Title: How to populate a spinner with the result of a Firestore query?
         * Author: StackOverflow
         * Modification: check if the current user (name) is found in the catalog collection
         *      Objects.equals(currentUserName, document.getString("userName"))
         * URL: https://stackoverflow.com/questions/54988533/how-to-populate-a-spinner-with-the-result-of-a-firestore-query
         * Code:
         * FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
         * CollectionReference subjectsRef = rootRef.collection("subjects");
         * Spinner spinner = (Spinner) findViewById(R.id.spinner);
         * List<String> subjects = new ArrayList<>();
         * ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, subjects);
         * adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         * spinner.setAdapter(adapter);
         * subjectsRefRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
         *     @Override
         *     public void onComplete(@NonNull Task<QuerySnapshot> task) {
         *         if (task.isSuccessful()) {
         *             for (QueryDocumentSnapshot document : task.getResult()) {
         *                 String subject = document.getString("name");
         *                 subjects.add(subject);
         *             }
         *             adapter.notifyDataSetChanged();
         *         }
         *     }
         * });
         * By: Abubakar Siddique
         * URL https://stackoverflow.com/users/11134589/abubakar-siddique
         */
        List<String> categories = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryItems.setAdapter(adapter);

        categoryRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String category = document.getString("name");
                        if (Objects.equals(currentUserName, document.getString("userName"))) {
                            categories.add(category);
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
        // Inflating menu.xml
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle other menu item clicks here
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
                selectImage();
                break;
        }
    }

    /**
     * Title: Capture Image From Camera and Select Image From Gallery of Android Phone Using Android Studio
     * Author: Chhavi Goel
     * Date uploaded:  17 February 2020
     * URL: https://www.c-sharpcorner.com/UploadFile/e14021/capture-image-from-camera-and-selecting-image-from-gallery-o/
     * Original CODE:
     *       private void selectImage() {
     *         final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
     *         AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
     *         builder.setTitle("Add Photo!");
     *         builder.setItems(options, new DialogInterface.OnClickListener() {
     *             @Override
     *             public void onClick(DialogInterface dialog, int item) {
     *                 if (options[item].equals("Take Photo"))
     *                 {
     *                     Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     *                     File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
     *                     intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
     *                     startActivityForResult(intent, 1);
     *                 }
     *                 else if (options[item].equals("Choose from Gallery"))
     *                 {
     *                     Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
     *                     startActivityForResult(intent, 2);
     *                 }
     *                 else if (options[item].equals("Cancel")) {
     *                     dialog.dismiss();
     *                 }
     *             }
     *         });
     *         builder.show();
     *     }
     * Modification:
     *  Reduced intent instantiation to one line of code e.x take - photo { Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); }
     *  Added path- gallery_intent.setType("image/*"); - for choose from gallery option
     */
    private void selectImage() {
        final CharSequence[] options = { "Take a Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(PostCatalogActivity.this);
        builder.setTitle("Add a Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take a Photo"))
                {
                    Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camera_intent, take_photo);
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent gallery_intent = new Intent(Intent.ACTION_GET_CONTENT);
                    gallery_intent.setType("image/*");
                    startActivityForResult(gallery_intent, GALLERY_CODE);
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Method to save a catalog
     */
    private void saveCatalog(){
        final String title = titleEditText.getText().toString().trim();
        final String category = categoryItems.getSelectedItem().toString().toLowerCase();
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
                                    DocumentReference documentReference =  db
                                            .collection("Catalog")
                                            .document();
                                    String imageUrl = uri.toString();
                                    //TODO: create a Catalog Object - model : COMPLETE
                                    Catalog catalog = new Catalog();
                                    catalog.setCatalogId(documentReference.getId());
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

                                                    Toast.makeText(PostCatalogActivity.this,
                                                            "Catalog saved successfully!",
                                                            Toast.LENGTH_LONG).show();

                                                    startActivity(new Intent(PostCatalogActivity.this,
                                                            CatalogListActivity.class));
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                                }
                                            });
//                                       SAVE CATALOG INSTANCE
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

            TextView error_postName;
            error_postName = findViewById(R.id.error_post_name);

            if (!isTextValid(title)) {
                error_postName.setText("Please enter more than 3 characters");
            } else {
                error_postName.setText(null); // Clear the error
            }

            if (imageUri == null){
                Toast.makeText(PostCatalogActivity.this,
                        "Please upload an Image!",
                        Toast.LENGTH_LONG).show();
            }

            titleEditText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (isTextValid(String.valueOf(titleEditText.getText()))) {
                        error_postName.setText(null); // Clear the error
                    }
                    return false;
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isTextValid(@Nullable String text) {
        return text != null && text.length() >= 3;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == take_photo && resultCode == RESULT_OK) {
            if (data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageUri = getImageUri(this,photo);
                imageView.setImageBitmap(photo); //show image
            }
        } else if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData(); // we have the actual path to the image
            imageView.setImageURI(imageUri); //show image
        }
    }

    /**
     * Method to convert Bitmap to Uri
     * Title: Android getting image URI from bitmap
     * Author: colinyeoh
     * Date posted: 18 May 2012
     * https://colinyeoh.wordpress.com/2012/05/18/android-getting-image-uri-from-bitmap/
     * @param inContext
     * @param inImage
     * @return
     */
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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
