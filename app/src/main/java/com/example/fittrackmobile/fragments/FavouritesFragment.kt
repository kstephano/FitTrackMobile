package com.example.fittrackmobile.fragments


import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.example.fittrackmobile.adaptersClasses.FavouriteStatusTabAdapter

import com.example.fittrackmobile.R
import com.google.android.material.tabs.TabLayout

/**
 * A fragment to display favourite statuses of both text and images.
 */
class FavouritesFragment : Fragment() {

    // XML variables.
    private lateinit var parent: View
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    // Class variables.
    private val TAG = "Favourites"
    private lateinit var favouriteStatusTabAdapter: FavouriteStatusTabAdapter
    private lateinit var favouriteImageStatusFragment: FavouriteImageStatusFragment
    private lateinit var favouriteTextStatusFragment: FavouriteTextStatusFragment
    private var tabIcons = intArrayOf(R.drawable.ic_text_statuses, R.drawable.ic_image_statuses)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        parent = inflater.inflate(R.layout.fragment_favourites, container, false)
        initializeVariables()
        addFragmentsToTabLayout()

        return parent
    }

    /**
     * Set up the icons for each tab.
     */
    private fun setUpTabIcons() {
        try {
            tabLayout.getTabAt(0)?.setIcon(tabIcons[0])
            tabLayout.getTabAt(1)?.setIcon(tabIcons[1])

            // Set the default icon colour for the text status tab.
            tabLayout.getTabAt(0)?.icon?.setColorFilter(resources.getColor(
                R.color.colorWhite), PorterDuff.Mode.SRC_IN)


            // Set the listener for selected tabs in order to change icon colour when pressed.
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.icon?.setColorFilter(resources.getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab?.icon?.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Add the fragments for favourited text/image statuses to the tab adapter to pass to the layout.
     */
    private fun addFragmentsToTabLayout() {
        try {
            favouriteStatusTabAdapter = FavouriteStatusTabAdapter(childFragmentManager)
            favouriteStatusTabAdapter.addFragment(favouriteTextStatusFragment)
            favouriteStatusTabAdapter.addFragment(favouriteImageStatusFragment)

            viewPager.adapter = favouriteStatusTabAdapter
            tabLayout.setupWithViewPager(viewPager)
            viewPager.isSaveFromParentEnabled = false

            setUpTabIcons()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Initialize variables the fragments to be used, TabLayout, and ViewPager objects.
     */
    private fun initializeVariables() {
        try {
            favouriteTextStatusFragment = FavouriteTextStatusFragment()
            favouriteImageStatusFragment = FavouriteImageStatusFragment()

            tabLayout = parent.findViewById(R.id.frag_favourites_tabLayout)
            viewPager = parent.findViewById(R.id.frag_favourites_viewPager)
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }
}
