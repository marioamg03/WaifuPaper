package ve.com.mariomendoza.waifupaper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import mabbas007.tagsedittext.TagsEditText
import ve.com.mariomendoza.waifupaper.models.Post
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private val mDatabase = Firebase.firestore

    private val mReferencePosts = mDatabase.collection("posts")

    private var storageReference: StorageReference? = FirebaseStorage.getInstance().reference

    private lateinit var authorEditText: EditText
    private lateinit var mTagsEditText: TagsEditText

    private lateinit var buttonClick: Button
    private lateinit var btnGoToList: Button

    private lateinit var imageSD: ImageView
    private lateinit var imageHD: ImageView


    private val tagsListener = TagsListener()

    private var listTags: List<String> = ArrayList()

    private val PICK_IMAGE_REQUEST_1 = 117
    private val PICK_IMAGE_REQUEST_2 = 118

    private var filePathHD: Uri? = null
    private var filePathSD: Uri? = null


    private val TAG:String = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTagsEditText = findViewById(R.id.tagsEditText)
        mTagsEditText.setTagsListener(tagsListener)

        authorEditText = findViewById(R.id.authorEditText)

        imageHD = findViewById(R.id.imageViewHD)
        imageSD = findViewById(R.id.imageViewSD)

        imageHD.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_1)
        }

        imageSD.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_2)
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

            if (filePathHD == null || filePathSD == null) {
                Toast.makeText(this,"Debes seleccionar la imagen HD y SD",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            writeNewPost()
        }




        btnGoToList = findViewById(R.id.btnGoToList)
        btnGoToList.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
    }


    private fun writeNewPost() {

        val refHDLink = UUID.randomUUID().toString()
        val refHD = storageReference?.child("myImages/$refHDLink")
        val uploadTaskHD = refHD?.putFile(filePathHD!!)

        val refSDLink = UUID.randomUUID().toString()
        val refSD = storageReference?.child("myImages/$refSDLink")
        val uploadTaskSD = refSD?.putFile(filePathSD!!)

        val cleanListTags: MutableList<String> = ArrayList()
        listTags.forEach {
            cleanListTags.add(it.trim().lowercase())
        }

        val post = Post(authorEditText.text.toString(), cleanListTags, refHDLink, refSDLink,null)
        val postValues = post.toMap()

        mReferencePosts.add(postValues)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added with ID: ${it.id}")
                Toast.makeText(this,"Subida Exitosa!",Toast.LENGTH_SHORT).show()
                cleanFields()
            }
            .addOnFailureListener {
                Log.w(TAG, "Error adding document", it)
            }
    }

    private fun cleanFields() {
        authorEditText.setText("")

        mTagsEditText.setText("")
        listTags = emptyList()
        mTagsEditText.setTags(emptyArray())

        imageSD.setImageResource(R.drawable.add_image)
        filePathSD = null

        imageHD.setImageResource(R.drawable.add_image)
        filePathHD = null
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_1 && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePathHD = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePathHD)
                imageHD.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST_2 && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePathSD = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePathSD)
                imageSD.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
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



