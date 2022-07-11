package net.horizonsend.ion.common.annotations

/**
 * This annotation provides required metadata to the command manager.
 *
 * When annotating a Class: Indicates a Command
 *
 * When annotating a Inner Class: Indicates a Subcommand Group
 *
 * When annotating a Function: Indicates a Subcommand
 *
 * When annotating a Parameter: Indicates a Parameter
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandMeta(val name: String, val description: String)