package com.lightningkite.kotlin.anko.full

import android.content.Context
import android.view.View
import com.lightningkite.kotlin.anko.lifecycle
import com.lightningkite.kotlin.anko.viewcontrollers.dialogs.standardDialog
import com.lightningkite.kotlin.async.doUiThread
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