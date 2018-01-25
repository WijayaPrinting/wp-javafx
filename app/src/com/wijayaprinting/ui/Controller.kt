package com.wijayaprinting.ui

import com.wijayaprinting.Language
import com.wijayaprinting.db.dao.Employee
import com.wijayaprinting.io.properties.ConfigFile
import javafx.fxml.FXML
import java.util.*

/** Base class of all controllers. */
abstract class Controller : Resourced, EmployeeHolder {

    override val resources: ResourceBundle = Language.from(ConfigFile.language.get()).resources
    override lateinit var _employee: Employee

    @FXML abstract fun initialize()

    private var extras: MutableMap<String, Any>? = null

    /** Register extra [value] with [key]. */
    fun addExtra(key: String, value: Any): Controller {
        if (extras == null) extras = mutableMapOf()
        extras!![key] = value
        return this
    }

    /** Get extra registered with [key], should be executed in platform thread. */
    fun <T : Any> getExtra(key: String): T {
        checkNotNull(extras) { "No extras added." }
        @Suppress("UNCHECKED_CAST") return checkNotNull(extras!![key] as T) { "Extra with that key is not found." }
    }
}