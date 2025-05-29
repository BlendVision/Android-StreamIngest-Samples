package com.blendvision.stream.ingest.sample.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blendvision.stream.ingest.common.presentation.model.BeautyFilter
import com.blendvision.stream.ingest.common.presentation.model.StreamConfig
import com.blendvision.stream.ingest.common.presentation.model.StreamIngestException
import com.blendvision.stream.ingest.core.presentation.StreamIngest
import com.blendvision.stream.ingest.core.presentation.callback.ValidationListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StreamViewModel : ViewModel() {

    private val _onValidationSuccess: MutableStateFlow<StreamIngest?> = MutableStateFlow(null)
    private val _onValidationFailed: MutableStateFlow<StreamIngestException?> = MutableStateFlow(null)
    private val _elapsedTime: MutableStateFlow<Int?> = MutableStateFlow(null)

    val onValidationSuccess: StateFlow<StreamIngest?> = _onValidationSuccess
    val onValidationFailed: StateFlow<StreamIngestException?> = _onValidationFailed
    val elapsedTime: StateFlow<Int?> = _elapsedTime

    val smoothFilter = BeautyFilter.SkinSmoothFilter()

    private var timerJob: Job? = null

    fun validateLicenseKey(context: Context, licenseKey: String) {
        val streamConfig = StreamConfig(licenseKey = licenseKey)
        StreamIngest.Factory(context, streamConfig).create(object : ValidationListener {
            override fun onValidationSuccess(streamIngest: StreamIngest) {
                _onValidationSuccess.tryEmit(streamIngest)

            }

            override fun onValidationFailed(streamIngestException: StreamIngestException) {
                _onValidationFailed.tryEmit(streamIngestException)
            }
        })
    }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                _elapsedTime.tryEmit(seconds)
                delay(1000)
                seconds++
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    override fun onCleared() {
        stopTimer()
        onValidationSuccess.value?.stopStream()
        onValidationSuccess.value?.release()
        super.onCleared()
    }
}

