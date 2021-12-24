package club.electro.utils

import android.content.Context
import club.electro.R
import java.util.*

fun AreaLastActivityTime(timestamp: Long, context: Context):String {
    val curDate = Date()
    val date = Date(timestamp * 1000)

    val curTime = curDate.getTime()
    val time = date.getTime()

    if ((curTime - time) > 24 * 3600 * 1000) {
        val days = ((curTime - time) / (24 * 3600 * 1000))
        return days.toString() + context.getString(R.string.short_days)
    } else {
        val sdf = java.text.SimpleDateFormat("HH:mm")
        return sdf.format(date).toString()
    }
}