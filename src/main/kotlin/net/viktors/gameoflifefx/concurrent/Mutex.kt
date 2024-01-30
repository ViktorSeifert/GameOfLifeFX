package net.viktors.gameoflifefx.concurrent

import java.util.concurrent.locks.ReentrantLock

class Mutex {
    private val lock = ReentrantLock()

    fun withLock(action: () -> Unit) {
        try {
            lock.lock()
            action()
        } finally {
            lock.unlock()
        }
    }
}