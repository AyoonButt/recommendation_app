import android.content.Intent
import android.os.Bundle
import com.example.firedatabase_assis.R
import androidx.appcompat.app.AppCompatActivity
import com.example.firedatabase_assis.MediaDBHelper
import com.example.firedatabase_assis.databinding.ActivityHomepageBinding

class HomePage : AppCompatActivity() {
    private lateinit var binding : ActivityHomepageBinding;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load movie titles from the database
        val movieTitles = fetchMovieTitlesFromDatabase()

        // Populate TextView with movie titles
        binding.idTVAuthorName.text = movieTitles.joinToString(separator = "\n")

        // Setup bottom navigation bar
        binding.bottom_nav_bar.setOnItemReselectedListener { menuItem->
            // Handle bottom navigation item clicks
            when(menuItem.itemId) {
                R.id.bottom_menu_home->{
                    // Do nothing as we are already on the home page
                    true
                }
                R.id.bottom_menu_explore->{
                    // Navigate to the ExploreActivity
                    startActivity(Intent(this, ExploreActivity::class.java))
                    true
                }
                R.id.bottom_menu_communities->{
                    // Navigate to the CommunitiesActivity
                    startActivity(Intent(this, CommunitiesActivity::class.java))
                    true
                }
                R.id.bottom_menu_profile->{
                    // Navigate to the ProfileActivity
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.bottom_menu_settings->{
                    // Navigate to the SettingsActivity
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchMovieTitlesFromDatabase(): List<String> {
        val dbHelper = MediaDBHelper(context)
        val db = dbHelper.readableDatabase
        val movieTitles = mutableListOf<String>()

        db.close()
        return movieTitles
    }
}
