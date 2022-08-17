package ve.com.mariomendoza.waifupaper

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ve.com.mariomendoza.waifupaper.adapters.WaifusHomeAdapter
import ve.com.mariomendoza.waifupaper.models.Post

class Home : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var waifusHomeAdapter: WaifusHomeAdapter

    private val mDatabase = Firebase.firestore
    private val mReferencePosts = mDatabase.collection("posts")

    private val TAG:String = "HOME"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.lstRefresh)
        recyclerView.layoutManager = LinearLayoutManager(this)

        waifusHomeAdapter = WaifusHomeAdapter(this)
        recyclerView.adapter = waifusHomeAdapter



        mReferencePosts.get()
            .addOnSuccessListener { result ->

                val waifusList:List<Post> = result.toObjects(Post::class.java)
                waifusHomeAdapter.setData(waifusList)

                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }

    }

}