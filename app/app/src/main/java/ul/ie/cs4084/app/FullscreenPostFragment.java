package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static ul.ie.cs4084.app.dataClasses.Database.displayPicture;

import android.content.Context;
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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.MapView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Account;
import ul.ie.cs4084.app.dataClasses.Board;
import ul.ie.cs4084.app.dataClasses.Post;

public class FullscreenPostFragment extends Fragment implements OnMapReadyCallback{
    private static final String ARG_POST = "postId";
    private String postId;
    private ExecutorService executor;
    private Handler mainHandler;
    private NavController navController;
    private FirebaseFirestore db ;
    private Post p;
    private Board bPosted;
    private Account OPoster;
    ButtonAdapter tagAdapter;
    private ImageView OPpfp;
    private TextView OPname;
    private View opClickable;
    private TextView board;
    private TextView postTitle;
    private TextView postBody;
    private RecyclerView postTags;
    private Button upvote;
    private Button downvote;
    private Button comment;
    private MapView mapView;
    private GeoPoint location;
    private final CountDownLatch locationLatch = new CountDownLatch(1);
    private boolean renderFlag = false;
    private DocumentReference userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST);
        }

        MainActivity act = (MainActivity)getActivity();
        assert act != null;
        executor = act.executorService;//DEF not how to do it firegure out later
        mainHandler = new Handler(Looper.getMainLooper());
        navController = NavHostFragment.findNavController(this);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fullscreen_post, container, false);
    }


    public static FullscreenPostFragment newInstance() {
        return new FullscreenPostFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        OPpfp = view.findViewById(R.id.OPpfp);
        OPname = view.findViewById(R.id.postUsernameText);
        opClickable = view.findViewById(R.id.opClickable);
        board = view.findViewById(R.id.postBoardText);
        postTitle = view.findViewById(R.id.postTitle);
        postBody = view.findViewById(R.id.postBody);
        postTags = view.findViewById(R.id.postTagRV);
        upvote = view.findViewById(R.id.upvoteButton);
        downvote = view.findViewById(R.id.downvoteButton);
        comment = view.findViewById(R.id.commentButton);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);



        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        postTags.setLayoutManager(layoutManager);

        userId = db.document("accounts/" +
                ((MainActivity)requireActivity()).signedInAccount.getId());
        if(!renderFlag) {
        DocumentReference viewingprofileDoc = db.collection("posts").document(postId);
        viewingprofileDoc.get().addOnCompleteListener(getPostTask -> {
            if (getPostTask.isSuccessful()) {
                DocumentSnapshot postDocument = getPostTask.getResult();
                //if yes populate local object
                if (postDocument.exists()) {
                    p = new Post(postDocument);
                    postTitle.setText(p.getTitle());
                    postBody.setText(p.getBody());
                    if (p.getImageUrl() != null) {
                        displayPicture(
                            p.getImageUrl(),
                            view.findViewById(R.id.postImage),
                            executor,
                            mainHandler,
                            getResources());
                    }

                    tagAdapter = new ButtonAdapter(p.getTags(), navController, false);
                    postTags.setAdapter(tagAdapter);

                    location = p.getGeotag();
                    locationLatch.countDown();
                    //set buttons
                    upvote.setOnClickListener(v -> p.addUpvote(userId, db));
                    downvote.setOnClickListener(v -> p.addDownvote(userId, db));
                    DocumentReference op = p.getProfile();
                    DocumentReference parentBoard = p.getParentBoard();
                    //display op information
                    executor.execute(() -> {//runnig on a thred because its slow
                        op.get().addOnCompleteListener(getOPTask -> {
                        if (getOPTask.isSuccessful()) {
                            DocumentSnapshot accountDocument = getOPTask.getResult();
                            if (accountDocument.exists()) {
                                OPoster = new Account(accountDocument);
                                displayPicture(accountDocument.getString("profilePictureUrl"), OPpfp, executor, mainHandler, getResources());
                                //post back to ui thred
                                mainHandler.post(() -> OPname.append(OPoster.getUsername()));
                                opClickable.setOnClickListener(v -> {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("profileId", OPoster.getId());
                                    navController.navigate(R.id.action_to_profile, bundle);
                                });
                            } else {
                                Log.d(TAG, "error fetching posts original poster");
                            }
                        }

                    });
                });
                //display the name of the board it was posted to
                    executor.execute(() -> {//runnig on a thred because its slow
                        parentBoard.get().addOnCompleteListener(getBoardTask -> {
                            if (getBoardTask.isSuccessful()) {
                                DocumentSnapshot boardDocument = getBoardTask.getResult();
                                if (boardDocument.exists()) {
                                bPosted = new Board(boardDocument);
                                //post back to ui thred
                                mainHandler.post(() -> board.append(bPosted.getName()));
                                board.setOnClickListener(clicked -> {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("boardId", bPosted.getId());
                                    navController.navigate(R.id.action_to_board, bundle);
                                });
                            } else {
                                Log.d(TAG, "error board");
                            }
                        }

                    });
                    });
                } else {
                    Log.d(TAG, "Post not found");
                }
            }
        });
        renderFlag = true;
        }else{
        setScreen(view.findViewById(R.id.postImage));
    }
    }

    private void setScreen(ImageView view){
        postTitle.setText(p.getTitle());
        postBody.setText(p.getBody());
        board.append(bPosted.getName());
        OPname.append(OPoster.getUsername());
        displayPicture(OPoster.getProfilePictureUrl(), OPpfp, executor, mainHandler, getResources());
        postTags.setAdapter(tagAdapter);
        if(p.getImageUrl() != null) {
            displayPicture(
                    p.getImageUrl(),
                    view,//.findViewById(R.id.postImage)
                    executor,
                    mainHandler,
                    getResources());
        }
        postTags.setAdapter(tagAdapter);
        opClickable.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("profileId", OPoster.getId());
            navController.navigate(R.id.action_to_profile, bundle);
        });

        board.setOnClickListener(clicked -> {
            Bundle bundle = new Bundle();
            bundle.putString("boardId", bPosted.getId());
            navController.navigate(R.id.action_to_board, bundle);
        });

        upvote.setOnClickListener(v -> p.addUpvote(userId, db));
        downvote.setOnClickListener(v -> p.addDownvote(userId, db));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
            executor.execute(() -> {
                try {
                    locationLatch.await();
                    if (location != null) {
                        //post back to ui thred
                        LatLng position = new LatLng(location.getLatitude(),location.getLongitude());
                        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(position, 15);
                        Runnable runnable = () -> {
                            mapView.setVisibility(View.VISIBLE);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title("Marker"));
                            googleMap.moveCamera(camera);
                        };
                        mainHandler.post(runnable);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
    }
}