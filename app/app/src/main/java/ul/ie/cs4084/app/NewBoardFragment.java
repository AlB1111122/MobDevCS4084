package ul.ie.cs4084.app;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewBoardFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class NewBoardFragment extends Fragment {

    public static NewBoardFragment newInstance(String param1, String param2) {
        NewBoardFragment fragment = new NewBoardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public NewBoardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_board, container, false);

        // Inflate the layout for this fragment
        return view;
    }
}