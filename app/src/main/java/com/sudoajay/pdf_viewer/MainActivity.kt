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
import com.sudoajay.pdf_viewer.helperClass.*
import com.sudoajay.pdf_viewer.permission.AndroidExternalStoragePermission
import com.sudoajay.pdf_viewer.permission.AndroidSdCardPermission
import com.sudoajay.pdf_viewer.recyclerView.MyAdapter
import com.sudoajay.pdf_viewer.sdCard.SdCardPath
import com.sudoajay.pdf_viewer.sharedPreference.ExternalPathSharedPreference
import com.sudoajay.pdf_viewer.sharedPreference.SdCardPathSharedPreference
import com.sudoajay.pdf_viewer.webView.ShowWebView
import java.io.File
import java.util.*

@SuppressLint("InflateParams", "Recycle")
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
    private var filePath: String? = null
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
            startScanningThread()
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

        val sheetView = layoutInflater.inflate(R.layout.layout_dialog_selectoption, null)
        mBottomSheetDialog!!.setContentView(sheetView)
        mBottomSheetDialog!!.setCancelable(false)
        val bottomSheetScanFile = sheetView.findViewById<LinearLayout>(R.id.bottomSheet_scanFile)
        bottomSheetScanFile.setOnClickListener(this)
        val bottomSheetSelectFile = sheetView.findViewById<LinearLayout>(R.id.bottomSheet_selectFile)
        bottomSheetSelectFile.setOnClickListener(this)
        //        More Option for file
        bottomSheetDialog1 = BottomSheetDialog(this@MainActivity)
        val sheetView1 = layoutInflater.inflate(R.layout.layout_dialog_moreoption, null)
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
            R.id.refresh_optionMenu -> clearDataBaseItem()
            R.id.filePicker_optionMenu -> openFileManager()
            R.id.more_sendFeedback_optionMenu -> openEmail()
            R.id.more_rateUs_optionMenu -> rateUs()
            R.id.sort_shareApp_optionMenu -> shareIt()
            R.id.sort_moreApp_optionMenu -> openMoreApp()
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
                    database!!.getPathFromSize()
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
                startScanningThread()
            }
        }
    }

    fun openPdf(position: Int) {
        this.filePath = getPdfPath[position]
        // do whatever
        startCopyingThread()
    }

    fun openMoreOption(position: Int) { // do whatever
        this.filePath = getPdfPath[position]
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

    private fun openMoreApp() {
        val link = "https://play.google.com/store/apps/dev?id=5309601131127361849"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(link)
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
                val externalPathSharedPreference = ExternalPathSharedPreference(applicationContext)
                externalPathSharedPreference.externalPath = AndroidExternalStoragePermission.getExternalPath(applicationContext)!!
                startScanningThread()
            }

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
            startCopyingThread()
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
                sdCardPathSharedPreference!!.sdCardPath = spiltThePath(stringURI, sdCardPathURL.toString())
                sdCardPathSharedPreference!!.stringURI = spiltUri(stringURI,spiltPart)
                if (!androidSdCardPermission!!.isSdStorageWritable) {
                    CustomToast.toastIt(applicationContext, resources.getString(R.string.wrongDirectorySelected))
                    return
                }

            } else {
                val realExternalPath = AndroidExternalStoragePermission.getExternalPath(applicationContext).toString()
                if (realExternalPath in sdCardPathURL.toString() + "/") {
                    spiltPart = "primary%3A"
                    externalSharedPreferences!!.externalPath = realExternalPath
                    externalSharedPreferences!!.stringURI = spiltUri(stringURI,spiltPart)
                } else {
                    CustomToast.toastIt(applicationContext, getString(R.string.wrongDirectorySelected))
                    mBottomSheetDialog!!.show()
                    return
                }


            }
            // refresh when you get sd card path & External Path
            startScanningThread()
        } else {
            CustomToast.toastIt(applicationContext, getString(R.string.reportIt))
        }
    }

    private fun spiltUri(uri:String, spiltPart:String) :String {
        return uri.split(spiltPart)[0] + spiltPart
    }

    private fun spiltThePath(url: String, path: String): String {
        val spilt = url.split("%3A").toTypedArray()
        val getPaths = spilt[0].split("/").toTypedArray()
        val paths = path.split(getPaths[getPaths.size - 1]).toTypedArray()
        return paths[0] + getPaths[getPaths.size - 1]+"/"

    }

    private fun startScanningThread() {
        MultiThreadingScanning(this@MainActivity).execute()
    }

    private fun startCopyingThread() {
        MultiThreadingCopying(this@MainActivity).execute()
    }

    fun sendSdCardPermission() {
        if (!androidSdCardPermission!!.isSdStorageWritable)
            androidSdCardPermission!!.callThread()
    }



    override fun onRefresh() {
        swipeToRefresh!!.isRefreshing = true
        Handler().postDelayed({
            startScanningThread()
            swipeToRefresh!!.isRefreshing = false
        }, 2000)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.bottomSheet_scanFile -> {
                if (!androidExternalStoragePermission!!.isExternalStorageWritable)
                    androidExternalStoragePermission!!.callThread()
                mBottomSheetDialog!!.cancel()
            }
            R.id.bottomSheet_selectFile -> openFileManager()
            R.id.bottomSheet_openFile -> startCopyingThread()
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
        val getPath: String? = if (!filePath!!.startsWith("content:")) {
            filePath?.replace("/" + File(filePath.toString()).name, "")
        } else {
            filePath
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
            startCopyingThread()
        }
    }

    private fun dialogInformationData() {
        val ft = supportFragmentManager.beginTransaction()
        DialogInformationData(filePath.toString(), this@MainActivity).show(ft, "dialog")
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

    public override fun onStart() {
        super.onStart()
        swipeToRefresh!!.viewTreeObserver.addOnScrollChangedListener(OnScrollChangedListener { swipeToRefresh!!.isEnabled = recyclerView!!.scrollY == 0 }.also { mOnScrollChangedListener = it })
    }

    public override fun onStop() {
        swipeToRefresh!!.viewTreeObserver.removeOnScrollChangedListener(mOnScrollChangedListener)
        super.onStop()
    }


    private class MultiThreadingScanning
    internal constructor(private val mainActivity: MainActivity) : AsyncTask<String?, String?, String?>() {
        private var scanPdf: ScanPdf = ScanPdf(mainActivity.applicationContext, mainActivity)
        override fun onPreExecute() {
            mainActivity.loadingAnimation!!.start()
            mainActivity.getPdfPath.clear()
            // Empty If the database have something
            mainActivity.clearDataBaseItem()
        }

        override fun doInBackground(vararg params: String?): String? {
            //             Its supports till android 9 & api 28
            if (Build.VERSION.SDK_INT <= 28) {
                scanPdf.scanUsingFile()
            } else {
                scanPdf.scanUsingDoc()
            }
            return null
        }

        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            mainActivity.loadingAnimation!!.stop()
            mainActivity.getPdfPath = scanPdf.pdfPath
            var type = 2
            if (mainActivity.sortNameOptionMenu != null) {
                type = when {
                    mainActivity.sortNameOptionMenu?.isChecked!! -> 1
                    mainActivity.sortSizeOptionMenu?.isChecked!! -> 3
                    else -> 2
                }
            }
            mainActivity.sortingResult(type)
            mainActivity.fillItem()
        }
    }


    class MultiThreadingCopying
    internal constructor(private val mainActivity: MainActivity) : AsyncTask<String?, String?, String?>() {
        var dst: File? = null
        override fun onPreExecute() {}
        override fun doInBackground(vararg params: String?): String? {
            try {
                if (!mainActivity.filePath.isNullOrEmpty() && mainActivity.filePath!!.startsWith("content:")) {
                    mainActivity.fileUri = Uri.parse(mainActivity.filePath)
                }
                if (mainActivity.fileUri == null) {
                    val src = File(mainActivity.filePath.toString())
                    dst = File(mainActivity.cacheDir.toString() + "/" + src.name)
                    //                If file exist with same size
                    if (dst!!.exists()) dst!!.delete()
                    CopyFile.copy(src, dst!!)
                } else {
                    val cache: String = mainActivity.cacheDir.absolutePath
                    val fileName: String = queryName(mainActivity.contentResolver, mainActivity.fileUri)
                    dst = File("""$cache/$fileName"""
                    )
                    if (Build.VERSION.SDK_INT <= 28) {
                        if (dst!!.exists()) dst!!.delete()
                    } else {
                        val documentHelperClass = DocumentHelperClass(mainActivity.applicationContext)
                        val documentFile = documentHelperClass.separatePath(cache)
                        if (documentFile.findFile(fileName)?.exists()!!) documentFile.findFile(fileName)!!.delete()
                    }


                    CopyFile.copyUri(mainActivity, mainActivity.fileUri!!, dst!!)
                }
            } catch (ignore: Exception) {
            }
            return null
        }

        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            val intent = Intent(mainActivity.applicationContext, ShowWebView::class.java)
            intent.action = dst!!.absolutePath
            mainActivity.startActivity(intent)
        }


        private fun queryName(resolver: ContentResolver, uri: Uri?): String {
            val returnCursor = resolver.query(uri!!, null, null, null, null)!!
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            return name
        }

    }


}



