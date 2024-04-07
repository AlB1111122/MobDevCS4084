package ul.ie.cs4084.app;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import ul.ie.cs4084.app.dataClasses.Post;


public class TimelineFragment extends Fragment {

    private static final String ARG_TAG = "tagsOnPosts";
    private ArrayList<String> tagsOnPosts;
    private FirebaseFirestore db;
    private Handler mainHandler;
    private NavController navController;
    private DocumentReference signedInDoc;
    private final CountDownLatch pLatch = new CountDownLatch(2);
    private Executor executor;
    RecyclerView timeline;
    LinearLayoutManager layoutManager;
    public TimelineFragment() {
        // Required empty public constructor
    }

    public static TimelineFragment newInstance(ArrayList<String>tagsOnPosts) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_TAG, tagsOnPosts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO add aguments to get diffrent timelines
        if (getArguments() != null) {
            tagsOnPosts = getArguments().getStringArrayList(ARG_TAG);
        }

        MainActivity act = (MainActivity)getActivity();
        assert act != null;
        mainHandler = new Handler(Looper.getMainLooper());
        navController = act.navController;
        db = FirebaseFirestore.getInstance();
        signedInDoc = db.collection("accounts").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        pLatch.countDown();
        executor = ((MainActivity) requireActivity()).executorService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        timeline = view.findViewById(R.id.postList);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        timeline.setLayoutManager(layoutManager);

        ArrayList<Post> posts = new ArrayList<Post>();
        Query postsColl;
        if(tagsOnPosts == null){
            postsColl = db.collection("posts");
        }else{
            postsColl = db.collection("posts").whereArrayContainsAny("tags",tagsOnPosts);
        }
        postsColl.orderBy("upvotes", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> executor.execute(() -> {{
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Post p = new Post(document);
                            if(Collections.disjoint(p.retriveTagsSet(),((MainActivity)requireActivity()).signedInAccount.retriveBlockedSet())){
                                posts.add(p);
                            }
                        }
                        pLatch.countDown();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }}));

        executor.execute(() -> {
            try {
                pLatch.await();
                PostAdapter postAdapter = new PostAdapter(posts,signedInDoc,db,navController);
                mainHandler.post(()->timeline.setAdapter(postAdapter));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return view;
    }
}