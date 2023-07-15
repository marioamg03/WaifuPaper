package ve.com.mariomendoza.waifupaper.main.ui.home

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ve.com.mariomendoza.waifupaper.adapters.SpacesItemDecoration
import ve.com.mariomendoza.waifupaper.adapters.WaifusHomeAdapter
import ve.com.mariomendoza.waifupaper.databinding.FragmentHomeBinding
import ve.com.mariomendoza.waifupaper.dialogs.DialogLoading
import ve.com.mariomendoza.waifupaper.models.Post
import ve.com.mariomendoza.waifupaper.models.TagRequest

class HomeFragment : Fragment() {

    private lateinit var binding:FragmentHomeBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var waifusHomeAdapter: WaifusHomeAdapter

    private val TAG:String = "HomeFragment"

    private var progressDialog: DialogLoading? = null

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = HomeFragmentArgs.fromBundle(requireArguments())
        val log = args.log

        if (log != "") {
            val tag = TagRequest(log)
            homeViewModel.getAllPostByTag(tag)
            binding.txtSearch.setText(log)
        } else {
            homeViewModel.getAllPost()
        }

        binding.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No es necesario implementar este método
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Aquí se ejecuta la búsqueda cada vez que se escribe una letra
                val tags = TagRequest(s.toString().replace(" ",""))
                homeViewModel.search(tags)
            }

            override fun afterTextChanged(s: Editable?) {
                // No es necesario implementar este método
            }
        })

        recyclerView = binding.lstRefresh
        recyclerView.setHasFixedSize(true)

        homeViewModel.spanCount.observe(requireActivity()) { spanCount ->
            val sGrid = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
            sGrid.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

            recyclerView.layoutManager = sGrid
        }

        val decoration = SpacesItemDecoration(16)
        recyclerView.addItemDecoration(decoration)

        waifusHomeAdapter = WaifusHomeAdapter(mContext = requireContext(),
            false,
            addToFavoritesListener,
            deletePostToFavoriteListener,
            postItemListener
        )

        recyclerView.adapter = waifusHomeAdapter

        showProgressBar(requireContext())
        homeViewModel.mListPost.observe(viewLifecycleOwner) {
            waifusHomeAdapter.setData(it)
            dismissProgressBar()
        }

        // When user hits back button transition takes backward
        postponeEnterTransition()
        recyclerView.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    private val addToFavoritesListener = WaifusHomeAdapter.OnAddToFavoritesListener { post, position ->
        homeViewModel.addPostToFavorite(post)
        post.isLiked = true
    }

    private val deletePostToFavoriteListener = WaifusHomeAdapter.OnDeleteToFavoriteListener { post, position ->
        homeViewModel.deletePostToFavorite(post.id)
        post.isLiked = false
    }

    private val postItemListener = WaifusHomeAdapter.OnClickListener { post, imageView ->

        val direction: NavDirections = HomeFragmentDirections.listToDetailFragment(post)

        val extras = FragmentNavigatorExtras(
            imageView to post.imagenSD.toString()
        )

        findNavController().navigate(direction, extras)
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

}