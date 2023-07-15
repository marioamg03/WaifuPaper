package ve.com.mariomendoza.waifupaper.main.ui.details

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ve.com.mariomendoza.waifupaper.BuildConfig
import ve.com.mariomendoza.waifupaper.R
import ve.com.mariomendoza.waifupaper.databinding.FragmentImageDetailsBinding
import ve.com.mariomendoza.waifupaper.dialogs.DialogLoading
import ve.com.mariomendoza.waifupaper.models.Post
import ve.com.mariomendoza.waifupaper.models.TagRequest
import ve.com.mariomendoza.waifupaper.utils.Vars
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit


class ImageDetailsFragment : Fragment() {

    private lateinit var binding:FragmentImageDetailsBinding

    private val TAG:String = "ImageDetailsFragment"

    private val imageDetailsViewModel: ImageDetailsViewModel by viewModels()

    lateinit var mAdView : AdView
    private lateinit var postArgs: Post
    private var progressDialog: DialogLoading? = null
    private var bmpUri: Uri? = null

    private var qualityImageShare = true

    private var mInterstitialAd: InterstitialAd? = null

    private var caseOption = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = requireArguments()
        postArgs = ImageDetailsFragmentArgs.fromBundle(args).modelArgs!!
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentImageDetailsBinding.inflate(inflater, container, false)

        val root: View = binding.root

        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        postponeEnterTransition(250, TimeUnit.MILLISECONDS)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navBar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navBar.clearAnimation()
        navBar.animate().translationY(navBar.height.toFloat()).duration = 500

        imageDetailsViewModel.sharePreferences.observe(requireActivity()) {
            qualityImageShare = it
        }

        val image = binding.imageView
        val author = binding.textView3

        val categories = binding.chipGroup

        val buttonShare = binding.buttonShare
        val backButton = binding.backButton
        val buttonSetWallpaper = binding.buttonSetWallpaper

        mAdView = binding.adView

        initAdsBanner()
        initAdsInterstitial()
        initInterstitialListeners()

        val url = BuildConfig.BASE_URL_IMG + postArgs.imagenHD

        val gson = Gson()
        val myType = object : TypeToken<List<String>>() {}.type
        val logs = gson.fromJson<List<String>>(postArgs.etiquetas, myType)

        Glide.with(this)
            .asBitmap()
            .load(url)
            .transform(GranularRoundedCorners(45F, 45F, 0F, 0F))
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    try {
                        image.setImageBitmap(resource)
                        val palette: Palette = Palette.from(resource).generate()

                        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                var vibrant = palette.lightVibrantSwatch

                                if (vibrant == null)
                                    vibrant = palette.lightMutedSwatch

                                if (vibrant != null) {
                                    // Update the title TextView with the proper text color
                                    author.setTextColor(vibrant.rgb)

                                    logs.forEach { log ->
                                        val chip = Chip(context)
                                        chip.text = log
                                        chip.isCloseIconVisible = false
                                        chip.setTextColor(vibrant!!.bodyTextColor)
                                        chip.chipBackgroundColor = ColorStateList.valueOf(vibrant!!.rgb)

                                        chip.setOnClickListener {
                                            navBar.clearAnimation()
                                            navBar.animate().translationY(0F).duration = 500
                                            val direction = ImageDetailsFragmentDirections.actionDetailFragmentToNavigationHome(log)
                                            findNavController().navigate(direction)
                                        }

                                        categories.addView(chip)
                                    }
                                    buttonShare.setColorFilter(vibrant.rgb, android.graphics.PorterDuff.Mode.SRC_IN)
                                    backButton.setColorFilter(vibrant.rgb, android.graphics.PorterDuff.Mode.SRC_IN)
                                    buttonSetWallpaper.setColorFilter(vibrant.rgb, android.graphics.PorterDuff.Mode.SRC_IN)
                                }
                            }
                            Configuration.UI_MODE_NIGHT_NO -> {
                                var  vibrant = palette.darkVibrantSwatch

                                if (vibrant == null)
                                    vibrant = palette.darkMutedSwatch

                                if (vibrant != null) {
                                    // Update the title TextView with the proper text color
                                    author.setTextColor(vibrant.rgb)

                                    logs.forEach { log ->
                                        val chip = Chip(context)
                                        chip.text = log
                                        chip.isCloseIconVisible = false
                                        chip.setTextColor(vibrant!!.bodyTextColor)
                                        chip.chipBackgroundColor = ColorStateList.valueOf(vibrant!!.rgb)

                                        chip.setOnClickListener {
                                            navBar.clearAnimation()
                                            navBar.animate().translationY(0F).duration = 500
                                            val direction = ImageDetailsFragmentDirections.actionDetailFragmentToNavigationHome(log)
                                            findNavController().navigate(direction)
                                        }

                                        categories.addView(chip)
                                    }
                                    buttonShare.setColorFilter(vibrant.rgb, android.graphics.PorterDuff.Mode.SRC_IN)
                                    backButton.setColorFilter(vibrant.rgb, android.graphics.PorterDuff.Mode.SRC_IN)
                                    buttonSetWallpaper.setColorFilter(vibrant.rgb, android.graphics.PorterDuff.Mode.SRC_IN)
                                }
                            }
                        }

                    } catch (e: Exception){

                    }

                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

        author.text = postArgs.autor

        image.transitionName = postArgs.imagenSD.toString()
        author.transitionName = postArgs.autor

        backButton.setOnClickListener{
            navBar.clearAnimation()
            navBar.animate().translationY(0F).duration = 500
            findNavController().navigateUp()
        }

        buttonSetWallpaper.setOnClickListener {
            Vars.ADS_CLICK +=1

            if (Vars.ADS_CLICK == 2) {

                caseOption = 2

                initInterstitialListeners()

                Vars.ADS_CLICK = 0
            } else {
                setWallpaper()
            }
        }
        buttonShare.setOnClickListener {

            Vars.ADS_CLICK +=1

            if (Vars.ADS_CLICK == 2) {

                caseOption = 1

                initInterstitialListeners()

                Vars.ADS_CLICK = 0
            } else {
                shareImage()
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navBar.clearAnimation()
            navBar.animate().translationY(0F).duration = 500
            findNavController().navigateUp()
        }
    }

    private fun setWallpaper() {
        showProgressBar(requireContext())

        val url = BuildConfig.BASE_URL_IMG + postArgs.imagenHD

        Glide.with(this)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {

                    try {

                        lifecycleScope.launch(Dispatchers.IO) {

                            if (bmpUri == null) {
                                bmpUri = getLocalBitmapUri(resource)
                            }

                            val intent = Intent().apply {
                                action = Intent.ACTION_ATTACH_DATA
                                addCategory(Intent.CATEGORY_DEFAULT)
                                type = "image/*"
                                setDataAndType(bmpUri, type)
                                putExtra("mimeType", type)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            lifecycleScope.launch(Dispatchers.Main) {
                                val shareIntent = Intent.createChooser(intent, "Compartiendo tu Waifu preferida...")
                                startActivity(shareIntent)
                                dismissProgressBar()
                            }

                        }
                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun shareImage () {

        showProgressBar(requireContext())

        val urlShow = BuildConfig.BASE_URL_IMG + postArgs.imagenSD
        var url = BuildConfig.BASE_URL_IMG + postArgs.imagenHD

        if (!qualityImageShare) {
            url = BuildConfig.BASE_URL_IMG + postArgs.imagenSD
        }

        Glide.with(this)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    try {

                        lifecycleScope.launch(Dispatchers.IO) {

                            if (bmpUri == null) {
                                bmpUri = getLocalBitmapUri(resource)
                            }

                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Para ver la imagen en calidad ULTRA ve al siguiente enlace... \n (\u2060ﾉ\u2060≧\u2060∇\u2060≦\u2060)\u2060ﾉ\u2060 \u2060ﾐ $urlShow"
                                )
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "Si tu pana te manda esto es porque quiere que veas una rica Waifu \uD83D\uDE0E"
                                )
                                putExtra(Intent.EXTRA_STREAM, bmpUri)

                                type = "image/*"
                            }

                            lifecycleScope.launch(Dispatchers.Main) {
                                val shareIntent = Intent.createChooser(intent, "Compartiendo tu Waifu preferida...")
                                startActivity(shareIntent)
                                dismissProgressBar()
                            }

                        }
                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun getLocalBitmapUri(imageView: Bitmap): Uri? {
        // Extract Bitmap from ImageView drawable
        val bmp: Bitmap? = imageView
        // Store image to default external storage directory
        var bmpUri: Uri? = null
        try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png"
            )
            file.parentFile?.mkdirs()
            val out = FileOutputStream(file)
            bmp?.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()

            bmpUri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }

    private fun initAdsBanner() {
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun initAdsInterstitial() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(requireContext(),"ca-app-pub-6670913552291405/7498629721", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mInterstitialAd = null
            }
        })
    }

    private fun initInterstitialListeners() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    Log.e(TAG, "Ad dismissed fullscreen content.")
                    mInterstitialAd = null

                    initAdsInterstitial()

                    when (caseOption) {
                        1 -> shareImage()
                        2 -> setWallpaper()
                    }
                }
                override fun onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.e(TAG, "Ad showed fullscreen content.")
                    mInterstitialAd = null
                }
            }
            mInterstitialAd?.show(requireActivity())
        }
    }

    private fun launchInterstitialAd() {
        mInterstitialAd?.show(requireActivity())
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