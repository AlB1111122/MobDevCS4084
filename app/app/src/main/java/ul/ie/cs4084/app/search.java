import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

// User class
class User {
    private String username;
    // Other user properties

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}

// Post class
class Post {
    private String title;
    private String content;
    // Other post properties

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}

// Board class
class Board {
    private String name;
    // Other board properties

    public Board(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

// Search class
class Search {
    private List<User> users;
    private List<Post> posts;
    private List<Board> boards;

    public Search() {
        // Initialize empty lists
        users = new ArrayList<>();
        posts = new ArrayList<>();
        boards = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    public void addBoard(Board board) {
        boards.add(board);
    }

    public List<User> searchUsers(String query) {
        List<User> result = new ArrayList<>();
        for (User user : users) {
            if (user.getUsername().contains(query)) {
                result.add(user);
            }
        }
        return result;
    }

    public List<Post> searchPosts(String query) {
        List<Post> result = new ArrayList<>();
        for (Post post : posts) {
            if (post.getTitle().contains(query) || post.getContent().contains(query)) {
                result.add(post);
            }
        }
        return result;
    }

    public List<Board> searchBoards(String query) {
        List<Board> result = new ArrayList<>();
        for (Board board : boards) {
            if (board.getName().contains(query)) {
                result.add(board);
            }
        }
        return result;
    }
}

public class search {
    public static void main(String[] args) {
        // Sample data
        Search searchEngine = new Search();
        searchEngine.addUser(new User("user1"));
        searchEngine.addUser(new User("user2"));

        searchEngine.addPost(new Post("Title 1", "Content 1"));
        searchEngine.addPost(new Post("Title 2", "Content 2"));

        searchEngine.addBoard(new Board("Board 1"));
        searchEngine.addBoard(new Board("Board 2"));

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create GUI
        JFrame frame = new JFrame("Search");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        JTextArea resultArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(resultArea);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = searchField.getText();
                StringBuilder searchResults = new StringBuilder();
                searchResults.append("Search results:\n");

                // Perform searches
                List<User> foundUsers = searchEngine.searchUsers(query);
                for (User user : foundUsers) {
                    searchResults.append("User: ").append(user.getUsername()).append("\n");
                }

                List<Post> foundPosts = searchEngine.searchPosts(query);
                for (Post post : foundPosts) {
                    searchResults.append("Post: ").append(post.getTitle()).append("\n");
                }

                List<Board> foundBoards = searchEngine.searchBoards(query);
                for (Board board : foundBoards) {
                    searchResults.append("Board: ").append(board.getName()).append("\n");
                }

                resultArea.setText(searchResults.toString());
            }
        });

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}
