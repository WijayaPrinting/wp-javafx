package com.hendraanggrian.openpss.ui.employee

import com.hendraanggrian.openpss.R
import com.hendraanggrian.openpss.db.schema.Employee
import com.hendraanggrian.openpss.db.schema.Employee.Companion.DEFAULT_PASSWORD
import com.hendraanggrian.openpss.db.schema.Employees
import com.hendraanggrian.openpss.db.transaction
import com.hendraanggrian.openpss.ui.AddUserDialog
import com.hendraanggrian.openpss.ui.Controller
import com.hendraanggrian.openpss.ui.Refreshable
import com.hendraanggrian.openpss.ui.main.ResetPasswordDialog
import com.hendraanggrian.openpss.util.tidy
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ButtonType.NO
import javafx.scene.control.ButtonType.YES
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import kfx.application.exit
import kfx.beans.property.toProperty
import kfx.collections.toMutableObservableList
import kfx.coroutines.onEditCommit
import kfx.scene.control.choiceBoxCellFactory
import kfx.scene.control.confirmAlert
import kfx.scene.control.infoAlert
import kotlinx.nosql.equal
import kotlinx.nosql.mongodb.MongoDBSession
import kotlinx.nosql.update

class EmployeeController : Controller(), Refreshable {

    @FXML lateinit var fullAccessButton: Button
    @FXML lateinit var resetPasswordButton: Button
    @FXML lateinit var deleteButton: Button

    @FXML lateinit var employeeTable: TableView<Employee>
    @FXML lateinit var nameColumn: TableColumn<Employee, String>
    @FXML lateinit var fullAccessColumn: TableColumn<Employee, String>

    override fun initialize() {
        arrayOf(fullAccessButton, resetPasswordButton, deleteButton).forEach {
            it.disableProperty().bind(employeeTable.selectionModel.selectedItemProperty().isNull)
        }

        nameColumn.setCellValueFactory { it.value.name.toProperty() }
        fullAccessColumn.setCellValueFactory {
            getString(if (it.value.fullAccess) R.string.yes else R.string.no).toProperty()
        }
        fullAccessColumn.choiceBoxCellFactory(*getStringArray(R.string.yes, R.string.no))
        fullAccessColumn.onEditCommit { event ->
            val result = event.newValue == getString(R.string.yes)
            transaction { Employees.find { name.equal(event.rowValue.name) }.projection { fullAccess }.update(result) }
            event.rowValue.fullAccess = result
        }
        refresh()
    }

    override fun refresh() {
        employeeTable.items = transaction { Employees.find().toMutableObservableList() }
    }

    @FXML fun add() = AddUserDialog(this, getString(R.string.add_employee)).showAndWait().ifPresent { name ->
        val employee = Employee(name.tidy())
        employee.id = transaction { Employees.insert(employee) }!!
        employeeTable.items.add(employee)
        employeeTable.selectionModel.select(employee)
    }

    @FXML fun fullAccess() = confirm({ employee ->
        Employees.find { name.equal(employee.name) }.projection { fullAccess }.update(!employee.fullAccess)
    })

    @FXML fun resetPassword() = confirm({ employee ->
        Employees.find { name.equal(employee.name) }.projection { password }.update(DEFAULT_PASSWORD)
    }) {
        ResetPasswordDialog(this).showAndWait().ifPresent { newPassword ->
            transaction {
                Employees.find { name.equal(employeeName) }.projection { password }.update(newPassword)
                infoAlert(getString(R.string.change_password_successful)).showAndWait()
            }
        }
    }

    @FXML fun delete() = confirm({ employee ->
        Employees.find { name.equal(employee.name) }.remove()
    })

    private fun confirm(
        confirmedAction: MongoDBSession.(Employee) -> Unit,
        isNotSelfAction: () -> Unit = {
            infoAlert(getString(R.string.please_restart)).showAndWait().ifPresent {
                exit()
            }
        }
    ) = confirmAlert(getString(R.string.are_you_sure), YES, NO)
        .showAndWait()
        .filter { it == YES }
        .ifPresent {
            employeeTable.selectionModel.selectedItem.let { employee ->
                transaction { confirmedAction(employee) }
                when {
                    employee.name != employeeName -> refresh()
                    else -> isNotSelfAction()
                }
            }
        }
}