package ul.ie.cs4084.app;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static ul.ie.cs4084.app.dataClasses.Database.displayPicture;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
            @NonNull LayoutInflater inflater,
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

        pfp = view.findViewById(R.id.imageView);
        usernameText = view.findViewById(R.id.signedInProfileUsername);
        db = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManagerf = new LinearLayoutManager(this.getContext());
        layoutManagerf.setOrientation(LinearLayoutManager.HORIZONTAL);
        LinearLayoutManager layoutManagerb = new LinearLayoutManager(this.getContext());
        layoutManagerb.setOrientation(LinearLayoutManager.HORIZONTAL);

        //check if the Account for this fireBaseAuthUser exists
        DocumentReference viewingprofileDoc = db.collection("accounts").document(profileId);
        viewingprofileDoc.get().addOnCompleteListener(getAccountTask -> {
            if (getAccountTask.isSuccessful()) {
                DocumentSnapshot accountDocument = getAccountTask.getResult();
                //if yes populate local object
                if (accountDocument.exists()) {
                    Log.d(TAG, "profile exists");
                    viewingAccount = new Account(accountDocument);
                    usernameText.append(viewingAccount.getUsername());
                    displayPicture(
                            viewingAccount.getProfilePictureUrl(),
                            pfp,
                            executor,
                            mainHandler,
                            getResources()
                    );

                    followedTags = view.findViewById(R.id.followList);
                    followedTags.setLayoutManager(layoutManagerf);

                    ButtonAdapter followAdapter = new ButtonAdapter(viewingAccount.getFollowedTags(), navController);
                    followedTags.setAdapter(followAdapter);

                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    Bundle bundle = new Bundle();
                    bundle.putString("tagsOnPosts", "u/"+viewingAccount.getUsername());
                    bundle.putBoolean("hideHashtagName", true);
                    fragmentManager.beginTransaction()
                            .replace(R.id.profileTagFragHolder, TagFragment.class, bundle)
                            .commit();

                    // only show edit options if looking at self
                    if(Objects.equals(viewingAccount.getId(), Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {

                        setChangeNameButton(view.findViewById(R.id.editUsernameButton), getContext());
                        setTagFollowButton(view.findViewById(R.id.followTagButton), getContext());
                        setTagBlockButton(view.findViewById(R.id.blockTagButton), getContext());

                        (view.findViewById(R.id.blockListTitle)).setVisibility(View.VISIBLE);
                        blockedTags = view.findViewById(R.id.blockList);
                        blockedTags.setVisibility(View.VISIBLE);
                        blockedTags.setLayoutManager(layoutManagerb);

                        ButtonAdapter blockedAdapter = new ButtonAdapter(viewingAccount.getBlockedTags(), navController);
                        blockedTags.setAdapter(blockedAdapter);
                        view.findViewById(R.id.button2).setVisibility(View.VISIBLE);
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

    private void setSignOutButtonListener(Button button){
        button.setOnClickListener(v -> AuthUI.getInstance()
                .signOut(requireActivity())
                .addOnCompleteListener(task -> navController.navigate(R.id.signIn)));
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
