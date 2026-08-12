package cx.kloinn.aster.utils

private fun getTime(): Long {
    return System.currentTimeMillis()
}

class Clock(private var initTime: Long = getTime()) {
    fun hasTimePassed(durationMillis: Long): Boolean {
        val now = getTime()

        if (now - initTime >= durationMillis) {
            initTime = now
            return true
        } else {
            return false
        }
    }
}