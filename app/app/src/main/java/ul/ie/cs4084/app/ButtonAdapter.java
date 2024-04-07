package ul.ie.cs4084.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ButtonAdapter extends RecyclerView.Adapter<ButtonAdapter.ViewHolder> {

    private ArrayList<String> localDataSet;
    private NavController navController;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final Button button;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            button = (Button) view.findViewById(R.id.tagButton);
        }
    }

    public ButtonAdapter(ArrayList<String> dataSet,NavController navController) {
        localDataSet = dataSet;
        this.navController = navController;
    }

    public ButtonAdapter(NavController navController) {
        localDataSet = new ArrayList<>();
        this.navController = navController;
    }

    public void addButton(String tag){
        localDataSet.add(tag);
        this.notifyItemInserted(this.getItemCount());
    }
    public void addButtons(ArrayList<String> tags){
        int start = this.getItemCount();
        int length = tags.size();
        localDataSet.addAll(tags);
        this.notifyItemRangeInserted(start,length);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_tag, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.button.append(localDataSet.get(position));
        viewHolder.button.setOnClickListener(v->{
            Bundle bundle = new Bundle();
            bundle.putString("tagsOnPosts", localDataSet.get(position));
            navController.navigate(R.id.action_to_tag_view, bundle);
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
