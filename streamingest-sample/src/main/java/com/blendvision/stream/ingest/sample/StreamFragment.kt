package com.blendvision.stream.ingest.sample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.blendvision.stream.ingest.domain.entities.State
import com.blendvision.stream.ingest.domain.entities.StreamQuality
import com.blendvision.stream.ingest.sample.databinding.FragmentStreamBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class StreamFragment : Fragment() {

    private var _binding: FragmentStreamBinding? = null

    private val binding get() = _binding!!

    private var rtmpUrl: String? = null

    private var streamName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            rtmpUrl = it.getString(RTMP_URL)
            streamName = it.getString(STREAM_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        setOnClickListener()
    }

    private fun initView() {
        val quality = when (arguments?.getString(QUALITY)) {
            Quality.LOW.name -> StreamQuality.Low()
            Quality.MEDIUM.name -> StreamQuality.Medium()
            Quality.HIGH.name -> StreamQuality.High()
            else -> StreamQuality.Low()
        }
        binding.streamIngestView.setStreamQuality(quality)
    }

    private fun setOnClickListener() {
        binding.cameraButton.setOnClickListener {
            binding.streamIngestView.switchCamera()
        }
        binding.audioButton.setOnClickListener { view ->
            view.isActivated = !view.isActivated
            binding.streamIngestView.mutedAudio(view.isActivated)
        }
        binding.videoButton.setOnClickListener { view ->
            view.isActivated = !view.isActivated
            binding.streamIngestView.mutedVideo(view.isActivated)
        }
        binding.playOrPauseButton.setOnClickListener { view ->
            view.isActivated = !view.isActivated
            if (view.isActivated) {
                rtmpUrl?.let {
                    binding.streamIngestView.connect(it)
                }
            } else {
                binding.streamIngestView.stopPublish()
            }

        }
    }

    private fun initObserver() {
        binding.streamIngestView.streamStatus.flowWithLifecycle(lifecycle).onEach { state ->
            Log.e("StreamIngestView_STATE", state.toString())
            when (state) {
                State.CONNECT_SUCCESS -> binding.streamIngestView.startPublish(streamName)
                else -> Unit
            }
        }.launchIn(lifecycleScope)
    }

    override fun onStop() {
        super.onStop()
        binding.streamIngestView.stopPublish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.streamIngestView.dispose()
        _binding = null
    }

    companion object {
        enum class Quality { LOW, MEDIUM, HIGH }

        const val QUALITY = "Quality"
        const val RTMP_URL = "RtmpUrl"
        const val STREAM_NAME = "StreamName"
    }
}
