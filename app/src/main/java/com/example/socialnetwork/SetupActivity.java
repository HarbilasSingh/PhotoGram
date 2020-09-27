package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

        private EditText UserName, FullName, CountryName;
        private Button SaveInfo;
        private CircleImageView ProfileImage;
        private FirebaseAuth mAuth;
        private DatabaseReference UserRef;
        private ProgressDialog loadingBar;
        private StorageReference UserProfileImageRef;

        String currentUserID;
        final static int Gallery_pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = findViewById(R.id.setup_username);
        FullName = findViewById(R.id.setup_full_name);
        CountryName = findViewById(R.id.setup_country);
        SaveInfo = findViewById(R.id.setup_save_button);
        ProfileImage = findViewById(R.id.setup_profile_image);


        SaveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });

        DisplayProfileImage();

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_pick);
            }
        });

    }

    private void DisplayProfileImage() {
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileImage")) {
                        String image = dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                    else{
                        Toast.makeText(SetupActivity.this, "Please select profile image first!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_pick && resultCode == RESULT_OK && data != null ){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                loadingBar.setTitle("Profile image");
                loadingBar.setMessage("Please wait while we are updating your profile image !!!");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetupActivity.this, "Profile Image saved successfully in the Firebase storage!!", Toast.LENGTH_SHORT).show();


                            task.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();

//                                    Picasso.with(SetupActivity.this).load(downloadUrl).placeholder(R.drawable.profile).into(ProfileImage);

                                    UserRef.child("profileImage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(Task<Void> task) {
                                                    if(task.isSuccessful()){

                                                        Toast.makeText(SetupActivity.this, "Profile Image stored in Firebase database successfully...", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                    else{
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SetupActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }

                                                }
                                            });
                                }
                            });

                        }
                    }
                });
            }
            else{
                Toast.makeText(SetupActivity.this, "Error occurred: Image can't be cropped!! " , Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Username is compulsory..", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Please write your full name..", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please write your country..", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait while we create your new account !!!");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            HashMap userMap = new HashMap();
            userMap.put("Username", username);
            userMap.put("Full_Name", fullname);
            userMap.put("Country", country);
            userMap.put("Status", "Hey there, I am using this Social Networking app!!!!");
            userMap.put("Gender", "");
            userMap.put("DOB", "");
            userMap.put("Relationship_Status", "");
            UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account is created successfully!!!", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
