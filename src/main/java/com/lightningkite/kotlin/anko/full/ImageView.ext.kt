package com.lightningkite.kotlin.anko.full

import android.net.Uri
import android.widget.ImageView
import com.lightningkite.kotlin.anko.image.getBitmapFromUri
import com.lightningkite.kotlin.anko.lifecycle
import com.lightningkite.kotlin.anko.networking.image.lambdaBitmapExif
import com.lightningkite.kotlin.async.invokeAsync
import com.lightningkite.kotlin.observable.property.MutableObservableProperty
import com.lightningkite.kotlin.observable.property.ObservableProperty
import com.lightningkite.kotlin.observable.property.StandardObservableProperty
import com.lightningkite.kotlin.observable.property.bind
import okhttp3.Request
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.imageResource

fun ImageView.bindUri(
        uriObservable: ObservableProperty<String?>,
        noImageResource: Int? = null,
        brokenImageResource: Int? = null,
        imageMinBytes: Long,
        requestBuilder: Request.Builder = Request.Builder(),
        loadingObs: MutableObservableProperty<Boolean> = StandardObservableProperty(false)
) {
    var lastUri: String? = "nomatch"
    lifecycle.bind(uriObservable) { uri ->
        if (lastUri == uri) return@bind
        lastUri = uri

        if (uri == null || uri.isEmpty()) {
            //set to default image
            if (noImageResource != null) {
                imageResource = noImageResource
            }
        } else {
            val uriObj = Uri.parse(uri)
            if (uriObj.scheme.contains("http")) {
                loadingObs.value = (true)
                requestBuilder.url(uri).lambdaBitmapExif(context, imageMinBytes).invokeAsync {
                    loadingObs.value = (false)
                    if (it.result == null) {
                        //set to default image or broken image
                        if (brokenImageResource != null) {
                            imageResource = brokenImageResource
                        }
                    } else {
                        imageBitmap = it.result
                    }
                }
            } else {
                loadingObs.value = (true)
                try {
                    imageBitmap = context.getBitmapFromUri(Uri.parse(uri), 2048, 2048)!!
                } catch(e: Exception) {
                    if (brokenImageResource != null) {
                        imageResource = brokenImageResource
                    }
                }
            }
        }
    }
}

fun ImageView.bindUri(
        uriObservable: ObservableProperty<String?>,
        noImageResource: Int? = null,
        brokenImageResource: Int? = null,
        imageMaxWidth: Int = 2048,
        imageMaxHeight: Int = 2048,
        requestBuilder: Request.Builder = Request.Builder(),
        loadingObs: MutableObservableProperty<Boolean> = StandardObservableProperty(false)
) {
    var lastUri: String? = "nomatch"
    lifecycle.bind(uriObservable) { uri ->
        if (lastUri == uri) return@bind
        lastUri = uri

        if (uri == null || uri.isEmpty()) {
            //set to default image
            if (noImageResource != null) {
                imageResource = noImageResource
            }
        } else {
            val uriObj = Uri.parse(uri)
            if (uriObj.scheme.contains("http")) {
                loadingObs.value = (true)
                requestBuilder.url(uri).lambdaBitmapExif(context, imageMaxWidth, imageMaxHeight).invokeAsync {
                    loadingObs.value = (false)
                    if (it.result == null) {
                        //set to default image or broken image
                        if (brokenImageResource != null) {
                            imageResource = brokenImageResource
                        }
                    } else {
                        imageBitmap = it.result
                    }
                }
            } else {
                loadingObs.value = (true)
                try {
                    imageBitmap = context.getBitmapFromUri(Uri.parse(uri), 2048, 2048)!!
                } catch(e: Exception) {
                    if (brokenImageResource != null) {
                        imageResource = brokenImageResource
                    }
                }
            }
        }
    }
}