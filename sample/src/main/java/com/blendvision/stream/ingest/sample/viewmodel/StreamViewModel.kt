package com.blendvision.stream.ingest.sample.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blendvision.stream.ingest.common.presentation.entity.BeautyFilter
import com.blendvision.stream.ingest.common.presentation.entity.Exception
import com.blendvision.stream.ingest.common.presentation.entity.StreamConfig
import com.blendvision.stream.ingest.core.presentation.callback.ValidationListener
import com.blendvision.stream.ingest.core.presentation.factory.StreamIngest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StreamViewModel : ViewModel() {

    private val _onValidationSuccess: MutableLiveData<StreamIngest> = MutableLiveData()
    private val _onValidationFailed: MutableLiveData<Exception> = MutableLiveData()
    private val _elapsedTime: MutableLiveData<Int> = MutableLiveData()

    val onValidationSuccess: LiveData<StreamIngest> = _onValidationSuccess
    val onValidationFailed: LiveData<Exception> = _onValidationFailed
    val elapsedTime: LiveData<Int> = _elapsedTime

    val smoothFilter = BeautyFilter.SkinSmoothFilter()

    private var timerJob: Job? = null

    fun validateLicenseKey(context: Context, licenseKey: String) {
        val streamConfig = StreamConfig(licenseKey = licenseKey)
        StreamIngest.Factory(context, streamConfig).create(object : ValidationListener {
            override fun onValidationSuccess(streamIngest: StreamIngest) {
                _onValidationSuccess.value = streamIngest

            }

            override fun onValidationFailed(exception: Exception) {
                _onValidationFailed.value = exception
            }
        })
    }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                _elapsedTime.postValue(seconds)
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
