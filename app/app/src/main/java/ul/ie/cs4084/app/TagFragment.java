package ul.ie.cs4084.app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TagFragment extends Fragment {

    private static final String ARG_TAG = "tag";

    // TODO: Rename and change types of parameters
    private String tag;

    public TagFragment() {
        // Required empty public constructor
    }

    public static TagFragment newInstance(String tag) {
        TagFragment fragment = new TagFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tag = getArguments().getString(ARG_TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tag_, container, false);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.tagPostsFrag, TimelineFragment.class,null)
                .commit();
        return view;
    }
}