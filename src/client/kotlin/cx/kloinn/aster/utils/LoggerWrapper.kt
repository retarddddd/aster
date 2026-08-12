package cx.kloinn.aster.utils

import org.slf4j.Logger

class LoggerWrapper(val sourceLogger: Logger) {
    fun formatMessage(message: String): String {
        return "[Aster] $message"
    }

    fun info(message: String) {
        sourceLogger.info(formatMessage(message))
    }

    fun warning(message: String) {
        sourceLogger.warn(formatMessage(message))
    }

    fun error(message: String) {
        sourceLogger.error(formatMessage(message))
    }
}