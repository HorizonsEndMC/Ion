package net.horizonsend.ion.proxy.commands.discord

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
annotation class ParamCompletion(vararg val values: String)

