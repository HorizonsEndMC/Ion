package net.horizonsend.ion.server.features.world.generation.generators.configuration

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material.MaterialConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.EvaluationConfiguration
import net.horizonsend.ion.server.features.world.generation.generators.configuration.feature.AsteroidPlacementConfiguration.AsteroidBuilder
import java.io.File

object AsteroidConfigurations : IonServerComponent() {
	private var structureTemplates: Map<String, EvaluationConfiguration> = AsteroidStructures.defaultStructureTemplates
	private var paletteTemplates: Map<String, MaterialConfiguration> = AsteroidStructures.defaultPaletteTemplates
	private var builders: Map<String, AsteroidBuilder> = AsteroidStructures.defaultBuilders

	private val STRUCTURE_FOLDER = ConfigurationFiles.configurationFolder.resolve("asteroid_noise").apply { mkdirs() }
	private val PALETTE_FOLDER = ConfigurationFiles.configurationFolder.resolve("asteroid_palette").apply { mkdirs() }
	private val ASTEROID_TEMPLATES_FOLDER = ConfigurationFiles.configurationFolder.resolve("asteroid_templates").apply { mkdirs() }

	override fun onEnable() {
		createDefaultFiles(AsteroidStructures.defaultStructureTemplates, STRUCTURE_FOLDER)
		createDefaultFiles(AsteroidStructures.defaultPaletteTemplates, PALETTE_FOLDER)
		createDefaultFiles(AsteroidStructures.defaultBuilders, ASTEROID_TEMPLATES_FOLDER)
		reload()
	}

	fun reload() {
		structureTemplates = STRUCTURE_FOLDER.listFiles().filter { it.isFile }.associateTo(Object2ObjectOpenHashMap()) { file ->
			file.nameWithoutExtension to runCatching { Configuration.load<EvaluationConfiguration>(STRUCTURE_FOLDER, file.name) }.onFailure { log.error("Error loading structure template ${file.name}") }.getOrThrow()
		}
		paletteTemplates = PALETTE_FOLDER.listFiles().filter { it.isFile }.associateTo(Object2ObjectOpenHashMap()) { file ->
			file.nameWithoutExtension to runCatching { Configuration.load<MaterialConfiguration>(PALETTE_FOLDER, file.name) }.onFailure { log.error("Error loading palette template ${file.name}") }.getOrThrow()
		}
		builders = ASTEROID_TEMPLATES_FOLDER.listFiles().filter { it.isFile }.associateTo(Object2ObjectOpenHashMap()) { file ->
			file.nameWithoutExtension to runCatching { Configuration.load<AsteroidBuilder>(ASTEROID_TEMPLATES_FOLDER, file.name) }.onFailure { log.error("Error loading builder template ${file.name}") }.getOrThrow()
		}
	}

	fun getStructure(name: String): EvaluationConfiguration? = structureTemplates[name]
	fun getStructures(): Map<String, EvaluationConfiguration> = structureTemplates

	fun getPalette(name: String): MaterialConfiguration? = paletteTemplates[name]
	fun getPalettes(): Map<String, MaterialConfiguration> = paletteTemplates

	fun getBuilder(name: String): AsteroidBuilder? = builders[name]
	fun getBuilders(): Map<String, AsteroidBuilder> = builders

    inline fun <reified T : Any> createDefaultFiles(configuration: Map<String, T>, directory: File) {
		for ((key, config) in configuration) {
			Configuration.loadOrDefault(directory, "$key.json", config)
		}
	}
}
