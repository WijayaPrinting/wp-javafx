package com.hendraanggrian.openpss.popup.popover

import com.hendraanggrian.openpss.control.DateBox
import com.hendraanggrian.openpss.control.dateBox
import com.hendraanggrian.openpss.i18n.Resourced
import org.joda.time.LocalDate

class DatePopover(
    resourced: Resourced,
    titleId: String,
    prefill: LocalDate = LocalDate.now()
) : ResultablePopover<LocalDate>(resourced, titleId) {

    private val dateBox: DateBox = dateBox(prefill)

    override val nullableResult: LocalDate? get() = dateBox.valueProperty().value
}