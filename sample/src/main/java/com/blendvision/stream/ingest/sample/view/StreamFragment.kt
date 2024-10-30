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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class StreamFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    private var _binding: FragmentStreamBinding? = null
    private val binding get() = _binding!!

    private val streamViewModel: StreamViewModel by viewModels()

    private var streamIngest: StreamIngest? = null

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
        streamViewModel.onValidationSuccess.filterNotNull().onEach { streamIngest ->
            setupStreamIngestView(streamIngest)
        }.launchIn(lifecycleScope)

        streamViewModel.onValidationFailed.filterNotNull().onEach { streamIngestException ->
            showMessage(streamIngestException.errorMessage)
        }.launchIn(lifecycleScope)

        streamViewModel.elapsedTime.filterNotNull().onEach { seconds ->
            binding.timeLabel.text = "$seconds 秒"
        }.launchIn(lifecycleScope)
    }

    private fun setupStreamIngestView(streamIngest: StreamIngest) {
        this.streamIngest = streamIngest
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
        binding.streamHorizontalFlipButton.setOnClickListener { view ->
            view.isActivated = !view.isActivated
            streamIngest.setIsStreamHorizontalFlip(view.isActivated)
        }
        binding.filterButton.setOnClickListener { view ->
            view.isActivated = !view.isActivated
            streamIngest.enableSkinSmoothFaceOnly(view.isActivated)
        }
        binding.smoothFilterBar.setOnSeekBarChangeListener(this)
    }

    private fun observeStreamStatus(streamIngest: StreamIngest) {
        streamIngest.streamStatus.onEach { streamState ->
            Log.i(TAG, "StreamState: $streamState")
            when (streamState) {
                is StreamState.INITIALIZED -> {
                    showMessage("INITIALIZED.")
                    binding.controlPanel.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }

                is StreamState.RTMP_SERVER_IS_CONNECTING -> showMessage("CONNECTING...")
                is StreamState.RTMP_SERVER_IS_CONNECT_SUCCESS -> showMessage("CONNECT SUCCESS.")
                is StreamState.RTMP_SERVER_IS_DISCONNECT -> {
                    showMessage("DISCONNECTED.")
                }

                else -> Unit
            }
        }.launchIn(lifecycleScope)

        streamIngest.error.onEach { streamIngestException ->
            showMessage(streamIngestException.errorMessage)
            streamViewModel.stopTimer()
        }.launchIn(lifecycleScope)
    }

    private fun initQuality(streamIngest: StreamIngest) {
        val quality = when (arguments?.getString(QUALITY_KEY)) {
            Quality.LOW.name -> StreamQuality.Low
            Quality.MEDIUM.name -> StreamQuality.Medium
            Quality.HIGH.name -> StreamQuality.High
            Quality.AUTO.name -> StreamQuality.Auto
            else -> StreamQuality.Low
        }
        streamIngest.setStreamQuality(quality)
        Log.i(TAG, "Quality: ${streamIngest.getStreamQuality()}")
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
        private val TAG = StreamFragment::class.java.simpleName
        const val QUALITY_KEY = "QUALITY_KEY"
        const val STREAM_KEY = "STREAM_KEY"
        const val RTMP_URL_KEY = "RTMP_URL_KEY"
        const val LICENSE_KEY = "LICENSE_KEY"

        enum class Quality { LOW, MEDIUM, HIGH, AUTO }
    }
}

