package club.electro

data class MainViewState (
//    val appBarTitle: String? = null,
//    val appBarSubtitle: String? = null,
//    val appBarScroll: Boolean = false,
//    val appBarOnClick: () -> Unit = {}
    val toolBar: ToolBarConfig = ToolBarConfig(),
    val menuHeader: MenuHeaderConfig = MenuHeaderConfig()
)

data class ToolBarConfig (
    val title: String? = null,
    val subtitle: String? = null,
    val scroll: Boolean = false,
    val onClick: () -> Unit = {}
)

data class MenuHeaderConfig (
    val imageUrl: String? = null,
    val title: String? = null,
    val subTitle: String? = null
)