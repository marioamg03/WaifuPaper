package ve.com.mariomendoza.waifupaper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mabbas007.tagsedittext.TagsEditText
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ve.com.mariomendoza.waifupaper.dialogs.DialogAlert
import ve.com.mariomendoza.waifupaper.dialogs.DialogLoading
import ve.com.mariomendoza.waifupaper.main.MainActivity
import ve.com.mariomendoza.waifupaper.utils.FileUtil
import ve.com.mariomendoza.waifupaper.utils.RealPathUtil
import java.io.File
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var authorEditText: EditText
    private lateinit var mTagsEditText: TagsEditText

    private lateinit var buttonClick: Button
    private lateinit var btnGoToList: Button

    private lateinit var imageHD: ImageView

    private val tagsListener = TagsListener()

    private var listTags: List<String> = ArrayList()

    private val PICK_IMAGE_REQUEST_1 = 117

    private var filePathHD: Uri? = null
    private var filePathSD: Uri? = null

    private val mainViewModel: MainViewModel by viewModels()

    private var progressDialog: DialogLoading? = null

    private val TAG:String = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTagsEditText = findViewById(R.id.tagsEditText)
        mTagsEditText.setTagsListener(tagsListener)

        authorEditText = findViewById(R.id.authorEditText)

        imageHD = findViewById(R.id.imageViewHD)
        imageHD.setOnClickListener {

            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if (permission == PackageManager.PERMISSION_GRANTED) {

                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_1)

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 117)
            }

        }

        buttonClick = findViewById(R.id.btnSave)
        buttonClick.setOnClickListener {

            if (authorEditText.text.toString() == "") {
                Toast.makeText(this,"El autor no debe venir vacio",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (listTags.isEmpty()) {
                Toast.makeText(this,"Debe tener Minimo 1 Tag",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (filePathHD == null) {
                Toast.makeText(this,"Debes seleccionar una imagen HD",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            writeNewPostV2()
        }

        mainViewModel.loading.observe(this) {

            if (it != null) {
                if (it) {
                    showProgressBar(this)
                } else {
                    dismissProgressBar()
                }
            }
        }

        mainViewModel.clean.observe(this) {
            if (it) {
                cleanFields()
                mainViewModel.clean.postValue(false)
            }
        }

        mainViewModel.alert.observe(this) {
            if (it != null) {
                showDialogAlert(this, it)
                mainViewModel.alert.postValue(null)
            }
        }


        btnGoToList = findViewById(R.id.btnGoToList)
        btnGoToList.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun writeNewPostV2() {

        val cleanListTags: MutableList<String> = ArrayList()
        listTags.forEach {
            cleanListTags.add(it.trim().lowercase())
        }

        val fileHD = RealPathUtil.getRealPath(this@MainActivity, filePathHD)
        val fileSD = RealPathUtil.getRealPath(this@MainActivity, filePathSD)

        val file1 = File(fileHD)
        val file2 = File(fileSD)

        val requestFileHD: RequestBody = file1.asRequestBody("image/jpg".toMediaType())
        val multipartImage = MultipartBody.Part.createFormData("urlImageHD", file1.name, requestFileHD)

        val requestFileSD: RequestBody = file2.asRequestBody("image/jpg".toMediaType())
        val multipartImage2 = MultipartBody.Part.createFormData("urlImageSD", file2.name, requestFileSD)

        val author: RequestBody = authorEditText.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val tags: RequestBody = Gson().toJson(cleanListTags).toRequestBody("multipart/form-data".toMediaTypeOrNull())

        mainViewModel.sendData(author, tags, multipartImage, multipartImage2)

    }

    private fun showDialogAlert(context: Context?, messsage: String?) {
        val dialogAlertRoot = DialogAlert(context)
        dialogAlertRoot.setDialogMessage(messsage)
        dialogAlertRoot.show()
    }

    private fun showProgressBar(context: Context?) {
        if (progressDialog == null) {
            progressDialog = DialogLoading(context)
        }
        progressDialog!!.show()
    }

    private fun dismissProgressBar() {
        progressDialog!!.dismiss()
    }

    private fun cleanFields() {


        imageHD.setImageResource(R.drawable.add_image)
        filePathHD = null
        filePathSD = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_1 && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            showProgressBar(this)

            filePathHD = data.data

            lifecycleScope.launch(Dispatchers.IO) {

                try {
                    val imageNameSD = "IMG_WPAPER_SD" + System.currentTimeMillis().toString() + ".jpg"
                    val picFileSD = File(applicationContext.cacheDir, imageNameSD)

                    val picFileHD = FileUtil.from(this@MainActivity, filePathHD)

                    val compressedImageFile = Compressor.compress(this@MainActivity, picFileHD) {
                        default()
                        destination(picFileSD)
                    }

                    filePathSD = picFileSD.toUri()

                    lifecycleScope.launch(Dispatchers.Main) {
                        imageHD.setImageURI(filePathSD)
                        dismissProgressBar()
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    inner class TagsListener: TagsEditText.TagsEditListener {
        override fun onTagsChanged(tags: MutableCollection<String>?) {
            if (tags != null) {
                listTags = tags.toList()
            }
        }

        override fun onEditingFinished() {
        }
    }
}



