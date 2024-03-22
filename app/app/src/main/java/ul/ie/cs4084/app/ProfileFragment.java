package ul.ie.cs4084.app;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.DialogCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Account;

public class ProfileFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Account viewingAccount;
    private ImageView pfp;
    private TextView usernameText;
    private ExecutorService executor;
    private Handler mainHandler;
    private RecyclerView followedTags;
    private RecyclerView blockedTags;
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
        // get the profile we are looking at
        assert getArguments() != null;
        String profileId = getArguments().getString("profileId");
        if(profileId == null){
            return null;
        }
        View view = inflater.inflate(R.layout.fragment_profile, container,false);
        setSignOutButtonListener(view.findViewById(R.id.button2));//TODO: remove this later

        pfp = (ImageView) view.findViewById(R.id.imageView);
        usernameText = (TextView) view.findViewById(R.id.signedInProfileUsername);
        db = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManagerf = new LinearLayoutManager(this.getContext());
        layoutManagerf.setOrientation(LinearLayoutManager.HORIZONTAL);
        LinearLayoutManager layoutManagerb = new LinearLayoutManager(this.getContext());
        layoutManagerb.setOrientation(LinearLayoutManager.HORIZONTAL);

        //check if the Account for this fireBaseAuthUser exists
        DocumentReference viewingprofileDoc = db.collection("accounts").document(profileId);
        viewingprofileDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

                        followedTags = (RecyclerView) view.findViewById(R.id.followList);
                        followedTags.setLayoutManager(layoutManagerf);

                        ButtonAdapter followAdapter = new ButtonAdapter(viewingAccount.getFollowedTags());
                        followedTags.setAdapter(followAdapter);
                        // only show edit options if looking at self
                        if(Objects.equals(viewingAccount.getId(), Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {

                            setChangeNameButton(view.findViewById(R.id.editUsernameButton), getContext());
                            setTagFollowButton(view.findViewById(R.id.followTagButton), getContext());
                            setTagBlockButton(view.findViewById(R.id.blockTagButton), getContext());

                            ((TextView)view.findViewById(R.id.blockListTitle)).setVisibility(View.VISIBLE);
                            blockedTags = (RecyclerView) view.findViewById(R.id.blockList);
                            blockedTags.setVisibility(View.VISIBLE);
                            blockedTags.setLayoutManager(layoutManagerb);

                            ButtonAdapter blockedAdapter = new ButtonAdapter(viewingAccount.getBlockedTags());
                            blockedTags.setAdapter(blockedAdapter);
                        }
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

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

    private void setSignOutButtonListener(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(requireActivity())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                navController.navigate(R.id.signIn);
                            }
                        });
            }
        });
    }

    private void setTagFollowButton(Button button, Context context){
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Follow a new tag");

                // Set up the input
                final EditText input = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setCancelable(true);
                builder.setPositiveButton("Follow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewingAccount.followTag(input.getText().toString(),db);
                    }
                });

                builder.show();
            }
        });
    }

    private void setTagBlockButton(Button button, Context context){
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Block a tag");

                // Set up the input
                final EditText input = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setCancelable(true);
                builder.setPositiveButton("Block", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewingAccount.blockTag(input.getText().toString(),db);
                    }
                });

                builder.show();
            }
        });
    }

    private void setChangeNameButton(Button button, Context context){
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Edit username");

                // Set up the input
                final EditText input = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setCancelable(true);
                builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewingAccount.setUsername(input.getText().toString(),db);
                    }
                });

                builder.show();
            }
        });
    }
}
