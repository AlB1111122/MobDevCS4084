package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Post;

public class FullscreenPostFragment extends Fragment implements OnMapReadyCallback{

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
    MapView mapView;
    GeoPoint location;
    private final CountDownLatch locationLatch = new CountDownLatch(1);

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

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Context c = getContext();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userId = db.document("accounts/"+
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        DocumentReference viewingprofileDoc = db.collection("posts").document(postId);
        viewingprofileDoc.get().addOnCompleteListener(getPostTask -> {
            if (getPostTask.isSuccessful()) {
                DocumentSnapshot postDocument = getPostTask.getResult();
                //if yes populate local object
                if (postDocument.exists()) {
                    p = new Post(postDocument);
                    postTitle.setText(p.getTitle());
                    postBody.setText(p.getBody());

                    LinearLayoutManager layoutManager = new LinearLayoutManager(c);
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    postTags.setLayoutManager(layoutManager);

                    ButtonAdapter tagAdapter = new ButtonAdapter(p.getTags());
                    postTags.setAdapter(tagAdapter);


                    location = p.getGeotag();
                    locationLatch.countDown();
                    //set buttons
                    upvote.setOnClickListener(v -> p.addUpvote(userId,db));
                    downvote.setOnClickListener(v -> p.addDownvote(userId,db));
                    DocumentReference op = p.getProfile();
                    DocumentReference parentBoard = p.getParentBoard();
                    //display op information
                    executor.execute(() -> {//runnig on a thred because its slow
                        op.get().addOnCompleteListener(getOPTask -> {
                            if (getOPTask.isSuccessful()) {
                                DocumentSnapshot accountDocument = getOPTask.getResult();
                                if (accountDocument.exists()) {
                                    //post back to ui thred
                                    Runnable runnable = () -> {
                                        displayProfilePicture(accountDocument.getString("profilePictureUrl"));
                                        OPname.append(accountDocument.getString("username"));
                                    };
                                    mainHandler.post(runnable);
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
                                    //post back to ui thred
                                    Runnable runnable = () -> board.append(boardDocument.getString("name"));
                                    mainHandler.post(runnable);
                                } else {
                                    Log.d(TAG, "error board");
                                }
                            }

                        });
                    });
                }else{
                    Log.d(TAG, "Post not found");
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
        gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> executor.execute(() -> {//runnig on a thred because its slow
            Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(
                    bytes,
                    0,
                    bytes.length
            ));
            //post back to ui thred
            mainHandler.post(() -> OPpfp.setImageDrawable(image));

        })).addOnFailureListener(exception -> Log.d(TAG, "error fetching PFP"));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
            executor.execute(() -> {
                try {
                    locationLatch.await();
                    if (location != null) {
                        //post back to ui thred
                        Runnable runnable = () -> {
                            mapView.setVisibility(View.VISIBLE);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .title("Marker"));
                        };
                        mainHandler.post(runnable);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
    }
}