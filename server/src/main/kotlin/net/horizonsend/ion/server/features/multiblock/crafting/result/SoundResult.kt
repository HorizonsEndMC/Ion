package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory

class SoundResult(val sound: String, private val soundCategory: SoundCategory, val volume: Float, val pitch: Float) : ActionResult {

    constructor(sound: Sound, soundCategory: SoundCategory, volume: Float, pitch: Float) :
            this(sound.key.key, soundCategory, volume, pitch)

    override fun execute(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign) {
        sign.world.playSound(sign.location, sound, soundCategory, volume, pitch)
    }

}