package com.blendvision.stream.ingest.sample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.blendvision.stream.ingest.domain.entities.State
import com.blendvision.stream.ingest.domain.entities.StreamQuality
import com.blendvision.stream.ingest.sample.databinding.FragmentStreamBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class StreamFragment : Fragment() {

    private var _binding: FragmentStreamBinding? = null

    private val binding get() = _binding!!

    private var rtmpUrl: String? = null

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            rtmpUrl = it.getString(RTMP_URL)
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
                    binding.streamIngestView.startStream(it)
                }
            } else {
                binding.streamIngestView.stopStream()
            }
        }
    }

    private fun initObserver() {
        binding.streamIngestView.streamStatus.flowWithLifecycle(lifecycle).onEach { state ->
            Log.e("StreamIngestView_STATE", state.toString())
            when (state) {
                State.INITIALIZED -> {
                    showMessage("INITIALIZED.")
                    binding.controlPanel.visibility = View.VISIBLE
                }

                State.CONNECT_STARTED -> {
                    showMessage("CONNECT STARTED.")
                }

                State.CONNECT_SUCCESS -> {
                    showMessage("CONNECT SUCCESS.")
                    recordTime()
                }

                State.DISCONNECT -> {
                    showMessage("DISCONNECT.")
                    job?.cancel()
                }

                else -> Unit
            }
        }.launchIn(lifecycleScope)

        binding.streamIngestView.error.flowWithLifecycle(lifecycle).onEach { error ->
            showMessage(error.message)
            job?.cancel()
        }.launchIn(lifecycleScope)

    }

    private fun recordTime() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            var seconds = 0
            while (isActive) {
                binding.timeLabel.text = "$seconds 秒"
                delay(1000)
                seconds++
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
        binding.streamIngestView.release()
        _binding = null
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
        binding.stateLabel.text = "State: $message"
    }

    companion object {
        enum class Quality { LOW, MEDIUM, HIGH }

        const val QUALITY = "Quality"
        const val RTMP_URL = "RtmpUrl"
    }
}
