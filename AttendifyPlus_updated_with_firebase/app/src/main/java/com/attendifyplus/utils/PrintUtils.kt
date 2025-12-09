package com.attendifyplus.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.print.PrintHelper

object PrintUtils {
    fun printBitmap(context: Context, bitmap: Bitmap, jobName: String = "Document") {
        try {
            val printHelper = PrintHelper(context)
            printHelper.scaleMode = PrintHelper.SCALE_MODE_FIT
            printHelper.printBitmap(jobName, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
