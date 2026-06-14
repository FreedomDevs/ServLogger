package dev.elysium.servlogger.database

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Unsafe database API"
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class UnsafeDatabaseApi