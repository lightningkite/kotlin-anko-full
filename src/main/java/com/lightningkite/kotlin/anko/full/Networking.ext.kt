package com.lightningkite.kotlin.anko.full

import android.app.Activity
import android.content.Context
import android.view.View
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.lightningkite.kotlin.anko.async.UIThread
import com.lightningkite.kotlin.anko.getActivity
import com.lightningkite.kotlin.anko.lifecycle
import com.lightningkite.kotlin.anko.snackbar
import com.lightningkite.kotlin.anko.viewcontrollers.dialogs.infoDialog
import com.lightningkite.kotlin.anko.viewcontrollers.dialogs.standardDialog
import com.lightningkite.kotlin.networking.TypedResponse
import com.lightningkite.kotlin.networking.jackson.MyJackson
import com.lightningkite.kotlin.observable.property.MutableObservableProperty
import com.lightningkite.kotlin.observable.property.ObservableProperty
import com.lightningkite.kotlin.observable.property.StandardObservableProperty
import com.lightningkite.kotlin.observable.property.bind
import org.jetbrains.anko.progressBar
import java.util.concurrent.Executor

fun <T> (() -> T).captureProgress(observable: MutableObservableProperty<Boolean>, executor: Executor = UIThread): (() -> T) {
    return {
        executor.execute {
            observable.value = true
        }
        val result = this()
        executor.execute {
            observable.value = false
        }
        result
    }
}

@JvmName("attachLoadingObservableInt")
fun <T> (() -> T).captureProgress(observable: MutableObservableProperty<Int>, executor: Executor = UIThread): (() -> T) {
    return {
        executor.execute {
            observable.value++
        }
        val result = this()
        executor.execute {
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
        UIThread.execute {
            runningObs.value = false
        }
        response
    }
}

fun <T> (() -> TypedResponse<T>).captureFailureInSnackbar(view: View, genericError: Int): () -> TypedResponse<T> {
    return {
        val response = this.invoke()
        if (!response.isSuccessful()) {
            UIThread.execute {
                view.snackbar(response.toHumanStringError(view.resources.getString(genericError)))
            }
        }

        response
    }
}

fun <T> (() -> TypedResponse<T>).captureFailureInDialog(view: View, errorTextResource: Int, genericError: Int): () -> TypedResponse<T>
        = captureFailureInDialog(view.getActivity(), errorTextResource, genericError)

fun <T> (() -> TypedResponse<T>).captureFailureInDialog(activity: Activity?, errorTextResource: Int, genericError: Int): () -> TypedResponse<T> {
    return {
        val response = this.invoke()
        if (!response.isSuccessful()) {
            UIThread.execute {
                activity?.infoDialog(
                        activity.resources.getString(errorTextResource),
                        response.toHumanStringError(activity.resources.getString(genericError))
                )
            }
        }

        response
    }
}

fun JsonNode.toHumanString(modifier: (String) -> String? = { it }, combiningString: String = "\n"): String {
    var error: String = ""
    when (this) {
        is ObjectNode -> {
            error = this.fields().asSequence().map {
                val key = modifier(it.key)
                val value = modifier(it.value.toHumanString(modifier, combiningString))
                if (key == null) value
                else if (value == null) key
                else "$key: $value"
            }.joinToString(combiningString)
        }
        is ArrayNode -> {
            error = this.map { it.toHumanString(modifier, combiningString) }.mapNotNull(modifier).joinToString(combiningString)
        }
        else -> this.asText()
    }
    return error
}

fun JsonNode.toHumanStringError() = toHumanString(modifier = { if (it.firstOrNull()?.isUpperCase() ?: false) it else null })

fun TypedResponse<*>.toHumanStringError(fallback: String): String {
    if (errorBytes == null) return fallback
    return try {
        errorBytes?.toString(Charsets.UTF_8)?.toHumanStringError(fallback) ?: fallback
    } catch(e: Exception) {
        fallback
    }
}

fun String.toHumanStringError(fallback: String): String {
    return try {
        MyJackson.mapper.valueToTree<JsonNode>(this).toHumanStringError()
    } catch(e: Exception) {
        fallback
    }
}