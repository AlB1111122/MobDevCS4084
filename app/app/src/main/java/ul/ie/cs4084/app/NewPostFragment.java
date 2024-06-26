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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Account;
import ul.ie.cs4084.app.dataClasses.Board;
import ul.ie.cs4084.app.dataClasses.Factory;

public class NewPostFragment extends Fragment implements OnMapReadyCallback {
    private static final String ARG_BOARD = "boardId";
    private String boardId;
    private Board parentBoard;
    private ExecutorService executor;
    private Handler mainHandler;
    private NavController navController;
    private ImageView OPpfp;
    private TextView OPname;
    private TextView board;
    private ButtonAdapter tagAdapter;
    private MapView mapView;
    private GoogleMap map;
    private final CountDownLatch mapLatch = new CountDownLatch(1);
    private FusedLocationProviderClient fusedLocationClient;
    private GeoPoint postLocation = null;
    private PlacesClient placesClient;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private Uri localImageUri = null;
    private DocumentReference opDoc;
    private boolean returningFlag = false;

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

        //local photo picker sets localImageUri on photo picked
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
        if(returningFlag){
            navController.popBackStack();//dont bring user back to the create post screen drop them at the board
        }
        returningFlag = true;

        OPpfp = view.findViewById(R.id.OPpfp);
        OPname = view.findViewById(R.id.postUsernameText);
        board = view.findViewById(R.id.postBoardText);
        RecyclerView postTags = view.findViewById(R.id.postTagRV);
        //setup the enableing and disableing of the create post button
        view.findViewById(R.id.createPostButton).setEnabled(false);
        addTagButton(view.findViewById(R.id.addPostTagButton), getContext());

        TextInputEditText title = view.findViewById(R.id.postTitle);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (title.getText().toString().length() == 0) {
                    title.setError("A post needs a title!");
                    view.findViewById(R.id.createPostButton).setEnabled(false);
                } else {
                    title.setError(null);
                    view.findViewById(R.id.createPostButton).setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        view.findViewById(R.id.addLocationButton).setOnClickListener(task -> {
            //set up map
            mapView = view.findViewById(R.id.setMapMarkerView);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
            mapView.setVisibility(View.VISIBLE);

            view.findViewById(R.id.addLocationButton).setVisibility(View.GONE);//hide add location
            (view.findViewById(R.id.setMapMarkerView)).setVisibility(View.VISIBLE);//show map
            (view.findViewById(R.id.cancelGeotag)).setVisibility(View.VISIBLE);//show geotag cancel
            view.findViewById(R.id.autoCompleteFragmentView).setVisibility(View.VISIBLE);//show places search
            this.setLocation();
            executor.execute(() -> {
                try {//allow user to place marker manually by tapping
                    mapLatch.await();
                    mainHandler.post(()-> map.setOnMapClickListener(mapClick -> updateMapMarker(new LatLng(mapClick.latitude, mapClick.longitude))));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
try {
    //make the places search connect to google
    Places.initialize(getContext(), getContext().getString(R.string.map_key));
    placesClient = Places.createClient(getContext());
    //add places autocomplete fragment so users can search for places and place marker
    final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
    AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
            this.getChildFragmentManager().findFragmentById(R.id.autoCompleteFragmentView);
    if (autocompleteFragment != null) {
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
    }
}catch(Exception e){
    Toast.makeText(getContext(),"Somethings wrong with places client, check that you have the AIPkey and that the bill is paid", Toast.LENGTH_LONG).show();
}
        });

        view.findViewById(R.id.cancelGeotag).setOnClickListener(task -> {//dont add a geotag to the post
            postLocation = null;
            mapView.setVisibility(View.GONE);//hide map
            (view.findViewById(R.id.cancelGeotag)).setVisibility(View.GONE);//hide the cancel button
            view.findViewById(R.id.addLocationButton).setVisibility(View.VISIBLE);//show the button that allows you to add a location
            view.findViewById(R.id.autoCompleteFragmentView).setVisibility(View.GONE);//show the autocomplete views
        });

        //allow user to add image to post from local storage
        view.findViewById(R.id.addImageButton).setOnClickListener(task -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build())
        );

        //set tags
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        postTags.setLayoutManager(layoutManager);
        tagAdapter = new ButtonAdapter(navController,true, requireContext());
        postTags.setAdapter(tagAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final DocumentReference parentBoardDoc = db.document("boards/" +
                boardId);
        executor.execute(() -> {//profile information
            Account op = ((MainActivity)requireActivity()).signedInAccount;
            opDoc = db.document("accounts/" + op.getId());
            String pfpUrl = op.getProfilePictureUrl();
            String username = op.getUsername();

            //write to screen
            tagAdapter.addButton("u/" + op.getId());
            displayPicture(pfpUrl, OPpfp, executor, mainHandler, getResources());
            mainHandler.post(() -> OPname.append(username));
        });//boardInfo
        executor.execute(() -> parentBoardDoc.get().addOnCompleteListener(getBoardTask -> {
            if (getBoardTask.isSuccessful()) {
                DocumentSnapshot boardDocument = getBoardTask.getResult();
                if (boardDocument.exists()) {
                    parentBoard = new Board(boardDocument);

                    //write board info to the screen
                    mainHandler.post(() -> board.append(parentBoard.getName()));
                    ArrayList<String> boardTagArray = parentBoard.getTags();
                    boardTagArray.add("b/" + parentBoard.getName());
                    tagAdapter.addButtons(boardTagArray);
                } else {
                    Log.d(TAG, "error board");
                }
            }
        }));

        //make the post
        (view.findViewById(R.id.createPostButton)).setOnClickListener(v -> {
            String postTitle = title.getText().toString();
                Factory factory = new Factory(executor);
                String cloudImageUriStr = null;

                if(localImageUri != null){
                    StorageReference cloudInstance = FirebaseStorage.getInstance().getReference();
                    StorageReference storageRef = cloudInstance.child("postPictures/" + localImageUri.getLastPathSegment());

                    storageRef.putFile(localImageUri);//upload image
                    cloudImageUriStr = "gs://socialmediaapp-38b04.appspot.com/postPictures/" + localImageUri.getLastPathSegment();//put the string in the post
                }
                factory.createNewPost(
                    db,
                    parentBoardDoc,
                    opDoc,
                    postTitle,
                    ((TextInputEditText) view.findViewById(R.id.postBody)).getText().toString(),
                    postLocation,
                    tagAdapter.getLocalDataSet(),
                    cloudImageUriStr,
                    documentReference -> {//go to the new post
                        Bundle bundle = new Bundle();
                        bundle.putString("postId", documentReference.getId());
                        navController.navigate(R.id.action_Home_to_FullscreenPost, bundle);
                    }
                );
        });
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
            });
            final AlertDialog alertDialog = builder.show();
            final Button submitButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // my validation condition
                    if (input.getText().toString().contains("/")) {
                        input.setError("you cant add a tag that contains '/'");
                        submitButton.setEnabled(false);
                    } else {
                        input.setError(null);
                        submitButton.setEnabled(true);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        });
    }

    private void setLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        //do we have location permission?
        if (!(ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(executor, location -> {
                        // Got last known location. In some situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            try {
                                mapLatch.await();
                                LatLng position = new LatLng(location.getLatitude(),location.getLongitude());

                                mainHandler.post(()->{
                                    //place the marker at users location on the map
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
        mapLatch.countDown();//allow the marker to be placed
    }

    private void updateMapMarker(LatLng position){
        map.clear();//wipe marker
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(position, 15);
        map.addMarker(new MarkerOptions()
                .position(position)
        );
        map.moveCamera(camera);//show new position
        //add the new marker to the post
        postLocation = new GeoPoint(position.latitude, position.longitude);
    }
}