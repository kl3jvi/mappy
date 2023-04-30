package com.kl3jvi.aprocessor.logger

import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

object Logger {

    internal var retainedInstance: LoggerImpl? = null

    @Synchronized
    fun retainInstance(
        pe: ProcessingEnvironment,
        messagePrefix: String
    ) {
        if (retainedInstance == null) {
            retainedInstance = LoggerImpl(pe, messagePrefix)
        }
    }

    @Synchronized
    fun clearInstance() {
        retainedInstance = null
    }
}

internal class LoggerImpl internal constructor(
    pe: ProcessingEnvironment,
    private val messagePrefix: String
) {

    private val messager: Messager = pe.messager

    fun log(message: String) {
        val prefixedMessage = message.addPrefix()
        messager.printMessage(Diagnostic.Kind.NOTE, prefixedMessage)
    }

    fun error(message: String) {
        val prefixedMessage = message.addPrefix()
        messager.printMessage(Diagnostic.Kind.ERROR, prefixedMessage)
    }

    fun warning(message: String) {
        val prefixedMessage = message.addPrefix()
        messager.printMessage(Diagnostic.Kind.WARNING, prefixedMessage)
    }

    private fun String.addPrefix(): String = "$messagePrefix:\n$this\n"
}

fun compilerLog(message: String) {
    checkNotNull(Logger.retainedInstance) { "No retained instance" }
    Logger.retainedInstance!!.log(message)
}

fun compilerError(message: String) {
    checkNotNull(Logger.retainedInstance) { "No retained instance" }
    Logger.retainedInstance!!.error(message)
}

fun compilerWarning(message: String) {
    checkNotNull(Logger.retainedInstance) { "No retained instance" }
    Logger.retainedInstance!!.warning(message)
}
