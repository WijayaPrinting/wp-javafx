package com.hendraanggrian.openpss.ui.invoice

import com.hendraanggrian.openpss.PATTERN_DATE
import com.hendraanggrian.openpss.R
import com.hendraanggrian.openpss.control.bold
import com.hendraanggrian.openpss.control.currencyCell
import com.hendraanggrian.openpss.control.dialog.ResultableDialog
import com.hendraanggrian.openpss.control.numberCell
import com.hendraanggrian.openpss.control.popover.ResultablePopover
import com.hendraanggrian.openpss.control.stringCell
import com.hendraanggrian.openpss.currencyConverter
import com.hendraanggrian.openpss.db.dbDateTime
import com.hendraanggrian.openpss.db.schemas.Customer
import com.hendraanggrian.openpss.db.schemas.Employee
import com.hendraanggrian.openpss.db.schemas.Invoice
import com.hendraanggrian.openpss.i18n.Resourced
import com.hendraanggrian.openpss.io.properties.PreferencesFile.INVOICE_QUICK_SELECT_CUSTOMER
import com.hendraanggrian.openpss.ui.invoice.order.AddOffsetPopover
import com.hendraanggrian.openpss.ui.invoice.order.AddOtherPopover
import com.hendraanggrian.openpss.ui.invoice.order.AddPlatePopover
import com.hendraanggrian.openpss.util.getColor
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.HPos.RIGHT
import javafx.scene.Node
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority.ALWAYS
import javafxx.application.later
import javafxx.beans.binding.`when`
import javafxx.beans.binding.doubleBindingOf
import javafxx.beans.binding.otherwise
import javafxx.beans.binding.stringBindingOf
import javafxx.beans.binding.then
import javafxx.beans.value.greater
import javafxx.beans.value.lessEq
import javafxx.beans.value.or
import javafxx.collections.isEmpty
import javafxx.coroutines.onAction
import javafxx.coroutines.onKeyPressed
import javafxx.layouts.LayoutManager
import javafxx.layouts.TableColumnsBuilder
import javafxx.layouts.button
import javafxx.layouts.columns
import javafxx.layouts.contextMenu
import javafxx.layouts.gridPane
import javafxx.layouts.label
import javafxx.layouts.separatorMenuItem
import javafxx.layouts.tableView
import javafxx.layouts.textArea
import javafxx.scene.control.cancelButton
import javafxx.scene.control.okButton
import javafxx.scene.input.isDelete
import javafxx.scene.layout.gap
import org.joda.time.DateTime

class AddInvoiceDialog(
    resourced: Resourced,
    private val employee: Employee
) : ResultableDialog<Invoice>(resourced, R.string.add_invoice, R.image.header_invoice) {

    private lateinit var plateTable: TableView<Invoice.Plate>
    private lateinit var offsetTable: TableView<Invoice.Offset>
    private lateinit var otherTable: TableView<Invoice.Other>
    private lateinit var noteArea: TextArea

    private val dateTime: DateTime = dbDateTime
    private val customerProperty: ObjectProperty<Customer> = SimpleObjectProperty(null)
    private val totalProperty: DoubleProperty = SimpleDoubleProperty()

    init {
        gridPane {
            gap = R.dimen.padding_small.toDouble()
            label(getString(R.string.employee)) col 0 row 0
            label(employee.name) { font = bold() } col 1 row 0
            label(getString(R.string.date)) col 2 row 0 hpriority ALWAYS halign RIGHT
            label(dateTime.toString(PATTERN_DATE)) { font = bold() } col 3 row 0
            label(getString(R.string.customer)) col 0 row 1
            button {
                textProperty().bind(stringBindingOf(customerProperty) {
                    customerProperty.value?.toString() ?: getString(R.string.search_customer)
                })
                onAction { _ ->
                    SearchCustomerPopover(this@AddInvoiceDialog).showAt(this@button) { customerProperty.set(it) }
                }
                if (INVOICE_QUICK_SELECT_CUSTOMER) fire()
            } col 1 row 1
            label(getString(R.string.plate)) col 0 row 2
            plateTable = invoiceTableView({ AddPlatePopover(this@AddInvoiceDialog) }) {
                columns {
                    column<Invoice.Plate, String>(R.string.machine, 64) { stringCell { machine } }
                    column<Invoice.Plate, String>(R.string.title, 256) { stringCell { title } }
                    column<Invoice.Plate, String>(R.string.qty, 64) { numberCell { qty } }
                    column<Invoice.Plate, String>(R.string.price, 416) { currencyCell { price } }
                    column<Invoice.Plate, String>(R.string.total, 128) { currencyCell { total } }
                }
            } col 1 row 2 colSpans 3
            label(getString(R.string.offset)) col 0 row 3
            offsetTable = invoiceTableView({ AddOffsetPopover(this@AddInvoiceDialog) }) {
                columns {
                    column<Invoice.Offset, String>(R.string.machine, 64) { stringCell { machine } }
                    column<Invoice.Offset, String>(R.string.title, 256) { stringCell { title } }
                    column<Invoice.Offset, String>(R.string.qty, 64) { numberCell { qty } }
                    column<Invoice.Offset, String>(R.string.technique, 128) {
                        stringCell { typedTechnique.toString(this@AddInvoiceDialog) }
                    }
                    column<Invoice.Offset, String>(R.string.min_qty, 64) { numberCell { minQty } }
                    column<Invoice.Offset, String>(R.string.min_price, 128) { currencyCell { minPrice } }
                    column<Invoice.Offset, String>(R.string.excess_price, 64) { currencyCell { excessPrice } }
                    column<Invoice.Offset, String>(R.string.total, 128) { currencyCell { total } }
                }
            } col 1 row 3 colSpans 3
            label(getString(R.string.others)) col 0 row 4
            otherTable = invoiceTableView({ AddOtherPopover(this@AddInvoiceDialog) }) {
                columns {
                    column<Invoice.Other, String>(R.string.title, 336) { stringCell { title } }
                    column<Invoice.Other, String>(R.string.qty, 64) { numberCell { qty } }
                    column<Invoice.Other, String>(R.string.price, 416) { currencyCell { price } }
                    column<Invoice.Other, String>(R.string.total, 128) { currencyCell { total } }
                }
            } col 1 row 4 colSpans 3
            totalProperty.bind(doubleBindingOf(plateTable.items, offsetTable.items, otherTable.items) {
                plateTable.items.sumByDouble { it.total } +
                    offsetTable.items.sumByDouble { it.total } +
                    otherTable.items.sumByDouble { it.total }
            })
            label(getString(R.string.note)) col 0 row 5
            noteArea = textArea {
                prefHeight = 48.0
            } col 1 row 5 colSpans 3
            label(getString(R.string.total)) col 0 row 6
            label {
                font = bold()
                textProperty().bind(stringBindingOf(totalProperty) {
                    currencyConverter.toString(totalProperty.value)
                })
                textFillProperty().bind(`when`(totalProperty greater 0)
                    then getColor(R.color.green)
                    otherwise getColor(R.color.red))
            } col 1 row 6
        }
        cancelButton()
        okButton().disableProperty().bind(customerProperty.isNull or totalProperty.lessEq(0))
    }

    override val optionalResult: Invoice?
        get() = Invoice.new(
            employee.id,
            customerProperty.value.id,
            dateTime,
            plateTable.items,
            offsetTable.items,
            otherTable.items,
            noteArea.text
        )

    private fun <S> LayoutManager<Node>.invoiceTableView(
        newAddOrderPopOver: () -> ResultablePopover<S>,
        init: TableView<S>.() -> Unit
    ): TableView<S> = tableView {
        prefHeight = 96.0
        init()
        prefWidth = columns.sumByDouble { it.minWidth } + 34 // just enough for vertical scrollbar
        contextMenu {
            getString(R.string.add)(ImageView(R.image.menu_add)) {
                onAction { _ -> newAddOrderPopOver().showAt(this@tableView) { this@tableView.items.add(it) } }
            }
            separatorMenuItem()
            getString(R.string.delete)(ImageView(R.image.menu_delete)) {
                later { disableProperty().bind(this@tableView.selectionModel.selectedItemProperty().isNull) }
                onAction { this@tableView.items.remove(this@tableView.selectionModel.selectedItem) }
            }
            getString(R.string.clear)(ImageView(R.image.menu_clear)) {
                later { disableProperty().bind(this@tableView.items.isEmpty) }
                onAction { this@tableView.items.clear() }
            }
        }
        onKeyPressed {
            if (it.code.isDelete() && selectionModel.selectedItem != null) items.remove(selectionModel.selectedItem)
        }
    }

    private fun <S, T> TableColumnsBuilder<S>.column(
        textId: String,
        minWidth: Int,
        init: TableColumn<S, T>.() -> Unit
    ): TableColumn<S, T> = column(getString(textId)) {
        this.minWidth = minWidth.toDouble()
        init()
    }
}