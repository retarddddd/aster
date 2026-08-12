package cx.kloinn.aster.utils

import java.util.concurrent.ConcurrentLinkedQueue

class CrossThreadQueue<T : Any> {
    private val items = ConcurrentLinkedQueue<T>()

    fun add(item: T) {
        items.add(item)
    }

    fun drain(consume: (T) -> Unit) {
        while (true) {
            consume(items.poll() ?: return)
        }
    }
}
