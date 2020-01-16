package com.sudoajay.pdf_viewer.customDialog

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import com.sudoajay.pdf_viewer.MainActivity
import com.sudoajay.pdf_viewer.R
import com.sudoajay.pdf_viewer.helperClass.FileSize.convertIt
import com.sudoajay.pdf_viewer.helperClass.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DialogInformationData(private val path: String, private val mainActivity: MainActivity) : DialogFragment(), View.OnClickListener {
    private var rootview: View? = null
    private var constraintLayout: ConstraintLayout? = null
    private var infoNameTextView: TextView? = null
    private var infoLocationTextView: TextView? = null
    private var infoSizeTextView: TextView? = null
    private var infoTypeTextView: TextView? = null
    private var infoExtTextView: TextView? = null
    private var infoCreatedTextView: TextView? = null
    private val activity: Activity
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootview = inflater.inflate(R.layout.layout_dialog_informationdata, container, false)
        reference()
        // setup dialog box
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        constraintLayout!!.setBackgroundColor(ContextCompat.getColor(context!!, R.color.tabBackgroundColor))
        // Fill Dialog Box
        fillIt()
        return rootview
    }

    private fun reference() { // Reference Object
        constraintLayout = rootview!!.findViewById(R.id.constraintLayout)
        infoNameTextView = rootview!!.findViewById(R.id.infoName_TextView)
        infoLocationTextView = rootview!!.findViewById(R.id.infoLocation_TextView)
        infoSizeTextView = rootview!!.findViewById(R.id.infoSize_TextView)
        infoTypeTextView = rootview!!.findViewById(R.id.infoType_TextView)
        infoExtTextView = rootview!!.findViewById(R.id.infoExt_TextView)
        infoCreatedTextView = rootview!!.findViewById(R.id.infoCreated_TextView)
        val closeImageView = rootview!!.findViewById<ImageView>(R.id.close_ImageView)
        closeImageView.setOnClickListener(this)
        val cancelButton = rootview!!.findViewById<Button>(R.id.cancel_Button)
        cancelButton.setOnClickListener(this)
        val openButton = rootview!!.findViewById<Button>(R.id.open_Button)
        openButton.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun fillIt() {
        val sdf = SimpleDateFormat("d MMM yyyy , h:mm a", Locale.getDefault())
        if (!path.startsWith("content:")) {
            val filePath = File(this.path)
            infoNameTextView!!.text = filePath.name
            val exactPath: String = path.replace(filePath.name, "")
            infoLocationTextView!!.text = exactPath
            infoSizeTextView!!.text = convertIt(filePath.length())
            if (filePath.isDirectory) {
                infoTypeTextView!!.text = "Folder"
            } else {
                infoTypeTextView!!.text = "application/pdf"
            }
            val fileName = filePath.name
            val i = fileName.lastIndexOf('.')
            val p = fileName.lastIndexOf('/').coerceAtLeast(fileName.lastIndexOf('\\'))
            if (i > p) {
                infoExtTextView!!.text = fileName.substring(i + 1)
            }
            infoCreatedTextView!!.text = sdf.format(filePath.lastModified())
        } else {
            val documentFile = DocumentFile.fromSingleUri(context!!, Uri.parse(path))
            infoNameTextView!!.text = documentFile!!.name
            val exactPath = FileUtils.replaceSdCardPath(context, FileUtils.getPath(context!!, Uri.parse(path)).toString()).replace(documentFile.name!!, "")
            infoLocationTextView!!.text = exactPath
            infoSizeTextView!!.text = convertIt(documentFile.length())
            if (documentFile.isDirectory) {
                infoTypeTextView!!.text = "Folder"
            } else {
                infoTypeTextView!!.text = "application/pdf"
            }
            val fileName = documentFile.name
            val i = fileName?.lastIndexOf('.')
            val p = fileName?.lastIndexOf('/')?.coerceAtLeast(fileName.lastIndexOf('\\'))
            if (i!! > p!!) {
                infoExtTextView!!.text = fileName.substring(i + 1)
            }
            infoCreatedTextView!!.text = sdf.format(documentFile.lastModified())
        }


    }

    override fun onStart() { // This MUST be called first! Otherwise the view tweaking will not be present in the displayed Dialog (most likely overriden)
        super.onStart()
        forceWrapContent(this.view)
    }

    private fun forceWrapContent(v: View?) { // Start with the provided view
        var current = v
        val dm = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        // Travel up the tree until fail, modifying the LayoutParams
        do { // Get the parent
            val parent = current!!.parent
            // Check if the parent exists
            if (parent != null) { // Get the view
                current = try {
                    parent as View
                } catch (e: ClassCastException) { // This will happen when at the top view, it cannot be cast to a View
                    break
                }
                // Modify the layout
                current.layoutParams.width = width - 10 * width / 100
            }
        } while (current!!.parent != null)
        // Request a layout to be re-done
        current!!.requestLayout()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.open_Button -> {
                MainActivity.MultiThreadingCopying(mainActivity).execute()
                dismiss()
            }
            R.id.cancel_Button, R.id.close_ImageView -> dismiss()
        }
    }
    init {
        activity = mainActivity
    }
}


