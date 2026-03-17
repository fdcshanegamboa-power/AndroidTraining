package com.learn.androidtraining.fragments.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.learn.androidtraining.R
import com.learn.androidtraining.databinding.FragmentHomeBinding
import com.learn.androidtraining.photos.PhotoAdapter
import com.learn.androidtraining.utils.PermissionHelper
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val tag = "HomeFragment"

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var photoAdapter: PhotoAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> launchCamera()
                shouldShowRequestPermissionRationale(PermissionHelper.CAMERA_PERMISSION) -> {
                    // Denied but not permanently — show rationale
                    Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Permanently denied — guide user to settings
                    showGoToSettingsDialog()
                }
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                if (bitmap != null) viewModel.uploadPhoto(bitmap)
                else Toast.makeText(requireContext(), "Failed to get photo", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeUiState()

        binding.buttonCamera.setOnClickListener {
            when {
                PermissionHelper.hasCameraPermission(requireContext()) -> {
                    // Already granted, go straight to camera
                    launchCamera()
                }
                shouldShowRequestPermissionRationale(PermissionHelper.CAMERA_PERMISSION) -> {
                    // User previously denied — show a rationale dialog first
                    showCameraPermissionRationale()
                }
                else -> {
                    // First time asking or permanently denied
                    requestPermissionLauncher.launch(PermissionHelper.CAMERA_PERMISSION)
                }
            }
        }
        binding.buttonGoHome2.setOnClickListener {
            navigateTo(HomeFragment2())
        }
        childFragmentManager.addOnBackStackChangedListener {
            val hasChildren = childFragmentManager.backStackEntryCount > 0
            binding.buttonGoHome2.visibility = if (hasChildren) View.GONE else View.VISIBLE
        }

        Log.d(tag, "onViewCreated")
    }
    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Camera permission was permanently denied. Enable it in Settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireActivity().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun showCameraPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Camera Permission Needed")
            .setMessage("This app needs camera access to take photos.")
            .setPositiveButton("Grant") { _, _ ->
                requestPermissionLauncher.launch(PermissionHelper.CAMERA_PERMISSION)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupRecyclerView() {
        photoAdapter = PhotoAdapter(onDeleteClick = { photo ->
            viewModel.deletePhoto(photo)
            Log.d(tag, "DeletePhoto: requested deletion of photoId=${photo.id}")
        })
        binding.recyclerPhotos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = photoAdapter
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    photoAdapter.submitList(state.photos)
                    binding.textPhotoCount.text = "${state.photos.size} photos"

                    // Load the last photo if available
                    if (state.lastPhotoUrl != null) {
                        Log.d(tag, "observeUiState: loading lastPhotoUrl=${state.lastPhotoUrl}")
                        // Load from Cloudinary URL or local file
                        val imageSource = if (state.lastPhotoUrl.startsWith("http")) {
                            state.lastPhotoUrl // Cloudinary URL
                        } else {
                            java.io.File(state.lastPhotoUrl) // Local file path
                        }
                        Glide.with(this@HomeFragment)
                            .load(imageSource)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_placeholder)
                            .into(binding.imagePreview)
                    } else {
                        Log.d(tag, "observeUiState: no lastPhotoUrl available")
                        binding.imagePreview.setImageResource(R.drawable.ic_image_placeholder)
                    }

                    state.errorMessage?.let { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    fun navigateTo(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .apply {
                childFragmentManager.fragments.lastOrNull()?.let { hide(it) }
            }
            .add(binding.homeChildContainer.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}