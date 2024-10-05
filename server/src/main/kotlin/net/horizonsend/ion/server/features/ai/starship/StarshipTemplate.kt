package net.horizonsend.ion.server.features.ai.starship

import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.starship.StarshipType
import net.kyori.adventure.text.Component
import java.io.File
import kotlin.jvm.optionals.getOrNull

@Serializable
data class StarshipTemplate(
	val schematicName: String,
	val type: StarshipType,
	val miniMessageName: String,

	val manualWeaponSets: MutableSet<AIStarshipTemplate.WeaponSet> = mutableSetOf(),
	val autoWeaponSets: MutableSet<AIStarshipTemplate.WeaponSet> = mutableSetOf(),
) {
	@Transient
	val schematicFile: File = IonServer.dataFolder.resolve("aiShips").resolve("$schematicName.schem")

	fun getSchematic(): Clipboard? = AISpawningManager.schematicCache[schematicFile].getOrNull()

	fun componentName(): Component = miniMessage.deserialize(miniMessageName)
}
