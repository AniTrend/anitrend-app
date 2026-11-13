package com.mxt.anitrend.widget

import android.content.Context
import android.graphics.Bitmap
import java.io.File

internal fun solidImageFile(
    context: Context,
    color: Int,
    width: Int = 4,
    height: Int = 3,
): File {
    require(width > 0 && height > 0)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
    val imageFile = File.createTempFile("compose-image-fixture-", ".png", context.cacheDir)
    try {
        imageFile.outputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    } finally {
        bitmap.recycle()
    }
    return imageFile
}
