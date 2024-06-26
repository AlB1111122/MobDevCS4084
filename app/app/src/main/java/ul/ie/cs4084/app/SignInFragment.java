package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import ul.ie.cs4084.app.dataClasses.Account;

public class SignInFragment extends Fragment {
    Snackbar snackbar;
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            //email and password auth providers
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build()
    );

    public SignInFragment() {
        // Required empty public constructor
    }

    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadAccount();
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ){
        View view = inflater.inflate(R.layout.fragment_sign_in, container,false);
        Button signInB = view.findViewById(R.id.signInButton);
        signInB.setOnClickListener(v -> startSignIn());
        snackbar = Snackbar.make(view.findViewById(R.id.signInLayout),R.string.example_text,Snackbar.LENGTH_SHORT);
        return view;
    }

    private void startSignIn() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            (result) -> {
                IdpResponse response = result.getIdpResponse();

                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    loadAccount();
                } else {
                    // Sign in failed
                    if (response == null) {
                        // User pressed back button
                        snackbar.setText(R.string.sign_in_cancelled_msg);
                        snackbar.show();
                        return;
                    }

                    if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                        snackbar.setText(R.string.no_internet_msg);
                        snackbar.show();
                        return;
                    }
                    snackbar.setText(R.string.generic_error_msg);
                    snackbar.show();
                    Log.e(TAG, "Sign-in error: ", response.getError());
                }
            }
    );
    private void loadAccount(){
        FirebaseUser fireBaseAuthUser = FirebaseAuth.getInstance().getCurrentUser();
        assert fireBaseAuthUser != null; // we know its not null because they just signed in
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        NavController navController = NavHostFragment.findNavController(this);
        DocumentReference signedInUser = db.collection("accounts").document(fireBaseAuthUser.getUid());
        signedInUser.get().addOnCompleteListener(getAccountTask -> {
            if (getAccountTask.isSuccessful()) {
                DocumentSnapshot document = getAccountTask.getResult();
                //if yes populate local object
                if (document.exists()) {
                    Log.d(TAG, "profile exists");
                    //get profile from db
                    ((MainActivity) requireActivity()).signedInAccount = new Account(document);
                    navController.navigate(R.id.action_to_timeline);
                    ((MainActivity) requireActivity()).navView.setVisibility(View.VISIBLE);
                } else {
                    //if not create a document in the db
                    Log.d(TAG, "profile does not exist");
                    ((MainActivity) requireActivity()).signedInAccount = new Account(fireBaseAuthUser.getUid(), fireBaseAuthUser.getDisplayName(), new HashSet<String>(), new HashSet<String>());
                    db.collection("accounts").document(fireBaseAuthUser.getUid())
                        .set(((MainActivity) requireActivity()).signedInAccount)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Account successfully written!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing account", e));
                    navController.navigate(R.id.action_to_timeline);
                    ((MainActivity) requireActivity()).navView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
