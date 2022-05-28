package com.opsc7311.catalog;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.opsc7311.catalog.model.Catalog;
import com.opsc7311.catalog.ui.CatalogRecyclerAdapter;

import java.util.List;

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
    }
}
