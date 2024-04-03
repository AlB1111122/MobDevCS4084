package ul.ie.cs4084.app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ul.ie.cs4084.app.dataClasses.Post;

public class timelinePostFragment extends Fragment {

    TextView postTitle;
    TextView postBody;
    Button upvote;
    Button downvote;
    public timelinePostFragment() {
        // Required empty public constructor
    }

    public static timelinePostFragment newInstance() {
        return new timelinePostFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timeline_post, container, false);
        postTitle = view.findViewById(R.id.postTitleText);
        postBody = view.findViewById(R.id.postBody);
        upvote = view.findViewById(R.id.upvoteButton);
        downvote = view.findViewById(R.id.downvoteButton);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference viewingprofileDoc = db.collection("posts").document("example post");
        viewingprofileDoc.get().addOnCompleteListener(getPostTask -> {
            if (getPostTask.isSuccessful()) {
                DocumentSnapshot postDocument = getPostTask.getResult();
                //if yes populate local object
                if (postDocument.exists()) {
                    Post p = new Post(postDocument);
                    postTitle.setText(p.getTitle());
                    //
                    String s =p.getBody();
                    postBody.setText(s);
                    int upvotesInt = p.retriveUpvotesSet().size();
                    upvote.append(" "+upvotesInt);
                    int downvotesInt = p.retriveDownvotesSet().size();
                    upvote.append(" "+downvotesInt);
                }
            }
        });
        return view;
    }
}