package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetActivity extends AppCompatActivity {

    private Button SendEmailButton;
    private EditText EmailInput;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        mAuth = FirebaseAuth.getInstance();

        SendEmailButton = findViewById(R.id.Send_Link_button);
        EmailInput = findViewById(R.id.email_input);


        mToolbar = findViewById(R.id.forget_password_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Reset Password...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMail = EmailInput.getText().toString();

                if(TextUtils.isEmpty(userMail)){
                    Toast.makeText(ResetActivity.this, "Please write your valid email address...", Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.sendPasswordResetEmail(userMail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ResetActivity.this, "Please check your email account for the link...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetActivity.this, LoginActivity.class));
                            }
                            else{
                                Toast.makeText(ResetActivity.this, "Error occurred: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
