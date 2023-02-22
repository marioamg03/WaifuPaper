package ve.com.mariomendoza.waifupaper;

import android.app.Application;
import com.google.android.gms.ads.MobileAds
import com.google.android.material.color.DynamicColors

class WaifusPaper:Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
