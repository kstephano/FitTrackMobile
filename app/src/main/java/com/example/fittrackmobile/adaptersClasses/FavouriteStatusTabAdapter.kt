package com.example.fittrackmobile.adaptersClasses

import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class FavouriteStatusTabAdapter(@NonNull fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private var listFragment: ArrayList<Fragment> = ArrayList()

    /**
     * Method to add a new fragment to the array list.
     * @param fragment The fragment to add to the tab adapter.
     */
    fun addFragment(fragment: Fragment) {
        listFragment.add(fragment)
    }

    /**
     * Method used to get the fragment after clicking the associated button.
     * @param position The position corresponding to a tab button.
     * @return The fragment for the given tab button.
     */
    override fun getItem(position: Int): Fragment {
        return listFragment[position]
    }

    /**
     * Get the size of the list fragment names array.
     * @return The number of fragments used by the adapter.
     */
    override fun getCount(): Int {
        return listFragment.size
    }
}