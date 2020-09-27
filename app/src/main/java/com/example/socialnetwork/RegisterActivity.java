package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button CreateButton;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        UserEmail = findViewById(R.id.register_email);
        UserPassword = findViewById(R.id.register_password);
        UserConfirmPassword = findViewById(R.id.register_confirm_password);
        CreateButton = findViewById(R.id.register_button);

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        CreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            SendUserToMainActivity();
        }
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmPassword = UserConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please write your Email....", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write your Password....", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "Please confirm your Password....", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirmPassword) ){
            Toast.makeText(this, "Your Password is not matching....", Toast.LENGTH_SHORT).show();
        }
        else{
            progressDialog.setTitle("Creating New Account");
            progressDialog.setMessage("Please wait until we create your account....");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                SendEmailVerificationMessage();
                                progressDialog.dismiss();

                            }
                            else{

                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error occurred : " + message, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                            }
                        }
                    });
        }
    }

    private void SendEmailVerificationMessage(){
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                   if(task.isSuccessful()){
                       Toast.makeText(RegisterActivity.this, "Registration successful....A mail is sent to you.....Please verify your account", Toast.LENGTH_SHORT).show();
                       SendUserToLoginActivity();
                       mAuth.signOut();
                   }
                   else{
                       String message = task.getException().getMessage();
                       Toast.makeText(RegisterActivity.this, "Error occurred : " + message, Toast.LENGTH_SHORT).show();
                       mAuth.signOut();
                   }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
