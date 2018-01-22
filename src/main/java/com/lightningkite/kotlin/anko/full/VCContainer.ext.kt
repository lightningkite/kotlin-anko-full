package com.lightningkite.kotlin.anko.full

import com.lightningkite.kotlin.anko.viewcontrollers.ViewController
import com.lightningkite.kotlin.anko.viewcontrollers.containers.VCContainer
import com.lightningkite.kotlin.observable.property.ObservableProperty
import com.lightningkite.kotlin.observable.property.transform

val VCContainer.currentObs: ObservableProperty<ViewController>
    get() = this.transform { it as ViewController }