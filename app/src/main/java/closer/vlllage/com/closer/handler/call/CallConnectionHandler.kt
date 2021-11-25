package closer.vlllage.com.closer.handler.call

import android.content.Context
import android.media.AudioManager
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.NotificationHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import com.queatz.on.On
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.webrtc.*


class CallConnectionHandler constructor(private val on: On) {

    companion object {
        private const val LOCAL_VIDEO_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
        private const val LOCAL_AUDIO_TRACK_ID = "local_audio_track"
    }

    init {
        PeerConnectionFactory.initializeInternalTracer()
        PeerConnectionFactory.initializeFieldTrials("WebRTC-H264HighProfile/Enabled/")
    }

    val active = PublishSubject.create<Boolean>()

    private var callTimeoutDisposable: Disposable? = null
    private var otherPhoneId: String? = null
    private var audioManager: AudioManager? = null
    private val dispose = on<DisposableHandler>().group()

    private var wasMicrophoneMute: Boolean? = null
    private var wasSpeakerphoneOn: Boolean? = null

    private val iceServers = listOf(
            // todo host own STUN server
            PeerConnection.IceServer("stun:stun.l.google.com:19302"),

            // todo host own TURN server
            PeerConnection.IceServer("turn:numb.viagenie.ca", "webrtc@live.com", "muazkh")
    )

    private val rootEglBase: EglBase = EglBase.create()

    private val sdpObserver = object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {}

        override fun onSetFailure(message: String?) {
            displayError(message)
        }
        override fun onSetSuccess() {}
        override fun onCreateFailure(message: String?) {
            displayError(message)
        }
    }

    private val peerConnectionFactory = PeerConnectionFactory().apply {
        setOptions(PeerConnectionFactory.Options().apply {
            disableEncryption = true // todo ???
            disableNetworkMonitor = true
        })
    }

    private var peerConnection: PeerConnection? = null
    private var videoCapturer: VideoCapturer? = null

    fun attach(otherPhoneId: String, localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        this.otherPhoneId = otherPhoneId

        initSurfaceView(localView)
        initSurfaceView(remoteView)
        localView.setZOrderMediaOverlay(true)

        audioManager = on<ApplicationHandler>().app.getSystemService(Context.AUDIO_SERVICE) as AudioManager?

        wasMicrophoneMute = audioManager?.isMicrophoneMute
        wasSpeakerphoneOn = audioManager?.isSpeakerphoneOn

        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager?.isMicrophoneMute = false
        audioManager?.isSpeakerphoneOn = true

        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, MediaConstraints(), object : PeerConnection.Observer {
            override fun onIceCandidate(iceCandidate: IceCandidate) {
                peerConnection?.addIceCandidate(iceCandidate)
                send("connect", iceCandidate)
            }

            override fun onDataChannel(p0: DataChannel?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}

            override fun onAddStream(mediaStream: MediaStream?) {
                remoteView.let {
//                    mediaStream?.videoTracks?.getOrNull(0)?.addRenderer(it)
                    it.post { it.visible = true }
                }
            }

            override fun onSignalingChange(signalingState: PeerConnection.SignalingState?) {
                if (signalingState == PeerConnection.SignalingState.STABLE) {
                    callTimeoutDisposable?.dispose()
                    active.onNext(true)
                }
            }
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onRenegotiationNeeded() {}
        })!!

        startLocalVideoCapture(localView)
    }

    fun detach() {
        videoCapturer?.dispose()
        videoCapturer = null

        audioManager?.isMicrophoneMute = wasMicrophoneMute ?: false
        audioManager?.isSpeakerphoneOn = wasSpeakerphoneOn ?: false
        audioManager?.mode = AudioManager.MODE_NORMAL

        peerConnection?.dispose()
        peerConnection = null
    }

    fun call() {
        on<CallMqttHandler>().newCall {
            on<CallConnectionHandler>().sendPushNotification("start", StartCallEvent(it))
        }
    }

    fun answerIncomingCall() {
        ensure(on<CallMqttHandler>().ready) {
            peerConnection?.createOffer(object : SdpObserver by sdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onSetFailure(message: String?) {
                            displayError(message)
                            endCall()
                        }
                        override fun onSetSuccess() {
                            callTimeoutDisposable = on<TimerHandler>().postDisposable({
                                endCall()
                            }, 45000)
                        }
                        override fun onCreateSuccess(sessionDescription: SessionDescription?) {

                        }
                        override fun onCreateFailure(message: String?) {
                            displayError(message)
                            endCall()
                        }
                    }, sessionDescription)
                    send("offer", sessionDescription)
                }
            }, MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            })
        }
    }

    fun endCall() {
        sendPushNotification("end", EndCallEvent("Call ended"))
        onEnd()
    }

    fun endCall(phoneId: String) {
        sendPushNotification("end", EndCallEvent("${on<NameHandler>().getFallbackName(on<PersistenceHandler>().phone, on<AccountHandler>().name)} isn't available"), phoneId)
    }

    fun isInCall() = peerConnection?.signalingState() == PeerConnection.SignalingState.STABLE

    private fun displayError(message: String?) {
        on<TimerHandler>().post {
            on<DefaultAlerts>().thatDidntWork(message)
        }
    }

    private fun onRemoteSessionReceived(sessionDescription: SessionDescription, success: () -> Unit) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(message: String?) {
                displayError(message)
            }
            override fun onSetSuccess() {
                success()
            }
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(message: String?) {
                displayError(message)
            }
        }, sessionDescription)
    }

    private fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
//        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    private fun startLocalVideoCapture(localVideoOutput: SurfaceViewRenderer) {
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)

        videoCapturer = VideoCapturerAndroid.create(CameraEnumerationAndroid.getNameOfFrontFacingDevice())
        val localVideoSource = peerConnectionFactory.createVideoSource(videoCapturer, MediaConstraints())
//        videoCapturer.startCapture(720, 1280, 60)
        val localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_VIDEO_TRACK_ID, localVideoSource)
//        localVideoTrack.addRenderer(localVideoOutput)
        localStream?.addTrack(localVideoTrack)


        if (videoCapturer == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

//      val localScreencastVideoSource = peerConnectionFactory.createVideoSource(true) // todo screencast
        val localAudioSource = peerConnectionFactory.createAudioSource(MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "false"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "false"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "false"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "false"))
        })

        val localAudioTrack = peerConnectionFactory.createAudioTrack(LOCAL_AUDIO_TRACK_ID, localAudioSource)
        localAudioTrack.setEnabled(true)
        localStream?.addTrack(localAudioTrack)
        peerConnection?.addStream(localStream)
    }

    fun onStart(callEvent: CallEvent) {
        if (isInCall()) {
            endCall(callEvent.phone)
            return
        }

        on<JsonHandler>().from(callEvent.data, StartCallEvent::class.java).apply {
            on<CallMqttHandler>().switchCall(token) {}
        }

        // todo listen for started calls and launch activity from service
        // todo note: activity can be closed and opened anytime during the call
        on<TimerHandler>().post { on<CallHandler>().onReceiveCall(callEvent.phone, callEvent.phoneName) }
    }

    fun onOffer(callEvent: CallEvent) {
        onRemoteSessionReceived(on<JsonHandler>().from(callEvent.data, SessionDescription::class.java)) {
            peerConnection?.createAnswer(object : SdpObserver by sdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onSetFailure(message: String?) {
                            displayError(message)
                        }
                        override fun onSetSuccess() {
                        }
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(message: String?) {
                            displayError(message)
                        }
                    }, sessionDescription)
                    send("answer", sessionDescription)
                }
            }, MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            })
        }
    }

    fun onAnswer(callEvent: CallEvent) {
        onRemoteSessionReceived(on<JsonHandler>().from(callEvent.data, SessionDescription::class.java)) {}
    }

    fun onConnect(callEvent: CallEvent) {
        val iceCandidate = on<JsonHandler>().from(callEvent.data, IceCandidate::class.java)
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun onEnd(callEvent: CallEvent? = null) {
        dispose.clear()

        active.onNext(false)

        callEvent?.let {
            on<TimerHandler>().post {
                val endCallEvent = on<JsonHandler>().from(it.data, EndCallEvent::class.java)
                on<ToastHandler>().show(endCallEvent.reason)

                on<NotificationHandler>().hideFullScreen()
            }
        }

        on<CallMqttHandler>().endActiveCall()
    }

    private fun sendPushNotification(event: String, data: Any, phoneId: String? = null) {
        on<ApiHandler>().call(phoneId ?: otherPhoneId ?: return, event, on<JsonHandler>().to(data)).subscribe({}, {
            displayError(null)
        }).also { on<DisposableHandler>().add(it) }
    }

    private fun ensure(state: Observable<Boolean>, block: () -> Unit) = state.filter { it }
            .take(1)
            .subscribe { block() }
            .also { dispose.add(it) }

    private fun send(event: String, payload: Any) {
        ensure(on<CallMqttHandler>().ready) {
            on<CallMqttHandler>().send(event, payload)
        }
    }
}

data class StartCallEvent(
    val token: String
)

data class EndCallEvent(
    val reason: String,
)
