package club.electro.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL


fun loadCircleImageBlocking(
    imageUrl: String
): Bitmap? = runBlocking {
    val url = URL(imageUrl)

    withContext(Dispatchers.IO) {
        try {
            val input = url.openStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            null
        }
    }?.let { bitmap ->
        GetCircleBitmap.make(bitmap)
    }
}