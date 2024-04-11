package ul.ie.cs4084.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
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
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Callback;

public class SearchFragment extends Fragment {
    NavController navController;
    ExecutorService executor;
    Handler mainHandler;
    RecyclerView requiredTags;
    RecyclerView excludedTags;
    ButtonAdapter rTagAdapter;
    ButtonAdapter eTagAdapter;
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        requiredTags = view.findViewById(R.id.includeTags);
        requiredTags.setLayoutManager(layoutManager);
        rTagAdapter = new ButtonAdapter(navController);
        requiredTags.setAdapter(rTagAdapter);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        excludedTags = view.findViewById(R.id.excludeTags);
        excludedTags.setLayoutManager(layoutManager2);
        eTagAdapter = new ButtonAdapter(navController);
        excludedTags.setAdapter(eTagAdapter);

        view.findViewById(R.id.requireTagButton).setOnClickListener(clicked -> requireTagButton(getContext()));
        view.findViewById(R.id.excludeTagButton).setOnClickListener(clicked -> excludeTagButton(getContext()));

        ChipGroup chipGroup = view.findViewById(R.id.searchChips);
        int accountChip = R.id.accountsChip;
        int postChip = R.id.postsChip;
        int boardChip = R.id.boardsChip;
        view.findViewById(R.id.button5).setOnClickListener(clicked ->{        executor.execute(() ->{
            //if(chipGroup.getCheckedChipId()==postChip){
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            Bundle bundle = null;
            if(!rTagAdapter.getLocalDataSet().isEmpty()) {
                bundle = new Bundle();
                ArrayList<String> tags = new ArrayList<>(rTagAdapter.getLocalDataSet());
                ArrayList<String> exlTags = new ArrayList<>(eTagAdapter.getLocalDataSet());
                bundle.putStringArrayList("tagsOnPosts", tags);
                bundle.putStringArrayList("excludeTags", exlTags);
            }
            if(!eTagAdapter.getLocalDataSet().isEmpty()) {
                if(bundle==null){ bundle=new Bundle();}
                ArrayList<String> exlTags = new ArrayList<>(eTagAdapter.getLocalDataSet());
                bundle.putStringArrayList("excludeTags", exlTags);
            }
            fragmentManager.beginTransaction()
                    .replace(R.id.searchResFragment, TimelineFragment.class,bundle)
                    .commit();
            mainHandler.post(()->{view.findViewById(R.id.searchResFragment).setVisibility(View.VISIBLE);});
            //}
        });
        });
        return view;
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
        builder.setTitle("Require a tag");
            // Set up the input
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

            // Set up the buttons
        builder.setCancelable(true);
        builder.setPositiveButton("Require", (dialog, which) -> eTagAdapter.addButton(input.getText().toString()));

        builder.show();
    }

    private void search(){

    }
}