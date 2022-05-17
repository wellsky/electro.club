package club.electro

import android.Manifest
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import club.electro.databinding.ActivityMainBinding
import club.electro.model.NetworkStatus
import club.electro.repository.thread.ThreadLoadTarget
import club.electro.ui.map.getDouble
import club.electro.ui.settings.SETTINGS_THEME_KEY
import club.electro.ui.thread.ThreadFragment.Companion.targetPostId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import club.electro.utils.AndroidUtils
import club.electro.utils.FixScrollingFooterBehavior
import club.electro.utils.loadCircleCrop
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity: AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels ()

    private lateinit var appBarConfiguration: AppBarConfiguration

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Этот список разделов будет в аппбаре отображать кнопку меню вместо кнопки "назад"
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_feed, R.id.nav_transport_list, R.id.nav_subscriptions, R.id.nav_map, R.id.nav_info, R.id.nav_settings
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

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.menu.findItem(R.id.nav_info).setOnMenuItemClickListener {
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

        viewModel.state.observe(this) { state ->
            with (state.menuHeader) {
                textLine1.text = title
                textLine2.text = subTitle
                if (imageUrl != null) {
                    headerImage.loadCircleCrop(imageUrl)
                } else {
                    headerImage.setImageResource(R.drawable.electro_club_icon_white_256)
                }
            }

            with (state.toolBar) {
                // https://developer.android.com/guide/fragments/appbar
                title?.let {
                    supportActionBar?.title = it
                }
                subtitle?.let {
                    supportActionBar?.subtitle = it
                }

                binding.appBarMain.toolbar.setOnClickListener {
                    //TODO или лучше сразу вызывать onClick, но тогда надо будет поменять параметры функции
                    onClick()
                }

                /**
                 * Утсановка скроллинга аппбара
                 * supportActionBar?.isHideOnContentScrollEnabled = true - почему-то выдает ошибку:
                 * Hide on content scroll is not supported in this action bar configuration.
                 * Даже если в начале onCreate вызвать supportRequestWindowFeature(FEATURE_ACTION_BAR_OVERLAY)
                 */
                if (scroll)
                    enableScrollingAppBar()
                else
                    disableScrollingAppBar()
            }

        }

        viewModel.requireLocationPermission.observe(this) { required ->
            println("request observed")
            if (required) {
                println("true")
                requestLocationPermissions()
            }
        }

        lifecycleScope.launch {
            // TODO как лучше запускать такие корутины, которые должны работать во время работы всего приложеия?
            viewModel.uploaderJob()
        }

        // TODO нормально ли инициализировать карту здесь или надо во фрагменте?
        MapKitFactory.initialize(this)

        //https://stackoverflow.com/questions/47495534/how-to-enable-night-mode-programmatically
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        AndroidUtils.setTheme(prefs.getString(SETTINGS_THEME_KEY, ""))
    }

    private fun enableScrollingAppBar() {
        val fragmentWrapperView = findViewById<ConstraintLayout>(R.id.contentMain)
        val param: CoordinatorLayout.LayoutParams = fragmentWrapperView.layoutParams as CoordinatorLayout.LayoutParams
        param.behavior = FixScrollingFooterBehavior()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams

        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL +
            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS

        toolbar.layoutParams = params
    }

    private fun disableScrollingAppBar() {
        val fragmentWrapperView = findViewById<ConstraintLayout>(R.id.contentMain)
        val param: CoordinatorLayout.LayoutParams = fragmentWrapperView.layoutParams as CoordinatorLayout.LayoutParams
        param.behavior = AppBarLayout.ScrollingViewBehavior()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams

        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL

        toolbar.layoutParams = params

        fragmentWrapperView.setPadding(
            fragmentWrapperView.paddingLeft,
            fragmentWrapperView.paddingTop,
            fragmentWrapperView.paddingRight,
            0
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun requestLocationPermissions() {
        println("request location permissions dialog")
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                viewModel.updateLocationPermissions()
            // Precise location access granted.
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            } else -> {
                // No location access granted.
            }
        }
    }
}