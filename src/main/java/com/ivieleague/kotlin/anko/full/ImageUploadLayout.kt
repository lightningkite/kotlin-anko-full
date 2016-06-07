package com.ivieleague.kotlin.anko.full

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.ivieleague.kotlin.anko.animation.transitionView
import com.ivieleague.kotlin.anko.networking.image.imageStreamExif
import com.ivieleague.kotlin.anko.observable.lifecycle
import com.ivieleague.kotlin.anko.selector
import com.ivieleague.kotlin.anko.viewcontrollers.image.getImageUriFromCamera
import com.ivieleague.kotlin.anko.viewcontrollers.image.getImageUriFromGallery
import com.ivieleague.kotlin.anko.viewcontrollers.implementations.VCActivity
import com.ivieleague.kotlin.networking.NetRequest
import com.ivieleague.kotlin.networking.Networking
import com.ivieleague.kotlin.networking.asStringOptional
import com.ivieleague.kotlin.networking.async
import com.ivieleague.kotlin.observable.property.MutableObservableProperty
import com.ivieleague.kotlin.observable.property.StandardObservableProperty
import com.ivieleague.kotlin.observable.property.bind
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
        downloadRequest: NetRequest,
        uploadingObs: StandardObservableProperty<Boolean>,
        uploadRequest: (Uri) -> NetRequest,
        onUploadError: () -> Unit
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
                    imageStreamExif(activity, downloadRequest.copy(url = url), 500) { success ->
                        loadingObs.value = (false)
                        if (!success) {
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
                            activity.getImageUriFromCamera() {
                                println(it)
                                if (it != null) uploadImage(context, uploadRequest(it), urlObs, uploadingObs, onUploadError)
                            }
                        }
                    },
                    R.string.gallery to {
                        activity.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                            activity.getImageUriFromGallery() {
                                println(it)
                                if (it != null) uploadImage(context, uploadRequest(it), urlObs, uploadingObs, onUploadError)
                            }
                        }
                    }
            )
        }


    }
}

inline fun uploadImage(
        context: Context,
        request: NetRequest,
        urlObs: MutableObservableProperty<String?>,
        uploading: MutableObservableProperty<Boolean>,
        crossinline onError: () -> Unit
) {
    uploading.value = (true)
    try {
        Networking.async(request) {
            uploading.value = (false)
            try {
                if (it.isSuccessful) {
                    val newUrl = it.jsonObject().get("url")?.asStringOptional
                    Log.i("image.Layouts", "newUrl=$newUrl")
                    if (newUrl != null) {
                        urlObs.value = (newUrl)
                    }
                } else {
                    Log.e("image.Layouts", "failed. ${it.code}: ${it.string()}")
                    onError()
                }
            } catch(e: Exception) {
                uploading.value = (false)
                e.printStackTrace()
                onError()
            }
        }
    } catch(e: Exception) {
        uploading.value = (false)
        e.printStackTrace()
        onError()
    }
}