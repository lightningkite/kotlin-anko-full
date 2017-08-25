package com.lightningkite.kotlin.anko.full

import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer
import com.lightningkite.kotlin.observable.property.VirtualObservableProperty
import java.util.*

private val VCContainer_currentObs = WeakHashMap<VCContainer, VirtualObservableProperty<ViewController>>()
val VCContainer.currentObs: VirtualObservableProperty<ViewController>
    get() {
        return VCContainer_currentObs.getOrPut(this) {
            VirtualObservableProperty(
                    getterFun = { this.current },
                    event = this.onSwap
            )
        }
    }