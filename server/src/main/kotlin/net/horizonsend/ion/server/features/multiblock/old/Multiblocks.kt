package net.horizonsend.ion.server.features.multiblock.old

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.misc.MultiblockCommand.setupCommand
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess.multiblockCoroutineScope
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier4Bottom
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier4Side
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier4Top
import net.horizonsend.ion.server.features.multiblock.type.ammo.AmmoLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.MissileLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.StandardAmmoPressMultiblock
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.defense.active.AntiAirCannonBaseMultiblock
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.farming.harvester.HarvesterMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.farming.harvester.HarvesterMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.farming.harvester.HarvesterMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.farming.planter.PlanterMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.farming.planter.PlanterMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.farming.planter.PlanterMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.fluid.GasPowerPlantMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.VentMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.collector.GasCollectorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CentrifugeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CircuitfabMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CompressorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.FabricatorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.GasFurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.PlatePressMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.DecomposerMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.DisposalMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.ItemSplitterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.power.charger.ChargerMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.power.charger.ChargerMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.power.charger.ChargerMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.power.generator.GeneratorMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.power.generator.GeneratorMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.power.generator.GeneratorMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.power.powerfurnace.PowerFurnaceMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.power.powerfurnace.PowerFurnaceMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.power.powerfurnace.PowerFurnaceMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.printer.ArmorPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.CarbonPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.CarbonProcessorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.GlassPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.TechnicalPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.gravitywell.AmplifiedGravityWellMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.gravitywell.StandardGravityWellMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers.MiningLaserMultiblockTier1Bottom
import net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers.MiningLaserMultiblockTier1Side
import net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers.MiningLaserMultiblockTier1Top
import net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers.MiningLaserMultiblockTier2Bottom
import net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers.MiningLaserMultiblockTier2Side
import net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers.MiningLaserMultiblockTier2Top
import net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers.MiningLaserMultiblockTier3Bottom
import net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers.MiningLaserMultiblockTier3Side
import net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers.MiningLaserMultiblockTier3Top
import net.horizonsend.ion.server.features.multiblock.util.getBukkitBlockState
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

object Multiblocks : IonServerComponent() {
	private val multiblocks: MutableMap<String, Multiblock> = mutableMapOf()

	override fun onEnable() {
		initMultiblocks()

		log.info("Loaded ${multiblocks.size} multiblocks")
	}

	private fun initMultiblocks() {
		registerMultiblock(CentrifugeMultiblock)
		registerMultiblock(CompressorMultiblock)
		registerMultiblock(FabricatorMultiblock)
		registerMultiblock(CircuitfabMultiblock)
		registerMultiblock(PlatePressMultiblock)
		registerMultiblock(GasFurnaceMultiblock)

		registerMultiblock(GeneratorMultiblockTier1)
		registerMultiblock(GeneratorMultiblockTier2)
		registerMultiblock(GeneratorMultiblockTier3)

		registerMultiblock(PowerFurnaceMultiblockTier1)
		registerMultiblock(PowerFurnaceMultiblockTier2)
		registerMultiblock(PowerFurnaceMultiblockTier3)

		registerMultiblock(ChargerMultiblockTier1)
		registerMultiblock(ChargerMultiblockTier2)
		registerMultiblock(ChargerMultiblockTier3)

		registerMultiblock(CarbonProcessorMultiblock)

		registerMultiblock(CarbonPrinterMultiblock)
		registerMultiblock(TechnicalPrinterMultiblock)
		registerMultiblock(GlassPrinterMultiblock)
		registerMultiblock(ArmorPrinterMultiblock)

		registerMultiblock(DrillMultiblockTier1)
		registerMultiblock(DrillMultiblockTier2)
		registerMultiblock(DrillMultiblockTier3)

		registerMultiblock(StandardGravityWellMultiblock)
		registerMultiblock(AmplifiedGravityWellMultiblock)

		registerMultiblock(StandardAmmoPressMultiblock)
		registerMultiblock(AmmoLoaderMultiblock)
		registerMultiblock(MissileLoaderMultiblock)

		registerMultiblock(DecomposerMultiblock)
		registerMultiblock(DisposalMultiblock)
		registerMultiblock(MiningLaserMultiblockTier1Top)
		registerMultiblock(MiningLaserMultiblockTier1Bottom)
		registerMultiblock(MiningLaserMultiblockTier1Side)
		registerMultiblock(MiningLaserMultiblockTier2Top)
		registerMultiblock(MiningLaserMultiblockTier2Bottom)
		registerMultiblock(MiningLaserMultiblockTier2Side)
		registerMultiblock(MiningLaserMultiblockTier3Top)
		registerMultiblock(MiningLaserMultiblockTier3Bottom)
		registerMultiblock(MiningLaserMultiblockTier3Side)
		registerMultiblock(MiningLaserMultiblockTier4Top)
		registerMultiblock(MiningLaserMultiblockTier4Bottom)
		registerMultiblock(MiningLaserMultiblockTier4Side)

		registerMultiblock(ItemSplitterMultiblock)
		registerMultiblock(GasCollectorMultiblock)
		registerMultiblock(GasPowerPlantMultiblock)
		registerMultiblock(VentMultiblock)

		registerMultiblock(AutoCrafterMultiblockTier1)
		registerMultiblock(AutoCrafterMultiblockTier2)
		registerMultiblock(AutoCrafterMultiblockTier3)

		registerMultiblock(PlanterMultiblockTier1)
		registerMultiblock(PlanterMultiblockTier2)
		registerMultiblock(PlanterMultiblockTier3)
		registerMultiblock(HarvesterMultiblockTier1)
		registerMultiblock(HarvesterMultiblockTier2)
		registerMultiblock(HarvesterMultiblockTier3)

		registerMultiblock(AntiAirCannonBaseMultiblock)
	}

	private fun registerMultiblock(multiblock: Multiblock) {
		val name = multiblock.javaClass.simpleName ?: throw IllegalArgumentException("Provided anonymous multiblock class!")

		if (multiblocks.containsKey(name)) {
			throw IllegalArgumentException("Attempted to register duplicate multiblock name! Exisitng: ${multiblocks[name]}, new: $multiblock")
		}

		multiblocks[name] = multiblock
	}

	private fun registerAlternateName(name: String, multiblock: Multiblock) {
		if (multiblocks.containsKey(name)) {
			throw IllegalArgumentException("Attempted to register duplicate multiblock name! Exisitng: ${multiblocks[name]}, new: $multiblock")
		}

		multiblocks[name] = multiblock
	}

	// Access

	/**
	 * Get a multiblock by its identifying name
	 **/
	fun getMultiblockByName(name: String): Multiblock = multiblocks[name]!!

	/** Check if a sign has been registered as a multiblock, if so, return that value */
	fun getFromPDC(sign: Sign): Multiblock? {
		return getFromPDC(sign.persistentDataContainer)
	}

	/** Check if a pdc, if so, return that value */
	fun getFromPDC(pdc: PersistentDataContainer): Multiblock? {
		val data = pdc.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING) ?: return null

		return getMultiblockByName(data)
	}

	/**
	 * Get all registered multiblocks
	 **/
	fun all(): List<Multiblock> = multiblocks.values.toList()

	// End access

	/**
	 * Map of world UUIDs to a map of block keys to Multiblock types
	 *
	 *  - world
	 *      |- blockKey to Multiblock
	 **/
	private val multiblockLocationCache: MutableMap<UUID, MutableMap<Long, Multiblock>> = Object2ObjectOpenHashMap()

	/**
	 * Get a previously found multiblock at this location
	 **/
	fun getCached(world: World, x: Int, y: Int, z: Int): Multiblock? {
		return getCached(world, toBlockKey(x, y, z))
	}

	/**
	 * Get a previously found multiblock at this location
	 **/
	fun getCached(world: World, key: Long): Multiblock? {
		return multiblockLocationCache.getOrPut(world.uid) { Object2ObjectOpenHashMap() }[key]
	}

	/**
	 * Get a multiblock from the sign
	 **/
	operator fun get(sign: Sign, checkStructure: Boolean = true, loadChunks: Boolean = false) = runBlocking {
		getFromSignPosition(sign.world, sign.x, sign.y, sign.z, checkStructure, loadChunks)
	}

	/**
	 * Get a multiblock from the sign position
	 **/
	operator fun get(world: World, x: Int, y: Int, z: Int, checkStructure: Boolean = true, loadChunks: Boolean = false) = runBlocking {
		getFromSignPosition(world, x, y, z, checkStructure, loadChunks)
	}

	/**
	 * Checks against the multiblock cache for a multiblock at a position
	 *
	 * Only considers detected multiblocks
	 **/
	suspend fun getFromSignPosition(world: World, x: Int, y: Int, z: Int, checkStructure: Boolean, loadChunks: Boolean = false): Multiblock? {
		val block = world.getBlockAt(x, y, z)

		val cached = checkCache(world, x, y, z, checkStructure, loadChunks)

		if (cached != null) return cached

		val sign = getBukkitBlockState(block, loadChunks) as? Sign ?: return null

		for ((name, multiblock) in multiblocks) {
			if (!matchesPersistentDataContainer(sign.persistentDataContainer, multiblock)) {
				if (!multiblock.matchesSign(sign.getSide(Side.FRONT).lines())) continue else Tasks.sync {
					sign.persistentDataContainer.set(
						NamespacedKeys.MULTIBLOCK,
						PersistentDataType.STRING,
						name
					)
					sign.isWaxed = true
					sign.update(false, false)
				}
			}

			if (!multiblock.signMatchesStructureAsync(world, Vec3i(x, y, z), loadChunks)) continue;

			return multiblock
		}

		return null;
	}

	/**
	 * Checks against the multiblock cache for a multiblock at a position
	 **/
	private suspend fun checkCache(world: World, x: Int, y: Int, z: Int, checkStructure: Boolean, loadChunks: Boolean): Multiblock? {
		val worldCache = multiblockLocationCache[world.uid] ?: return null

		val key = toBlockKey(x, y, z)

		val possibleMultiblock = worldCache[key] ?: return null

		if (checkStructure && !possibleMultiblock.signMatchesStructureAsync(world, Vec3i(x, y, z), loadChunks)) return null

		return possibleMultiblock
	}

	private fun matchesPersistentDataContainer(persistentDataContainer: PersistentDataContainer, multiblock: Multiblock): Boolean {
		val value = persistentDataContainer.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING) ?: return false

		return value == multiblock::class.simpleName
	}

	/**
	 * The check for when someone right-clicks an undetected multiblock
	 **/
	@EventHandler(priority = EventPriority.HIGHEST)
	fun tryDetectMultiblock(event: PlayerInteractEvent) = multiblockCoroutineScope.launch {
		if (event.hand != EquipmentSlot.HAND || event.action != Action.RIGHT_CLICK_BLOCK) return@launch

		val clickedBlock = event.clickedBlock ?: return@launch
		val sign = getBukkitBlockState(clickedBlock, false) as? Sign ?: return@launch

		// Don't bother checking detected multiblocks
		if (sign.persistentDataContainer.has(NamespacedKeys.MULTIBLOCK)) return@launch

		var lastMatch: Multiblock? = null
		val player = event.player

		if (!player.hasPermission("starlegacy.multiblock.detect")) {
			player.userError("You don't have permission to detect multiblocks!")
			return@launch
		}

		// Check all multiblocks with a matching undetected sign
		for ((_, multiblock) in multiblocks.filterValues { it.matchesUndetectedSign(sign) }) {
			// And is built properly
			if (multiblock.signMatchesStructure(sign, particles = true)) {
				// Check permissions here because different tiers might have the same text
				multiblock.requiredPermission?.let {
					if (!player.hasPermission(it)) player.userError("You don't have permission to use that multiblock!")
					return@launch
				}

				// Update everything that needs to be done sync
				createNewMultiblock(multiblock, sign, event.player)

				return@launch
			}

			// Store the multi that last matched sign text
			lastMatch = multiblock
		}

		if (lastMatch != null) {
			player.userError("Improperly built ${lastMatch.name}. Make sure every block is correctly placed!")

			// Prompt the help command
			setupCommand(player, sign, lastMatch)
		}
	}

	@EventHandler
	fun onPlayerBreakBlock(event: BlockBreakEvent) {
		if (getBlockTypeSafe(event.block.world, event.block.x, event.block.y, event.block.z)?.isSign == false) return
		val sign = event.block.state as? Sign ?: return

		runBlocking { //TODO replace this
			val multiblock = getFromSignPosition(
				sign.world,
				sign.x,
				sign.y,
				sign.z,
				checkStructure = true,
				loadChunks = false
			) ?: return@runBlocking

			removeMultiblock(multiblock, sign)
		}
	}

	/**
	 * Called upon the creation of a new multiblock
	 *
	 * Handles the sign, registration
	 **/
	fun createNewMultiblock(multiblock: Multiblock, sign: Sign, detector: Player) = Tasks.sync {
		detector.rewardAchievement(Achievement.DETECT_MULTIBLOCK)

		multiblock.setupSign(detector, sign)

		sign.persistentDataContainer.set(
			NamespacedKeys.MULTIBLOCK,
			PersistentDataType.STRING,
			multiblock::class.simpleName!! // Shouldn't be any anonymous multiblocks
		)

		sign.isWaxed = true
		sign.update()
	}

	/** Upon a multiblock being removed */
	fun removeMultiblock(multiblock: Multiblock, sign: Sign) = Tasks.sync  {
		val (x, y, z) = Multiblock.getOrigin(sign)

		val chunkX = x.shr(4)
		val chunkZ = z.shr(4)

		val chunk = sign.world.ion.getChunk(chunkX, chunkZ) ?: return@sync
	}
}
