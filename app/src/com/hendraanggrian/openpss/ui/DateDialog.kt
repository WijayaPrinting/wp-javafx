package com.hendraanggrian.openpss.ui

import com.hendraanggrian.openpss.R
import com.hendraanggrian.openpss.scene.layout.dateBox
import javafx.scene.control.ButtonType.OK
import javafx.scene.control.Dialog
import javafx.scene.image.ImageView
import ktfx.application.later
import ktfx.scene.control.cancelButton
import ktfx.scene.control.graphicIcon
import ktfx.scene.control.headerTitle
import ktfx.scene.control.okButton
import org.joda.time.LocalDate
import org.joda.time.LocalDate.now

class DateDialog(
    resourced: Resourced,
    headerId: String,
    prefill: LocalDate = now()
) : Dialog<LocalDate>(), Resourced by resourced {

    private val dateBox = dateBox(prefill)

    init {
        headerTitle = getString(headerId)
        graphicIcon = ImageView(R.image.ic_date)
        dialogPane.content = dateBox
        later { dateBox.requestFocus() }
        cancelButton()
        okButton()
        setResultConverter { if (it != OK) null else dateBox.valueProperty.value }
    }
}