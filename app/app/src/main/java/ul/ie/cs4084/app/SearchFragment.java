package ul.ie.cs4084.app;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;


public class SearchFragment extends Fragment {
    private NavController navController;
    private ExecutorService executor;
    private Handler mainHandler;
    private RecyclerView requiredTags;
    private RecyclerView excludedTags;
    private ButtonAdapter rTagAdapter;
    private ButtonAdapter eTagAdapter;
    //flags
    private boolean renderFlag = false;
    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity act = (MainActivity) getActivity();
        assert act != null;
        executor = act.executorService;//DEF not how to do it firegure out later
        mainHandler = new Handler(Looper.getMainLooper());
        navController = NavHostFragment.findNavController(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set up recyclers
        if(!renderFlag){
            rTagAdapter = new ButtonAdapter(navController, true, requireContext());
            eTagAdapter = new ButtonAdapter(navController,true, requireContext());

            //show results
            view.findViewById(R.id.searchButton).setOnClickListener(clicked -> executor.execute(() ->{
                Bundle bundle = null;
                if(!rTagAdapter.getLocalDataSet().isEmpty()) {
                    bundle = new Bundle();
                    ArrayList<String> tags = new ArrayList<>(rTagAdapter.getLocalDataSet());
                    bundle.putStringArrayList("tagsOnPosts", tags);
                }
                if(!eTagAdapter.getLocalDataSet().isEmpty()) {
                    if(bundle==null){ bundle=new Bundle();}
                    ArrayList<String> exlTags = new ArrayList<>(eTagAdapter.getLocalDataSet());
                    bundle.putStringArrayList("excludeTags", exlTags);
                }
                String searchTerm = Objects.requireNonNull(((TextInputEditText) view.findViewById(R.id.search_bar)).getText()).toString();
                if(!searchTerm.isEmpty()) {
                    if(bundle==null){ bundle=new Bundle();}
                    bundle.putString("searchTerm", searchTerm);
                }
                FragmentManager fragmentManager = getChildFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.searchResFragment, TimelineFragment.newInstance(
                                bundle.getStringArrayList("tagsOnPosts"),
                                bundle.getStringArrayList("excludeTags"),
                                bundle.getString("searchTerm"))
                        ).commit();
                renderFlag = true;
            }));
        }

        view.findViewById(R.id.requireTagButton).setOnClickListener(clicked -> requireTagButton(getContext()));
        view.findViewById(R.id.excludeTagButton).setOnClickListener(clicked -> excludeTagButton(getContext()));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        requiredTags = view.findViewById(R.id.includeTags);
        requiredTags.setLayoutManager(layoutManager);
        requiredTags.setAdapter(rTagAdapter);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        excludedTags = view.findViewById(R.id.excludeTags);
        excludedTags.setLayoutManager(layoutManager2);
        excludedTags.setAdapter(eTagAdapter);
    }

    private void requireTagButton(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Require a tag");

            // Set up the input
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

            // Set up the buttons
        builder.setCancelable(true);
        builder.setPositiveButton("Require", (dialog, which) -> rTagAdapter.addButton(input.getText().toString()));

        builder.show();
    }

    private void excludeTagButton(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Exclude a tag");
            // Set up the input
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

            // Set up the buttons
        builder.setCancelable(true);
        builder.setPositiveButton("Exclude", (dialog, which) -> eTagAdapter.addButton(input.getText().toString()));

        builder.show();
    }
}