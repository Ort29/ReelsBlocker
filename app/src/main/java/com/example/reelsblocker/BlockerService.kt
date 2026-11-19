package com.example.reelsblocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BlockerService : AccessibilityService() {

    private var lastBack = 0L

    // معرّفات شاشات الريلز/الشورتس داخل كل تطبيق. قد تتغير مع تحديثات التطبيقات،
    // فعدّلها هنا إذا توقف الحجب (يمكن اكتشافها بأداة Layout Inspector).
    private val reelViewIds = mapOf(
        "com.instagram.android" to listOf("clips_viewer_view_pager", "root_clips_layout"),
        "com.google.android.youtube" to listOf("reel_recycler", "reel_player_page_container"),
        "com.facebook.katana" to listOf("reels_viewer", "reel_viewer_container")
    )

    // كلمات تبويب الريلز/الشورتس: إذا كان التبويب بهذا الاسم محدَّدًا فنحن داخل القسم.
    private val tabWords = listOf("Reels", "Shorts", "ريلز", "شورتس")

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !Prefs.isEnabled(this)) return
        val pkg = event.packageName?.toString() ?: return
        val ids = reelViewIds[pkg] ?: return
        val root = rootInActiveWindow ?: return

        val onReelScreen = ids.any {
            root.findAccessibilityNodeInfosByViewId("$pkg:id/$it").isNotEmpty()
        } || isReelsTabSelected(root)

        if (onReelScreen) goBack()
    }

    private fun isReelsTabSelected(root: AccessibilityNodeInfo): Boolean =
        tabWords.any { word ->
            root.findAccessibilityNodeInfosByText(word).any { it.isSelected }
        }

    private fun goBack() {
        val now = System.currentTimeMillis()
        if (now - lastBack < 800) return // تجنّب التكرار السريع
        lastBack = now
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    override fun onInterrupt() {}
}
