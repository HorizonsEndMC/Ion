package net.horizonsend.ion.server.annotations
/**
 * This annotation should be used when using any code from IonCore in anywhere but IonCore
 * Code annotated with this should be replaced if practical to do so.
 */
@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Deprecated("Code using IonCore, which wont work without it")
annotation class IonCore
