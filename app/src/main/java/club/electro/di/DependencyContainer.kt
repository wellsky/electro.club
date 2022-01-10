package club.electro.di

class DependencyContainer constructor() {

    //@Inject lateinit var appAuth: AppAuth

    //@ApplicationContext lateinit var context: Context
    //val resources = context.resources
    //val accessToken = resources.getString(R.string.electro_club_access_token)

    //val appDb = AppDb.getInstance(context = context)

    //lateinit var apiService: ApiService = Api.service


    //val postDao = appDb.postDao()

    // val networkStatus = NetworkStatus.getInstance()

    //val postRepository: PostRepository = PostRepositoryServerImpl(this)
    //val userRepository: UserRepository = UserRepositoryServerImpl(this)

    //val workManager = WorkManager.getInstance(context)



//    init {
//        // TODO криво реализованная взаимная инъекция?
//        postRepository.setupWorkManager(workManager)
//    }

    companion object {
        @Volatile
        private var instance: DependencyContainer? = null

        fun getInstance(): DependencyContainer = synchronized(this) {
            instance ?: throw IllegalStateException(
                "DI is not initialized, you must call DI.initContainer(Context context) first."
            )
        }

        fun initContainer(): DependencyContainer = instance ?: synchronized(this) {
            instance ?: buildDI().also { instance = it }
        }

        private fun buildDI(): DependencyContainer = DependencyContainer()
    }
}