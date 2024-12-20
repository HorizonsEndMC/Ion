package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.SoundCategory

class SoundResult<T: MultiblockEntity>(val sound: String, private val soundCategory: SoundCategory, val volume: Float, val pitch: Float) : ActionResult<T> {

    constructor(sound: Sound, soundCategory: SoundCategory, volume: Float, pitch: Float) :
            this(Registry.SOUNDS.getKey(sound)?.key!!, soundCategory, volume, pitch)

    override fun execute(context: RecipeExecutionContext<T>) {
		val entity = context.entity
		entity.world.playSound(entity.location, sound, soundCategory, volume, pitch)
    }
}
