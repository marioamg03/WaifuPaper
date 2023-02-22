package ve.com.mariomendoza.waifupaper.main.ui.favorites

import android.os.Bundle
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
import ve.com.mariomendoza.waifupaper.databinding.FragmentFavoritesBinding
import ve.com.mariomendoza.waifupaper.models.Post

class FavoritesFragment : Fragment() {

    private lateinit var binding: FragmentFavoritesBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var waifusHomeAdapter: WaifusHomeAdapter

    private val TAG:String = "HomeFragment"

    private val favoritesViewModel: FavoritesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.lstRefresh
        recyclerView.setHasFixedSize(true)

        favoritesViewModel.spanCount.observe(requireActivity()) { spanCount ->
            val sGrid = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
            sGrid.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

            recyclerView.layoutManager = sGrid
        }

        val decoration = SpacesItemDecoration(16)
        recyclerView.addItemDecoration(decoration)

        waifusHomeAdapter = WaifusHomeAdapter(mContext = requireContext(),
            true,
            addToFavoritesListener,
            deletePostToFavoriteListener,
            postItemListener
        )

        recyclerView.adapter = waifusHomeAdapter

        favoritesViewModel.getFavoritesPosts().observe(viewLifecycleOwner) {
            waifusHomeAdapter.setData(it)
        }

        // When user hits back button transition takes backward
        postponeEnterTransition()
        recyclerView.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    private val addToFavoritesListener = WaifusHomeAdapter.OnAddToFavoritesListener { post, position ->
        favoritesViewModel.addPostToFavorite(post)
        waifusHomeAdapter.notifyItemChanged(position)
    }

    private val deletePostToFavoriteListener = WaifusHomeAdapter.OnDeleteToFavoriteListener { post, position ->
        favoritesViewModel.deletePostToFavorite(post.id)
        waifusHomeAdapter.notifyItemChanged(position)
    }

    private val postItemListener = WaifusHomeAdapter.OnClickListener { post, imageView ->

        val direction: NavDirections = FavoritesFragmentDirections.listToDetailFragment(post)

        val extras = FragmentNavigatorExtras(
            imageView to post.imagenSD.toString()
        )

        findNavController().navigate(direction, extras)
    }

}