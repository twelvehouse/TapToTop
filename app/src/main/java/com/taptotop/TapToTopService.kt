package com.taptotop

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

@SuppressLint("AccessibilityService")
class TapToTopService : AccessibilityService() {

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        setupOverlay()
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val height = prefs.getInt("overlay_height", 30)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            height,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or 
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP

        overlayView = View(this).apply {
            setOnClickListener { performScrollAction() }
        }
        
        windowManager?.addView(overlayView, params)
    }

    private fun getStatusBarHeight(): Int {
        val metrics = windowManager?.currentWindowMetrics
        val insets = metrics?.windowInsets?.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())
        val height = insets?.top ?: 0
        return if (height > 0) height else 100
    }

    private fun performScrollAction() {
        // Refresh overlay height if it was changed in settings
        updateOverlayHeight()

        val currentWindows = windows
        var targetNode: AccessibilityNodeInfo? = null
        
        for (i in currentWindows.size - 1 downTo 0) {
            val root = currentWindows[i].root ?: continue
            targetNode = findMainScrollableNode(root)
            if (targetNode != null) break
        }

        val node = targetNode ?: return

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val repeatCount = prefs.getInt("repeat_count", 2)
        val duration = prefs.getInt("duration", 20).toLong()

        executeCustomFlick(node, repeatCount, duration)
    }

    private fun updateOverlayHeight() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val newHeight = prefs.getInt("overlay_height", 30)
        
        overlayView?.let { view ->
            val params = view.layoutParams as? WindowManager.LayoutParams
            if (params != null && params.height != newHeight) {
                params.height = newHeight
                windowManager?.updateViewLayout(view, params)
            }
        }
    }

    private fun executeCustomFlick(node: AccessibilityNodeInfo, repeats: Int, duration: Long) {
        val rect = Rect()
        node.getBoundsInScreen(rect)

        val startX = rect.centerX().toFloat()
        val startY = rect.top + (rect.height() * 0.2f)
        val endY = rect.top + (rect.height() * 0.9f)

        val swipePath = Path().apply {
            moveTo(startX, startY)
            lineTo(startX, endY)
        }

        val gestureBuilder = GestureDescription.Builder()
        val interval = 5L
        
        for (i in 0 until repeats) {
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, i * (duration + interval), duration))
        }

        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun findMainScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findMainScrollableNode(child)
            if (found != null) return found
        }
        return null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager?.removeView(it) }
    }
}
