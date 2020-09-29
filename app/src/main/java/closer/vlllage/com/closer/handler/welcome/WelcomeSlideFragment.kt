package closer.vlllage.com.closer.handler.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ScanQrCodeHandler
import closer.vlllage.com.closer.handler.map.VerifyNumberHandler
import closer.vlllage.com.closer.pool.PoolFragment
import kotlinx.android.synthetic.main.activity_welcome.view.*

class WelcomeSlideFragment : PoolFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.scanInviteButton.setOnClickListener {
            on<ScanQrCodeHandler>().scan()
        }
        view.inviteLink.doOnTextChanged { text, _, _, _ ->  }
        view.phoneNumberButton.setOnClickListener {
            on<VerifyNumberHandler>().verify()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.activity_welcome, container, false)
    }
}