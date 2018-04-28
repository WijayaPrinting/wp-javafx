package com.hendraanggrian.openpss.ui.invoice.price

import com.hendraanggrian.openpss.db.schemas.PlatePrice
import com.hendraanggrian.openpss.db.schemas.PlatePrices
import com.hendraanggrian.openpss.db.transaction
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import kotlinx.nosql.equal
import kotlinx.nosql.update
import ktfx.beans.property.asObservable
import ktfx.beans.property.toProperty
import ktfx.coroutines.onEditCommit
import ktfx.listeners.textFieldCellFactory
import java.net.URL
import java.util.ResourceBundle

class PlatePriceController : PriceController<PlatePrice, PlatePrices>(PlatePrices) {

    @FXML lateinit var priceColumn: TableColumn<PlatePrice, Double>

    override fun initialize(location: URL, resources: ResourceBundle) {
        super.initialize(location, resources)
        priceColumn.setCellValueFactory { it.value.price.toProperty().asObservable() }
        priceColumn.textFieldCellFactory {
            fromString { it.toDoubleOrNull() ?: 0.0 }
        }
        priceColumn.onEditCommit { cell ->
            transaction {
                PlatePrices.find { it.name.equal(cell.rowValue.name) }.projection { price }.update(cell.newValue)
            }
            cell.rowValue.price = cell.newValue
        }
    }

    override fun newPrice(name: String): PlatePrice = PlatePrice.new(name)
}