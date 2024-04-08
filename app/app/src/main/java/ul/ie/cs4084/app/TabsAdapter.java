package ul.ie.cs4084.app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashSet;

public class TabsAdapter extends FragmentStateAdapter {
    int noTabs = 2;
    TagFragment posts;
    BoardInfoFragment info;

    TabsAdapter(Fragment f, String name,String description, ArrayList<String> rules, ArrayList<String> mods) {
        super(f);
        posts = TagFragment.newInstance(name, true);
        info = BoardInfoFragment.newInstance(description, rules, mods);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return posts;
        } else {
            return info;
        }
    }

    @Override
    public int getItemCount() {
        return noTabs;
    }
}
