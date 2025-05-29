package com.blendvision.stream.ingest.sample.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.blendvision.stream.ingest.sample.R
import com.blendvision.stream.ingest.sample.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    private val binding get() = _binding!!

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // 所有權限都被授予，執行相應的操作
                goToStreamPage()
            } else {
                // 權限被拒絕
                if (shouldShowRequestPermissionRationale()) {
                    // 顯示解釋信息或其他處理邏輯
                    showPermissionDeniedMessage()
                } else {
                    // 引導使用者到應用程式設定頁面
                    redirectToAppSettings()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.licenseKeyTextInputView.hint = getString(R.string.input_license_key)
        binding.rtmpUrlTextInputView.hint = getString(R.string.input_rtmp_url)
        binding.streamKeyTextInputView.hint = getString(R.string.input_stream_key)
        binding.goToStreamPageButton.setOnClickListener {
            checkAndRequestPermissions()
        }
    }

    private fun checkAndRequestPermissions() {
        /// 檢查是否已獲得所需權限
        val isAllPermissionsGranted = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (isAllPermissionsGranted) {
            // 如果已經有權限，直接執行相應的操作
            goToStreamPage()
        } else {
            // 如果還沒有權限，啟動權限請求
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

    }

    private fun goToStreamPage() {

        val quality = when (binding.qualityChipGroup.checkedChipId) {
            R.id.ultraLowQualityChip -> StreamFragment.Companion.Quality.ULTRA_LOW.name
            R.id.lowQualityChip -> StreamFragment.Companion.Quality.LOW.name
            R.id.mediumQualityChip -> StreamFragment.Companion.Quality.MEDIUM.name
            R.id.highQualityChip -> StreamFragment.Companion.Quality.HIGH.name
            R.id.autoQualityChip -> StreamFragment.Companion.Quality.AUTO.name
            else -> StreamFragment.Companion.Quality.LOW.name
        }

        val licenseKey = binding.licenseKeyInputEditText.text.toString()
        val rtmpUrl = binding.rtmpUrlInputEditText.text.toString()
        val streamKey = binding.streamKeyInputEditText.text.toString()

        val bundle = bundleOf(
            StreamFragment.QUALITY_KEY to quality,
            StreamFragment.RTMP_URL_KEY to rtmpUrl,
            StreamFragment.STREAM_KEY to streamKey,
            StreamFragment.LICENSE_KEY to licenseKey
        )

        findNavController().navigate(R.id.action_mainFragment_to_streamFragment, bundle)
    }

    private fun redirectToAppSettings() {
        val settingsIntent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(settingsIntent)
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        return REQUIRED_PERMISSIONS.any {
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it)
        }
    }

    private fun showPermissionDeniedMessage() {
        // 使用Snackbar顯示一條信息
        Snackbar.make(
            binding.root,
            getString(R.string.permission_rejected),
            Snackbar.LENGTH_LONG
        ).setAction(getString(R.string.settings)) {
            // 如果使用者點擊了"設定"按鈕，引導到應用程式設定頁面
            redirectToAppSettings()
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )

    }
}
