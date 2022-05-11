package club.electro

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
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


//
//        viewModel.appAuth.authState.observe(this) { authState ->
//            if (authState.authorized) {
//                headerImage.loadCircleCrop(authState.avatar)
//
//                textLine1.text = authState.name
//                authState.transportName?.let {
//                    textLine2.text = it
//                } ?: run { textLine2.text = getString(R.string.transport_not_set) }
//            } else {
//                headerImage.setImageResource(R.drawable.electro_club_icon_white_256)
//                textLine1.text = getString(R.string.nav_header_title)
//                textLine2.text = getString(R.string.nav_header_subtitle)
//            }
//        }


//        // https://developer.android.com/guide/fragments/appbar
//        viewModel.toolBarConfig.observe(this) { config ->
//            config.title?.let {
//                supportActionBar?.title = it
//            }
//            config.subtitle?.let {
//                supportActionBar?.subtitle = it
//            }
//
//            binding.appBarMain.toolbar.setOnClickListener {
//                config.onClick()
//            }
//
//            /**
//             * Утсановка скроллинга аппбара
//             * supportActionBar?.isHideOnContentScrollEnabled = true - почему-то выдает ошибку:
//             * Hide on content scroll is not supported in this action bar configuration.
//             * Даже если в начале onCreate вызвать supportRequestWindowFeature(FEATURE_ACTION_BAR_OVERLAY)
//            */
//            if (config.scroll)
//                enableScrollingAppBar()
//            else
//                disableScrollingAppBar()
//
//        }

//        CoroutineScope(Dispatchers.Default).launch {
//            viewModel.networkStatus.status.collectLatest {
//                val statusString = when (it) {
//                    NetworkStatus.Status.ONLINE -> getString(R.string.network_status_online)
//                    NetworkStatus.Status.OFFLINE -> getString(R.string.network_status_offline)
//                    NetworkStatus.Status.ERROR -> getString(R.string.network_status_error)
//                }
//
//                Snackbar.make(binding.root, statusString, Snackbar.LENGTH_LONG)
//                    .show()
//            }
//        }

        lifecycleScope.launch {
            // TODO как лучше запускать такие корутины, которые должны работать во время работы всего приложеия?
            viewModel.uploaderJob()
        }

        // TODO нормально ли инициализировать карту здесь или надо во фрагменте?
        MapKitFactory.initialize(this)

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

//    override fun getTheme(): Resources.Theme {
//        // Смениа стандартных светлой/темной темы:
//        //https://stackoverflow.com/questions/47495534/how-to-enable-night-mode-programmatically
//
//        // Назначение кастомных тем:
//        // https://stackoverflow.com/questions/11562051/change-activitys-theme-programmatically
//
//        val theme: Resources.Theme = super.getTheme()
//        if (true) {
//            theme.applyStyle(R.style.Theme_Electroclub, true)
//            theme.applyStyle(R.style.Theme_Electroclub, true)
//        }
//
//        // you could also use a switch if you have many themes that could apply
//
//        return theme
//    }
}