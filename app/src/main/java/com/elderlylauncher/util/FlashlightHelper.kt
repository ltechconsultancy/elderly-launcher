package com.elderlylauncher.util

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper

/**
 * Zaklamp via de flitser van de camera. setTorchMode heeft geen permissie nodig.
 */
object FlashlightHelper {

    private fun cameraManager(context: Context): CameraManager? =
        runCatching { context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager }.getOrNull()

    fun hasFlash(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) &&
            torchCameraId(context) != null

    fun torchCameraId(context: Context): String? {
        val manager = cameraManager(context) ?: return null
        return try {
            manager.cameraIdList.firstOrNull { id ->
                val chars = manager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Zet de zaklamp aan of uit. Geeft false als het niet lukte (camera in gebruik). */
    fun setTorch(context: Context, on: Boolean): Boolean {
        val manager = cameraManager(context) ?: return false
        val id = torchCameraId(context) ?: return false
        return try {
            manager.setTorchMode(id, on)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Volgt de echte stand (ook als de zaklamp uit het systeemmenu wordt bediend). */
    fun register(context: Context, onChange: (Boolean) -> Unit): () -> Unit {
        val manager = cameraManager(context) ?: return {}
        val id = torchCameraId(context) ?: return {}
        val callback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                if (cameraId == id) onChange(enabled)
            }

            override fun onTorchModeUnavailable(cameraId: String) {
                if (cameraId == id) onChange(false)
            }
        }
        manager.registerTorchCallback(callback, Handler(Looper.getMainLooper()))
        return { manager.unregisterTorchCallback(callback) }
    }
}
