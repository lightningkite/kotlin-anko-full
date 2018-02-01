package com.lightningkite.kotlin.anko.full

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.lightningkite.kotlin.anko.async.cancelling
import com.lightningkite.kotlin.anko.image.getBitmapFromUri
import com.lightningkite.kotlin.anko.lifecycle
import com.lightningkite.kotlin.anko.networking.image.ImageLoader
import com.lightningkite.kotlin.anko.networking.image.lambdaBitmapExif
import com.lightningkite.kotlin.async.invokeAsync
import com.lightningkite.kotlin.observable.property.MutableObservableProperty
import com.lightningkite.kotlin.observable.property.ObservableProperty
import com.lightningkite.kotlin.observable.property.StandardObservableProperty
import com.lightningkite.kotlin.observable.property.bind
import okhttp3.Request
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.imageResource

object ImageViewBindUriConstants {
    const val STATE_NO_IMAGE = 0
    const val STATE_BROKEN_IMAGE = -1
    const val STATE_IMAGE = 1
}

fun ImageView.bindUri(
        uriObservable: ObservableProperty<String?>,
        noImageResource: Int? = null,
        brokenImageResource: Int? = null,
        imageMinBytes: Long,
        requestBuilder: Request.Builder = Request.Builder(),
        loadingObs: MutableObservableProperty<Boolean> = StandardObservableProperty(false),
        onLoadComplete: (state: Int) -> Unit = {}
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
            onLoadComplete(ImageViewBindUriConstants.STATE_NO_IMAGE)
        } else {
            val uriObj = Uri.parse(uri)
            if (uriObj.scheme.contains("http")) {
                loadingObs.value = (true)
                post {
                    requestBuilder.url(uri).lambdaBitmapExif(context, imageMinBytes).cancelling(this).invokeAsync {
                        if (it == null) return@invokeAsync
                        loadingObs.value = (false)
                        if (it.result == null) {
                            //set to default image or broken image
                            if (brokenImageResource != null) {
                                imageResource = brokenImageResource
                            }
                            Log.e("ImageView.ext", "Error: " + it.errorString)
                            onLoadComplete(ImageViewBindUriConstants.STATE_BROKEN_IMAGE)
                        } else {
                            imageBitmap = it.result
                            onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
                        }
                    }
                }
            } else {
                try {
                    imageBitmap = context.getBitmapFromUri(Uri.parse(uri), 2048, 2048)!!
                    onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
                } catch (e: Exception) {
                    if (brokenImageResource != null) {
                        imageResource = brokenImageResource
                    }
                    Log.e("ImageView.ext", "Error: " + e.message)
                    e.printStackTrace()
                    onLoadComplete(ImageViewBindUriConstants.STATE_BROKEN_IMAGE)
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
        loadingObs: MutableObservableProperty<Boolean> = StandardObservableProperty(false),
        onLoadComplete: (state: Int) -> Unit = {}
) {
    var lastUri: String? = "nomatch"
    lifecycle.bind(uriObservable) { uri ->
        println("URI: $lastUri -> $uri")
        if (lastUri == uri) return@bind
        lastUri = uri

        if (uri == null || uri.isEmpty()) {
            //set to default image
            if (noImageResource != null) {
                imageResource = noImageResource
            }
            onLoadComplete(ImageViewBindUriConstants.STATE_NO_IMAGE)
        } else {
            val uriObj = Uri.parse(uri)
            if (uriObj.scheme?.contains("http") == true) {
                loadingObs.value = (true)
                post {
                    requestBuilder.url(uri).lambdaBitmapExif(context, imageMaxWidth, imageMaxHeight).cancelling(this).invokeAsync {
                        if (it == null) {
                            return@invokeAsync
                        }
                        loadingObs.value = (false)
                        if (it.result == null) {
                            //set to default image or broken image
                            if (brokenImageResource != null) {
                                imageResource = brokenImageResource
                            }
                            Log.e("ImageView.ext", "Error: " + it.errorString)
                            onLoadComplete(ImageViewBindUriConstants.STATE_BROKEN_IMAGE)
                        } else {
                            imageBitmap = it.result
                            onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
                        }
                    }
                }
            } else {
                try {
                    imageBitmap = context.getBitmapFromUri(Uri.parse(uri), imageMaxWidth, imageMaxHeight)!!
                    onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
                } catch (e: Exception) {
                    if (brokenImageResource != null) {
                        imageResource = brokenImageResource
                    }
                    Log.e("ImageView.ext", "Error: " + e.message)
                    e.printStackTrace()
                    onLoadComplete(ImageViewBindUriConstants.STATE_BROKEN_IMAGE)
                }
            }
        }
    }
}

fun ImageView.bindUri(
        uriObservable: ObservableProperty<String?>,
        cache: MutableMap<String, Bitmap>,
        noImageResource: Int? = null,
        brokenImageResource: Int? = null,
        imageMinBytes: Long,
        requestBuilder: Request.Builder = Request.Builder(),
        loadingObs: MutableObservableProperty<Boolean> = StandardObservableProperty(false),
        onLoadComplete: (state: Int) -> Unit = {}
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
            onLoadComplete(ImageViewBindUriConstants.STATE_NO_IMAGE)
        } else if (cache.containsKey(uri)) {
            imageBitmap = cache[uri]!!
            onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
        } else {
            val uriObj = Uri.parse(uri)
            if (uriObj.scheme.contains("http")) {
                loadingObs.value = (true)
                post {
                    requestBuilder.url(uri).lambdaBitmapExif(context, imageMinBytes).cancelling(this).invokeAsync {
                        if (it == null) return@invokeAsync
                        loadingObs.value = (false)
                        if (it.result == null) {
                            //set to default image or broken image
                            if (brokenImageResource != null) {
                                imageResource = brokenImageResource
                            }
                            Log.e("ImageView.ext", "Error: " + it.errorString)
                            onLoadComplete(ImageViewBindUriConstants.STATE_BROKEN_IMAGE)
                        } else {
                            cache.put(uri, it.result!!)
                            imageBitmap = it.result
                            onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
                        }
                    }
                }
            } else {
                try {
                    imageBitmap = context.getBitmapFromUri(Uri.parse(uri), 2048, 2048)!!
                    onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
                } catch (e: Exception) {
                    if (brokenImageResource != null) {
                        imageResource = brokenImageResource
                    }
                    onLoadComplete(ImageViewBindUriConstants.STATE_BROKEN_IMAGE)
                    Log.e("ImageView.ext", "Error: " + e.message)
                    e.printStackTrace()
                }
            }
        }
    }
}

fun ImageView.bindUri(
        uriObservable: ObservableProperty<String?>,
        cache: MutableMap<String, Bitmap>,
        noImageResource: Int? = null,
        brokenImageResource: Int? = null,
        imageMaxWidth: Int = 2048,
        imageMaxHeight: Int = 2048,
        requestBuilder: Request.Builder = Request.Builder(),
        loadingObs: MutableObservableProperty<Boolean> = StandardObservableProperty(false),
        onLoadComplete: (state: Int) -> Unit = {}
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
            onLoadComplete(ImageViewBindUriConstants.STATE_NO_IMAGE)
        } else if (cache.containsKey(uri)) {
            imageBitmap = cache[uri]!!
            onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
        } else {
            val uriObj = Uri.parse(uri)
            if (uriObj.scheme.contains("http")) {
                loadingObs.value = (true)
                post {
                    requestBuilder.url(uri).lambdaBitmapExif(context, imageMaxWidth, imageMaxHeight).cancelling(this).invokeAsync {
                        if (it == null) return@invokeAsync
                        loadingObs.value = (false)
                        if (it.result == null) {
                            //set to default image or broken image
                            if (brokenImageResource != null) {
                                imageResource = brokenImageResource
                            }
                            Log.e("ImageView.ext", "Error: " + it.errorString)
                            onLoadComplete(ImageViewBindUriConstants.STATE_BROKEN_IMAGE)
                        } else {
                            cache.put(uri, it.result!!)
                            imageBitmap = it.result
                            onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
                        }
                    }
                }
            } else {
                try {
                    imageBitmap = context.getBitmapFromUri(Uri.parse(uri), 2048, 2048)!!
                    onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
                } catch (e: Exception) {
                    if (brokenImageResource != null) {
                        imageResource = brokenImageResource
                    }
                    Log.e("ImageView.ext", "Error: " + e.message)
                    onLoadComplete(ImageViewBindUriConstants.STATE_BROKEN_IMAGE)
                }
            }
        }
    }
}

fun ImageView.bindUri(
        uriObservable: ObservableProperty<String?>,
        imageLoader: ImageLoader,
        noImageResource: Int? = null,
        brokenImageResource: Int? = null,
        loadingObs: MutableObservableProperty<Boolean> = StandardObservableProperty(false),
        onLoadComplete: (state: Int) -> Unit = {}
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
            onLoadComplete(ImageViewBindUriConstants.STATE_NO_IMAGE)
        } else {
            val uriObj = Uri.parse(uri)
            if (uriObj.scheme.contains("http")) {
                loadingObs.value = (true)
                imageLoader.getImage(context, uri) {
                    loadingObs.value = (false)
                    if (it.result == null) {
                        //set to default image or broken image
                        if (brokenImageResource != null) {
                            imageResource = brokenImageResource
                        }
                        Log.e("ImageView.ext", "Error: " + it.errorString)
                        onLoadComplete(ImageViewBindUriConstants.STATE_BROKEN_IMAGE)
                    } else {
                        imageBitmap = it.result
                        onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
                    }
                }
            } else {
                try {
                    imageBitmap = context.getBitmapFromUri(Uri.parse(uri), 2048, 2048)!!
                    onLoadComplete(ImageViewBindUriConstants.STATE_IMAGE)
                } catch (e: Exception) {
                    if (brokenImageResource != null) {
                        imageResource = brokenImageResource
                    }
                    Log.e("ImageView.ext", "Error: " + e.message)
                    onLoadComplete(ImageViewBindUriConstants.STATE_BROKEN_IMAGE)
                }
            }
        }
    }
}