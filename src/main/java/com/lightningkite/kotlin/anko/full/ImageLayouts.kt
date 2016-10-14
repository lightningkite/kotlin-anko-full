package com.lightningkite.kotlin.anko.full

import android.view.View
import android.view.ViewGroup
import com.lightningkite.kotlin.anko.animation.TransitionView
import com.lightningkite.kotlin.anko.observable.progressLayout
import com.lightningkite.kotlin.networking.NetMethod
import com.lightningkite.kotlin.networking.NetRequest
import com.lightningkite.kotlin.observable.property.MutableObservableProperty
import com.lightningkite.kotlin.observable.property.ObservableProperty
import com.lightningkite.kotlin.observable.property.StandardObservableProperty
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding

/**
 * Various layouts that require all packages.
 * Created by jivie on 7/15/16.
 */
fun ViewGroup.layoutImage(
        urlObservable: ObservableProperty<String?>,
        noImageResource: Int,
        brokenImageResource: Int,
        imageMinBytes: Long = 250 * 250 * 4,
        downloadRequest: NetRequest = NetRequest(NetMethod.GET, ""),
        loadingObs: MutableObservableProperty<Boolean> = StandardObservableProperty(false),
        setup: TransitionView.() -> Unit = {}
): View = progressLayout(loadingObs) {
    padding = dip(8)
    imageView {
        bindUrl(
                urlObservable = urlObservable,
                noImageResource = noImageResource,
                brokenImageResource = brokenImageResource,
                imageMinBytes = imageMinBytes,
                downloadRequest = downloadRequest,
                loadingObs = loadingObs
        )
    }.lparams(matchParent, matchParent)
}.apply(setup)

fun ViewGroup.layoutImageUri(
        uriObservable: ObservableProperty<String?>,
        noImageResource: Int,
        brokenImageResource: Int,
        imageMinBytes: Long = 250 * 250 * 4,
        downloadRequest: NetRequest = NetRequest(NetMethod.GET, ""),
        loadingObs: MutableObservableProperty<Boolean> = StandardObservableProperty(false),
        setup: TransitionView.() -> Unit = {}
): View = progressLayout(loadingObs) {
    padding = dip(8)
    imageView {
        bindUri(
                uriObservable = uriObservable,
                noImageResource = noImageResource,
                brokenImageResource = brokenImageResource,
                imageMinBytes = imageMinBytes,
                downloadRequest = downloadRequest,
                loadingObs = loadingObs
        )
    }.lparams(matchParent, matchParent)
}.apply(setup)