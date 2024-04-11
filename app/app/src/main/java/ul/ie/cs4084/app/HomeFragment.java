package ul.ie.cs4084.app;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {
    private FirebaseFirestore db;
    Handler mainHandler;
    NavController navController;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity act = (MainActivity)getActivity();
        assert act != null;
        mainHandler = new Handler(Looper.getMainLooper());
        navController = NavHostFragment.findNavController(this);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container,false);
        Button button = view.findViewById(R.id.button3);
        button.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("profileId", ((MainActivity)requireActivity()).signedInAccount.getId());
            navController.navigate(R.id.action_to_profile, bundle);
        });

        (view.findViewById(R.id.button4)).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("profileId", "example profile");
            navController.navigate(R.id.action_to_profile, bundle);
        });

        (view.findViewById(R.id.postExampleButton)).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("postId", "example post");
            navController.navigate(R.id.action_Home_to_FullscreenPost,bundle);
        });

        (view.findViewById(R.id.createPostButton)).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("boardId", "example board");
            navController.navigate(R.id.action_to_new_post, bundle);
        });

        (view.findViewById(R.id.tagViewExampleButton)).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("tagsOnPosts", "u/USerNAmmme");
            navController.navigate(R.id.action_to_tag_view, bundle);
        });
        (view.findViewById(R.id.boardViewExampleButton)).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("boardId", "example board");
            navController.navigate(R.id.action_to_board, bundle);
        });
        (view.findViewById(R.id.searchPage)).setOnClickListener(v -> {
            navController.navigate(R.id.action_to_search, null);
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}

