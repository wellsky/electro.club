package club.electro.ui.subscriptions

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import club.electro.R
import club.electro.databinding.FragmentSubscriptionsBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener


@AndroidEntryPoint
class SubscriptionsFragment : Fragment() {
    private val viewModel: SubscriptionsTabsViewModel by viewModels()

    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var subscriptionsTabAdapter: SubscriptionsTabAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscriptionsTabAdapter = SubscriptionsTabAdapter(this)
        viewPager = binding.viewPager
        viewPager.adapter = subscriptionsTabAdapter

        tabLayout = binding.tabLayout
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setCurrentTabPosition(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val position = viewModel.getCurrentTabPosition()

        viewPager.setCurrentItem(position, false)

        TabLayoutMediator(tabLayout, viewPager) { tab, newPosition ->
            tab.text = when (newPosition) {
                0 -> getString(R.string.subscriptions_tab_all)
                else -> getString(R.string.subscriptions_tab_my)
            }
        }.attach()

        tabLayout.selectTab(tabLayout.getTabAt(position))
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

        val fragment = SubscriptionsTabFragment()
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putInt(ARG_OBJECT, position)
        }

        return fragment
    }
}