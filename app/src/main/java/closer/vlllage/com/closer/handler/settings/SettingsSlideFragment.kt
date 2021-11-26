package closer.vlllage.com.closer.handler.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import closer.vlllage.com.closer.BuildConfig
import closer.vlllage.com.closer.MapsActivity
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.ActivitySettingsBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.featurerequests.FeatureRequestsHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.map.NetworkConnectionViewHandler
import closer.vlllage.com.closer.pool.PoolFragment
import closer.vlllage.com.closer.ui.Animate

class SettingsSlideFragment : PoolFragment() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ActivitySettingsBinding.inflate(inflater, container, false).let {
            binding = it
            it.root
        }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}
