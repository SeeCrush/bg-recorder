package com.example.bgrecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val requiredPermissions by lazy {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.toTypedArray()
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            startRecordingService()
        } else {
            Toast.makeText(this, "Camera & Audio permissions are required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val btnStart = Button(this).apply {
            text = "Start Background Recording"
            setOnClickListener { checkAndStart() }
        }

        val btnStop = Button(this).apply {
            text = "Stop Recording"
            setOnClickListener { stopRecordingService() }
        }

        layout.addView(btnStart)
        layout.addView(btnStop)
        setContentView(layout)
    }

    private fun checkAndStart() {
        if (hasAllPermissions()) {
            startRecordingService()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    private fun hasAllPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startRecordingService() {
        val intent = Intent(this, VideoCaptureService::class.java).apply {
            action = VideoCaptureService.ACTION_START
        }
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "Recording started. You can minimize or lock screen.", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecordingService() {
        val intent = Intent(this, VideoCaptureService::class.java).apply {
            action = VideoCaptureService.ACTION_STOP
        }
        startService(intent)
        Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show()
    }
}
