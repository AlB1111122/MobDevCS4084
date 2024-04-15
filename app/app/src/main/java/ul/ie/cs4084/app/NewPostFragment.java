package ul.ie.cs4084.app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static ul.ie.cs4084.app.dataClasses.Database.displayPicture;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Account;
import ul.ie.cs4084.app.dataClasses.Factory;

public class NewPostFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_BOARD = "boardId";
    private String boardId;
    private ExecutorService executor;
    private Handler mainHandler;
    private NavController navController;
    private ImageView OPpfp;
    private TextView OPname;
    private TextView board;
    private ButtonAdapter tagAdapter;
    private HashSet<String> tagSet = new HashSet<>();
    private MapView mapView;
    private GoogleMap map;
    private final CountDownLatch mapLatch = new CountDownLatch(1);
    private FusedLocationProviderClient fusedLocationClient;
    private GeoPoint postLocation = null;
    PlacesClient placesClient;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    Uri localImageUri = null;
    DocumentReference opDoc;

    public static NewPostFragment newInstance(String boardId) {
        NewPostFragment fragment = new NewPostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BOARD, boardId);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boardId = getArguments().getString(ARG_BOARD);
        }

        MainActivity act = (MainActivity) getActivity();
        assert act != null;
        executor = act.executorService;//DEF not how to do it firegure out later
        mainHandler = new Handler(Looper.getMainLooper());
        navController = NavHostFragment.findNavController(this);

        Places.initialize(getContext(), getContext().getString(R.string.map_key));
        placesClient = Places.createClient(getContext());
        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        localImageUri = uri;
                    } else {
                        localImageUri = null;
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OPpfp = view.findViewById(R.id.OPpfp);
        OPname = view.findViewById(R.id.postUsernameText);
        board = view.findViewById(R.id.postBoardText);
        RecyclerView postTags = view.findViewById(R.id.postTagRV);
        addTagButton(view.findViewById(R.id.addPostTagButton), getContext());
        view.findViewById(R.id.addLocationButton).setOnClickListener(task -> {
            mapView = view.findViewById(R.id.setMapMarkerView);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
            mapView.setVisibility(View.VISIBLE);
            view.findViewById(R.id.addLocationButton).setVisibility(View.GONE);
            (view.findViewById(R.id.setMapMarkerView)).setVisibility(View.VISIBLE);
            (view.findViewById(R.id.cancelGeotag)).setVisibility(View.VISIBLE);
            view.findViewById(R.id.autoCompleteFragmentView).setVisibility(View.VISIBLE);
            this.setLocation();
            executor.execute(() -> {
                try {
                    mapLatch.await();
                    mainHandler.post(()-> map.setOnMapClickListener(mapClick -> updateMapMarker(new LatLng(mapClick.latitude, mapClick.longitude))));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                    this.getChildFragmentManager().findFragmentById(R.id.autoCompleteFragmentView);

            assert autocompleteFragment != null;
            autocompleteFragment.setPlaceFields(placeFields);
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onError(@NonNull Status status) {
                    if (status.getStatusMessage() != null) {
                        Log.i(TAG, status.getStatusMessage());
                    }
                }

                @Override
                public void onPlaceSelected(Place place) {
                    if (place != null) {
                        updateMapMarker(place.getLatLng());
                    }
                }
            });
        });
        view.findViewById(R.id.cancelGeotag).setOnClickListener(task -> {
            postLocation = null;
            mapView.setVisibility(View.GONE);
            (view.findViewById(R.id.setMapMarkerView)).setVisibility(View.GONE);
            (view.findViewById(R.id.cancelGeotag)).setVisibility(View.GONE);
            view.findViewById(R.id.addLocationButton).setVisibility(View.VISIBLE);
            view.findViewById(R.id.autoCompleteFragmentView).setVisibility(View.GONE);
        });
        view.findViewById(R.id.addImageButton).setOnClickListener(task -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build())
        );

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        postTags.setLayoutManager(layoutManager);

        tagAdapter = new ButtonAdapter(navController,true);
        postTags.setAdapter(tagAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final DocumentReference parentBoard = db.document("boards/" +
                boardId);
        executor.execute(() -> {//profile information
            Account op = ((MainActivity)requireActivity()).signedInAccount;

            opDoc = db.document("accounts/" + op.getId());
            //post back to ui thred
            String pfpUrl = op.getProfilePictureUrl();
            String username = op.getUsername();

            tagAdapter.addButton("u/" + username);
            tagSet.add("u/" + username);

            displayPicture(pfpUrl, OPpfp, executor, mainHandler, getResources());
            Runnable runnable = () -> {
                OPname.append(username);
            };
            mainHandler.post(runnable);
        });//boardInfo
        executor.execute(() -> parentBoard.get().addOnCompleteListener(getBoardTask -> {
            if (getBoardTask.isSuccessful()) {
                DocumentSnapshot boardDocument = getBoardTask.getResult();
                if (boardDocument.exists()) {
                    String name = boardDocument.getString("name");
                    //post back to ui thred
                    mainHandler.post(() -> board.append(name));
                    ArrayList<String> boardTagArray = (ArrayList<String>) Objects.requireNonNull(boardDocument.get("tags"));
                    boardTagArray.add("b/" + name);
                    tagSet.addAll(boardTagArray);
                    tagAdapter.addButtons(boardTagArray);
                } else {
                    Log.d(TAG, "error board");
                }
            }

        }));
//make the post
        (view.findViewById(R.id.createPostButton)).setOnClickListener(v -> {
            String postTitle = ((TextInputEditText) view.findViewById(R.id.postTitle)).getText().toString();
            if(!postTitle.equals("")){
            Factory factory = new Factory(executor);
            String cloudImageUriStr = null;

            if(localImageUri != null){
                StorageReference cloudInstance = FirebaseStorage.getInstance().getReference();
                StorageReference storageRef = cloudInstance.child("postPictures/" + localImageUri.getLastPathSegment());

                storageRef.putFile(localImageUri);
                cloudImageUriStr = "gs://socialmediaapp-38b04.appspot.com/postPictures/" + localImageUri.getLastPathSegment();
            }
            factory.createNewPost(
                    db,
                    parentBoard,
                    opDoc,
                    postTitle,
                    ((TextInputEditText) view.findViewById(R.id.postBody)).getText().toString(),
                    postLocation,
                    tagSet,
                    cloudImageUriStr,
                    documentReference -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("postId", documentReference.getId());
                        navController.navigate(R.id.action_Home_to_FullscreenPost, bundle);
                    }
            );
        }else{
                Toast toast = Toast.makeText(getContext(), "A post needs a title", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        );
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
                if(!tagStr.contains("/")) {
                    tagAdapter.addButton(tagStr);
                    tagSet.add(tagStr);
                }
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
                                LatLng position = new LatLng(location.getLatitude(),location.getLongitude());

                                mainHandler.post(()->{
                                    map.setMyLocationEnabled(true);
                                    updateMapMarker(position);
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

    private void updateMapMarker(LatLng position){
        map.clear();
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(position, 15);
        map.addMarker(new MarkerOptions()
                .position(position)
        );
        map.moveCamera(camera);
        postLocation = new GeoPoint(position.latitude, position.longitude);
    }
}