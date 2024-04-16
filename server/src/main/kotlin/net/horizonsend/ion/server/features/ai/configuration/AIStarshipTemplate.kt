package net.horizonsend.ion.server.features.ai.configuration

import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.starship.StarshipType
import org.apache.commons.lang.math.DoubleRange
import java.io.File
import kotlin.jvm.optionals.getOrNull

@Serializable
data class AIStarshipTemplate(
	val identifier: String = "VESTA",
	var schematicName: String = "Vesta",

	var miniMessageName: String = "<red><bold>Vesta",
	var color: Int = Integer.parseInt("ff0000", 16),

	var type: StarshipType = StarshipType.SHUTTLE,

	var controllerFactory: String = "STARFIGHTER",

	var xpMultiplier: Double = 1.0,
	var creditReward: Double = 100.0,

	var maxSpeed: Int = -1,
	var engagementRange: Double = 500.0,

	val manualWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),
	val autoWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),

	val smackInformation: SmackInformation? = null,
	val radiusMessageInformation: RadiusMessageInformation? = null,
	val reinforcementInformation: ReinforcementInformation? = null
) {
    init {
//			if (AISpawningManager.templates.values.contains(this)) error("Identifiers must be unique! $identifier already exists!")

        AISpawningManager.templates[identifier] = this
    }

    @Transient
    val schematicFile: File = IonServer.dataFolder.resolve("aiShips").resolve("$schematicName.schem")
    fun getSchematic(): Clipboard? = AISpawningManager.schematicCache[schematicFile].getOrNull()

    @Serializable
    data class WeaponSet(val name: String, private val engagementRangeMin: Double, private val engagementRangeMax: Double) {
        @Transient
        val engagementRange = DoubleRange(engagementRangeMin, engagementRangeMax)
    }

    @Serializable
    data class SmackInformation(
        val prefix: String,
        val messages: List<String>
    )

    @Serializable
    data class RadiusMessageInformation(
        val prefix: String,
        val messages: Map<Double, String>
    )

    @Serializable
    data class ReinforcementInformation(
        val activationThreshold: Double,
        val delay: Long,
        val broadcastMessage: String?,
        val configuration: AISpawningConfiguration.AISpawnerConfiguration
    )
}
