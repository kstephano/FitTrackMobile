package com.example.fittrackmobile.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager.widget.ViewPager
import com.example.fittrackmobile.adaptersClasses.AddStatusesTabAdapter
import com.example.fittrackmobile.fragments.AddImageStatusFragment
import com.example.fittrackmobile.fragments.AddTextStatusFragment
import com.example.fittrackmobile.R
import com.google.android.material.tabs.TabLayout

/**
 * Activity containing fragments allowing users to add text/image statuses.
 */
class AddStatusesActivity : AppCompatActivity() {

    // XML object variables.
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    // Class variables.
    private lateinit var addStatusesTabAdapter: AddStatusesTabAdapter
    private var tabIcons = intArrayOf(
        R.drawable.ic_text_statuses,
        R.drawable.ic_image_statuses
    )
    private val TAG: String = "AddStatusesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_statuses)

        attachJavaToXML()

        // Initialize the tab adapter for adding new text/image status fragments.
        addStatusesTabAdapter = AddStatusesTabAdapter(supportFragmentManager)
        val addTextStatusFragment = AddTextStatusFragment()
        val addImageStatusFragment = AddImageStatusFragment()
        addStatusesTabAdapter.addFragment(addTextStatusFragment, "Text")
        addStatusesTabAdapter.addFragment(addImageStatusFragment, "Image")
        viewPager.adapter = addStatusesTabAdapter
        tabLayout.setupWithViewPager(viewPager)

        setUpIcons()
    }

    /**
     * Set the icons for the add image/text status fragments.
     */
    private fun setUpIcons() {
        try {
            tabLayout.getTabAt(0)?.setIcon(tabIcons[0])
            tabLayout.getTabAt(1)?.setIcon(tabIcons[1])
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }


    /**
     * Attach Java/Kotlin objects to their XML counterparts.
     */
    private fun attachJavaToXML() {
        try {
            tabLayout = findViewById(R.id.AddStatuses_TabLayout)
            viewPager = findViewById(R.id.AddStatuses_ViewPager)

        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }
}