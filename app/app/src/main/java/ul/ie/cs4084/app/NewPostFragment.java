package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Factory;

public class NewPostFragment extends Fragment implements OnMapReadyCallback {

    private ExecutorService executor;
    private Handler mainHandler;
    private NavController navController;
    private ImageView OPpfp;
    private TextView OPname;
    private TextView board;
    private RecyclerView postTags;
    private ButtonAdapter tagAdapter;
    private HashSet<String> tagSet = new HashSet<>();
    private MapView mapView;
    private GoogleMap map;
    private final CountDownLatch mapLatch = new CountDownLatch(1);
    private FusedLocationProviderClient fusedLocationClient;
    private GeoPoint postLocation = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_post, container, false);

        assert getArguments() != null;
        String boardId = getArguments().getString("boardId");
        if (boardId == null) {
            return null;
        }

        OPpfp = view.findViewById(R.id.OPpfp);
        OPname = view.findViewById(R.id.postUsernameText);
        board = view.findViewById(R.id.postBoardText);
        postTags = view.findViewById(R.id.postTagRV);
        addTagButton(view.findViewById(R.id.addPostTagButton), getContext());
        view.findViewById(R.id.addLocationButton).setOnClickListener(task -> {
            mapView.setVisibility(View.VISIBLE);
            (view.findViewById(R.id.addLocationByText)).setVisibility(View.VISIBLE);
            (view.findViewById(R.id.cancelGeotag)).setVisibility(View.VISIBLE);
            this.setLocation();
        });

        view.findViewById(R.id.cancelGeotag).setOnClickListener(task -> {
            postLocation = null;
            mapView.setVisibility(View.GONE);
            (view.findViewById(R.id.addLocationByText)).setVisibility(View.GONE);
            (view.findViewById(R.id.cancelGeotag)).setVisibility(View.GONE);
            view.findViewById(R.id.addLocationButton).setVisibility(View.GONE);
        });
        //(view.findViewById(R.id.addLocationByText)).setOnClickListener(task ->{

        //});
        mapView = view.findViewById(R.id.setMapMarkerView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        postTags.setLayoutManager(layoutManager);

        tagAdapter = new ButtonAdapter();
        postTags.setAdapter(tagAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final DocumentReference parentBoard = db.document("boards/" +
                boardId);

        final DocumentReference account = db.document("accounts/" +
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        executor.execute(() -> account.get().addOnCompleteListener(getProfileTask -> {
            if (getProfileTask.isSuccessful()) {
                DocumentSnapshot accountDocument = getProfileTask.getResult();
                if (accountDocument.exists()) {
                    //post back to ui thred
                    String pfpUrl = accountDocument.getString("profilePictureUrl");
                    String username = accountDocument.getString("username");
                    Runnable runnable = () -> {
                        displayProfilePicture(pfpUrl);
                        OPname.append(username);
                    };
                    mainHandler.post(runnable);
                } else {
                    Log.d(TAG, "error fetching posts original poster");
                }
            }
        }));
        executor.execute(() -> parentBoard.get().addOnCompleteListener(getBoardTask -> {
            if (getBoardTask.isSuccessful()) {
                DocumentSnapshot boardDocument = getBoardTask.getResult();
                if (boardDocument.exists()) {
                    String name = boardDocument.getString("name");
                    //post back to ui thred
                    mainHandler.post(() -> board.append(name));
                    ArrayList<String> boardTagArray = (ArrayList<String>) Objects.requireNonNull(boardDocument.get("tags"));
                    tagSet.addAll(boardTagArray);
                    tagAdapter.addButtons(boardTagArray);
                } else {
                    Log.d(TAG, "error board");
                }
            }

        }));

        (view.findViewById(R.id.createPostButton)).setOnClickListener(v -> {
            Factory factory = new Factory(executor);
            factory.createNewPost(
                    db,
                    parentBoard,
                    account,
                    ((TextInputEditText) view.findViewById(R.id.postTitle)).getText().toString(),
                    ((TextInputEditText) view.findViewById(R.id.postBody)).getText().toString(),
                    postLocation,
                    tagSet,
                    documentReference -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("postId", documentReference.getId());
                        navController.navigate(R.id.action_Home_to_FullscreenPost, bundle);
                    }
            );
        });
        return view;
    }


    public static NewPostFragment newInstance() {
        return new NewPostFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity act = (MainActivity) getActivity();
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

    private void addTagButton(Button button, Context context){
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Add a tag");

            // Set up the input
            final EditText input = new EditText(context);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setCancelable(true);
            builder.setPositiveButton("Add", (dialog, which) -> {
                String tagStr = input.getText().toString();
                tagAdapter.addButton(tagStr);
                tagSet.add(tagStr);
            });

            builder.show();
        });
    }

    private void setLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (!(ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(executor, location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            try {
                                mapLatch.await();
                                postLocation = new GeoPoint(location.getLatitude(),location.getLongitude());
                                LatLng position = new LatLng(location.getLatitude(),location.getLongitude());

                                CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(position, 15);

                                mainHandler.post(()->{
                                        map.addMarker(new MarkerOptions()
                                                .position(position)
                                        );
                                        map.moveCamera(camera);
                                });
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    });
        }else{
            Log.d(TAG, "no location permission");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map=googleMap;
        mapLatch.countDown();
    }
}