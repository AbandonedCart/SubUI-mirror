package com.carudibu.android.subuimirror

import android.app.Presentation
import android.content.Context
import android.content.DialogInterface
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.WindowManager.InvalidDisplayException
import androidx.appcompat.app.AppCompatActivity
import java.lang.reflect.Field


class MainActivity : AppCompatActivity() {
    private val mAttachedLcdSize: Point = Point()
    private var mPresentationDialog: Presentation? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.v("subuimirror", "connectSubLcd")
        val subDisplay: Display? = getSubDisplay()
        if (subDisplay == null) {
            Log.e("subuimirror", "No subscreen found")
            return
        }
        if (mPresentationDialog == null) {
            mPresentationDialog = CoverPresentation(this, subDisplay)
        }
        mPresentationDialog!!.setOnKeyListener { dialogInterface, i, keyEvent ->
            connectSubLcd(dialogInterface, i, keyEvent)
        }
        setWindowAttributes()
        try {
            mPresentationDialog!!.show()
        } catch (unused: InvalidDisplayException) {
            Log.w("subuimirror", "Display was removed")
            mPresentationDialog = null
        }

    }

    private fun getSubDisplay(): Display? {
        val displays: Array<Display> =
            (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).getDisplays("com.samsung.android.hardware.display.category.BUILTIN")
        Log.d("subuimirror", "builtInDisplays size : " + displays.size)
        val display: Display? = if (displays.size > 1) displays[1] else null
        display?.getRealSize(this.mAttachedLcdSize)
        return display
    }

    private fun setWindowAttributes() {
        mPresentationDialog!!.window!!.clearFlags(8)
        mPresentationDialog!!.window!!.addFlags(2097152)
        mPresentationDialog!!.window!!.addFlags(128)
        mPresentationDialog!!.window!!.addFlags(67108864)
        mPresentationDialog!!.window!!.addFlags(1024)
        val attributes = mPresentationDialog!!.window!!.attributes
        try {
            val field: Field = attributes.javaClass.getField("layoutInDisplayCutoutMode")
            field.isAccessible = true
            field.setInt(attributes, 1)
            mPresentationDialog!!.window!!.attributes = attributes
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun connectSubLcd(
        dialogInterface: DialogInterface?,
        i: Int,
        keyEvent: KeyEvent
    ): Boolean {
        if (keyEvent.action == ACTION_DOWN) {
            return onKeyDown(i, keyEvent)
        }
        return if (keyEvent.action == ACTION_UP) {
            onKeyUp(i, keyEvent)
        } else false
    }


}