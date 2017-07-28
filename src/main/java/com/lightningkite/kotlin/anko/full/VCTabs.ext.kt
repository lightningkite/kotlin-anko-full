package com.lightningkite.kotlin.anko.full

import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCTabs
import com.lightningkite.kotlin.observable.property.MutableObservableProperty

val VCTabs.indexObs: MutableObservableProperty<Int> get() = object : MutableObservableProperty<Int>, MutableList<(Int) -> Unit> by this.onIndexChange {
    override var value: Int
        get() = this@indexObs.index
        set(value) {
            this@indexObs.index = value
        }
}