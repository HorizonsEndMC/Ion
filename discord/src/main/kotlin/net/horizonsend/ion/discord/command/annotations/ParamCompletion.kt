package net.horizonsend.ion.discord.command.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
annotation class ParamCompletion(vararg val values: String)

