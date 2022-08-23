package com.kl3jvi.aprocessor.internal

import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic


object MappyLogger {
    fun ProcessingEnvironment.error(msg: String) {
        messager.printMessage(
            Diagnostic.Kind.ERROR, msg
        )
    }

}