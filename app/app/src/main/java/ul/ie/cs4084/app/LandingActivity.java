package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.Log;
import android.widget.ImageView;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ul.ie.cs4084.app.dataClasses.Account;
//NOTE::NOT USED ANYMORE LEFT IN AS EXAMPLE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
public class LandingActivity extends AppCompatActivity {
    ExecutorService executorService;
    Account signedInAccount;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(NUMBER_OF_CORES);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //display the pfp on screen
        ImageView pfp = (ImageView)findViewById(R.id.imageView);
        //get the user who just signed in
        FirebaseUser fireBaseAuthUser = FirebaseAuth.getInstance().getCurrentUser();
        assert fireBaseAuthUser != null; // we know its not null because they just signed in
        //make an object to represent the Account
        //signedInAccount = new Account(fireBaseAuthUser.getUid());
        db = FirebaseFirestore.getInstance();
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
                        signedInAccount = new Account(document);
                    } else {
                        //if not create a document in the db
                        Log.d(TAG, "profile does not exist");
                        signedInAccount = new Account(fireBaseAuthUser.getUid(), fireBaseAuthUser.getUid(), new HashSet<String>(), new HashSet<String>());
                        db.collection("accounts").document(fireBaseAuthUser.getUid())
                                .set(signedInAccount)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Account successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing account", e);
                                    }
                                });
                    }
                }
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
                /*ArrayList<String> rules = new ArrayList<>();
                rules.add("rule1");
                rules.add("rule2");
                rules.add("rule3");

                HashSet<String> tags = new HashSet<String>();
                tags.add("a");
                tags.add("v");
                tags.add("f");
                tags.add("a");

                DocumentReference q = db.document("accounts/eAq9tvZUAqdxM1Me2ytvP1jsoXy2");
                //DocumentReference w = db.document("accounts/eAq9tvZUAqdxM1Me2ytvP1jsoXy2");

                DocumentReference z = db.document("accounts/UoXcHOdoVVnGe0bJgtpz");
                DocumentReference x = db.document("accounts/YZjRucblHUvEWIs30iE1");

                HashSet<DocumentReference> mods = new HashSet<>();
                mods.add(q);
                //mods.add(w);
                HashSet<DocumentReference> voters = new HashSet<>();
                voters.add(z);
                voters.add(x);


                Factory factory = new Factory(executorService);

                CollectionReference boards = db.collection("boards");
                DocumentReference testBoard =
                        boards.document("n5rfl0XL6Wwo8vfcGLux");

                testBoard.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        Board b = new Board(snapshot);
                        b.removeModerators(q,db);
                        b.addTag("womp",db);
                    }
                });*/
            }
        });
    }


        /*factory.createNewPost(
                db,
                testBoard,
                q,
                "test  threded jaNHVHJBVIFUYSGDHVOHU",
                "body of the post made on a thred2NJAVBUIODJKVNBUOIAHJNIOVDA",
                null,
                tags,
                new DBCallback<DocumentReference>() {
                    @Override
                    public void callback(DocumentReference documentReference) {
                        factory.createNewComment(documentReference, x, "String bodyNVJIBDIOXHB(UGDIOAVHJBGOIUDAHKJXBUIHV from thred2",new DBCallback<DocumentReference>() {
                            @Override
                            public void callback(DocumentReference documentReference) {
                                Log.d(TAG, "fjiewjfwjfoifejfjwjf");
                            }
                        });
                    }
                }
        );
            }
        });*/

        /*StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(signedInAccount.getProfilePictureUrl());
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
        });*/

}