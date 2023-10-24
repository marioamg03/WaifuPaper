package ve.com.mariomendoza.waifupaper.main.ui.upload

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mabbas007.tagsedittext.TagsEditText
import mabbas007.tagsedittext.TagsEditText.TagsEditListener
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ve.com.mariomendoza.waifupaper.R
import ve.com.mariomendoza.waifupaper.databinding.FragmentUploadBinding
import ve.com.mariomendoza.waifupaper.dialogs.DialogAlert
import ve.com.mariomendoza.waifupaper.dialogs.DialogLoading
import ve.com.mariomendoza.waifupaper.utils.FileUtil
import ve.com.mariomendoza.waifupaper.utils.RealPathUtil
import java.io.File
import java.io.IOException


class UploadFragment : Fragment() {

    private lateinit var authorEditText: EditText
    private lateinit var mTagsEditText: TagsEditText

    private lateinit var buttonClick: Button

    private lateinit var imageHD: ImageView

    private val tagsListener = TagsListener()

    private var listTags: List<String> = ArrayList()

    private val PICK_IMAGE_REQUEST_1 = 117

    private var filePathHD: Uri? = null
    private var filePathSD: Uri? = null

    private val uploadViewModel: UploadViewModel by viewModels()

    private var progressDialog: DialogLoading? = null

    private lateinit var binding: FragmentUploadBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentUploadBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mTagsEditText = binding.tagsEditText
        mTagsEditText.setTagsListener(tagsListener)

        authorEditText = binding.authorEditText

        imageHD = binding.imageViewHD
        imageHD.setOnClickListener {

            val permissions = permissions()
            var allPermissionsGranted = true

            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }

            if (allPermissionsGranted) {

                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_1)

            } else {
                ActivityCompat.requestPermissions(requireActivity(), permissions(), 117)
            }

        }

        buttonClick = binding.btnSave
        buttonClick.setOnClickListener {

            if (authorEditText.text.toString() == "") {
                Toast.makeText(requireContext(),"El autor no debe venir vacio",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (listTags.isEmpty()) {
                Toast.makeText(requireContext(),"Debe tener Minimo 1 Tag",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (filePathHD == null) {
                Toast.makeText(requireContext(),"Debes seleccionar una imagen HD",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            writeNewPostV2()
        }

        uploadViewModel.loading.observe(viewLifecycleOwner) {

            if (it != null) {
                if (it) {
                    showProgressBar(requireContext())
                } else {
                    dismissProgressBar()
                }
            }
        }

        uploadViewModel.clean.observe(viewLifecycleOwner) {
            if (it) {
                cleanFields()
                uploadViewModel.clean.postValue(false)
            }
        }

        uploadViewModel.alert.observe(viewLifecycleOwner) {
            if (it != null) {
                showDialogAlert(requireContext(), it)
                uploadViewModel.alert.postValue(null)
            }
        }

        return root
    }

    private fun writeNewPostV2() {

        val cleanListTags: MutableList<String> = ArrayList()
        listTags.forEach {
            cleanListTags.add(it.trim().lowercase())
        }

        val fileHD = RealPathUtil.getRealPath(requireContext(), filePathHD)
        val fileSD = RealPathUtil.getRealPath(requireContext(), filePathSD)

        val file1 = File(fileHD)
        val file2 = File(fileSD)

        val requestFileHD: RequestBody = file1.asRequestBody("image/jpg".toMediaType())
        val multipartImage = MultipartBody.Part.createFormData("urlImageHD", file1.name, requestFileHD)

        val requestFileSD: RequestBody = file2.asRequestBody("image/jpg".toMediaType())
        val multipartImage2 = MultipartBody.Part.createFormData("urlImageSD", file2.name, requestFileSD)

        val author: RequestBody = authorEditText.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val tags: RequestBody = Gson().toJson(cleanListTags).toRequestBody("multipart/form-data".toMediaTypeOrNull())

        uploadViewModel.sendData(author, tags, multipartImage, multipartImage2)

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

            showProgressBar(requireContext())

            filePathHD = data.data

            lifecycleScope.launch(Dispatchers.IO) {

                try {
                    val imageNameSD = "IMG_WPAPER_SD" + System.currentTimeMillis().toString() + ".jpg"
                    val picFileSD = File(requireContext().cacheDir, imageNameSD)

                    val picFileHD = FileUtil.from(requireContext(), filePathHD)

                    val compressedImageFile = Compressor.compress(requireContext(), picFileHD) {
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

    inner class TagsListener: TagsEditListener {
        override fun onTagsChanged(tags: MutableCollection<String>?) {
            if (tags != null) {
                listTags = tags.toList()
            }
        }

        override fun onEditingFinished() {
        }
    }

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private val REQUIRED_PERMISSIONS_33 = arrayOf(
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO
    )

    fun permissions(): Array<String> {
        // Verifica la versión de Android para utilizar los permisos correspondientes
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            REQUIRED_PERMISSIONS_33
        } else {
            REQUIRED_PERMISSIONS
        }
    }

}