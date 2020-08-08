package closer.vlllage.com.closer.handler.call

import android.app.Application
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.*
import com.queatz.on.On
import org.webrtc.*

class CallConnectionHandler constructor(private val on: On) {

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
    }

    init {
        initPeerConnectionFactory(on<ApplicationHandler>().app)
    }

    private lateinit var otherPhoneId: String
    private var localView: SurfaceViewRenderer? = null
    private var remoteView: SurfaceViewRenderer? = null

    private val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer()
    )

    private val rootEglBase: EglBase = EglBase.create()

    private var remoteMediaStream: MediaStream? = null

    private val sdpObserver = object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {
            send("start", sessionDescription)
        }

        override fun onSetFailure(p0: String?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
    }

    private val peerConnectionFactory = PeerConnectionFactory
            .builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true // todo ???
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()

    private val peerConnection = peerConnectionFactory.createPeerConnection(iceServers, object : PeerConnection.Observer {
        override fun onIceCandidate(iceCandidate: IceCandidate) {
            send("connect", iceCandidate)
        }

        override fun onDataChannel(p0: DataChannel?) {}
        override fun onIceConnectionReceivingChange(p0: Boolean) {}
        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}

        override fun onAddStream(mediaStream: MediaStream?) {
            remoteView?.let {
                mediaStream?.videoTracks?.get(0)?.addSink(it)
            } ?: run {
                remoteMediaStream = mediaStream
            }
        }

        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
        override fun onRemoveStream(p0: MediaStream?) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
    })!!

    private val videoCapturer = Camera2Enumerator(on<ApplicationHandler>().app).run {
        deviceNames.find {
            isFrontFacing(it)
        }?.let {
            createCapturer(it, null)
        } ?: throw IllegalStateException()
    }

    private val localVideoSource = peerConnectionFactory.createVideoSource(false)
//    private val localScreencastVideoSource = peerConnectionFactory.createVideoSource(true) // todo screencast
//    private val localAudioSource = peerConnectionFactory.createAudioSource(MediaConstraints()) // todo audio

    private fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    fun attach(otherPhoneId: String, localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer) {
        this.otherPhoneId = otherPhoneId
        this.remoteView = remoteView
        this.localView = localView

        initSurfaceView(localView)
        initSurfaceView(remoteView)

        this.remoteView?.let { view ->
            remoteMediaStream?.let {
                it.videoTracks.firstOrNull()?.addSink(view)
            }
        }

        startLocalVideoCapture(localView)
    }

    fun call() {
        peerConnection.createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(message: String?) {}
                    override fun onSetSuccess() {}
                    override fun onCreateSuccess(sessionDescription: SessionDescription?) {}
                    override fun onCreateFailure(message: String?) {}
                }, desc)
                sdpObserver.onCreateSuccess(desc!!)
            }
        }, MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        })
    }

    fun answerIncomingCall() {
        peerConnection.createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {}

                    override fun onSetSuccess() {
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {}

                    override fun onCreateFailure(p0: String?) {}
                }, sessionDescription)
                sdpObserver.onCreateSuccess(sessionDescription)
            }
        }, MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        })
    }

    private fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        peerConnection.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {}
            override fun onSetSuccess() {}
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, sessionDescription)
    }

    private fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    private fun startLocalVideoCapture(localVideoOutput: SurfaceViewRenderer) {
        val surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        (videoCapturer as VideoCapturer).initialize(surfaceTextureHelper, localVideoOutput.context, localVideoSource.capturerObserver)
        videoCapturer.startCapture(720, 1280, 60)
        val localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
        localVideoTrack.addSink(localVideoOutput)
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localVideoTrack)
        peerConnection.addStream(localStream)
    }

    fun send(event: String, data: Any) {
        on<ApiHandler>().call(otherPhoneId, event, on<JsonHandler>().to(data)).subscribe({}, {
            on<DefaultAlerts>().thatDidntWork()
        }).also {
            on<DisposableHandler>().add(it)
        }
    }

    fun onStart(callEvent: CallEvent) {
        // todo listen for started calls and launch activity from service
        // note: activity can be closed and opened anytime during the call
        on<TimerHandler>().post(Runnable { on<CallHandler>().onReceiveCall(callEvent.phone) })
        // end todo //

        val sessionDescription = on<JsonHandler>().from(callEvent.data, SessionDescription::class.java)
        onRemoteSessionReceived(sessionDescription)
    }

    fun onConnect(callEvent: CallEvent) {
        val iceCandidate = on<JsonHandler>().from(callEvent.data, IceCandidate::class.java)
        peerConnection.addIceCandidate(iceCandidate)
    }

    fun onAccept(callEvent: CallEvent) {
        val sessionDescription = on<JsonHandler>().from(callEvent.data, SessionDescription::class.java)
        onRemoteSessionReceived(sessionDescription)
    }

    fun onEnd(callEvent: CallEvent) {
        peerConnection.close()
    }

    fun endCall() {
        send("end", "")
    }
}