package ul.ie.cs4084.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import ul.ie.cs4084.app.dataClasses.Board;
import ul.ie.cs4084.app.dataClasses.Database;

public class BoardFragment extends Fragment {

    private static final String ARG_ID = "boardId";
    private String boardId;
    private ExecutorService executor;
    private Handler mainHandler;
    private String name;
    private CountDownLatch latch = new CountDownLatch(1);
    public BoardFragment() {
        // Required empty public constructor
    }

    public static BoardFragment newInstance(String boardId) {
        BoardFragment fragment = new BoardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, boardId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boardId = getArguments().getString(ARG_ID);
        }
        executor = ((MainActivity)requireActivity()).executorService;
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        // Inflate the layout for this fragment
        ViewPager2 pager = view.findViewById(R.id.pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference boardRef = db.collection("boards").document(boardId);
        boardRef.get().addOnCompleteListener(getBoardTask -> executor.execute(()->{
            if (getBoardTask.isSuccessful()) {
                DocumentSnapshot boardDoc = getBoardTask.getResult();
                //if yes populate local object
                if (boardDoc.exists()) {
                    Board b = new Board(boardDoc);
                    name = b.getName();
                    mainHandler.post(()->((TextView)view.findViewById(R.id.boardName)).append(name));
                    latch.countDown();
                    Database.displayPicture(b.getRelatedImageUrl(),view.findViewById(R.id.boardImage),executor,mainHandler,getResources());
                    /*
                    *
            String name,
            String description,
            String relatedImageUrl,
            ArrayList<String> rules,
            HashSet<DocumentReference> moderators,
            HashSet<String> tags*/

                }
            }
        }));
            executor.execute(() -> {
                try {
                    latch.await();
                    mainHandler.post(()->{
                        pager.setAdapter(new TabsAdapter(this, "b/"+name));
                        new TabLayoutMediator(tabLayout, pager,
                                (tab, position) -> tab.setText(position==0?"Posts":"Info")

                        ).attach();

                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        return view;
    }
}
