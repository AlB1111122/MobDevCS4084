import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class CommentFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.comment_fragment, container, false);

        // Retrieve the comment text view
        TextView commentTextView = view.findViewById(R.id.commentTextView);

        // Get the comment content and poster information from arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            String content = arguments.getString("content");
            String poster = arguments.getString("poster");
            // Display the comment content and poster information
            String commentText = "Poster: " + poster + "\nContent: " + content;
            commentTextView.setText(commentText);
        }

        // Retrieve the upvote and downvote buttons
        Button upvoteButton = view.findViewById(R.id.upvoteButton);
        Button downvoteButton = view.findViewById(R.id.downvoteButton);

        // Set click listeners for upvote and downvote buttons
        upvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call method to handle upvoting the comment
                handleUpvote();
            }
        });

        downvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call method to handle downvoting the comment
                handleDownvote();
            }
        });

        return view;
    }

    // Method to handle upvoting the comment
    private void handleUpvote() {
        // Call method from other Java classes to handle upvoting
    }

    // Method to handle downvoting the comment
    private void handleDownvote() {
        // Call method from other Java classes to handle downvoting
    }
}
