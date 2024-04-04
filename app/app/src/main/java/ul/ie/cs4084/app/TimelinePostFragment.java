package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Post;

public class TimelinePostFragment extends Fragment {

    private TextView postTitle;
    private TextView postBody;
    private Button upvote;
    private Button downvote;
    private Handler mainHandler;
    private NavController navController;
    private ExecutorService executor;
    public TimelinePostFragment() {
        // Required empty public constructor
    }

    public static TimelinePostFragment newInstance() {
        return new TimelinePostFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity act = (MainActivity)getActivity();
        assert act != null;
        executor = act.executorService;//DEF not how to do it firegure out later
        mainHandler = new Handler(Looper.getMainLooper());
        navController = act.navController;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_timeline_post, container, false);

        assert getArguments() != null;
        String postId = getArguments().getString("postId");
        if(postId == null){
            Log.e(TAG, "no arguments");
            return null;
        }
        executor.execute(()->{
        // Inflate the layout for this fragment
        postTitle = view.findViewById(R.id.postTitleText);
        postBody = view.findViewById(R.id.postBody);
        upvote = view.findViewById(R.id.upvoteButton);
        downvote = view.findViewById(R.id.downvoteButton);
        TextView upCount = view.findViewById(R.id.upvoteCounter);
        TextView downCount = view.findViewById(R.id.downvoteCounter);
        CardView card = view.findViewById(R.id.card);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userId = db.document("accounts/"+
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        DocumentReference postDoc = db.collection("posts").document(postId);
        postDoc.get().addOnCompleteListener(getPostTask -> executor.execute(()-> {
            if (getPostTask.isSuccessful()) {
                DocumentSnapshot postDocument = getPostTask.getResult();
                //if yes populate local object
                if (postDocument.exists()) {
                    Post p = new Post(postDocument);
                    String title = p.getTitle();
                    String body = p.getBody();
                    String upStr = Integer.toString(p.retriveUpvotesSet().size());
                    String downStr = Integer.toString(p.retriveDownvotesSet().size());

                    mainHandler.post(()->{
                        postTitle.setText(title);
                        postBody.setText(body);
                        upCount.setText(upStr);
                        downCount.setText(downStr);
                        card.setVisibility(View.VISIBLE);
                        card.setOnClickListener(clicked -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("postId", p.getId());
                            navController.navigate(R.id.action_Home_to_FullscreenPost,bundle);
                        });
                        upvote.setOnClickListener(upvote -> {
                            p.addUpvote(userId,db);
                            upCount.setText(Integer.toString(p.retriveUpvotesSet().size()));
                            downCount.setText(Integer.toString(p.retriveDownvotesSet().size()));
                        });
                        downvote.setOnClickListener(upvote -> {
                            p.addDownvote(userId,db);
                            upCount.setText(Integer.toString(p.retriveUpvotesSet().size()));
                            downCount.setText(Integer.toString(p.retriveDownvotesSet().size()));
                        });
                    });
                }
            }
        }));
        });
        return view;
    }
}