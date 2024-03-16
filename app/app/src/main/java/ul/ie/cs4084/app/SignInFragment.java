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
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SignInFragment extends Fragment {

    private FirebaseAuth authStatus;
    private TextView mainText;
    private FirebaseUser user;
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            //email and password auth providers
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build()
    );


    public SignInFragment() {
        // Required empty public constructor
    }

    public static SignInFragment newInstance(String param1, String param2) {
        return new SignInFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authStatus = FirebaseAuth.getInstance();
        //mainText = (TextView)getView().findViewById(R.id.textView);
        user = authStatus.getCurrentUser();
        if (user != null) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_signInTestFrag);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ){
        View view = inflater.inflate(R.layout.fragment_sign_in, container,false);
        //test TBR
        TextView t = (TextView) view.findViewById(R.id.textView);
        t.setText("YIPPIEE");

        Button signInB = (Button) view.findViewById(R.id.signInButton);
        signInB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
            }
        });
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

    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            (result) -> {
                IdpResponse response = result.getIdpResponse();

                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    NavController navController = NavHostFragment.findNavController(this);
                    navController.navigate(R.id.action_signInTestFrag);
                    //Intent landingIntent = new Intent(this, LandingActivity.class);
                    //startActivity(landingIntent);
                    //finish();
                } else {
                    // Sign in failed
                    if (response == null) {
                        // User pressed back button
                        mainText.setText(R.string.sign_in_cancelled_msg);
                        return;
                    }

                    if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                        mainText.setText(R.string.no_internet_msg);
                        return;
                    }

                    mainText.setText(R.string.generic_error_msg);
                    Log.e(TAG, "Sign-in error: ", response.getError());
                }
            }
    );
}
