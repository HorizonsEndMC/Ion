package net.horizonsend.ion.server.features.gas

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Hopper
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

object Gasses : IonServerComponent() {
	private var gasses = mutableMapOf<String, Gas>()

	override fun onEnable() {
		val file = File(IonServer.dataFolder, "gasses.yml")
		file.createNewFile()
		val configuration = YamlConfiguration.loadConfiguration(file)

		gasses = HashMap()

		for (id in configuration.getKeys(false)) {
			val item = CustomItems[id] ?: return
			val name = configuration.getString("$id.name")!!
			val factors = configuration.getStringList("$id.factors").map(CollectionFactor::collectionSetFromString)
			gasses[item.id] = Gas(name, id, factors)
		}
	}

	fun tickCollectorAsync(collector: Sign) {
		Tasks.async {
			tickCollector(collector)
		}
	}

	fun tickCollector(collector: Sign) {
		val attachedFace = collector.getFacing().oppositeFace
		val world = collector.world
		if (!world.isChunkLoaded((collector.x + attachedFace.modX) shr 4, (collector.z + attachedFace.modZ) shr 4)) {
			return
		}
		val furnace = collector.block.getRelativeIfLoaded(attachedFace) ?: return
		val hopper = furnace.getRelativeIfLoaded(attachedFace) ?: return
		for (face in arrayOf(
			attachedFace.rightFace,
			attachedFace.leftFace,
			BlockFace.UP,
			BlockFace.DOWN
		)) {
			val endRod = furnace.getRelativeIfLoaded(face) ?: continue
			if (endRod.type != Material.END_ROD) {
				continue
			}
			val blockFace = (endRod.blockData as Directional).facing
			if (blockFace != face && blockFace != face.oppositeFace) {
				continue
			}
			val location = endRod.getRelativeIfLoaded(face)?.location ?: continue
			val availableGas = findGas(location)
			Tasks.sync {
				harvestGasses(availableGas, furnace, hopper, endRod)
			}
		}
	}

	private fun harvestGasses(availableGas: List<Gas>, furnace: Block, hopper: Block, endRod: Block) {
		if (!furnace.location.isChunkLoaded || !hopper.location.isChunkLoaded || !endRod.location.isChunkLoaded) {
			return
		}

		for (gas in availableGas) {
			val result = tryHarvestGas(furnace, hopper, gas)
			val sound = if (result) Sound.ITEM_BOTTLE_FILL_DRAGONBREATH else Sound.ITEM_BOTTLE_FILL
			endRod.world.playSound(endRod.location, sound, 10.0f, 0.5f)
		}
	}

	fun isEmptyCanister(itemStack: ItemStack?): Boolean {
		return itemStack != null && CustomItems[itemStack] === CustomItems.GAS_CANISTER_EMPTY
	}

	private fun tryHarvestGas(furnaceBlock: Block, hopperBlock: Block, gas: Gas): Boolean {
		val furnace = furnaceBlock.getState(false) as Furnace
		val hopper = hopperBlock.getState(false) as Hopper
		val canisterItem = furnace.inventory.smelting ?: return false
		val gasItem = gas.item.itemStack(1)
		if (!isEmptyCanister(canisterItem)) {
			return false
		}
		canisterItem.amount = canisterItem.amount - 1
		furnace.inventory.smelting = canisterItem
		hopper.inventory.addItem(gasItem)
		return true
	}

	private fun findGas(location: Location) = gasses.values.filter { it.isAvailable(location) }
}
