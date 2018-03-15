@file:Suppress("NOTHING_TO_INLINE", "UNUSED")

package com.hendraanggrian.openpss.scene.control

import com.hendraanggrian.openpss.scene.control.FileField.Scope
import com.hendraanggrian.openpss.scene.control.FileField.Scope.FILE
import com.hendraanggrian.openpss.scene.control.FileField.Scope.FOLDER
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TextField
import kfx.beans.binding.bindingOf
import kfx.beans.binding.booleanBindingOf
import kfx.layouts.ChildManager
import kfx.layouts.ItemManager
import kfx.layouts.LayoutDsl
import java.io.File

/** Field that display file or directory path. */
open class FileField(scope: Scope = FILE) : TextField() {

    val fileProperty: ObjectProperty<File> = SimpleObjectProperty<File>()
    val validProperty: BooleanProperty = SimpleBooleanProperty()

    init {
        fileProperty.bind(bindingOf(textProperty()) { File(text) })
        validProperty.bind(booleanBindingOf(textProperty()) {
            !file.exists() || when (scope) {
                FILE -> !file.isFile
                FOLDER -> !file.isDirectory
                else -> false
            }
        })
    }

    val file: File get() = fileProperty.get()

    val isValid: Boolean get() = validProperty.get()

    enum class Scope {
        FILE, FOLDER, ANY
    }
}

inline fun fileField(scope: Scope = FILE, noinline init: ((@LayoutDsl FileField).() -> Unit)? = null): FileField = FileField(scope).apply { init?.invoke(this) }
inline fun ChildManager.fileField(scope: Scope = FILE, noinline init: ((@LayoutDsl FileField).() -> Unit)? = null): FileField = FileField(scope).apply { init?.invoke(this) }.add()
inline fun ItemManager.fileField(scope: Scope = FILE, noinline init: ((@LayoutDsl FileField).() -> Unit)? = null): FileField = FileField(scope).apply { init?.invoke(this) }.add()