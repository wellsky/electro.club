package club.electro

import android.os.Bundle
import android.view.MenuItem
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
import androidx.lifecycle.lifecycleScope
import club.electro.databinding.ActivityMainBinding
import club.electro.model.NetworkStatus
import club.electro.repository.thread.ThreadLoadTarget
import club.electro.ui.thread.ThreadFragment.Companion.targetPostId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.utils.loadCircleCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels ()

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_feed, R.id.nav_transport_list, R.id.nav_subscriptions, R.id.nav_map, R.id.nav_info
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val headerView = binding.navView.getHeaderView(0).findViewById<LinearLayout>(R.id.headerView)
        val headerImage = binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.accountAvatar)
        val textLine1 = binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textLine1)
        val textLine2 = binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textLine2)

        headerView.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            navController.navigate(
                R.id.action_global_nav_login
            )
        }

        val navigationView: NavigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.menu!!.findItem(R.id.nav_info).setOnMenuItemClickListener { menuItem: MenuItem? ->
            //write your implementation here
            //to close the navigation drawer
            navController.navigate(
                R.id.action_global_threadFragment,
                Bundle().apply {
                    threadType = 2
                    threadId = 8510
                    targetPostId = ThreadLoadTarget.TARGET_POSITION_FIRST
                }
            )

            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            true
        }

        viewModel.appAuth.authState.observe(this, { authState ->
            if (authState.authorized) {
                headerImage.loadCircleCrop(authState.avatar)

                textLine1.text = authState.name
                authState.transportName?.let {
                    textLine2.text = it
                } ?: run { textLine2.text = getString(R.string.transport_not_set) }
            } else {
                headerImage.setImageResource(R.drawable.electro_club_icon_white_256)
                textLine1.text = getString(R.string.nav_header_title)
                textLine2.text = getString(R.string.nav_header_subtitle)
            }
        })


        // https://developer.android.com/guide/fragments/appbar
        viewModel.config.observe(this, { config ->
            config.title?.let {
                supportActionBar?.title = it
            }
            config.subtitle?.let {
                supportActionBar?.subtitle = it
            }

            binding.appBarMain.toolbar.setOnClickListener {
                //TODO или лучше сразу вызывать onClick, но тогда надо будет поменять параметры функции
                config.onClick()
            }
        })

        CoroutineScope(Dispatchers.Default).launch {
            viewModel.networkStatus.status.collectLatest {
                val statusString = when (it) {
                    NetworkStatus.Status.ONLINE -> getString(R.string.network_status_online)
                    NetworkStatus.Status.OFFLINE -> getString(R.string.network_status_offline)
                    NetworkStatus.Status.ERROR -> getString(R.string.network_status_error)
                }

                Snackbar.make(binding.root, statusString, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        lifecycleScope.launch {
            // TODO как лучше запускать такие корутины, которые должны работать во время работы всего приложеия?
            viewModel.uploaderJob()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}