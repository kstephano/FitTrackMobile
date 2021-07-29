package com.example.fittrackmobile.adaptersClasses

import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class AddStatusesTabAdapter(@NonNull fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private var listFragment: ArrayList<Fragment> = ArrayList()
    private var listFragmentNames: ArrayList<String> = ArrayList()

    /**
     * Method to add a new fragment and fragment title to the respective array lists.
     * @param fragment The fragment to add to the tab adapter.
     * @param titleFragment The title of the fragment to be added to the adapter.
     */
    fun addFragment(fragment: Fragment, titleFragment: String) {
        listFragment.add(fragment)
        listFragmentNames.add(titleFragment)
    }

    /**
     * Method used to get and show the list fragment name for each fragment.
     * @param position The position corresponding to a tab button.
     * @return The fragment title for the given tab button.
     */
    override fun getPageTitle(position: Int): CharSequence? {
        return listFragmentNames.get(position)
    }

    /**
     * Method used to get the fragment after clicking the associated button.
     * @param position The position corresponding to a tab button.
     * @return The fragment for the given tab button.
     */
    override fun getItem(position: Int): Fragment {
        return listFragment.get(position)
    }

    /**
     * Get the size of the list fragment names array.
     * @return The number of fragments used by the adapter.
     */
    override fun getCount(): Int {
        return listFragmentNames.size
    }
}