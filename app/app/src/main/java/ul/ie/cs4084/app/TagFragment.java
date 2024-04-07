package ul.ie.cs4084.app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class TagFragment extends Fragment {

    private static final String ARG_TAG = "tagsOnPosts";

    private static final String ARG_HIDE_NAME = "hideHashtagName";
    private String tag;
    private Boolean isFollowing;
    private Button followButton;
    private Boolean isBlocked;
    private Button blockButton;
    private FirebaseFirestore db;
    private Boolean hideHashtagName;
    public TagFragment() {
        // Required empty public constructor
    }

    public static TagFragment newInstance(String tag,Boolean hideHashtagName) {
        TagFragment fragment = new TagFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, tag);
        args.putBoolean(ARG_HIDE_NAME, hideHashtagName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tag = getArguments().getString(ARG_TAG);
            hideHashtagName = getArguments().getBoolean(ARG_HIDE_NAME);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tag_, container, false);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        isFollowing = ((MainActivity)requireActivity()).signedInAccount.retriveFollowedSet().contains(tag);
        isBlocked = ((MainActivity)requireActivity()).signedInAccount.retriveBlockedSet().contains(tag);
        followButton = view.findViewById(R.id.followTagButton);
        blockButton = view.findViewById(R.id.blockTagButton);

        if(isFollowing){
            followButton.setText(R.string.unfollow);
        }
        if(isBlocked){
            blockButton.setText(R.string.unblock);
        }
        if(!hideHashtagName){
            TextView nameView = ((TextView)view.findViewById(R.id.viewingTagName));
            String hashtagStr = getString(R.string.hashtag)+tag;
            nameView.setText(hashtagStr);
            nameView.setVisibility(View.VISIBLE);
        }
        followButton.setOnClickListener(v -> buttonFollow());
        blockButton.setOnClickListener(v -> buttonBlock());

        Bundle bundle = new Bundle();
        ArrayList<String> tags = new ArrayList<>();
        tags.add(tag);
        bundle.putStringArrayList(ARG_TAG, tags);
        fragmentManager.beginTransaction()
                .replace(R.id.tagPostsFrag, TimelineFragment.class,bundle)
                .commit();

        return view;
    }

    private void buttonFollow(){
        if(isFollowing) {
            ((MainActivity) requireActivity()).signedInAccount.unfollowTag(tag,db);
            followButton.setText(R.string.follow);
        }else{
            ((MainActivity) requireActivity()).signedInAccount.followTag(tag, db);
            followButton.setText(R.string.unfollow);
        }
    }

    private void buttonBlock(){
        if(isBlocked) {
            ((MainActivity) requireActivity()).signedInAccount.unBlockTag(tag,db);
            blockButton.setText(R.string.block);
        }else{
            ((MainActivity) requireActivity()).signedInAccount.blockTag(tag, db);
            blockButton.setText(R.string.unblock);
        }
    }
}