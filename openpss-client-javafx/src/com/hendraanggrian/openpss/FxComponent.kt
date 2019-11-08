package com.hendraanggrian.openpss

import com.hendraanggrian.defaults.WritableDefaults
import com.hendraanggrian.openpss.api.OpenPSSApi
import com.hendraanggrian.openpss.schema.Employee
import com.hendraanggrian.openpss.schema.GlobalSetting
import com.hendraanggrian.openpss.ui.ResultableDialog
import java.awt.Desktop
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.PasswordField
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.util.StringConverter
import javafx.util.converter.CurrencyStringConverter
import javafx.util.converter.NumberStringConverter
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ktfx.bindings.isBlank
import ktfx.bindings.or
import ktfx.collections.toObservableList
import ktfx.jfoenix.controls.jfxSnackbar
import ktfx.jfoenix.layouts.jfxComboBox
import ktfx.jfoenix.layouts.jfxPasswordField
import ktfx.layouts.gap
import ktfx.layouts.gridPane
import ktfx.layouts.label

/** StackPane is the root layout for [ktfx.jfoenix.jfxSnackbar]. */
interface FxComponent : Component<StackPane, WritableDefaults>, StringResources, ValueResources {

    /** Number decimal string converter. */
    val numberConverter: StringConverter<Number>
        get() = NumberStringConverter()

    /** Number decimal with currency prefix string converter. */
    val currencyConverter: StringConverter<Number>
        get() = CurrencyStringConverter(runBlocking(Dispatchers.IO) {
            Language.ofFullCode(
                OpenPSSApi.getSetting(GlobalSetting.KEY_LANGUAGE).value
            ).toLocale()
        })

    /** Returns [Desktop] instance, may be null if it is unsupported. */
    val desktop: Desktop?
        get() {
            if (!Desktop.isDesktopSupported()) {
                rootLayout.jfxSnackbar(
                    "java.awt.Desktop is not supported.",
                    getLong(R.value.duration_short)
                )
                return null
            }
            return Desktop.getDesktop()
        }

    suspend fun CoroutineScope.withPermission(
        context: CoroutineContext = Dispatchers.JavaFx,
        action: suspend CoroutineScope.() -> Unit
    ) {
        when {
            OpenPSSApi.isAdmin(login) -> action()
            else -> PermissionDialog(this@FxComponent).show { admin ->
                when (admin) {
                    null -> rootLayout.jfxSnackbar(
                        getString(R2.string.invalid_password),
                        getLong(R.value.duration_short)
                    )
                    else -> launch(context) { action() }
                }
            }
        }
    }

    fun getColor(id: String): Color = Color.web(valueProperties.getProperty(id))

    private class PermissionDialog(component: FxComponent) :
        ResultableDialog<Employee>(component, R2.string.permission_required) {

        private val adminCombo: ComboBox<Employee>
        private val passwordField: PasswordField

        override val focusedNode: Node? get() = adminCombo

        init {
            gridPane {
                gap = getDouble(R.value.padding_medium)
                label {
                    gridAt(0, 0)
                    colSpans = 2
                    text = getString(R2.string._permission_required)
                }
                label(getString(R2.string.admin)) {
                    gridAt(1, 0)
                }
                adminCombo = jfxComboBox(runBlocking(Dispatchers.IO) { OpenPSSApi.getEmployees() }
                    .filter { it.isAdmin }
                    .toObservableList()
                ) {
                    gridAt(1, 1)
                    promptText = getString(R2.string.admin)
                }
                label(getString(R2.string.password)) {
                    gridAt(2, 0)
                }
                passwordField = jfxPasswordField {
                    gridAt(2, 1)
                    promptText = getString(R2.string.password)
                }
            }
            defaultButton.disableProperty().bind(
                adminCombo.valueProperty().isNull or passwordField.textProperty().isBlank()
            )
        }

        override val nullableResult: Employee?
            get() = runBlocking(Dispatchers.IO) { OpenPSSApi.login(adminCombo.value.name, passwordField.text) }
    }
}
