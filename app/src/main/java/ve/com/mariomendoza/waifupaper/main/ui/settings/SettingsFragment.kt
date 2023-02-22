package ve.com.mariomendoza.waifupaper.main.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.switchmaterial.SwitchMaterial
import ve.com.mariomendoza.waifupaper.databinding.FragmentSettingsBinding
import java.io.File


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val TAG:String = "SettingsFragment"

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val switch: SwitchMaterial = binding.switch1
        val switch2: SwitchMaterial = binding.switch2

        val cacheAmount = binding.cacheAmount

        cacheAmount.text = String.format("Liberar %s de espacio", getMemoryValue(getCacheSize()))


        val imgCleanCache = binding.imgCleanCache

        imgCleanCache.setOnClickListener { v ->
            clearCacheData()
            cacheAmount.text = String.format("Liberar %s de espacio", getMemoryValue(getCacheSize()))
        }


        settingsViewModel.getColumns(switch)
        settingsViewModel.getSharePreferences(switch2)

        switch.setOnCheckedChangeListener { v, isChecked ->
            settingsViewModel.setColumns(isChecked)
        }

        switch2.setOnCheckedChangeListener { v, isChecked ->
            settingsViewModel.setSharePreferences(isChecked)
        }


        return root
    }


    private fun clearCacheData() {
        requireContext().cacheDir.deleteRecursively()
    }

    private fun getMemoryValue(cacheSize: Long): String {
        var value:Double = cacheSize.toDouble() / 1024
        value /= 1024

        return if (value > 1024) {
            value /= 1024
            String.format("%.2f GB", value)
        } else {
            String.format("%.2f MB", value)
        }
    }

    private fun getCacheSize(): Long {
        return requireContext().cacheDir.calculateSizeRecursively()
    }

    private fun File.calculateSizeRecursively(): Long {
        return walkBottomUp().fold(0L) { acc, file -> acc + file.length() }
    }

    fun deleteCache(context: Context) {
        try {
            val dir: File = context.cacheDir
            deleteDir(dir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }

}