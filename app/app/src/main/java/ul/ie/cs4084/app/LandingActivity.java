package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashSet;
import java.util.List;

import ul.ie.cs4084.app.dataClasses.Account;
import ul.ie.cs4084.app.dataClasses.Database;

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
                    DocumentSnapshot document = task.getResult();
                    //if yes populate local object
                    if (document.exists()) {
                        Log.d(TAG, "profile exists");
                        HashSet<String> followed;
                        HashSet<String> blocked;
                        Object followedObj = document.get("followedTags");
                        Object blockedObj = document.get("blockedTags");
                        //check if tag lists are null to avoid crash may remove later
                        followed = new HashSet<String>((List) followedObj);//we know this is strings and not null because DB
                        blocked = new HashSet<String>((List) blockedObj);//we know this is strings and not null because DB

                        signedInAccount.setAttributes(
                                (String) document.get("username"),
                                (String) document.get("profilePictureUrl"),
                                followed,
                                blocked
                        );
                    }else{
                        //if not create a document in the db
                        Log.d(TAG, "profile does not exist");
                        signedInAccount.setUsername(fireBaseAuthUser.getDisplayName());
                        HashSet<String> blockedTags = new HashSet<String>();
                        HashSet<String> followedTags = new HashSet<String>();
                        signedInAccount.setBlockedTags(blockedTags);
                        signedInAccount.setFollowedTags(followedTags);
                        Database.set(signedInAccount, "accounts");
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
        //test post creation
        HashSet<String> tags = new HashSet<String>();
        tags.add("a");
        tags.add("v");
        tags.add("f");
        tags.add("a");
        //PostFactory poster = new PostFactory();
        //poster.createNewPost(db,"b/test","test","test","body of the post",null,tags,tags,tags);
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