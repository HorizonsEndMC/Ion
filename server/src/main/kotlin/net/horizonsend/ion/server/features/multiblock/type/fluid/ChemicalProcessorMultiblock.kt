package net.horizonsend.ion.server.features.multiblock.type.fluid

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.MultiblockRecipeKeys
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.ComplexFluidDisplayModule
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui.Companion.createSettingsPage
import net.horizonsend.ion.server.features.gui.custom.settings.button.ArbitraryButton
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.RegistryKeyConsumerInputButton
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.crafting.input.ChemicalProcessorEnvironment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.ChemicalProcessorRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidPortMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.ChemicalProcessorMultiblock.ChemicalProcessorEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.persistence.SettingsContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataAdapterContext

object ChemicalProcessorMultiblock : Multiblock(), EntityMultiblock<ChemicalProcessorEntity>, InteractableMultiblock {
	override val name: String = "chemprocessor"
	override val signText: Array<Component?> = createSignText(
		Component.text("Chemical", NamedTextColor.GOLD),
		Component.text("Processor", HEColorScheme.Companion.HE_MEDIUM_GRAY),
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-1).ironBlock()
				x(0).powerInput()
				x(1).ironBlock()
			}
			y(0) {
				x(-1).anyGlass()
				x(0).ironBlock()
				x(1).anyGlass()
			}
			y(1) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(1) {
			y(-1) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(0) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(-1).steelBlock()
				x(1).steelBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(1) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
			y(2) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(4) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(6) {
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
			}
			y(7) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(8) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
		}
		z(2) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-3).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-4).ironBlock()
				x(-3).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(3).ironBlock()
				x(4).ironBlock()
			}
			y(1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(1).ironBlock()
				x(2).ironBlock()
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(2) {
				x(-2).titaniumBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(2).titaniumBlock()
			}
			y(3) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(4) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(5) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(6) {
				x(-2).steelBlock()
				x(2).steelBlock()
			}
			y(7) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(8) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(9) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(3) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-4).fluidPort()
				x(4).fluidPort()
			}
			y(1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(2).ironBlock()
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(2) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(3) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(4) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(5) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(6) {
				x(-3).steelBlock()
				x(3).steelBlock()
			}
			y(7) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(8) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(9) {
				x(-2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(4) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-2).type(Material.PALE_OAK_WOOD)
				x(2).type(Material.PALE_OAK_WOOD)
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-4).anyPipedInventory()
				x(-2).type(Material.PALE_OAK_WOOD)
				x(2).type(Material.PALE_OAK_WOOD)
				x(4).anyPipedInventory()
			}
			y(1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-3).ironBlock()
				x(-2).type(Material.PALE_OAK_WOOD)
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(2).type(Material.PALE_OAK_WOOD)
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(2) {
				x(-3).titaniumBlock()
				x(-2).type(Material.PALE_OAK_WOOD)
				x(2).type(Material.PALE_OAK_WOOD)
				x(3).titaniumBlock()
			}
			y(3) {
				x(-3).type(Material.WAXED_COPPER_GRATE)
				x(3).type(Material.WAXED_COPPER_GRATE)
			}
			y(4) {
				x(-3).type(Material.WAXED_COPPER_GRATE)
				x(3).type(Material.WAXED_COPPER_GRATE)
			}
			y(5) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(6) {
				x(-3).steelBlock()
				x(3).steelBlock()
			}
			y(7) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(8) {
				x(-3).titaniumBlock()
				x(0).steelBlock()
				x(3).titaniumBlock()
			}
			y(9) {
				x(-2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-1).titaniumBlock()
				x(0).steelBlock()
				x(1).titaniumBlock()
				x(2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(5) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-4).fluidPort()
				x(4).fluidPort()
			}
			y(1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(2).ironBlock()
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(2) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(3) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(4) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(5) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(6) {
				x(-3).steelBlock()
				x(3).steelBlock()
			}
			y(7) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(8) {
				x(-3).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(9) {
				x(-2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(6) {
			y(-1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-3).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-4).ironBlock()
				x(-3).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(3).ironBlock()
				x(4).ironBlock()
			}
			y(1) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(1).ironBlock()
				x(2).ironBlock()
				x(3).ironBlock()
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(2) {
				x(-2).titaniumBlock()
				x(0).type(Material.PALE_OAK_WOOD)
				x(2).titaniumBlock()
			}
			y(3) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(4) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(5) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(6) {
				x(-2).steelBlock()
				x(2).steelBlock()
			}
			y(7) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(8) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(9) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).fluidPort()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
			y(10) {
				x(0).customBlock(CustomBlockKeys.FLUID_PIPE.getValue())
			}
		}
		z(7) {
			y(-1) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(0) {
				x(-3).steelBlock()
				x(-2).steelBlock()
				x(2).steelBlock()
				x(3).steelBlock()
			}
			y(1) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
			y(2) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(4) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(6) {
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
			}
			y(7) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(8) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
		}
		z(8) {
			y(-1) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
			y(0) {
				x(-1).ironBlock()
				x(0).type(Material.TARGET)
				x(1).ironBlock()
			}
			y(1) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): ChemicalProcessorEntity {
		return ChemicalProcessorEntity(data, manager, world, x, y, z, structureDirection)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		getMultiblockEntity(sign, false)?.openSettingsGui(player)
	}

	class ChemicalProcessorEntity(data: PersistentMultiblockData, manager: MultiblockManager, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace) : MultiblockEntity(
		manager, ChemicalProcessorMultiblock, world, x, y, z, structureDirection
	), DisplayMultiblockEntity,
        FluidStoringMultiblock,
        SyncTickingMultiblockEntity,
		AsyncTickingMultiblockEntity,
        ProgressMultiblock,
        RecipeProcessingMultiblockEntity<ChemicalProcessorEnvironment>,
		StatusTickedMultiblockEntity
	{
		override val recipeManager: RecipeProcessingMultiblockEntity.MultiblockRecipeManager<ChemicalProcessorEnvironment> = RecipeProcessingMultiblockEntity.MultiblockRecipeManager()

		override val progressManager: ProgressMultiblock.ProgressManager = ProgressMultiblock.ProgressManager(data)
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(20)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val ioData: IOData = IOData.builder(this)
			// Inputs
			.addPort(IOType.FLUID, -4, 0, 3) { IOPort.RegisteredMetaDataInput(this, FluidPortMetadata(connectedStore = primaryInput, inputAllowed = true, outputAllowed = false)) }
			.addPort(IOType.FLUID, -4, 0, 5) {
                IOPort.RegisteredMetaDataInput(
                    this,
                    FluidPortMetadata(connectedStore = secondaryInput, inputAllowed = true, outputAllowed = false)
                )
            }

			// Outputs
			.addPort(IOType.FLUID, 4, 0, 3) { IOPort.RegisteredMetaDataInput(this, FluidPortMetadata(connectedStore = primaryOutput, inputAllowed = false, outputAllowed = true)) }
			.addPort(IOType.FLUID, 4, 0, 5) {
                IOPort.RegisteredMetaDataInput(
                    this,
                    FluidPortMetadata(connectedStore = secondaryOutput, inputAllowed = false, outputAllowed = true)
                )
            }

			.addPort(IOType.FLUID, 0, 9, 6) {
                IOPort.RegisteredMetaDataInput(
                    this,
                    FluidPortMetadata(connectedStore = pollutionOutput, inputAllowed = false, outputAllowed = true)
                )
            }

			.build()

		val primaryInput = FluidStorageContainer(data, "primaryin", Component.text("Primary Input"), NamespacedKeys.key("primaryin"), 100_000.0, FluidRestriction.Unlimited)
		val secondaryInput = FluidStorageContainer(data, "secondaryin", Component.text("Secondary Input"), NamespacedKeys.key("secondaryin"), 100_000.0, FluidRestriction.Unlimited)
		val primaryOutput = FluidStorageContainer(data, "primaryout", Component.text("Primary Output"), NamespacedKeys.key("primaryout"), 100_000.0, FluidRestriction.Unlimited)
		val secondaryOutput = FluidStorageContainer(data, "secondaryout", Component.text("Secondary Output"), NamespacedKeys.key("secondaryout"), 100_000.0, FluidRestriction.Unlimited)
		val pollutionOutput = FluidStorageContainer(data, "pollutionout", Component.text("Pollution Output"), NamespacedKeys.key("pollutionout"), 100_000.0, FluidRestriction.Unlimited)

		val settings = SettingsContainer.multiblockSettings(data,
			SettingsContainer.SettingsProperty(ChemicalProcessorEntity::lockedRecipe, MultiblockRecipeKeys.serializer, null),
		)

		@Suppress("UNCHECKED_CAST")
		var lockedRecipe: IonRegistryKey<MultiblockRecipe<*>, out MultiblockRecipe<*>>? by settings.getDelegate { recipeManager.lockedRecipe = it as IonRegistryKey<MultiblockRecipe<*>, MultiblockRecipe<ChemicalProcessorEnvironment>>? }

		init {
			recipeManager.lockedRecipe = lockedRecipe as IonRegistryKey<MultiblockRecipe<*>, MultiblockRecipe<ChemicalProcessorEnvironment>>?
		}
		fun openSettingsGui(player: Player) {
			createSettingsPage(
				player,
				"Locked Recipe",
				RegistryKeyConsumerInputButton(
					keyRegistry = MultiblockRecipeKeys,
					keyFilter = { it.getValue().entityType == ChemicalProcessorEntity::class },
					valueSupplier = this::lockedRecipe,
					valueConsumer = { lockedRecipe = it as IonRegistryKey<MultiblockRecipe<*>, out MultiblockRecipe<ChemicalProcessorEnvironment>> },
					name = text("Lock to Recipe"),
					buttonDescription = "This multiblock will only process the specifed recipe. ",
					inputDescription = text("Enter Key"),
					icon = GuiItem.LIST,
					defaultValue = null,
					searchTermProvider = { key ->
						key as IonRegistryKey<MultiblockRecipe<*>, out MultiblockRecipe<ChemicalProcessorEnvironment>>
						val recipe: ChemicalProcessorRecipe = key.getValue() as ChemicalProcessorRecipe

						listOfNotNull(
							key.key,
							recipe.fluidRequirementOne?.asFluidStack()?.getDisplayName()?.plainText(),
							recipe.fluidRequirementTwo?.asFluidStack()?.getDisplayName()?.plainText(),
							recipe.itemRequirement?.asItemStack()?.displayNameComponent?.plainText(),
							recipe.fluidResultTwo?.stack?.getDisplayName()?.plainText(),
							recipe.fluidResultTwo?.stack?.getDisplayName()?.plainText(),
							recipe.fluidResultTwo?.stack?.getDisplayName()?.plainText(),
							recipe.itemResult?.result?.asItem()?.displayNameComponent?.plainText(),
						)
					}
				),
				ArbitraryButton(
					name = "Unset Locked Recipe",
					firstLine = Component.empty(),
					secondLine = text("Unset Locked Recipe"),
					icon = GuiItem.CANCEL,
					handleClick = { _, gui ->
						lockedRecipe = null
						gui.openGui()
					}
				)
			).openGui()
		}

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{
                ComplexFluidDisplayModule(
                    handler = it,
                    container = primaryInput,
                    title = primaryInput.displayName,
                    offsetLeft = 4.5,
                    offsetUp = 1.15,
                    offsetBack = -4.0 + 0.39,
                    scale = 0.7f,
                    relativeFace = RelativeFace.RIGHT
                )
            },
			{
                ComplexFluidDisplayModule(
                    handler = it,
                    container = secondaryInput,
                    title = secondaryInput.displayName,
                    offsetLeft = 4.5,
                    offsetUp = 1.15,
                    offsetBack = -6.0 + 0.39,
                    scale = 0.7f,
                    relativeFace = RelativeFace.RIGHT
                )
            },
			{
                ComplexFluidDisplayModule(
                    handler = it,
                    container = primaryOutput,
                    title = primaryOutput.displayName,
                    offsetLeft = -4.5,
                    offsetUp = 1.15,
                    offsetBack = -4.0 + 0.39,
                    scale = 0.7f,
                    relativeFace = RelativeFace.LEFT
                )
            },
			{
                ComplexFluidDisplayModule(
                    handler = it,
                    container = secondaryOutput,
                    title = secondaryOutput.displayName,
                    offsetLeft = -4.5,
                    offsetUp = 1.15,
                    offsetBack = -6.0 + 0.39,
                    scale = 0.7f,
                    relativeFace = RelativeFace.LEFT
                )
            }
		)

		override fun getStores(): List<FluidStorageContainer> {
			return listOf(primaryInput, secondaryInput, primaryOutput, secondaryOutput, pollutionOutput)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			progressManager.saveProgressData(store)
			saveStorageData(store)
		}


		override fun tick() {
			if (!tryProcessRecipe()) {
				progressManager.reset()
				return
			}
		}

		override fun tickAsync() {
			bootstrapFluidNetwork()
		}

		override fun buildRecipeEnvironment(): ChemicalProcessorEnvironment = ChemicalProcessorEnvironment(
            this,
            leftInventory!!,
            rightInventory!!,
            primaryInput,
            secondaryInput,
            primaryOutput,
            secondaryOutput,
            pollutionOutput
        )

		val leftInventory get() = getInventory(INVENTORY_OFFSET_LEFT.x, INVENTORY_OFFSET_LEFT.y, INVENTORY_OFFSET_LEFT.z)
		val rightInventory get() = getInventory(INVENTORY_OFFSET_RIGHT.x, INVENTORY_OFFSET_RIGHT.y, INVENTORY_OFFSET_RIGHT.z)

		companion object {
			private val INVENTORY_OFFSET_LEFT = Vec3i(-4, 0, 4)
			private val INVENTORY_OFFSET_RIGHT = Vec3i(4, 0, 4)
		}
	}
}
