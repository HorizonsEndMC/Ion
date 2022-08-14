package net.horizonsend.ion.common

/**
 * This annotation should be used anywhere which heavily relies on net.minecraft.server, reflection, or any other code
 * which is likely to break. This serves as a reminder to check for whenever there is a update.
 */
@Retention(AnnotationRetention.SOURCE)
@Deprecated("Likely to break as a result of Minecraft updates.")
annotation class UpdateUnsafe