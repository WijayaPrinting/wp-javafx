package com.hendraanggrian.openpss.ui.wage

import com.hendraanggrian.openpss.R
import com.hendraanggrian.openpss.controls.Popup
import com.hendraanggrian.openpss.db.schemas.Recesses
import com.hendraanggrian.openpss.db.transaction
import com.hendraanggrian.openpss.resources.Resourced
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Separator
import ktfx.beans.value.or
import ktfx.collections.mutableObservableListOf
import ktfx.collections.toObservableList
import ktfx.coroutines.onAction
import ktfx.layouts.button
import ktfx.layouts.choiceBox
import ktfx.layouts.gridPane
import ktfx.layouts.label
import ktfx.scene.layout.gap

class DisableRecessPopup(
    resourced: Resourced,
    private val attendeePanes: List<AttendeePane>
) : Popup<Nothing>(resourced, R.string.disable_recess) {

    private lateinit var recessChoice: ChoiceBox<*>
    private lateinit var roleChoice: ChoiceBox<*>

    override val content: Node = gridPane {
        gap = 8.0
        label(getString(R.string.recess)) col 0 row 0
        transaction {
            recessChoice = choiceBox(mutableObservableListOf(getString(R.string.all),
                Separator(),
                *Recesses().toObservableList().toTypedArray())
            ) { selectionModel.selectFirst() } col 1 row 0
        }
        label(getString(R.string.employee)) col 0 row 1
        roleChoice = choiceBox(mutableObservableListOf(
            *attendees.filter { it.role != null }.map { it.role!! }.distinct().toTypedArray(),
            Separator(),
            *attendees.toTypedArray())) col 1 row 1
    }

    override val buttons: List<Button> = listOf(button("Apply") {
        disableProperty().bind(recessChoice.valueProperty().isNull or roleChoice.valueProperty().isNull)
        onAction {
            attendeePanes.filter {
                when {
                    roleChoice.value is String -> it.attendee.role == roleChoice.value
                    else -> it.attendee == roleChoice.value as Attendee
                }
            }.map { it.recessChecks }.forEach {
                (when {
                    recessChoice.value is String -> it
                    else -> it.filter { it.text == recessChoice.value.toString() }
                }).forEach { it.isSelected = false }
            }
        }
    })

    private inline val attendees: List<Attendee> get() = attendeePanes.map { it.attendee }
}