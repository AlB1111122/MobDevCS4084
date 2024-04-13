package ul.ie.cs4084.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executor;
import ul.ie.cs4084.app.dataClasses.Post;

public class TimelineFragment extends Fragment {

    private static final String ARG_TAG = "tagsOnPosts";
    private static final String ARG_EX_TAG = "excludeTags";
    private static final String ARG_TERM = "searchTerm";

    private ArrayList<String> tagsOnPosts;
    private ArrayList<String> excludeTags;
    private String searchTerm;
    private FirebaseFirestore db;
    private NavController navController;
    private DocumentReference signedInDoc;
    private Executor executor;
    private RecyclerView timeline;
    private boolean dataFetched = false; // Flag to track data fetch status
    PostAdapter postAdapter;

    public TimelineFragment() {
        // Required empty public constructor
    }

    public static TimelineFragment newInstance(ArrayList<String> tagsOnPosts, ArrayList<String> excludeTags, String searchTerm) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_TAG, tagsOnPosts);
        args.putStringArrayList(ARG_EX_TAG, excludeTags);
        args.putString(ARG_TERM, searchTerm);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tagsOnPosts = getArguments().getStringArrayList(ARG_TAG);
            excludeTags = getArguments().getStringArrayList(ARG_EX_TAG);
            searchTerm = getArguments().getString(ARG_TERM);
        }

        MainActivity act = (MainActivity) requireActivity();
        navController = act.navController;
        db = FirebaseFirestore.getInstance();
        signedInDoc = db.collection("accounts").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        executor = act.executorService;
        postAdapter = new PostAdapter(new ArrayList<>(), signedInDoc, db, navController);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timeline = view.findViewById(R.id.postList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        timeline.setLayoutManager(layoutManager);
        timeline.setAdapter(postAdapter);

        if (!dataFetched) {
            fetchPosts();
        }else{
            view.findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
        }
    }

    private void fetchPosts() {
        Query postsColl;
        if (tagsOnPosts == null) {
            postsColl = db.collection("posts");
        } else {
            postsColl = db.collection("posts").whereArrayContainsAny("tags", tagsOnPosts);
        }

        executor.execute(() -> {
            requireActivity().runOnUiThread(() -> timeline.setAdapter(postAdapter));

            postsColl.orderBy("upvotes", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> executor.execute(() -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post p = new Post(document);
                                if (Collections.disjoint(p.retriveTagsSet(), ((MainActivity) requireActivity()).signedInAccount.retriveBlockedSet())
                                        && (excludeTags == null || Collections.disjoint(p.retriveTagsSet(), excludeTags))
                                        && (searchTerm == null || (p.getTitle().contains(searchTerm) || p.getBody().contains(searchTerm)))) {
                                    requireActivity().runOnUiThread(() -> postAdapter.addPost(p));
                                }
                            }
                            dataFetched = true; // Update data fetch status
                        }
                        requireActivity().runOnUiThread(() ->requireView().findViewById(R.id.loadingProgressBar).setVisibility(View.GONE));
                    }));
        });
    }
}