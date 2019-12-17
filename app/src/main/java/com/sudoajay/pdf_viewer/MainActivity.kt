package com.sudoajay.pdf_viewer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.os.StrictMode.VmPolicy
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sudoajay.lodinganimation.LoadingAnimation
import com.sudoajay.pdf_viewer.customDialog.DialogInformationData
import com.sudoajay.pdf_viewer.databaseClasses.Database
import com.sudoajay.pdf_viewer.helperClass.CopyFile
import com.sudoajay.pdf_viewer.helperClass.CustomToast
import com.sudoajay.pdf_viewer.helperClass.DeleteCache
import com.sudoajay.pdf_viewer.helperClass.ScanPdf
import com.sudoajay.pdf_viewer.permission.AndroidExternalStoragePermission
import com.sudoajay.pdf_viewer.permission.AndroidSdCardPermission
import com.sudoajay.pdf_viewer.recyclerView.MyAdapter
import com.sudoajay.pdf_viewer.sdCard.SdCardPath
import com.sudoajay.pdf_viewer.sharedPreference.ExternalPathSharedPreference
import com.sudoajay.pdf_viewer.sharedPreference.SdCardPathSharedPreference
import com.sudoajay.pdf_viewer.webView.ShowWebView
import java.io.File
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(), OnRefreshListener, View.OnClickListener, SearchView.OnQueryTextListener {
    private var androidExternalStoragePermission: AndroidExternalStoragePermission? = null
    private var androidSdCardPermission: AndroidSdCardPermission? = null
    private var externalSharedPreferences: ExternalPathSharedPreference? = null
    private var sdCardPathSharedPreference: SdCardPathSharedPreference? = null
    private var refreshImageView: ImageView? = null
    private var swipeToRefresh: SwipeRefreshLayout? = null
    private var getPdfPath = ArrayList<String>()
    private var recyclerView: RecyclerView? = null
    private var mOnScrollChangedListener: OnScrollChangedListener? = null
    private val requestCode = 100
    private var path: String? = null
    private var mBottomSheetDialog: BottomSheetDialog? = null
    private var bottomSheetDialog1: BottomSheetDialog? = null
    private var loadingAnimation: LoadingAnimation? = null
    private var fileUri: Uri? = null
    private var toolbar: Toolbar? = null
    private var database: Database? = null
    private var mAdapter: MyAdapter? = null
    private val ratingLink = "https://play.google.com/store/apps/details?id=com.sudoajay.whatsapp_media_mover"
    private var sortDateOptionMenu: MenuItem? = null
    private var sortNameOptionMenu: MenuItem? = null
    private var sortSizeOptionMenu: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleIntent(intent)
        reference()

        //        Take Permission
        if (!androidExternalStoragePermission!!.isExternalStorageWritable) {
            mBottomSheetDialog!!.show()
        } else {
            MultiThreadingScanning().execute()
        }
    }

    private fun reference() {
        refreshImageView = findViewById(R.id.refresh_imageView)
        recyclerView = findViewById(R.id.recyclerView)
        swipeToRefresh = findViewById(R.id.swipeToRefresh)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        toolbar = findViewById(R.id.toolbar)

        swipeToRefresh?.setColorSchemeColors(ContextCompat.getColor(applicationContext, R.color.colorPrimary))


        setSupportActionBar(toolbar)
        swipeToRefresh?.setOnRefreshListener(this)
        // create object
        database = Database(applicationContext)
        androidExternalStoragePermission = AndroidExternalStoragePermission(this@MainActivity, this@MainActivity)
        androidSdCardPermission = AndroidSdCardPermission(applicationContext, this@MainActivity)
        externalSharedPreferences = ExternalPathSharedPreference(applicationContext)
        sdCardPathSharedPreference = SdCardPathSharedPreference(applicationContext)
        //         Select Option
        mBottomSheetDialog = BottomSheetDialog(this@MainActivity)
        @SuppressLint("InflateParams") val sheetView = layoutInflater.inflate(R.layout.layout_dialog_selectoption, null)
        mBottomSheetDialog!!.setContentView(sheetView)
        mBottomSheetDialog!!.setCancelable(false)
        val bottomSheetScanFile = sheetView.findViewById<LinearLayout>(R.id.bottomSheet_scanFile)
        bottomSheetScanFile.setOnClickListener(this)
        val bottomSheetSelectFile = sheetView.findViewById<LinearLayout>(R.id.bottomSheet_selectFile)
        bottomSheetSelectFile.setOnClickListener(this)
        //        More Option for file
        bottomSheetDialog1 = BottomSheetDialog(this@MainActivity)
        @SuppressLint("InflateParams") val sheetView1 = layoutInflater.inflate(R.layout.layout_dialog_moreoption, null)
        bottomSheetDialog1!!.setContentView(sheetView1)
        val bottomSheetOpenFile = sheetView1.findViewById<LinearLayout>(R.id.bottomSheet_openFile)
        bottomSheetOpenFile.setOnClickListener(this)
        val bottomSheetViewFolder = sheetView1.findViewById<LinearLayout>(R.id.bottomSheet_viewFolder)
        bottomSheetViewFolder.setOnClickListener(this)
        val bottomSheetMoreInfo = sheetView1.findViewById<LinearLayout>(R.id.bottomSheet_moreInfo)
        bottomSheetMoreInfo.setOnClickListener(this)

    }

    private fun fillItem() { // use this setting to improve performance if you know that changes
// in content do not change the layout size of the RecyclerView
        recyclerView!!.setHasFixedSize(true)
        // use a linear layout manager
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        // specify an adapter (see also next example)

        mAdapter = MyAdapter(this@MainActivity, ArrayList(getPdfPath))
        recyclerView!!.adapter = mAdapter
        recyclerView!!.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        val menuItem = menu.findItem(R.id.action_search)
        sortNameOptionMenu = menu.findItem(R.id.sort_name_optionMenu)
        sortDateOptionMenu = menu.findItem(R.id.sort_date_optionMenu)
        sortSizeOptionMenu = menu.findItem(R.id.sort_size_optionMenu)
        val searchView = menuItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchView.setOnQueryTextListener(this)
        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                saveToData()
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                mAdapter!!.transferItem(getPdfPath)
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle item selection
        when (item.itemId) {
            R.id.sort_name_optionMenu -> {
                sortingResult(1)
                item.isChecked = true
                mAdapter!!.transferItem(getPdfPath)
            }
            R.id.sort_date_optionMenu -> {
                sortingResult(2)
                item.isChecked = true
                mAdapter!!.transferItem(getPdfPath)
            }
            R.id.sort_size_optionMenu -> {
                sortingResult(3)
                item.isChecked = true
                mAdapter!!.transferItem(getPdfPath)
            }
            R.id.refresh_optionMenu -> refreshList()
            R.id.filePicker_optionMenu -> openFileManager()
            R.id.more_sendFeedback_optionMenu -> openEmail()
            R.id.more_rateUs_optionMenu -> rateUs()
            R.id.sort_shareApp_optionMenu -> shareIt()
            R.id.sort_aboutApp_optionMenu -> aboutApp()
            R.id.clearCache_optionMenu -> DeleteCache.deleteCache(applicationContext)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun sortingResult(type: Int) {
        saveToData()
        getPdfPath.clear()
        if (!database!!.isEmpty) {
            val cursor: Cursor? = when (type) {
                1 -> database!!.pathFromName
                2 -> database!!.lastModified
                else -> {
                    database!!.getpathFromSize()
                }
            }
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    getPdfPath.add(cursor.getString(0))
                } while (cursor.moveToNext())
            }
        }
    }

    private fun saveToData(){
        clearDataBaseItem()
        for (get in getPdfPath) {
            if (!get.startsWith("content:")) {
                val file = File(get)
                database!!.fill(file.name, get, file.length(), file.lastModified())
            } else {
                val documentFile = DocumentFile.fromSingleUri(applicationContext, Uri.parse(get))
                database!!.fill(documentFile!!.name, get, documentFile.length(), documentFile.lastModified())
            }
        }
    }
    fun onClickItem(view: View) {
        if (view.id == R.id.refresh_imageView) {
            if (refreshImageView != null && refreshImageView!!.rotation % 360 == 0f) {
                refreshImageView!!.animate().rotationBy(360f).duration = 1000
                refreshList()
            }
        }
    }

    fun openPdf(position: Int) {
        this.path = getPdfPath[position]
        // do whatever
        MultiThreadingCopying().execute()
    }

    fun openMoreOption(position: Int) { // do whatever
        this.path = getPdfPath[position]
        bottomSheetDialog1!!.show()
    }

    private fun shareIt() {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, "Link-Share")
        i.putExtra(Intent.EXTRA_TEXT, R.string.share_info.toString() + ratingLink)
        startActivity(Intent.createChooser(i, "Share via"))
    }

    private fun rateUs() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(ratingLink)
        startActivity(i)
    }

    private fun aboutApp() {
        val appLink = ""
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(appLink)
        startActivity(i)
    }

    private fun openEmail() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "sudoajay@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            intent.putExtra(Intent.EXTRA_TEXT, "")
            startActivity(intent)
        } catch (e: Exception) {
            CustomToast.toastIt(applicationContext, "There is no Email App")
        }
    }

    private fun refreshList() {
        clearDataBaseItem()
        MultiThreadingScanning().execute()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1) { // If request is cancelled, the result arrays are empty.
            if (!(grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED)) { // permission denied, boo! Disable the
// functionality that depends on this permission.
                CustomToast.toastIt(applicationContext, getString(R.string.giveUsPermission))
                mBottomSheetDialog!!.show()
                //                if (!androidExternalStoragePermission.isExternalStorageWritable())
//                    androidExternalStoragePermission.call_Thread();
            } else {
                MultiThreadingScanning().execute()
            }
            // other 'case' lines to check for other
// permissions this app might request
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // local variable
        super.onActivityResult(requestCode, resultCode, data)
        val sdCardPathURL: String?
        val stringURI: String
        val spiltPart: String?
        if (resultCode != Activity.RESULT_OK) return

        if (this.requestCode == requestCode && data != null) {
            fileUri = data.data
            MultiThreadingCopying().execute()
            return
        } else if (requestCode == 42 || requestCode == 58) {
            val sdCardURL: Uri? = data!!.data
            grantUriPermission(this@MainActivity.packageName, sdCardURL, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            this@MainActivity.contentResolver.takePersistableUriPermission(sdCardURL!!, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            sdCardPathURL = SdCardPath.getFullPathFromTreeUri(sdCardURL, this@MainActivity)
            stringURI = sdCardURL.toString()

            // Its supports till android 9 & api 28
            if (requestCode == 42) {
                 spiltPart = "%3A"
                sdCardPathSharedPreference!!.sdCardPath = spiltThePath(stringURI, sdCardPathURL)
                sdCardPathSharedPreference!!.stringURI = spiltUri(stringURI,spiltPart)
                if (!androidSdCardPermission!!.isSdStorageWritable) {
                    CustomToast.toastIt(applicationContext, resources.getString(R.string.errorMesSdCard))
                    return
                }

            } else {
                val realExternalPath = AndroidExternalStoragePermission.getExternalPath(applicationContext).toString()
                if (realExternalPath in sdCardPathURL.toString() + "/") {
                    spiltPart = "primary%3A"
                    externalSharedPreferences!!.externalPath = realExternalPath
                    externalSharedPreferences!!.stringURI = spiltUri(stringURI,spiltPart)
                } else {
                    CustomToast.toastIt(applicationContext, getString(R.string.errorMesExternal))
                    mBottomSheetDialog!!.show()
                    return
                }


            }
            // refresh when you get sd card path & External Path
            refreshList()
        } else {
            CustomToast.toastIt(applicationContext, getString(R.string.reportIt))
        }
    }

    private fun spiltUri(uri:String, spiltPart:String) :String {
        return uri.split(spiltPart)[0] + spiltPart
    }

    private fun spiltThePath(url: String, path: String?): String {
        val spilt = url.split("%3A").toTypedArray()
        val getPaths = spilt[0].split("/").toTypedArray()
        val paths = path!!.split(getPaths[getPaths.size - 1]).toTypedArray()
        return paths[0] + getPaths[getPaths.size - 1]+"/"

    }

    fun sendSdCardPermission() {
        if (!androidSdCardPermission!!.isSdStorageWritable) androidSdCardPermission!!.callThread()

    }

    @SuppressLint("Recycle")
    private fun queryName(resolver: ContentResolver, uri: Uri?): String {
        val returnCursor = resolver.query(uri!!, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    override fun onRefresh() {
        swipeToRefresh!!.isRefreshing = true
        Handler().postDelayed({
            refreshList()
            swipeToRefresh!!.isRefreshing = false
        }, 2000)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.bottomSheet_scanFile -> {
                if (!androidExternalStoragePermission!!.isExternalStorageWritable) androidExternalStoragePermission!!.callThread()
                mBottomSheetDialog!!.cancel()
            }
            R.id.bottomSheet_selectFile -> openFileManager()
            R.id.bottomSheet_openFile -> MultiThreadingCopying().execute()
            R.id.bottomSheet_viewFolder -> specificFolder()
            R.id.bottomSheet_moreInfo -> {
                dialogInformationData()
                bottomSheetDialog1!!.dismiss()
            }
        }
    }

    private fun openFileManager() {
        val intent = Intent()
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        // Set your required file type
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Pdf File"), requestCode)
    }

    private fun specificFolder() {
        val getPath: String? = if (!path!!.startsWith("content:")) {
            path?.replace("/" + File(path).name, "")
        } else {
            path
        }
        val selectedUri = Uri.parse(getPath)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(selectedUri, "resource/folder")
        if (intent.resolveActivityInfo(packageManager, 0) != null) {
            startActivity(intent)
        } else {
            CustomToast.toastIt(applicationContext, "No file explorer found")
        }
    }

    private fun handleIntent(intent: Intent) { //Kinda not recommended by google but whatever
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val appLinkData = intent.data
        val appLinkAction = intent.action
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            fileUri = appLinkData
            MultiThreadingCopying().execute()
        }
    }

    private fun dialogInformationData() {
        val ft = supportFragmentManager.beginTransaction()
        DialogInformationData(path.toString(), this@MainActivity).show(ft, "dialog")
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        mAdapter!!.filter.filter(query)
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        mAdapter!!.filter.filter(newText)
        return false
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}

    private fun clearDataBaseItem() {
        if (!database!!.isEmpty) database!!.deleteData()

    }


    @SuppressLint("StaticFieldLeak")
    inner class MultiThreadingScanning : AsyncTask<String?, String?, String?>() {
        private var scanPdf: ScanPdf? = null
        override fun onPreExecute() {
            loadingAnimation!!.start()
            getPdfPath.clear()
            // Empty If the database have something
            if (!database!!.isEmpty) database!!.deleteData()
            scanPdf = ScanPdf()
        }

        override fun doInBackground(vararg params: String?): String? {
            //             Its supports till android 9 & api 28
            if (Build.VERSION.SDK_INT <= 28) {
                scanPdf!!.scanFIle(this@MainActivity, AndroidExternalStoragePermission.getExternalPath(applicationContext), androidSdCardPermission!!.getSdCardPathURL())
            } else {
                scanPdf?.scanFile(this@MainActivity)
            }
            return null
        }

        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            loadingAnimation!!.stop()
            getPdfPath = scanPdf!!.pdfPath
            val type: Int = if (sortDateOptionMenu == null || sortNameOptionMenu!!.isChecked) 2 else if (sortSizeOptionMenu!!.isChecked) 1 else {
                3
            }
            sortingResult(type)
            fillItem()
        }
    }

    public override fun onStart() {
        super.onStart()
        swipeToRefresh!!.viewTreeObserver.addOnScrollChangedListener(OnScrollChangedListener { swipeToRefresh!!.isEnabled = recyclerView!!.scrollY == 0 }.also { mOnScrollChangedListener = it })
    }

    public override fun onStop() {
        swipeToRefresh!!.viewTreeObserver.removeOnScrollChangedListener(mOnScrollChangedListener)
        super.onStop()
    }

    @SuppressLint("StaticFieldLeak")
    inner class MultiThreadingCopying : AsyncTask<String?, String?, String?>() {
        var dst: File? = null
        override fun onPreExecute() {}
        override fun doInBackground(vararg params: String?): String? {
            try {
                if (path!!.startsWith("content:")) {
                    fileUri = Uri.parse(path)
                }
                if (fileUri == null && path != null) {
                    val src = File(path)
                    dst = File(cacheDir.toString() + "/" + src.name)
                    //                If file exist with same size
                    if (dst!!.exists()) dst!!.delete()
                    CopyFile.copy(src, dst)
                } else {
                    dst = File(cacheDir.toString() + "/" + queryName(contentResolver, fileUri))
                    if (dst!!.exists()) dst!!.delete()
                    CopyFile.copyUri(this@MainActivity, fileUri, dst)
                }
            } catch (ignored: Exception) {
            }
            return null
        }

        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            if (dst != null) {
                val intent = Intent(applicationContext, ShowWebView::class.java)
                intent.action = dst!!.absolutePath
                startActivity(intent)
            }
        }

    }


}



