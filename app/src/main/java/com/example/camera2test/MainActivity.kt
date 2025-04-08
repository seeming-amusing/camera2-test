package com.example.camera2test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.camera2test.ui.theme.Camera2TestTheme

class MainActivity : ComponentActivity() {
    
    private val TAG = "MainActivity"
    private lateinit var cameraLogger: CameraCharacteristicsLogger
    
    // State for camera characteristics display
    private val cameraCharacteristics = mutableStateOf("")
    
    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i(TAG, "Camera permission granted")
            logCameraCharacteristics()
        } else {
            Log.e(TAG, "Camera permission denied")
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize the camera logger
        cameraLogger = CameraCharacteristicsLogger(this)
        
        setContent {
            Camera2TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CameraTestScreen(
                        modifier = Modifier.padding(innerPadding),
                        onLogExposureClick = { checkPermissionAndLogCharacteristics() },
                        cameraCharacteristics = cameraCharacteristics
                    )
                }
            }
        }
    }
    
    private fun checkPermissionAndLogCharacteristics() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, proceed with logging
                logCameraCharacteristics()
            }
            else -> {
                // Request camera permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun logCameraCharacteristics() {
        try {
            Log.i(TAG, "Logging camera exposure characteristics")
            val characteristics = cameraLogger.logExposureCharacteristics()
            
            // Update the state with the new characteristics
            cameraCharacteristics.value = characteristics
            
            Toast.makeText(
                this,
                "Camera characteristics logged",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error logging camera characteristics", e)
            Toast.makeText(
                this,
                "Error logging camera characteristics: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            
            // Update the state with the error
            cameraCharacteristics.value = "Error: ${e.message}"
        }
    }
}

@Composable
fun CameraTestScreen(
    modifier: Modifier = Modifier,
    onLogExposureClick: () -> Unit,
    cameraCharacteristics: MutableState<String>
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Camera2 API Test",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Button(
                onClick = onLogExposureClick,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(text = "Log Exposure Characteristics")
            }
            
            // Only show the card if we have characteristics data
            if (cameraCharacteristics.value.isNotEmpty()) {
                // Add a divider
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
                
                // Display the characteristics
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Camera Characteristics",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = cameraCharacteristics.value,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CameraTestScreenPreview() {
    Camera2TestTheme {
        CameraTestScreen(
            onLogExposureClick = {},
            cameraCharacteristics = remember { mutableStateOf("Sample camera data would appear here") }
        )
    }
}