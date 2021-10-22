package club.electro

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import club.electro.application.ElectroClubApp
import club.electro.databinding.ActivityMainBinding
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_feed, R.id.nav_subscriptions, R.id.nav_map
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)



        //val navHeader = binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.accountAvatar)
        val headerView = binding.navView.getHeaderView(0).findViewById<LinearLayout>(R.id.headerView)
        val headerImage = binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.accountAvatar)
        val textLine1 = binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textLine1)
        val textLine2 = binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textLine2)

        //findViewById<View>(R.id.navHeaderGroup)
        headerView.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            navController.navigate(
                R.id.action_global_nav_login
            )
        }

        val appAuth = (application as ElectroClubApp).diContainer.appAuth

        appAuth.authState.observe(this, {
            if (it.id != 0L) {
                Glide.with(headerImage.context)
                    .load(it.avatar)
                    .circleCrop()
                    .timeout(5_000)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .into(headerImage)

                textLine1.setText(it.name)
                textLine2.setText("My default transport")
            } else {
                headerImage.setImageResource(R.drawable.electro_club_icon)
                textLine1.setText(R.string.nav_header_title)
                textLine2.setText(R.string.nav_header_subtitle)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}