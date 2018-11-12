@file:Suppress("NOTHING_TO_INLINE")

package com.hendraanggrian.openpss.control

import com.hendraanggrian.openpss.control.base.StretchableLabeled
import com.jfoenix.controls.JFXButton
import javafx.beans.DefaultProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.Node
import ktfx.LayoutDsl
import ktfx.NodeInvokable

/**
 * A button that will display log when the window have sufficient width.
 * When it doesn't, [StretchableButton] will store its log as tooltip.
 */
@DefaultProperty("graphic")
class StretchableButton @JvmOverloads constructor(
    stretchPoint: Double = -1.0,
    stretchableText: String? = null,
    graphic: Node? = null
) : JFXButton(null, graphic), StretchableLabeled {

    private val stretchPointProperty = SimpleDoubleProperty(stretchPoint)
    override fun stretchPointProperty(): DoubleProperty = stretchPointProperty

    private val stretchableTextProperty = SimpleStringProperty(stretchableText)
    override fun stretchableTextProperty(): StringProperty = stretchableTextProperty

    init {
        initialize()
        styleClass += "flat"
    }
}

/** Creates an [StretchableButton]. */
fun stretchableButton(
    stretchPoint: Double,
    adaptableText: String,
    graphic: Node? = null,
    init: ((@LayoutDsl StretchableButton).() -> Unit)? = null
): StretchableButton = StretchableButton(stretchPoint, adaptableText, graphic).also {
    init?.invoke(it)
}

/** Creates an [StretchableButton] and add it to this manager. */
inline fun NodeInvokable.stretchableButton(
    stretchPoint: Double,
    adaptableText: String,
    graphic: Node? = null,
    noinline init: ((@LayoutDsl StretchableButton).() -> Unit)? = null
): StretchableButton =
    com.hendraanggrian.openpss.control.stretchableButton(stretchPoint, adaptableText, graphic, init)()