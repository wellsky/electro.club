package club.electro.ui.subscriptions

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import club.electro.R
import club.electro.databinding.FragmentSubscriptionsBinding
import club.electro.repository.thread.ThreadLoadTarget
import club.electro.ui.thread.IncomingChangesStatus
import club.electro.ui.thread.ThreadFragment.Companion.targetPostId
import club.electro.ui.thread.ThreadFragment.Companion.threadId
import club.electro.ui.thread.ThreadFragment.Companion.threadType
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.tabs.TabLayout




@AndroidEntryPoint
class SubscriptionsFragment : Fragment() {
    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var subscriptionsTabAdapter: SubscriptionsTabAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscriptionsTabAdapter = SubscriptionsTabAdapter(this)
        viewPager = binding.viewPager
        tabLayout = binding.tabLayout
        viewPager.adapter = subscriptionsTabAdapter

        viewPager.setCurrentItem(1, false)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.subscriptions_tab_all)
                else -> getString(R.string.subscriptions_tab_my)
            }
        }.attach()

        tabLayout.selectTab(tabLayout.getTabAt(1))

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private const val ARG_OBJECT = "group"

class SubscriptionsTabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val group = position

        val fragment = SubscriptionsTabFragment()
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putInt(ARG_OBJECT, group)
        }
        return fragment
    }
}