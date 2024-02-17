package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth authStaus;
    private TextView mainText;
    private FirebaseUser user;
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            //email and password auth
            new AuthUI.IdpConfig.EmailBuilder().build()
            //TODO: add config files for google auth
            // new AuthUI.IdpConfig.GoogleBuilder().build()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authStaus = FirebaseAuth.getInstance();
        mainText = (TextView)findViewById(R.id.textView);
        user = authStaus.getCurrentUser();
        if (user != null) {
            Intent landingIntent = new Intent(this, LandingActivity.class);
            startActivity(landingIntent);
            finish();
        }
    }

    public void signIn(View view){
        startSignIn();
    }

    private void startSignIn() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .build();
        signInLauncher.launch(signInIntent);
    }

    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            (result) -> {
                IdpResponse response = result.getIdpResponse();

                if (result.getResultCode() == RESULT_OK) {
                    Intent landingIntent = new Intent(this, LandingActivity.class);
                    startActivity(landingIntent);
                    finish();
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