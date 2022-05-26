package com.opsc7311.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private ImageButton BackButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    BackButton = findViewById(R.id.img_back);
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

    }

    public void openMainActivity()
    {
        Intent intent = new Intent(this,MainActivity.class );
        startActivity(intent);
    }
}
