package com.opsc7311.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
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
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private List<Catalog> catalogList;
    private RecyclerView recyclerView;
    private CatalogRecyclerAdapter catalogRecyclerAdapter;

    private CollectionReference collectionReference = db.collection("Catalog");
    private TextView noCatalogEntry;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_list);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        noCatalogEntry = findViewById(R.id.list_no_items);
        catalogList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

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
                        // TODO: OnFailure for CatalogList
                    }
                });
    }
}
