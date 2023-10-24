package ve.com.mariomendoza.waifupaper.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ve.com.mariomendoza.waifupaper.BuildConfig
import ve.com.mariomendoza.waifupaper.R
import ve.com.mariomendoza.waifupaper.databinding.ActivityMain2Binding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        navView = binding.navView

        supportActionBar?.hide()

        val navController = findNavController(R.id.nav_host_fragment_activity_main2)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        var appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )

        // Verifica si la aplicación está en modo debug
        if (BuildConfig.DEBUG) {
            // Agrega el botón de navegación "Upload" al conjunto de destinos
            appBarConfiguration = AppBarConfiguration.Builder(appBarConfiguration.topLevelDestinations + setOf(R.id.navigation_upload)).build()
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main2)
        navView.clearAnimation()
        navView.animate().translationY(0F).duration = 500

        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}