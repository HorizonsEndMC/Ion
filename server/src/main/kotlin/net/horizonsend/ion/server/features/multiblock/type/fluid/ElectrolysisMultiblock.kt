package net.horizonsend.ion.server.features.multiblock.type.fluid

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.SingleFluidStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset.pane
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset.stairs
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.horizonsend.ion.server.features.transport.fluids.TransportedFluids.HYDROGEN
import net.horizonsend.ion.server.features.transport.fluids.TransportedFluids.OXYGEN
import net.horizonsend.ion.server.features.transport.fluids.TransportedFluids.WATER
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_1
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_2
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_3
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.LEFT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.OPPOSITE
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.RIGHT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.SELF
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected.Half.BOTTOM
import org.bukkit.block.data.Bisected.Half.TOP
import org.bukkit.block.data.type.Stairs.Shape.STRAIGHT
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

object ElectrolysisMultiblock : Multiblock(), EntityMultiblock<ElectrolysisMultiblock.ElectrolysisMultiblockEntity>, InteractableMultiblock {
	override val name: String = "Electrolysis"

	override val signText: Array<Component?> = arrayOf(
		text("Electrolysis", NamedTextColor.GOLD),
		text("Machine", NamedTextColor.RED),
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-2).anyStairs(stairs(SELF, TOP, STRAIGHT))
				x(-1).craftingTable()
				x(+0).fluidInput()
				x(+1).craftingTable()
				x(+2).anyStairs(stairs(SELF, TOP, STRAIGHT))
			}
			y(+0) {
				x(-2).anyStairs(stairs(SELF, BOTTOM, STRAIGHT))
				x(-1).anyGlass()
				x(+0).copperBlock()
				x(+1).anyGlass()
				x(+2).anyStairs(stairs(SELF, BOTTOM, STRAIGHT))
			}
			y(+1) {
				x(-2).anyGlassPane(pane(SELF, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(SELF, LEFT))
			}
			y(+2) {
				x(-2).anyGlassPane(pane(SELF, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(SELF, LEFT))
			}
		}
		z(1) {
			y(-1) {
				x(-2).titaniumBlock()
				x(-1).redstoneBlock()
				x(+0).copperBlock()
				x(+1).redstoneBlock()
				x(+2).titaniumBlock()
			}
			y(+0) {
				x(-2).titaniumBlock()
				x(-1).copperGrate()
				x(+0).copperBlock()
				x(+1).copperGrate()
				x(+2).titaniumBlock()
			}
			y(+1) {
				x(-2).anyGlass()
				x(-1).copperGrate()
				x(+0).anyGlass()
				x(+1).copperGrate()
				x(+2).anyGlass()
			}
			y(+2) {
				x(-2).anyGlass()
				x(-1).copperGrate()
				x(+0).anyGlass()
				x(+1).copperGrate()
				x(+2).anyGlass()
			}
		}
		z(2) {
			y(-1) {
				x(-2).anyStairs(stairs(OPPOSITE, TOP, STRAIGHT))
				x(-1).titaniumBlock()
				x(+0).copperBlock()
				x(+1).titaniumBlock()
				x(+2).anyStairs(stairs(OPPOSITE, TOP, STRAIGHT))
			}
			y(+0) {
				x(-2).anyStairs(stairs(OPPOSITE, BOTTOM, STRAIGHT))
				x(-1).titaniumBlock()
				x(+0).anyStairs(stairs(OPPOSITE, BOTTOM, STRAIGHT))
				x(+1).titaniumBlock()
				x(+2).anyStairs(stairs(OPPOSITE, BOTTOM, STRAIGHT))
			}
			y(+1) {
				x(-2).anyGlassPane(pane(OPPOSITE, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(OPPOSITE, LEFT))
			}
			y(+2) {
				x(-2).anyGlassPane(pane(OPPOSITE, RIGHT))
				x(-1).anyGlass()
				x(+0).anyGlassPane(pane(LEFT, RIGHT))
				x(+1).anyGlass()
				x(+2).anyGlassPane(pane(OPPOSITE, LEFT))
			}
		}
	}

	override fun createEntity(manager: ChunkMultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): ElectrolysisMultiblockEntity {
		return ElectrolysisMultiblockEntity(manager, data, x, y, z, world, structureDirection)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val entity = getMultiblockEntity(sign)

		player.information(entity.toString())
	}

	class ElectrolysisMultiblockEntity(
		manager: ChunkMultiblockManager,
		data: PersistentMultiblockData,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace
	) : MultiblockEntity(
		manager,
		ElectrolysisMultiblock,
		x,
		y,
		z,
		world,
		structureDirection
	), AsyncTickingMultiblockEntity, FluidStoringEntity {
		override val capacities: Array<StorageContainer> = arrayOf(
			loadStoredResource(data, "hydrogen_tank", text("Hydrogen Tank"), TANK_1, SingleFluidStorage(500, HYDROGEN)),
			loadStoredResource(data, "oxygen_tank", text("Oxygen Tank"), TANK_2, SingleFluidStorage(500, OXYGEN)),
			loadStoredResource(data, "water_tank", text("Water Tank"), TANK_3, SingleFluidStorage(500, WATER))
		)

		override suspend fun tickAsync() {

		}

		override fun toString(): String = """
			Electrolysis multi
				Hydrogen: ${getNamedStorage("hydrogen_tank")}
				Oxygen: ${getNamedStorage("oxygen_tank")}
				Water: ${getNamedStorage("water_tank")}
		""".trimIndent()
	}
}
