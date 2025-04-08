package com.example.camera2test

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.params.StreamConfigurationMap
import android.util.Log
import android.util.Range
import android.util.Rational
import java.lang.StringBuilder

/**
 * Utility class to log camera characteristics related to exposure
 * modes and ranges using the Camera2 API.
 */
class CameraCharacteristicsLogger(private val context: Context) {

    private val TAG = "CameraCharLogger"
    
    /**
     * Log exposure-related characteristics for all available cameras
     * @return A string representation of all the camera characteristics
     */
    fun logExposureCharacteristics(): String {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val resultBuilder = StringBuilder()
        
        // Get list of available cameras
        val cameraIds = cameraManager.cameraIdList
        
        if (cameraIds.isEmpty()) {
            val message = "No cameras available on this device"
            Log.i(TAG, message)
            return message
        }
        
        val cameraCountMessage = "Found ${cameraIds.size} camera(s)"
        Log.i(TAG, cameraCountMessage)
        resultBuilder.appendLine(cameraCountMessage)
        
        // Log characteristics for each camera
        cameraIds.forEach { cameraId ->
            val characteristics = logCameraExposureCharacteristics(cameraManager, cameraId)
            resultBuilder.appendLine(characteristics)
        }
        
        return resultBuilder.toString()
    }
    
    /**
     * Log exposure characteristics for a specific camera
     * @return A string representation of this camera's characteristics
     */
    private fun logCameraExposureCharacteristics(cameraManager: CameraManager, cameraId: String): String {
        val resultBuilder = StringBuilder()
        
        try {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            
            val headerMessage = "=== Camera $cameraId Exposure Characteristics ==="
            Log.i(TAG, headerMessage)
            resultBuilder.appendLine(headerMessage)
            
            // Log available AE modes
            val availableAeModes = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES)
            val aeModesMessage = if (availableAeModes != null) {
                "Available AE modes: ${aeModeToString(availableAeModes)}"
            } else {
                "Available AE modes: null"
            }
            Log.i(TAG, aeModesMessage)
            resultBuilder.appendLine(aeModesMessage)
            
            // Log AE compensation range
            val compensationRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
            val compensationRangeMessage = "AE compensation range: $compensationRange"
            Log.i(TAG, compensationRangeMessage)
            resultBuilder.appendLine(compensationRangeMessage)
            
            // Log AE compensation step
            val compensationStep = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)
            val compensationStepMessage = "AE compensation step: $compensationStep"
            Log.i(TAG, compensationStepMessage)
            resultBuilder.appendLine(compensationStepMessage)
            
            // Log if AE lock is available
            val aeLockAvailable = characteristics.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE)
            val aeLockMessage = "AE lock available: $aeLockAvailable"
            Log.i(TAG, aeLockMessage)
            resultBuilder.appendLine(aeLockMessage)
            
            // Log exposure time range if available
            val exposureTimeRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
            val exposureTimeMessage = "Exposure time range (ns): $exposureTimeRange"
            Log.i(TAG, exposureTimeMessage)
            resultBuilder.appendLine(exposureTimeMessage)
            
            // Log sensitivity range if available
            val sensitivityRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            val sensitivityRangeMessage = "Sensitivity range (ISO): $sensitivityRange"
            Log.i(TAG, sensitivityRangeMessage)
            resultBuilder.appendLine(sensitivityRangeMessage)
            
            // Log if manual sensor control is available
            val manualSensorInfo = logManualSensorControl(characteristics)
            resultBuilder.appendLine(manualSensorInfo)
            
            // Log available regions for metering
            val meteringRegionsInfo = logMeteringRegions(characteristics)
            resultBuilder.appendLine(meteringRegionsInfo)
            
        } catch (e: Exception) {
            val errorMessage = "Error getting camera characteristics for camera $cameraId: ${e.message}"
            Log.e(TAG, errorMessage, e)
            resultBuilder.appendLine(errorMessage)
        }
        
        return resultBuilder.toString()
    }
    
    /**
     * Log if manual sensor control is available
     * @return A string with the manual sensor control information
     */
    private fun logManualSensorControl(characteristics: CameraCharacteristics): String {
        val availableCapabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        
        val hasManualSensor = availableCapabilities?.contains(
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR
        ) ?: false
        
        val message = "Manual sensor control available: $hasManualSensor"
        Log.i(TAG, message)
        return message
    }
    
    /**
     * Log available regions for metering
     * @return A string with the metering regions information
     */
    private fun logMeteringRegions(characteristics: CameraCharacteristics): String {
        val resultBuilder = StringBuilder()
        
        val maxRegionsAe = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE) ?: 0
        val maxRegionsMessage = "Max AE metering regions: $maxRegionsAe"
        Log.i(TAG, maxRegionsMessage)
        resultBuilder.appendLine(maxRegionsMessage)
        
        if (maxRegionsAe > 0) {
            val activeArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            val arraySizeMessage = "Active array size (for metering regions): $activeArraySize"
            Log.i(TAG, arraySizeMessage)
            resultBuilder.appendLine(arraySizeMessage)
        }
        
        return resultBuilder.toString()
    }
    
    /**
     * Convert AE mode values to readable strings
     */
    private fun aeModeToString(modes: IntArray): String {
        return modes.joinToString(", ") { mode ->
            when (mode) {
                CameraMetadata.CONTROL_AE_MODE_OFF -> "OFF"
                CameraMetadata.CONTROL_AE_MODE_ON -> "ON"
                CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH -> "ON_AUTO_FLASH"
                CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH -> "ON_ALWAYS_FLASH"
                CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE -> "ON_AUTO_FLASH_REDEYE"
                else -> "UNKNOWN($mode)"
            }
        }
    }
} 