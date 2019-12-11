package com.sudoajay.pdf_viewer.introSceen

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.sudoajay.pdf_viewer.MainActivity
import com.sudoajay.pdf_viewer.R
import com.sudoajay.pdf_viewer.sharedPreference.PrefManager
import com.sudoajay.pdf_viewer.splashScreen.Splash

@Suppress("DEPRECATION")
class WelcomeActivity : AppCompatActivity() {
    private var viewPager: ViewPager? = null
    private var dotsLayout: LinearLayout? = null
    private lateinit var layouts: IntArray
    private var btnSkip: Button? = null
    private var btnNext: Button? = null
    private var prefManager: PrefManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Checking for first time launch - before calling setContentView()
        prefManager = PrefManager(this)
        if (!prefManager!!.isFirstTimeLaunch) {
            launchHomeScreen()
            finish()
        }
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        setContentView(R.layout.activity_welcome)
        viewPager = findViewById(R.id.view_pager)
        dotsLayout = findViewById(R.id.layoutDots)
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = findViewById(R.id.btn_next)
        // layouts of all welcome sliders
// add few more layouts if you want
        layouts = intArrayOf(
                R.layout.welcome_slide1,
                R.layout.welcome_slide2,
                R.layout.welcome_slide3)
        // adding bottom dots
        addBottomDots(0)
        // making notification bar transparent
        changeStatusBarColor()
        val myViewPagerAdapter = MyViewPagerAdapter()
        viewPager?.adapter = myViewPagerAdapter
        viewPager?.addOnPageChangeListener(viewPagerPageChangeListener)
        btnSkip?.setOnClickListener { launchHomeScreen() }
        btnNext?.setOnClickListener {
            // checking for last page
// if last page home screen will be launched
            val current = item
            if (current < layouts.size) { // move to next screen
                viewPager?.currentItem = current
            } else {
                launchHomeScreen()
            }
        }
    }

    private fun addBottomDots(currentPage: Int) {
        val dots = arrayOfNulls<TextView>(layouts.size)
        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)
        dotsLayout!!.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]!!.text = fromHtml("&#8226;")
            dots[i]!!.textSize = 35f
            dots[i]!!.setTextColor(colorsInactive[currentPage])
            dotsLayout!!.addView(dots[i])
        }
        if (dots.isNotEmpty()) dots[currentPage]!!.setTextColor(colorsActive[currentPage])
    }

    private val item: Int
        get() = viewPager!!.currentItem + 1

    private fun launchHomeScreen() {
        if (intent.extras != null && intent.extras!!.getString("Url") != null) {
            val url = intent.extras!!.getString("Url")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } else {
            if (prefManager!!.isFirstTimeLaunch) {
                startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@WelcomeActivity, Splash::class.java))
            }
            prefManager!!.isFirstTimeLaunch = false
            finish()
        }
    }

    //	viewpager change listener
    private var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.size - 1) { // last page. make button text to GOT IT
                btnNext!!.text = getString(R.string.start_text)
                btnSkip!!.visibility = View.GONE
            } else { // still pages are left
                btnNext!!.text = getString(R.string.next_text)
                btnSkip!!.visibility = View.VISIBLE
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    /**
     * Making notification bar transparent
     */
    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    /**
     * View pager adapter
     */
    inner class MyViewPagerAdapter internal constructor() : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(layouts[position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }

    companion object {
        fun fromHtml(html: String?): Spanned {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(html)
            }
        }
    }
}