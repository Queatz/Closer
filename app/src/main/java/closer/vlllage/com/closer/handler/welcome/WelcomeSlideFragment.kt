package closer.vlllage.com.closer.handler.welcome

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import closer.vlllage.com.closer.databinding.ActivityWelcomeBinding
import closer.vlllage.com.closer.handler.data.*
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.GroupDraftHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ScanQrCodeHandler
import closer.vlllage.com.closer.handler.map.SetNameHandler
import closer.vlllage.com.closer.handler.map.VerifyNumberHandler
import closer.vlllage.com.closer.handler.settings.ConfigHandler
import closer.vlllage.com.closer.pool.PoolFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class WelcomeSlideFragment : PoolFragment() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.scanInviteButton.setOnClickListener {
            on<ScanQrCodeHandler>().scan()
        }

        binding.inviteLink.doOnTextChanged { text, _, before, count ->
            if (count > before) {
                val uri = Uri.parse(text.toString())

                on<ScanQrCodeHandler>().handleResult(uri)
            }
        }

        binding.phoneNumberButton.setOnClickListener {
            on<VerifyNumberHandler>().verify()
        }

        binding.requestInviteButton.setOnClickListener {
            if (on<AccountHandler>().name.isBlank()) {
                on<SetNameHandler>().modifyName({
                    requestInvite(binding.requestInviteButton)
                })
            } else {
                requestInvite(binding.requestInviteButton)
            }
        }

        // Handle invite from another user
        // Current logic is: If user exists in any group that is not a direct group, the get acces
        on<RefreshHandler>().refreshMe { me ->
            on<ApiHandler>().getGroupContactsForPhone(me.id!!)
                .subscribe({
                    if (it.any { it.group?.direct != true }) {
                        on<PersistenceHandler>().access = true
                    }
                }, {}).also {
                    on<DisposableHandler>().add(it)
                }
        }
    }

    private fun requestInvite(view: View) {
        on<DataHandler>().getDirectGroup(on<ConfigHandler>().requestInvitePhoneId())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    on<GroupDraftHandler>().saveDraft(it.id!!, "Hey Jacob! May I have an invite?")
                    on<GroupActivityTransitionHandler>().showGroupMessages(binding.requestInviteButton, it.id!!, isRespond = true)
                }, {
                    on<DefaultAlerts>().thatDidntWork()
                }
                ).also {
                    on<DisposableHandler>().add(it)
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ActivityWelcomeBinding.inflate(inflater, container, false).let {
            binding = it
            it.root
        }
}
