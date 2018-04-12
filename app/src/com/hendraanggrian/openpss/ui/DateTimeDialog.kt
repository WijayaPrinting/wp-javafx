package com.hendraanggrian.openpss.ui

import com.hendraanggrian.openpss.R
import com.hendraanggrian.openpss.scene.layout.DateBox
import com.hendraanggrian.openpss.scene.layout.TimeBox
import com.hendraanggrian.openpss.scene.layout.dateBox
import com.hendraanggrian.openpss.scene.layout.timeBox
import com.hendraanggrian.openpss.ui.wage.Record.Companion.WORKING_HOURS
import javafx.scene.control.ButtonType.OK
import javafx.scene.control.Dialog
import javafx.scene.image.ImageView
import ktfx.application.later
import ktfx.coroutines.onAction
import ktfx.layouts.button
import ktfx.layouts.gridPane
import ktfx.scene.control.cancelButton
import ktfx.scene.control.graphicIcon
import ktfx.scene.control.headerTitle
import ktfx.scene.control.okButton
import ktfx.scene.layout.gap
import org.joda.time.DateTime
import org.joda.time.DateTime.now

class DateTimeDialog(
    resourced: Resourced,
    headerId: String,
    prefill: DateTime = now()
) : Dialog<DateTime>(), Resourced by resourced {

    private lateinit var dateBox: DateBox
    private lateinit var timeBox: TimeBox

    init {
        headerTitle = getString(headerId)
        graphicIcon = ImageView(R.image.ic_date)
        dialogPane.content = gridPane {
            gap = 8.0
            dateBox = dateBox(prefill.toLocalDate()) row 0 col 1
            button("-$WORKING_HOURS") {
                onAction { repeat(WORKING_HOURS) { timeBox.previousButton.fire() } }
            } row 1 col 0
            timeBox = timeBox(prefill.toLocalTime()) {
                setOnOverlap { plus ->
                    dateBox.picker.value = when {
                        plus -> dateBox.picker.value.plusDays(1)
                        else -> dateBox.picker.value.minusDays(1)
                    }
                }
            } row 1 col 1
            button("+$WORKING_HOURS") {
                onAction { repeat(WORKING_HOURS) { timeBox.nextButton.fire() } }
            } row 1 col 2
        }
        later { dateBox.requestFocus() }
        cancelButton()
        okButton()
        setResultConverter {
            if (it != OK) null else DateTime(
                dateBox.value.year,
                dateBox.value.monthOfYear,
                dateBox.value.dayOfMonth,
                timeBox.value.hourOfDay,
                timeBox.value.minuteOfHour
            )
        }
    }
}