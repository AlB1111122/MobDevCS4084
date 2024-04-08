package ul.ie.cs4084.app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class BoardInfoFragment extends Fragment {
    private static final String ARG_DESC = "description";
    private static final String ARG_RULES = "rules";
    private static final String ARG_MODS = "moderators";
    private String description;
    private ArrayList<String> rules;
    private ArrayList<String> moderators;
    public BoardInfoFragment() {
        // Required empty public constructor
    }
    public static BoardInfoFragment newInstance(String description, ArrayList<String> rules, ArrayList<String> moderators) {
        BoardInfoFragment fragment = new BoardInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DESC, description);
        args.putStringArrayList(ARG_RULES, rules);
        args.putStringArrayList(ARG_MODS, moderators);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            description = getArguments().getString(ARG_DESC);
            rules = getArguments().getStringArrayList(ARG_RULES);
            moderators = getArguments().getStringArrayList(ARG_MODS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_board_info, container, false);
        ((TextView)view.findViewById(R.id.boardDesc)).setText(description);
        StringBuilder rulesStr = new StringBuilder();
        for (String rule: rules) {
            rulesStr.append("- ").append(rule).append("\n");
        }
        ((TextView)view.findViewById(R.id.rulesList)).setText(rulesStr);
        StringBuilder modsStr = new StringBuilder();
        for (String mod: moderators) {
            modsStr.append("- ").append(mod).append("\n");
        }
        ((TextView)view.findViewById(R.id.modsList)).setText(modsStr);
        return view;
    }
}