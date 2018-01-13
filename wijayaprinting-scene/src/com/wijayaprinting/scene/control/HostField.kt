@file:Suppress("NOTHING_TO_INLINE", "UNUSED")

package com.wijayaprinting.scene.control

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.TextField
import kotfx.*
import org.apache.commons.validator.routines.InetAddressValidator.getInstance

open class HostField : TextField() {

    val validProperty = SimpleBooleanProperty()

    init {
        validProperty bind booleanBindingOf(textProperty()) { getInstance().isValidInet4Address(text) }
    }

    val isValid: Boolean get() = validProperty.value
}

@JvmOverloads inline fun hostField(noinline init: ((@KotfxDsl HostField).() -> Unit)? = null): HostField = HostField().apply { init?.invoke(this) }
@JvmOverloads inline fun ChildRoot.hostField(noinline init: ((@KotfxDsl HostField).() -> Unit)? = null): HostField = HostField().apply { init?.invoke(this) }.add()
@JvmOverloads inline fun ItemRoot.hostField(noinline init: ((@KotfxDsl HostField).() -> Unit)? = null): HostField = HostField().apply { init?.invoke(this) }.add()