package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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

import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Account;

public class HomeFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Account signedInAccount;
    private ImageView pfp;
    private TextView usernameText;
    private ExecutorService executor;
    Handler mainHandler;

    NavController navController;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container,false);
        Button button = (Button) view.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("profileId", signedInAccount.getId());
                navController.navigate(R.id.action_to_profile, bundle);
            }
        });

        ((Button)view.findViewById(R.id.button4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("profileId", "example profile");
                navController.navigate(R.id.action_to_profile, bundle);
            }
        });

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
                        signedInAccount = new Account(fireBaseAuthUser.getUid(), fireBaseAuthUser.getDisplayName(), new HashSet<String>(), new HashSet<String>());
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
            }
        });
        return view;
    }
}