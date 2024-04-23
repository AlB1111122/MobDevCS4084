package ul.ie.cs4084.app;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static ul.ie.cs4084.app.dataClasses.Database.displayPicture;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Account;

public class ProfileFragment extends Fragment {
    private static final String ARG_PROFILE = "profileId";
    private String profileId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Account viewingAccount;
    private ImageView pfp;
    private TextView usernameText;
    private ExecutorService executor;
    private Handler mainHandler;
    private RecyclerView followedTags;
    private RecyclerView blockedTags;
    private NavController navController;
    private ButtonAdapter blockedAdapter;
    private ButtonAdapter followAdapter;
    private boolean renderFlag = false;
    boolean isSelf;
    private CountDownLatch profileLatch = new CountDownLatch(1);

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    Uri localImageUri = null;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String profileId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROFILE, profileId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            profileId = getArguments().getString(ARG_PROFILE);
        }
        MainActivity act = (MainActivity)getActivity();
        assert act != null;
        executor = act.executorService;//DEF not how to do it firegure out later
        mainHandler = new Handler(Looper.getMainLooper());
        navController = NavHostFragment.findNavController(this);
        db = FirebaseFirestore.getInstance();
        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {

                    if (uri != null) {
                        executor.execute(()->{
                            try {
                                profileLatch.await();
                        localImageUri = uri;
                        StorageReference cloudInstance = FirebaseStorage.getInstance().getReference();
                        StorageReference storageRef = cloudInstance.child("profilePictures/" + localImageUri.getLastPathSegment());

                        storageRef.putFile(localImageUri).addOnCompleteListener(image ->{
                            if(image.isSuccessful()){
                                String cloudImageUriStr = "gs://socialmediaapp-38b04.appspot.com/profilePictures/" + localImageUri.getLastPathSegment();
                                viewingAccount.setProfilePictureUrl(cloudImageUriStr,db);
                                displayPicture(
                                        viewingAccount.getProfilePictureUrl(),
                                        pfp,
                                        executor,
                                        mainHandler,
                                        getResources()
                                );
                            }
                        });} catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }});

                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_profile, container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isSelf = Objects.equals(profileId, ((MainActivity)requireActivity()).signedInAccount.getId());

        Log.e(TAG, String.valueOf(isSelf));
        Log.e(TAG, profileId);

        if(viewingAccount == null) {
            getAccount();
        }else{
            profileLatch.countDown();
        }

        pfp = view.findViewById(R.id.imageView);
        usernameText = view.findViewById(R.id.signedInProfileUsername);
        followedTags = view.findViewById(R.id.followList);
        blockedTags = view.findViewById(R.id.blockList);

        LinearLayoutManager layoutManagerf = new LinearLayoutManager(this.getContext());
        layoutManagerf.setOrientation(LinearLayoutManager.HORIZONTAL);
        followedTags.setLayoutManager(layoutManagerf);
        LinearLayoutManager layoutManagerb = new LinearLayoutManager(this.getContext());
        layoutManagerb.setOrientation(LinearLayoutManager.HORIZONTAL);
        blockedTags.setLayoutManager(layoutManagerb);

        executor.execute(()->{
        try {
            profileLatch.await();

            if(!renderFlag) {
                //show followed tags
                followAdapter = new ButtonAdapter(viewingAccount.getFollowedTags(), navController, false, requireContext());
                //show the accounts posts
                FragmentManager fragmentManager = getChildFragmentManager();
                Bundle bundle = new Bundle();
                bundle.putString("tagsOnPosts", "u/" + viewingAccount.getUsername());
                bundle.putBoolean("hideHashtagName", true);
                fragmentManager.beginTransaction()
                        .replace(R.id.profileTagFragHolder, TagFragment.class, bundle)
                        .commit();
                if (isSelf) {
                    //show blocked buttons if is viewing self
                    blockedAdapter = new ButtonAdapter(viewingAccount.getBlockedTags(), navController, false, requireContext());
                }
            }
            //write to screen
            //pfp
            displayPicture(
                    viewingAccount.getProfilePictureUrl(),
                    pfp,
                    executor,
                    mainHandler,
                    getResources()
            );
            mainHandler.post(()->{
                usernameText.append(viewingAccount.getUsername());
                followedTags.setAdapter(followAdapter);

                if (isSelf) {
                    view.findViewById(R.id.buttonSignOut).setVisibility(View.VISIBLE);
                    //let user change their php
                    view.findViewById(R.id.editImageButton).setOnClickListener(task -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                            .build())
                    );
                    view.findViewById(R.id.editImageButton).setVisibility(View.VISIBLE);

                    //show block button and blocks
                    blockedTags.setAdapter(blockedAdapter);
                    (view.findViewById(R.id.blockListTitle)).setVisibility(View.VISIBLE);
                    blockedTags.setVisibility(View.VISIBLE);

                    //set buttons for editing profile
                    setChangeNameButton(view.findViewById(R.id.editUsernameButton), getContext());
                    setTagFollowButton(view.findViewById(R.id.followTagButton), getContext());
                    setTagBlockButton(view.findViewById(R.id.blockTagButton), getContext());
                    setSignOutButtonListener(view.findViewById(R.id.buttonSignOut));
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
            renderFlag = true;
        });
    }

    private void getAccount(){
        if (!isSelf) {
            DocumentReference viewingprofileDoc = db.collection("accounts").document(profileId);
            viewingprofileDoc.get().addOnCompleteListener(getAccountTask -> {
                if (getAccountTask.isSuccessful()) {
                    DocumentSnapshot accountDocument = getAccountTask.getResult();
                    //if yes populate local object
                    if (accountDocument.exists()) {
                        Log.d(TAG, "profile exists");
                        viewingAccount = new Account(accountDocument);
                        profileLatch.countDown();
                    }
                }
            });
        }else{
            viewingAccount = ((MainActivity)requireActivity()).signedInAccount;
            profileLatch.countDown();
        }
    }

    private void setSignOutButtonListener(Button button){
        button.setOnClickListener(v -> {
            AuthUI.getInstance()
                .signOut(requireActivity())
                .addOnCompleteListener(task -> navController.navigate(R.id.signIn));
            ((MainActivity) requireActivity()).navView.setVisibility(View.GONE);});
    }

    private void setTagFollowButton(Button button, Context context){
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Follow a new tag");

            // Set up the input
            final EditText input = new EditText(context);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setCancelable(true);
            builder.setPositiveButton("Follow", (dialog, which) -> viewingAccount.followTag(input.getText().toString(),db));

            builder.show();
        });
    }

    private void setTagBlockButton(Button button, Context context){
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Block a tag");

            // Set up the input
            final EditText input = new EditText(context);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setCancelable(true);
            builder.setPositiveButton("Block", (dialog, which) -> viewingAccount.blockTag(input.getText().toString(),db));

            builder.show();
        });
    }

    private void setChangeNameButton(Button button, Context context){
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Edit username");
            // Set up the input
            final EditText input = new EditText(context);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setCancelable(true);
            builder.setPositiveButton("Edit", (dialog, which) -> viewingAccount.setUsername(input.getText().toString(),db));

            builder.show();
        });
    }
}
