package net.horizonsend.ion.common.configuration

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigurationName(val name: String)