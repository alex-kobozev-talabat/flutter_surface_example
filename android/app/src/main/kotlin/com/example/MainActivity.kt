package com.example

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformViewRenderTarget
import io.flutter.plugin.platform.PlatformViewWrapper

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.myapp/release"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "release" -> {
                    val found = traverseAndReleaseAllSurfaces(window.decorView)
                    if (found > 0) {
                        Toast.makeText(
                            this@MainActivity,
                            "Surfaces released: $found",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Surfaces not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    result.success(null)
                }

                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun traverseAndReleaseAllSurfaces(view: View): Int {
        var foundCount = 0
        if (view is PlatformViewWrapper) {
            try {
                // Use reflection to access the private field 'renderTarget'
                val renderTargetField =
                    PlatformViewWrapper::class.java.getDeclaredField("renderTarget")
                renderTargetField.isAccessible = true
                val renderTarget: PlatformViewRenderTarget =
                    renderTargetField.get(view) as PlatformViewRenderTarget
                renderTarget.surface.release()
                foundCount++
                Log.d(
                    "SurfaceRelease",
                    "Released PlatformViewRenderTarget from PlatformViewWrapper"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                foundCount += traverseAndReleaseAllSurfaces(child)
            }
        }
        return foundCount
    }
}
