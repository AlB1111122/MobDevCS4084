package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Account;
import ul.ie.cs4084.app.dataClasses.Post;

public class FullscreenPostFragment extends Fragment {

    private ExecutorService executor;
    Handler mainHandler;
    NavController navController;
    Post p;
    ImageView OPpfp;
    TextView OPname;
    TextView board;
    TextView postTitle;
    TextView postBody;
    RecyclerView postTags;
    Button upvote;
    Button downvote;
    Button comment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreen_post, container, false);

        assert getArguments() != null;
        String postId = getArguments().getString("postId");
        if(postId == null){
            return null;
        }

        OPpfp = view.findViewById(R.id.OPpfp);
        OPname = view.findViewById(R.id.postUsernameText);
        board = view.findViewById(R.id.postBoardText);
        postTitle = view.findViewById(R.id.postTitle);
        postBody = view.findViewById(R.id.postBody);
        postTags = view.findViewById(R.id.postTagRV);
        upvote = view.findViewById(R.id.upvoteButton);
        downvote = view.findViewById(R.id.downvoteButton);
        comment = view.findViewById(R.id.commentButton);

        Context c = getContext();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userId = db.document("accounts/"+
                FirebaseAuth.getInstance().getCurrentUser().getUid());
        DocumentReference viewingprofileDoc = db.collection("posts").document(postId);
        viewingprofileDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    //if yes populate local object
                    if (document.exists()) {
                        p = new Post(document);
                        postTitle.setText(p.getTitle());
                        postBody.setText(p.getBody());

                        LinearLayoutManager layoutManager = new LinearLayoutManager(c);
                        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                        postTags.setLayoutManager(layoutManager);

                        ButtonAdapter tagAdapter = new ButtonAdapter(p.getTags());
                        postTags.setAdapter(tagAdapter);
                        //set buttons
                        upvote.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                p.addUpvote(userId,db);
                            }
                        });
                        downvote.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                p.addDownvote(userId,db);
                            }
                        });
                        DocumentReference op = p.getProfile();
                        DocumentReference parentBoard = p.getParentBoard();
                        //display op information
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {//runnig on a thred because its slow
                                op.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                Runnable runnable = new Runnable() {//post back to ui thred
                                                    @Override
                                                    public void run() {
                                                        displayProfilePicture(document.getString("profilePictureUrl"));
                                                        OPname.append(document.getString("username"));
                                                    }
                                                };
                                                mainHandler.post(runnable);
                                            } else {
                                                Log.d(TAG, "error fetching posts original poster");
                                            }
                                        }

                                    }
                                });
                            }});
                        //display the name of the board it was posted to
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {//runnig on a thred because its slow
                                parentBoard.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {

                                                Runnable runnable = new Runnable() {//post back to ui thred
                                                    @Override
                                                    public void run() {
                                                        board.append(document.getString("name"));
                                                    }
                                                };
                                                mainHandler.post(runnable);
                                            } else {
                                                Log.d(TAG, "error board");
                                            }
                                        }

                                    }
                                });
                            }});
                    }else{
                        Log.d(TAG, "Post not found");
                    }
                }
            }
        });

        return view;
    }


    public static FullscreenPostFragment newInstance() {
        return new FullscreenPostFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity act = (MainActivity)getActivity();
        assert act != null;
        executor = act.executorService;//DEF not how to do it firegure out later
        mainHandler = new Handler(Looper.getMainLooper());
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void displayProfilePicture(String url) {
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        final long ONE_MEGABYTE = 1024 * 1024;
        gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {//runnig on a thred because its slow
                        Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(
                                bytes,
                                0,
                                bytes.length
                        ));
                        Runnable pfpRunnable = new Runnable() {//post back to ui thred
                            @Override
                            public void run() {
                                OPpfp.setImageDrawable(image);}
                        };
                        mainHandler.post(pfpRunnable);

                    }});
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "error fetching PFP");
            }
        });
    }
}