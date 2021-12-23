package club.electro.utils

import android.widget.ImageView
import club.electro.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop

fun ImageView.load(url: String?, vararg transforms: BitmapTransformation = emptyArray()) =
    Glide.with(this)
        .load(url)
        .timeout(5_000)
        .transform(*transforms)
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .into(this)

fun ImageView.loadCircleCrop(url: String?, vararg transforms: BitmapTransformation = emptyArray()) =
    load(url, CircleCrop(), *transforms)

//                    .load(area.image)
//                    .circleCrop()
//                    .timeout(5_000)
//                    .placeholder(R.drawable.ic_loading_100dp)
//                    .error(R.drawable.ic_error_100dp)
//                    .into(areaImage)
