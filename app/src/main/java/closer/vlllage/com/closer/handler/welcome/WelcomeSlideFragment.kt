package closer.vlllage.com.closer.handler.welcome

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.ScanQrCodeHandler
import closer.vlllage.com.closer.handler.map.SetNameHandler
import closer.vlllage.com.closer.handler.map.VerifyNumberHandler
import closer.vlllage.com.closer.handler.settings.ConfigHandler
import closer.vlllage.com.closer.pool.PoolFragment
import kotlinx.android.synthetic.main.activity_welcome.view.*

class WelcomeSlideFragment : PoolFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.scanInviteButton.setOnClickListener {
            on<ScanQrCodeHandler>().scan()
        }

        view.inviteLink.doOnTextChanged { text, _, before, count ->
            if (count > before) {
                val uri = Uri.parse(text.toString())

                on<ScanQrCodeHandler>().handleResult(uri)
            }
        }

        view.phoneNumberButton.setOnClickListener {
            on<VerifyNumberHandler>().verify()
        }

        view.requestInviteButton.setOnClickListener {
            if (on<AccountHandler>().name.isBlank()) {
                on<SetNameHandler>().modifyName({
                    on<GroupActivityTransitionHandler>().showGroupMessages(view.requestInviteButton, on<ConfigHandler>().feedbackGroupId())
                })
            } else {
                on<GroupActivityTransitionHandler>().showGroupMessages(view.requestInviteButton, on<ConfigHandler>().feedbackGroupId())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.activity_welcome, container, false)
    }
}
