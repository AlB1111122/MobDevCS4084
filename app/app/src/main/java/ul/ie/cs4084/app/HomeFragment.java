package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;

import ul.ie.cs4084.app.dataClasses.Account;

public class HomeFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Account signedInAccount;
    Handler mainHandler;
    NavController navController;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity act = (MainActivity)getActivity();
        assert act != null;
        mainHandler = new Handler(Looper.getMainLooper());
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container,false);
        Button button = view.findViewById(R.id.button3);
        button.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("profileId", signedInAccount.getId());
            navController.navigate(R.id.action_to_profile, bundle);
        });

        (view.findViewById(R.id.button4)).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("profileId", "example profile");
            navController.navigate(R.id.action_to_profile, bundle);
        });

        (view.findViewById(R.id.postExampleButton)).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("postId", "example post");
            navController.navigate(R.id.action_Home_to_FullscreenPost,bundle);
        });

        (view.findViewById(R.id.createPostButton)).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("boardId", "example board");
            navController.navigate(R.id.action_to_new_post, bundle);
        });

        FirebaseUser fireBaseAuthUser = FirebaseAuth.getInstance().getCurrentUser();
        assert fireBaseAuthUser != null; // we know its not null because they just signed in
        db = FirebaseFirestore.getInstance();
        //check if the Account for this fireBaseAuthUser exists
        DocumentReference signedInUser = db.collection("accounts").document(fireBaseAuthUser.getUid());
        signedInUser.get().addOnCompleteListener(getAccountTask -> {
            if (getAccountTask.isSuccessful()) {
                DocumentSnapshot document = getAccountTask.getResult();
                //if yes populate local object
                if (document.exists()) {
                    Log.d(TAG, "profile exists");
                    signedInAccount = new Account(document);
                } else {
                    //if not create a document in the db
                    Log.d(TAG, "profile does not exist");
                    signedInAccount = new Account(fireBaseAuthUser.getUid(), fireBaseAuthUser.getDisplayName(), new HashSet<String>(), new HashSet<String>());
                    db.collection("accounts").document(fireBaseAuthUser.getUid())
                            .set(signedInAccount)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Account successfully written!"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error writing account", e));
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
