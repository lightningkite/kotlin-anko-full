package com.lightningkite.kotlin.anko.full

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.lightningkite.kotlin.anko.getActivity
import com.lightningkite.kotlin.anko.lifecycle
import com.lightningkite.kotlin.anko.viewcontrollers.dialogs.infoDialog
import com.lightningkite.kotlin.anko.viewcontrollers.dialogs.standardDialog
import com.lightningkite.kotlin.async.doUiThread
import com.lightningkite.kotlin.networking.MyGson
import com.lightningkite.kotlin.networking.TypedResponse
import com.lightningkite.kotlin.observable.property.MutableObservableProperty
import com.lightningkite.kotlin.observable.property.ObservableProperty
import com.lightningkite.kotlin.observable.property.StandardObservableProperty
import com.lightningkite.kotlin.observable.property.bind
import org.jetbrains.anko.progressBar

@Deprecated("Use the version in kotlin-observable instead.", replaceWith = ReplaceWith("captureProgress(observable)", "com.lightningkite.kotlin.observable"))
fun <T> (() -> T).captureProgress(observable: MutableObservableProperty<Boolean>): (() -> T) {
    return {
        doUiThread {
            observable.value = true
        }
        val result = this()
        doUiThread {
            observable.value = false
        }
        result
    }
}

@Deprecated("Use the version in kotlin-observable instead.", replaceWith = ReplaceWith("captureProgress(observable)", "com.lightningkite.kotlin.observable"))
@JvmName("attachLoadingObservableInt")
fun <T> (() -> T).captureProgress(observable: MutableObservableProperty<Int>): (() -> T) {
    return {
        doUiThread {
            observable.value++
        }
        val result = this()
        doUiThread {
            observable.value--
        }
        result
    }
}

fun Context.progressDialog(title: Int? = null, message: Int, runningObs: ObservableProperty<Boolean>) {
    return standardDialog(title, message, listOf(), content = { stack ->
        lifecycle.bind(runningObs) {
            if (!it) stack.pop()
        }
        progressBar()
    }, dismissOnClickOutside = false)
}

fun Context.progressDialog(title: String? = null, message: String, runningObs: ObservableProperty<Boolean>) {
    return standardDialog(title, message, listOf(), content = { stack ->
        lifecycle.bind(runningObs) {
            if (!it) stack.pop()
        }
        progressBar()
    }, dismissOnClickOutside = false)
}

fun <T> (() -> T).captureProgressInDialog(view: View, title: Int? = null, message: Int): (() -> T)
        = captureProgressInDialog(view.context, title, message)

fun <T> (() -> T).captureProgressInDialog(context: Context, title: Int? = null, message: Int): (() -> T) {
    return {
        val runningObs = StandardObservableProperty(true)
        context.progressDialog(
                title = title,
                message = message,
                runningObs = runningObs
        )
        val response = this.invoke()
        doUiThread {
            runningObs.value = false
        }
        response
    }
}

fun <T> (() -> TypedResponse<T>).captureFailureInDialog(view: View, errorTextResource: Int, genericError: Int): () -> TypedResponse<T> = captureFailureInDialog(view.getActivity(), errorTextResource, genericError)

fun <T> (() -> TypedResponse<T>).captureFailureInDialog(activity: Activity?, errorTextResource: Int, genericError: Int): () -> TypedResponse<T> {
    return {
        val response = this.invoke()
        if (!response.isSuccessful()) {
            doUiThread {
                activity?.infoDialog(
                        activity.resources.getString(errorTextResource),
                        response.toHumanStringError(activity.resources.getString(genericError))
                )
            }
        }

        response
    }
}

fun JsonElement.toHumanString(modifier: (String) -> String? = { it }, combiningString: String = "\n"): String {
    var error: String = ""
    when (this) {
        is JsonPrimitive -> {
            error = if (this.isString)
                this.asString
            else
                this.toString()

        }
        is JsonObject -> {
            error = this.entrySet().map {
                val key = modifier(it.key)
                val value = modifier(it.value.toHumanString(modifier, combiningString))
                if (key == null) value
                else if (value == null) key
                else "$key: $value"
            }.joinToString(combiningString)
        }
        is JsonArray -> {
            error = this.map { it.toHumanString(modifier, combiningString) }.mapNotNull(modifier).joinToString(combiningString)
        }
    }
    return error
}

fun JsonElement.toHumanStringError() = toHumanString(modifier = {
    if (it.firstOrNull()?.isUpperCase() ?: false) it else null
})

fun TypedResponse<*>.toHumanStringError(fallback: String): String {
    if (errorBytes == null) return fallback
    return try {
        errorBytes?.toString(Charsets.UTF_8)?.toHumanStringError(fallback) ?: fallback
    } catch (e: Exception) {
        fallback
    }
}

fun String.toHumanStringError(fallback: String): String {
    return try {
        MyGson.json.parse(this).toHumanStringError()
    } catch (e: Exception) {
        fallback
    }
}