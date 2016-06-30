package com.lightningkite.kotlin.anko.full

import android.Manifest
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.lightningkite.kotlin.anko.animation.transitionView
import com.lightningkite.kotlin.anko.networking.image.imageStreamExif
import com.lightningkite.kotlin.anko.observable.lifecycle
import com.lightningkite.kotlin.anko.selector
import com.lightningkite.kotlin.anko.viewcontrollers.image.getImageUriFromCamera
import com.lightningkite.kotlin.anko.viewcontrollers.image.getImageUriFromGallery
import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity
import com.lightningkite.kotlin.networking.*
import com.lightningkite.kotlin.observable.property.MutableObservableProperty
import com.lightningkite.kotlin.observable.property.StandardObservableProperty
import com.lightningkite.kotlin.observable.property.bind
import org.jetbrains.anko.*

/**
 * Makes a layout to upload an image.
 * Created by jivie on 6/2/16.
 */
fun ViewGroup.layoutImageUpload(
        activity: VCActivity,
        urlObs: MutableObservableProperty<String?>,
        noImageResource: Int,
        brokenImageResource: Int,
        downloadRequest: NetRequest = NetRequest(NetMethod.GET, ""),
        uploadingObs: StandardObservableProperty<Boolean>,
        doUpload: (Uri, (String?) -> Unit) -> Unit,
        onUploadError: () -> Unit,
        fileProviderAuthority: String,
        imageMinBytes: Long = 250 * 250
): View {
    val loadingObs = StandardObservableProperty(false)
    return transitionView {
        padding = dip(8)
        imageView {
            lifecycle.bind(urlObs) { url ->
                println(url)
                if (url == null) {
                    //set to default image
                    imageResource = noImageResource
                } else {
                    loadingObs.value = (true)
                    imageStreamExif(activity, downloadRequest.copy(url = url), minBytes = imageMinBytes, brokenImageResource = brokenImageResource) { disposer ->
                        loadingObs.value = (false)
                        if (disposer == null) {
                            //set to default image or broken image
                            imageResource = brokenImageResource
                        }
                    }
                }
            }
        }.lparams(matchParent, matchParent).tag("image")

        progressBar().lparams(wrapContent, wrapContent) { gravity = Gravity.CENTER }.tag("loading")

        lifecycle.bind(loadingObs, uploadingObs) { loading, uploading ->
            if (loading || uploading) animate("loading")
            else animate("image")
        }

        onClick {
            activity.selector(
                    null,
                    R.string.camera to {
                        activity.requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            activity.getImageUriFromCamera(fileProviderAuthority = fileProviderAuthority) {
                                Log.i("ImageUploadLayout", it.toString())
                                if (it != null) {
                                    uploadingObs.value = true
                                    doUpload(it) {
                                        uploadingObs.value = false
                                        if (it == null) onUploadError()
                                        else urlObs.value = it
                                    }
                                }
                            }
                        }
                    },
                    R.string.gallery to {
                        activity.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                            activity.getImageUriFromGallery() {
                                Log.i("ImageUploadLayout", it.toString())
                                if (it != null) {
                                    uploadingObs.value = true
                                    doUpload(it) {
                                        uploadingObs.value = false
                                        if (it == null) onUploadError()
                                        else urlObs.value = it
                                    }
                                }
                            }
                        }
                    }
            )
        }


    }
}

/**
 * Makes a layout to upload an image.
 * Created by jivie on 6/2/16.
 */
fun ViewGroup.layoutImageUpload(
        activity: VCActivity,
        urlObs: MutableObservableProperty<String?>,
        noImageResource: Int,
        brokenImageResource: Int,
        downloadRequest: NetRequest = NetRequest(NetMethod.GET, ""),
        uploadingObs: StandardObservableProperty<Boolean>,
        fileProviderAuthority: String,
        uploadRequest: (Uri) -> NetRequest,
        onUploadError: () -> Unit,
        imageMinBytes: Long = 250 * 250
): View = layoutImageUpload(
        activity = activity,
        urlObs = urlObs,
        noImageResource = noImageResource,
        brokenImageResource = brokenImageResource,
        downloadRequest = downloadRequest,
        uploadingObs = uploadingObs,
        onUploadError = onUploadError,
        fileProviderAuthority = fileProviderAuthority,
        imageMinBytes = imageMinBytes,
        doUpload = { uri, callback ->
            Networking.async(uploadRequest(uri)) {
                val url = try {
                    println(it.string())
                    it.jsonObject().get("url")?.asStringOptional
                } catch(e: Exception) {
                    e.printStackTrace()
                    null
                }
                callback(url)
            }
        }
)