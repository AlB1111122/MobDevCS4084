package ul.ie.cs4084.app;


import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ul.ie.cs4084.app.dataClasses.Account;
import ul.ie.cs4084.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    public ExecutorService executorService;
    public NavController navController;
    public Account signedInAccount;
    public BottomNavigationView navView;

    private LocationReceiver locationReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(NUMBER_OF_CORES);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.navigation_popular);
//navigation bar
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if(itemId == R.id.navigation_search){
                navController.navigate(R.id.action_to_search, null);
                return true;
            }else if(itemId == R.id.navigation_popular){
                navController.navigate(R.id.action_to_timeline, null);
                return true;
            }else if(itemId == R.id.navigation_following){
                if(!signedInAccount.getFollowedTags().isEmpty()){
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("tagsOnPosts", signedInAccount.getFollowedTags());
                    navController.navigate(R.id.action_to_timeline, bundle);
                    return true;
                }else{
                    Toast toast = Toast.makeText(this, "You don't follow any tags!", Toast.LENGTH_SHORT);
                    toast.show();
                    return false;
                }
            }else if(itemId == R.id.navigation_profile){
                Bundle profileBundle = new Bundle();
                profileBundle.putString("profileId", signedInAccount.getId());
                navController.navigate(R.id.action_to_profile, profileBundle);
                return true;
            }
            return false;
        });

//back button
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navController.popBackStack();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        //Location broadcast reciver
        locationReceiver = new LocationReceiver();
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        this.registerReceiver(locationReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(locationReceiver);
    }
}