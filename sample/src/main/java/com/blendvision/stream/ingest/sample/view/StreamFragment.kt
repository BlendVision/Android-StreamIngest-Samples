package com.blendvision.stream.ingest.sample.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.blendvision.stream.ingest.common.presentation.entity.StreamQuality
import com.blendvision.stream.ingest.common.presentation.entity.StreamState
import com.blendvision.stream.ingest.core.presentation.factory.StreamIngest
import com.blendvision.stream.ingest.sample.databinding.FragmentStreamBinding
import com.blendvision.stream.ingest.sample.viewmodel.StreamViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class StreamFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    private var _binding: FragmentStreamBinding? = null
    private val binding get() = _binding!!

    private val streamViewModel: StreamViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        val licenseKey = arguments?.getString(LICENSE_KEY) ?: ""
        streamViewModel.validateLicenseKey(requireContext(), licenseKey)
    }

    private fun initViewModelObservers() {
        streamViewModel.onValidationSuccess.observe(viewLifecycleOwner) { streamIngest ->
            setupStreamIngestView(streamIngest)
        }
        streamViewModel.onValidationFailed.observe(viewLifecycleOwner) { exception ->
            showMessage(exception.errorMessage)
        }
        streamViewModel.elapsedTime.observe(viewLifecycleOwner) { seconds ->
            binding.timeLabel.text = "$seconds 秒"
        }
    }

    private fun setupStreamIngestView(streamIngest: StreamIngest) {
        setOnClickListeners(streamIngest)
        observeStreamStatus(streamIngest)
        initQuality(streamIngest)
        initBeautyFilter(streamIngest)
        binding.streamIngestView.streamIngest = streamIngest
    }

    private fun setOnClickListeners(streamIngest: StreamIngest) {
        binding.cameraButton.setOnClickListener {
            streamIngest.switchCamera()
        }
        binding.audioButton.setOnClickListener { view ->
            view.isActivated = !view.isActivated
            streamIngest.mutedAudio(view.isActivated)
        }
        binding.videoButton.setOnClickListener { view ->
            view.isActivated = !view.isActivated
            streamIngest.mutedVideo(view.isActivated)
        }
        binding.playOrPauseButton.setOnClickListener { view ->
            val rtmpUrl = arguments?.getString(RTMP_URL_KEY) ?: return@setOnClickListener
            val streamKey = arguments?.getString(STREAM_KEY) ?: return@setOnClickListener
            view.isActivated = !view.isActivated
            if (view.isActivated) {
                streamIngest.startStream(rtmpUrl, streamKey)
                streamViewModel.startTimer()
            } else {
                streamIngest.stopStream()
                streamViewModel.stopTimer()
            }
        }
        binding.smoothFilterBar.setOnSeekBarChangeListener(this)
    }

    private fun observeStreamStatus(streamIngest: StreamIngest) {
        streamIngest.streamStatus.onEach { streamState ->
            Log.e("StreamIngestView_STATE", streamState.toString())
            when (streamState) {
                is StreamState.INITIALIZED -> {
                    showMessage("INITIALIZED.")
                    binding.controlPanel.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }

                is StreamState.CONNECT_STARTED -> showMessage("CONNECT STARTED.")
                is StreamState.CONNECT_SUCCESS -> showMessage("CONNECT SUCCESS.")
                is StreamState.DISCONNECT -> {
                    showMessage("DISCONNECT.")
                    streamViewModel.stopTimer()
                }

                else -> Unit
            }
        }.launchIn(lifecycleScope)

        streamIngest.error.onEach { exception ->
            showMessage(exception.errorMessage)
            streamViewModel.stopTimer()
        }.launchIn(lifecycleScope)
    }

    private fun initQuality(streamIngest: StreamIngest) {
        val quality = when (arguments?.getString(QUALITY_KEY)) {
            Quality.LOW.name -> StreamQuality.Low()
            Quality.MEDIUM.name -> StreamQuality.Medium()
            Quality.HIGH.name -> StreamQuality.High()
            else -> StreamQuality.Low()
        }
        streamIngest.setStreamQuality(quality)
    }

    private fun initBeautyFilter(streamIngest: StreamIngest) {
        streamIngest.registerFilter(listOf(streamViewModel.smoothFilter))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val streamIngest = streamViewModel.onValidationSuccess.value
        streamIngest?.release()
        binding.streamIngestView.streamIngest = null
        _binding = null
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
        binding.stateLabel.text = "State: $message"
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        streamViewModel.smoothFilter.setIntensity(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

    companion object {
        const val QUALITY_KEY = "QUALITY_KEY"
        const val STREAM_KEY = "STREAM_KEY"
        const val RTMP_URL_KEY = "RTMP_URL_KEY"
        const val LICENSE_KEY = "LICENSE_KEY"

        enum class Quality { LOW, MEDIUM, HIGH }
    }
}
