package com.lightningkite.kotlin.anko.full

import android.widget.ImageView
import com.lightningkite.kotlin.anko.lifecycle
import com.lightningkite.kotlin.anko.networking.image.imageStreamExif
import com.lightningkite.kotlin.networking.NetMethod
import com.lightningkite.kotlin.networking.NetRequest
import com.lightningkite.kotlin.observable.property.MutableObservableProperty
import com.lightningkite.kotlin.observable.property.ObservableProperty
import com.lightningkite.kotlin.observable.property.StandardObservableProperty
import com.lightningkite.kotlin.observable.property.bind
import org.jetbrains.anko.imageResource

/**
 * Created by jivie on 7/15/16.
 */
fun ImageView.bindUrl(
        urlObservable: ObservableProperty<String?>,
        noImageResource: Int,
        brokenImageResource: Int,
        imageMinBytes: Long,
        downloadRequest: NetRequest = NetRequest(NetMethod.GET, ""),
        loadingObs: MutableObservableProperty<Boolean> = StandardObservableProperty(false)
) {
    lifecycle.bind(urlObservable) { url ->
        if (url == null) {
            //set to default image
            imageResource = noImageResource
        } else {
            loadingObs.value = (true)
            imageStreamExif(context, downloadRequest.copy(url = url), minBytes = imageMinBytes, brokenImageResource = brokenImageResource) { disposer ->
                loadingObs.value = (false)
                if (disposer == null) {
                    //set to default image or broken image
                    imageResource = brokenImageResource
                }
            }
        }
    }
}

fun ImageView.bindUrl(
        urlObservable: ObservableProperty<String?>,
        noImageResource: Int,
        brokenImageResource: Int,
        imageMaxWidth: Int = 2048,
        imageMaxHeight: Int = 2048,
        downloadRequest: NetRequest = NetRequest(NetMethod.GET, ""),
        loadingObs: MutableObservableProperty<Boolean> = StandardObservableProperty(false)
) {
    lifecycle.bind(urlObservable) { url ->
        if (url == null) {
            //set to default image
            imageResource = noImageResource
        } else {
            loadingObs.value = (true)
            imageStreamExif(
                    context,
                    downloadRequest.copy(url = url),
                    maxWidth = imageMaxWidth,
                    maxHeight = imageMaxHeight,
                    brokenImageResource = brokenImageResource
            ) { disposer ->
                loadingObs.value = (false)
                if (disposer == null) {
                    //set to default image or broken image
                    imageResource = brokenImageResource
                }
            }
        }
    }
}