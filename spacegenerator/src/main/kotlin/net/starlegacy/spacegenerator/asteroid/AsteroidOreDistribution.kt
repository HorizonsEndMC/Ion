package net.starlegacy.spacegenerator.asteroid

import org.bukkit.block.data.BlockData
import java.util.Random

internal class AsteroidOreDistribution(map: Map<String, String>) {
    private val oreReplacements: MutableMap<ClosedFloatingPointRange<Float>, BlockData>

    init {
        oreReplacements = mutableMapOf()

        var totalChance = 0.0f

        for ((typeString: String, chanceString: String) in map) {
            val type = AsteroidOreType.valueOf(typeString)
            val chance = chanceString.removeSuffix("%").toFloat() * .01f

            val blockData: BlockData = type.blockData
            oreReplacements[totalChance..(totalChance + chance)] = blockData
            totalChance += chance
        }

        check(totalChance <= 1f)
    }

    fun pickType(current: BlockData, random: Random): BlockData {
        val noise = random.nextFloat()

        for ((range, blockData) in oreReplacements) {
            if (!range.contains(noise)) {
                continue
            }

            return blockData
        }

        return current
    }
}
