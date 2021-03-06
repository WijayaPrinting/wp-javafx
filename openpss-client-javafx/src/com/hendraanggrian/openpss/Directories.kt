package com.hendraanggrian.openpss

import java.io.File
import org.apache.commons.lang3.SystemUtils
import org.joda.time.DateTime

object MainDirectory : Directory(SystemUtils.USER_HOME, ".${BuildConfig2.ARTIFACT}")

object SettingsFile : File(MainDirectory, ".settings")

object WageDirectory : Directory(MainDirectory, "wage")

class WageFile : File(WageDirectory, "${DateTime.now().toString("yyyy-MM-dd HH.mm")}.png")

sealed class Directory : File {
    constructor(parent: String, child: String) : super(parent, child)
    constructor(parent: Directory, child: String) : super(parent, child)

    init {
        @Suppress("LeakingThis") mkdirs()
    }
}
