package com.wijayaprinting.javafx

import com.wijayaprinting.javafx.dialog.LoginDialog
import com.wijayaprinting.javafx.io.JavaFXFile
import javafx.application.Application
import javafx.stage.Stage
import kotfx.runLater
import org.apache.commons.lang3.SystemUtils.IS_OS_MAC_OSX
import java.util.*

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
abstract class WPApp : Application() {

    protected lateinit var dialog: LoginDialog
    protected lateinit var resources: ResourceBundle

    abstract fun onStart()
    abstract fun onSuccess(employeeName: String, stage: Stage)

    override fun init() {
        resources = Language.parse(JavaFXFile()[JavaFXFile.LANGUAGE].value).getResources("javafx")
        runLater {
            dialog = LoginDialog(resources)
            onStart()
        }
    }

    override fun start(primaryStage: Stage) {
        dialog.showAndWait()
                .filter { it is String }
                .ifPresent { onSuccess(it as String, primaryStage) }
    }

    protected fun setImageOnOSX(image: java.awt.Image) {
        if (IS_OS_MAC_OSX) {
            Class.forName("com.apple.eawt.Application")
                    .newInstance()
                    .javaClass
                    .getMethod("getApplication")
                    .invoke(null).let { application ->
                application.javaClass
                        .getMethod("setDockIconImage", java.awt.Image::class.java)
                        .invoke(application, image)
            }
        }
    }
}