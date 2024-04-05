package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import ul.ie.cs4084.app.dataClasses.Account;
import ul.ie.cs4084.app.dataClasses.Post;

public class HomeFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public Account signedInAccount;
    Handler mainHandler;
    NavController navController;
    DocumentReference profileDoc;

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

        CountDownLatch pLatch = new CountDownLatch(9);
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
                    profileDoc = db.collection("account").document(signedInAccount.getId());

                    pLatch.countDown();
                } else {
                    //if not create a document in the db
                    Log.d(TAG, "profile does not exist");
                    signedInAccount = new Account(fireBaseAuthUser.getUid(), fireBaseAuthUser.getDisplayName(), new HashSet<String>(), new HashSet<String>());
                    db.collection("accounts").document(fireBaseAuthUser.getUid())
                            .set(signedInAccount)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Account successfully written!"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error writing account", e));

                    profileDoc = db.collection("account").document(signedInAccount.getId());
                    pLatch.countDown();
                }
            }
        });

        RecyclerView timeline = (RecyclerView) view.findViewById(R.id.postList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        timeline.setLayoutManager(layoutManager);
        ArrayList<String> a = new ArrayList();
        a.add("5KISWTxsVvYvM5tXaBBj");
        a.add("RrIVado9AC67l6uSsIil");
        a.add("example post");
        a.add("wiTBS0va3moGozMn36pf");
        a.add("sn7R4zHDjmT1SwWN2nzI");
        a.add("nTREHez1vCUAQ536p4ta");
        a.add("luPqmfwe8XexTL4Gtjvq");
        a.add("ZJiLgnRXHMMVitYPKIEB");
        ArrayList<Post> posts = new ArrayList<Post>();

        Executor ex = ((MainActivity) requireActivity()).executorService;

        for(String s:a){
            DocumentReference postDoc = db.collection("posts").document(s);
            postDoc.get().addOnCompleteListener(getPostTask ->  ex.execute(() ->{
                if (getPostTask.isSuccessful()) {
                    DocumentSnapshot postDocument = getPostTask.getResult();
                    //if yes populate local object
                    if (postDocument.exists()) {
                        Post p = new Post(postDocument);
                        posts.add(p);
                        pLatch.countDown();
                    }
                }
            }));
        }

        ex.execute(() -> {
            try {
                pLatch.await();
                PostAdapter postAdapter = new PostAdapter(posts,profileDoc,db,navController);
                mainHandler.post(()->timeline.setAdapter(postAdapter));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}

