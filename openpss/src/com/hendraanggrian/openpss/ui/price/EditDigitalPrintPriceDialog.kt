package com.hendraanggrian.openpss.ui.price

import com.hendraanggrian.openpss.R
import com.hendraanggrian.openpss.content.Context
import com.hendraanggrian.openpss.db.schemas.DigitalPrice
import com.hendraanggrian.openpss.db.schemas.DigitalPrices
import com.hendraanggrian.openpss.db.transaction
import javafx.beans.value.ObservableValue
import kotlinx.nosql.equal
import kotlinx.nosql.update
import ktfx.cells.textFieldCellFactory
import ktfx.coroutines.onEditCommit
import ktfx.doublePropertyOf
import ktfx.text.buildStringConverter

@Suppress("UNCHECKED_CAST")
class EditDigitalPrintPriceDialog(
    context: Context
) : EditPriceDialog<DigitalPrice, DigitalPrices>(context, R.string.digital_print_price, DigitalPrices) {

    init {
        getString(R.string.one_side_price)<Double> {
            minWidth = 128.0
            style = "-fx-alignment: center-right;"
            setCellValueFactory { doublePropertyOf(it.value.oneSidePrice) as ObservableValue<Double> }
            textFieldCellFactory(buildStringConverter { fromString { it.toDoubleOrNull() ?: 0.0 } })
            onEditCommit { cell ->
                transaction {
                    DigitalPrices { it.name.equal(cell.rowValue.name) }
                        .projection { oneSidePrice }
                        .update(cell.newValue)
                }
                cell.rowValue.oneSidePrice = cell.newValue
            }
        }
        getString(R.string.two_side_price)<Double> {
            minWidth = 128.0
            style = "-fx-alignment: center-right;"
            setCellValueFactory { doublePropertyOf(it.value.twoSidePrice) as ObservableValue<Double> }
            textFieldCellFactory(buildStringConverter { fromString { it.toDoubleOrNull() ?: 0.0 } })
            onEditCommit { cell ->
                transaction {
                    DigitalPrices { it.name.equal(cell.rowValue.name) }
                        .projection { twoSidePrice }
                        .update(cell.newValue)
                }
                cell.rowValue.twoSidePrice = cell.newValue
            }
        }
    }

    override fun newPrice(name: String): DigitalPrice = DigitalPrice.new(name)
}
