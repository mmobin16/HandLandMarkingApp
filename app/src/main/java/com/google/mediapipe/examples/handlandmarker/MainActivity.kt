package com.google.mediapipe.examples.handlandmarker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.handlandmarker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                "Camera permission is required to use this app",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        // Set up status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.signspeak_dark_blue)

        // Check and request camera permission
        checkCameraPermission()

        // Set up navigation
        setupNavigation()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Explain why permission is needed
                Toast.makeText(
                    this,
                    "Camera permission is required for hand tracking functionality",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        activityMainBinding.navigation.apply {
            setupWithNavController(navController)
            itemIconTintList = null // To show custom icons in original colors
            setOnNavigationItemReselectedListener {
                // Ignore reselection to prevent fragment recreation
            }
        }
    }

    override fun onBackPressed() {
        // Handle back press properly with navigation component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as? NavHostFragment
        if (navHostFragment?.navController?.currentDestination?.id != R.id.camera_fragment) {
            super.onBackPressed()
        } else {
            // If we're on the main screen, confirm exit
            if (doubleBackToExitPressedOnce) {
                finish()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

            handler.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    companion object {
        private const val DOUBLE_PRESS_INTERVAL = 2000L // 2 seconds
    }
    private var doubleBackToExitPressedOnce = false
    private val handler = android.os.Handler()
}