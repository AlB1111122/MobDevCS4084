package ul.ie.cs4084.app;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Account;

public class ProfileFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Account viewingAccount;
    private ImageView pfp;
    private TextView usernameText;
    private ExecutorService executor;
    Handler mainHandler;

    NavController navController;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity act = (MainActivity)getActivity();
        assert act != null;
        executor = act.executorService;//DEF not how to do it firegure out later
        mainHandler = new Handler(Looper.getMainLooper());
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container,false);

        pfp = (ImageView) view.findViewById(R.id.imageView);
        usernameText = (TextView) view.findViewById(R.id.signedInProfileUsername);

        assert getArguments() != null;
        String profileId = getArguments().getString("profileId");
        if(profileId == null){
            navController.navigate(R.id.action_to_home);
            return null;
        }

        Button signOutB = (Button) view.findViewById(R.id.button2);
        setSignOutButtonListener(signOutB);

        //FirebaseUser fireBaseAuthUser = FirebaseAuth.getInstance().getCurrentUser();
        //assert fireBaseAuthUser != null; // we know its not null because they just signed in
        //make an object to represent the Account
        //viewingAccount = new Account(fireBaseAuthUser.getUid());
        db = FirebaseFirestore.getInstance();
        //check if the Account for this fireBaseAuthUser exists
        DocumentReference signedInUser = db.collection("accounts").document(profileId);
        signedInUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    //if yes populate local object
                    if (document.exists()) {
                        Log.d(TAG, "profile exists");
                        viewingAccount = new Account(document);
                        usernameText.append(viewingAccount.getUsername());
                        displayProfilePicture();
                    } else {
                        return;
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //get the user who just signed in

    }

    private void displayProfilePicture() {
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(viewingAccount.getProfilePictureUrl());
        final long ONE_MEGABYTE = 1024 * 1024;
        gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {//runnig on a thred because its slow
                        Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(
                                bytes,
                                0,
                                bytes.length
                        ));
                        Runnable pfpRunnable = new Runnable() {//post back to ui thred
                            @Override
                            public void run() {
                                pfp.setImageDrawable(image);}
                        };
                        mainHandler.post(pfpRunnable);

                    }});
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "error fetching PFP");
            }
        });
    }

    private void setSignOutButtonListener(
            Button button
    ){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(getActivity())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                navController.navigate(R.id.signIn);
                            }
                        });
            }
        });
    }
}

