package dev.elysium.servlogger.database

import dev.elysium.servlogger.database.actions.LogAction
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class DatabaseQueue(val database: ServLoggerDatabase) {
    private val queue = ConcurrentLinkedQueue<LogAction>()
    private val handlers = mutableMapOf<String, LogHandler>()
    private val registeredLogActions = mutableMapOf<Class<out LogAction>, LogHandler>()
    var disabledHandlers = listOf<String>()

    fun addToQueue(action: LogAction) {
        val handler = registeredLogActions[action::class.java]
            ?: throw IllegalArgumentException("Trying to add action ${action::class.java.name}, handler for this type not found")
        if (disabledHandlers.contains(handler.name)) return

        queue.add(action)
    }

    fun registerHandler(handler: LogHandler) {
        if (handlers[handler.name] != null) {
            throw IllegalArgumentException("Error registering ${handler.name}: handler with same name already registered")
        }

        for (logAction in handler.acceptedActions) {
            if (registeredLogActions[logAction] != null) {
                throw IllegalArgumentException("Error registering ${handler.name}: handler with same logAction (${logAction.name}) already registered")
            }
        }

        handlers[handler.name] = handler
        for (logAction in handler.acceptedActions) {
            registeredLogActions[logAction] = handler
        }
    }

    fun unregisterHandler(handler: LogHandler) {
        if (handlers[handler.name] == null) {
            throw IllegalArgumentException("Error unregistering ${handler.name}: handler with this name not found")
        }

        handlers.remove(handler.name)
        for (logAction in handler.acceptedActions) {
            registeredLogActions.remove(logAction)
        }
    }

    private val isFlushing = AtomicBoolean(false)
    fun flushQueue() {
        if (!isFlushing.compareAndSet(false, true)) {
            return
        }

        @OptIn(UnsafeDatabaseApi::class)
        val writeConnection = database.getAnyConnection()

        try {
            if (queue.isEmpty()) return

            val batch = mutableListOf<LogAction>()
            while (!queue.isEmpty()) {
                batch.add(queue.poll() ?: break)
            }

            writeConnection.autoCommit = false
            for (handler in handlers) {
                if (!disabledHandlers.contains(handler.key)) {
                    val actions = batch.filter { item -> item::class.java in handler.value.acceptedActions }
                    if (!actions.isEmpty())
                        handler.value.process(actions, database)
                }
            }
            writeConnection.commit()

        } catch (e: Exception) {
            writeConnection.rollback()
            e.printStackTrace()
        } finally {
            writeConnection.autoCommit = true
            isFlushing.set(false)
        }
    }
}