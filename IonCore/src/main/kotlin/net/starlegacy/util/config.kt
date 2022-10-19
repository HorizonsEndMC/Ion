package net.starlegacy.util

import com.google.gson.Gson
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import io.github.config4k.extract
import io.github.config4k.toConfig
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import net.starlegacy.PLUGIN

inline fun <reified T : Any> loadConfig(parent: File, name: String): T {
	parent.mkdirs()
	val jsonFile = File(parent, "$name.json")
	val json = jsonFile.exists()

	val file = if (json) jsonFile else File(parent, "$name.conf")

	// If the file exists, read it from the file, otherwise instantiate it.
	// If new values were added they will remain default
	val config = if (file.exists()) {
		FileReader(file).use {
			if (json) Gson().fromJson(it, T::class.java)
			else ConfigFactory.parseString(it.readText()).extract<T>(name)
		}
	} else T::class.java.newInstance()

	// Write it with proper formatting
	try {
		saveConfig(config, parent, name)
	} catch (exception: Exception) {
		PLUGIN.slF4JLogger.warn("Failed to save config file, this could be an issue, or intentional.")
	}

	if (json) jsonFile.delete()

	return config
}

val configRenderOptions: ConfigRenderOptions = ConfigRenderOptions.defaults()
	.setJson(false)
	.setOriginComments(false)

inline fun <reified T : Any> saveConfig(config: T, parent: File, name: String) {
	FileWriter(File(parent, "$name.conf")).use { writer ->
		writer.write(config.toConfig(name).root().render(configRenderOptions))
	}
}
