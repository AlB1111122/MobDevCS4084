package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //get the user who just signed in
        FirebaseUser fireBaseAuthUser = FirebaseAuth.getInstance().getCurrentUser();
        assert fireBaseAuthUser != null; // we know its not null because they just signed in
        //make an object to represent the Account
        Account signedInAccount = new Account(fireBaseAuthUser.getUid());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //check if the Account for this fireBaseAuthUser exists
        DocumentReference signedInUser = db.collection("accounts").document(fireBaseAuthUser.getUid());
        signedInUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    //if yes fully populate local object
                    DocumentSnapshot document = task.getResult();
                    signedInAccount.setAttributes(
                            (String) document.get("username"),
                            (String) document.get("profilePictureUrl"),
                            (ArrayList<String>) document.get("followedTags" ),
                            (ArrayList<String>) document.get("blockedTags" )
                    );
                    if (!document.exists()) {
                        //if not create a document in the db
                        signedInAccount.saveToDb();
                    }
                }
            }
        });
        //display the pfp on screen
        ImageView pfp = (ImageView)findViewById(R.id.imageView);
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(signedInAccount.getProfilePictureUrl());
        final long ONE_MEGABYTE = 1024 * 1024;
        gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.length
                ));
                pfp.setImageDrawable(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "error fetching defaultPFP");
            }
        });
    }

    public void signOut(View view){
        AuthUI.getInstance()
                .signOut(getApplicationContext())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        startActivity(new Intent(LandingActivity.this, MainActivity.class));
                        finish();
                    }
                });

    }
}