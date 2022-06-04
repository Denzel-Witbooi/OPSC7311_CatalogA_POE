package com.opsc7311.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.opsc7311.catalog.model.Catalog;
import com.opsc7311.catalog.ui.CatalogRecyclerAdapter;
import com.opsc7311.catalog.util.CatalogApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CatalogListActivity extends AppCompatActivity {

    //Firebase vars
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressBar progressBar;
    // Create a list of catalog object
    private List<Catalog> catalogList;
    // Recycler view and adapter
    private RecyclerView recyclerView;
    private CatalogRecyclerAdapter catalogRecyclerAdapter;

    // collection reference for catalog collection
    private CollectionReference collectionReference = db.collection("Catalog");

    // TextView for when user has no catalogs
    private TextView noCatalogEntry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_list);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        progressBar = findViewById(R.id.progressBar_search);

        noCatalogEntry = findViewById(R.id.list_no_items);
        catalogList = new ArrayList<>();

        // set recycler view and layout manager
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // called when we press search button
                searchData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // called when we type even a single letter
                return false;
            }
        });
        return  super.onCreateOptionsMenu(menu);
    }

    /**
     * Method to search database for different categories
     * @param query
     */
    private void searchData(String query)
    {
        progressBar.setVisibility(View.VISIBLE);
        collectionReference.whereEqualTo("category", query.toLowerCase())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        catalogList.clear();
                        progressBar.setVisibility(View.INVISIBLE);
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot catalogs : queryDocumentSnapshots) {
                                Catalog catalog = catalogs.toObject(Catalog.class);
                                catalogList.add(catalog);
                            }
                            // Invoke recyclerView
                            catalogRecyclerAdapter = new CatalogRecyclerAdapter(
                                    CatalogListActivity.this, catalogList);
                            recyclerView.setAdapter(catalogRecyclerAdapter);
                            catalogRecyclerAdapter.notifyDataSetChanged();
                        } else {
                            noCatalogEntry.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_add:
                // Take users to add Catalog
                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(CatalogListActivity.this, PostCatalogActivity.class));
//                    finish();
                }
                break;
            case R.id.action_signout:
                // sign user out
                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    startActivity(new Intent(CatalogListActivity.this, MainActivity.class));
//                    finish();
                }
                break;
            case R.id.action_home:
                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(CatalogListActivity.this, CatalogListActivity.class));
                }
                break;
            case R.id.action_category:
                    startActivity(new Intent(CatalogListActivity.this, AddCategoryActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Gets all the catalogs with the current users user ID
        // Code steps found on Firebase documentation and modified to include call to user
        // Id with catalog api (singleton)
        // URL: https://firebase.google.com/docs/database/android/lists-of-data
        collectionReference.whereEqualTo("userId", CatalogApi.getInstance()
                .getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot catalogs : queryDocumentSnapshots) {
                                Catalog catalog = catalogs.toObject(Catalog.class);
                                catalogList.add(catalog);
                            }
                            // Invoke recyclerView
                            catalogRecyclerAdapter = new CatalogRecyclerAdapter(
                                   CatalogListActivity.this, catalogList);
                            recyclerView.setAdapter(catalogRecyclerAdapter);
                            catalogRecyclerAdapter.notifyDataSetChanged();
                        } else {
                            noCatalogEntry.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        noCatalogEntry.setVisibility(View.VISIBLE);
                    }
                });
    }

}
