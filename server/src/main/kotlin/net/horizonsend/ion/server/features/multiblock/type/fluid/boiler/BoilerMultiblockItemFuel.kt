package net.horizonsend.ion.server.features.multiblock.type.fluid.boiler

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.ComplexFluidDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.getLinePos
import net.horizonsend.ion.server.features.industry.ItemFuelProperties
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidPortMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.fluid.boiler.BoilerMultiblockItemFuel.ItemBoilerEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Slab
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

object BoilerMultiblockItemFuel : BoilerMultiblock<ItemBoilerEntity>() {
	override val signText: Array<Component?> = createSignText(
		ofChildren(text("Item ", NamedTextColor.RED), text("Boiler", HEColorScheme.HE_MEDIUM_GRAY)),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(6) {
			y(-1) {
				x(-3).anySlab(PrepackagedPreset.slab(Slab.Type.TOP))
				x(-2).titaniumBlock()
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
				x(2).titaniumBlock()
				x(3).anySlab(PrepackagedPreset.slab(Slab.Type.TOP))
			}
			y(0) {
				x(-3).anyWall()
				x(-2).refractoryBricks()
				x(-1).refractoryBricks()
				x(0).refractoryBricks()
				x(1).refractoryBricks()
				x(2).refractoryBricks()
				x(3).anyWall()
			}
			y(1) {
				x(-3).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-2).titaniumBlock()
				x(-1).titaniumBlock()
				x(0).fluidPort()
				x(1).titaniumBlock()
				x(2).titaniumBlock()
				x(3).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
			y(2) {
				x(0).anyFluidPipe()
			}
		}
		z(5) {
			y(-1) {
				x(-3).titaniumBlock()
				x(-2).titaniumBlock()
				x(-1).refractoryBricks()
				x(0).refractoryBricks()
				x(1).refractoryBricks()
				x(2).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(0) {
				x(-3).refractoryBricks()
				x(-2).refractoryBricks()
				x(2).refractoryBricks()
				x(3).refractoryBricks()
			}
			y(1) {
				x(-3).titaniumBlock()
				x(-2).titaniumBlock()
				x(-1).refractoryBricks()
				x(0).refractoryBricks()
				x(1).refractoryBricks()
				x(2).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(2) {
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(4) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(6) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(7) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(4) {
			y(-1) {
				x(-3).titaniumBlock()
				x(-2).refractoryBricks()
				x(-1).refractoryBricks()
				x(0).refractoryBricks()
				x(1).refractoryBricks()
				x(2).refractoryBricks()
				x(3).titaniumBlock()
			}
			y(0) {
				x(-3).refractoryBricks()
				x(3).refractoryBricks()
			}
			y(1) {
				x(-3).titaniumBlock()
				x(-2).refractoryBricks()
				x(0).refractoryBricks()
				x(2).refractoryBricks()
				x(3).titaniumBlock()
			}
			y(2) {
				x(-2).steelBlock()
				x(2).steelBlock()
			}
			y(3) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(4) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(5) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(6) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(7) {
				x(-2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-1).titaniumBlock()
				x(1).titaniumBlock()
				x(2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
			y(8) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(3) {
			y(-1) {
				x(-3).titaniumBlock()
				x(-2).refractoryBricks()
				x(-1).refractoryBricks()
				x(0).refractoryBricks()
				x(1).refractoryBricks()
				x(2).refractoryBricks()
				x(3).titaniumBlock()
			}
			y(0) {
				x(-3).fluidPort()
				x(3).fluidPort()
			}
			y(1) {
				x(-3).titaniumBlock()
				x(-2).refractoryBricks()
				x(-1).refractoryBricks()
				x(0).type(Material.TARGET)
				x(1).refractoryBricks()
				x(2).refractoryBricks()
				x(3).titaniumBlock()
			}
			y(2) {
				x(-2).steelBlock()
				x(2).steelBlock()
			}
			y(3) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(4) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(5) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(6) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(7) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(8) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
		}
		z(2) {
			y(-1) {
				x(-3).titaniumBlock()
				x(-2).refractoryBricks()
				x(-1).refractoryBricks()
				x(0).refractoryBricks()
				x(1).refractoryBricks()
				x(2).refractoryBricks()
				x(3).titaniumBlock()
			}
			y(0) {
				x(-3).refractoryBricks()
				x(3).refractoryBricks()
			}
			y(1) {
				x(-3).titaniumBlock()
				x(-2).refractoryBricks()
				x(0).refractoryBricks()
				x(2).refractoryBricks()
				x(3).titaniumBlock()
			}
			y(2) {
				x(-2).steelBlock()
				x(2).steelBlock()
			}
			y(3) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(4) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(5) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(6) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(7) {
				x(-2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-1).titaniumBlock()
				x(1).titaniumBlock()
				x(2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
			y(8) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(1) {
			y(-1) {
				x(-3).titaniumBlock()
				x(-2).titaniumBlock()
				x(-1).refractoryBricks()
				x(0).refractoryBricks()
				x(1).refractoryBricks()
				x(2).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(0) {
				x(-3).refractoryBricks()
				x(-2).refractoryBricks()
				x(-1).type(Material.IRON_BARS)
				x(0).type(Material.IRON_BARS)
				x(1).type(Material.IRON_BARS)
				x(2).refractoryBricks()
				x(3).refractoryBricks()
			}
			y(1) {
				x(-3).titaniumBlock()
				x(-2).titaniumBlock()
				x(-1).refractoryBricks()
				x(0).refractoryBricks()
				x(1).refractoryBricks()
				x(2).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(2) {
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(4) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(6) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(7) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(0) {
			y(-1) {
				x(-3).anySlab(PrepackagedPreset.slab(Slab.Type.TOP))
				x(-2).titaniumBlock()
				x(-1).titaniumBlock()
				x(0).anyPipedInventory()
				x(1).titaniumBlock()
				x(2).titaniumBlock()
				x(3).anySlab(PrepackagedPreset.slab(Slab.Type.TOP))
			}
			y(0) {
				x(-3).anyWall()
				x(-2).anyCustomBlockOrMaterial(
					listOf(CustomBlockKeys.REDSTONE_CONTROL_PORT),
					listOf(Material.MUD_BRICKS),
					"redstone control port or mud bricks",
				) { setExample(CustomBlockKeys.REDSTONE_CONTROL_PORT.getValue().blockData) }
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(0).anyGlass()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(2).anyCustomBlockOrMaterial(
					listOf(CustomBlockKeys.REDSTONE_CONTROL_PORT),
					listOf(Material.MUD_BRICKS),
					"redstone control port or mud bricks",
				) { setExample(Material.MUD_BRICKS) }
				x(3).anyWall()
			}
			y(1) {
				x(-3).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-2).titaniumBlock()
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
				x(2).titaniumBlock()
				x(3).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
	}

	override fun createEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	): ItemBoilerEntity {
		return ItemBoilerEntity(manager, data, world, x, y, z, structureDirection)
	}

	class ItemBoilerEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : BoilerMultiblockEntity(manager, data, BoilerMultiblockItemFuel, world, x, y, z, structureDirection) {
		val pollutionStorage = FluidStorageContainer(data, "pollution_out", text("Pollution Output"), NamespacedKeys.key("pollution_out"), 100_000.0, FluidRestriction.Unlimited)

		override fun IOData.Builder.registerAdditionalIO(): IOData.Builder =
			addPort(IOType.FLUID, 0, 1, 6) { IOPort.RegisteredMetaDataInput(this@ItemBoilerEntity, FluidPortMetadata(connectedStore = pollutionStorage, inputAllowed = false, outputAllowed = true)) }

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(this,
			{ ComplexFluidDisplayModule(handler = it, container = fluidInput, title = text("Input"), offsetLeft = 3.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, RelativeFace.RIGHT) },
			{ ComplexFluidDisplayModule(handler = it, container = fluidOutput, title = text("Output"), offsetLeft = -3.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, RelativeFace.LEFT) },
			{ StatusDisplayModule(handler = it, statusSupplier = statusManager, offsetLeft = 0.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
		)
		override fun tickAsync() {
			bootstrapFluidNetwork()
			val deltaSeconds = deltaTMS / 1000.0

			val outputContents = fluidOutput.getContents()
			if (outputContents.isNotEmpty()) {
				val temperature = outputContents.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE, location).value
				if (temperature > 600.0 && fluidOutput.getRemainingRoom() <= 0) {
					Tasks.sync {
						val location = getBlockRelative(0, 5, 3).location.toCenterLocation()
						world.createExplosion(location, 30.0f)
					}
					fluidOutput.clear()
					fluidInput.clear()

					return
				}
			}

			tickGauges()

			Tasks.sync {
				val preTickResult = preTick(deltaSeconds)

				Tasks.async {
					if (!isRedstoneEnabled() || !preTickResult) {
						setRunning(false)
						reduceInputTemperature(deltaSeconds)
						return@async
					}

					heatFluid(deltaSeconds)
					postTick(deltaSeconds)
				}
			}
		}

		private var burningEnds = 0L
		private var burningOutput = 0.0

		override fun getHeatProductionJoulesPerSecond(): Double {
			return burningOutput
		}

		override fun preTick(deltaSeconds: Double): Boolean {
			val now = System.currentTimeMillis()
			if (burningEnds > now) return true

			val fuelInput = getInventory(0, -1, 0) ?: return false

			for (itemStack: ItemStack? in fuelInput.contents) {
				if (itemStack == null) continue

				val fuelProperties = ItemFuelProperties[itemStack] ?: continue

				val pollutionStack = fuelProperties.pollutionResult.clone()
				if (!pollutionStorage.canAdd(pollutionStack)) continue

				burningEnds = now + fuelProperties.burnDurationMillis
				burningOutput = fuelProperties.heatOutputJoulesPerSecond

				itemStack.amount--

				pollutionStorage.addFluid(pollutionStack, location)

				return true
			}

			return false
		}

		override fun postTick(deltaSeconds: Double) {
			if (!isRunning) return

			displayBurningParticles()
		}

		fun displayBurningParticles() {
			val location = getBlockRelative(0, 0, 3).location.toCenterLocation()

			repeat(2) {
				val offsetX = Random.nextDouble(-2.5, 2.5)
				val offsetY = Random.nextDouble(-0.45, 0.45)
				val offsetZ = Random.nextDouble(-2.5, 2.5)

				world.spawnParticle(Particle.FLAME, location.x + offsetX, location.y + offsetY, location.z + offsetZ, 1, 0.0, 0.0, 0.0, 0.0, null)
			}
		}
	}
}
