package net.horizonsend.ion.server.features.multiblock

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.horizonsend.ion.server.features.multiblock.type.ammo.AmmoLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.MissileLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.StandardAmmoPressMultiblock
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield10
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield20
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield30
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield5
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.charger.ChargerMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.charger.ChargerMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.charger.ChargerMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.checklist.BargeReactorMultiBlock
import net.horizonsend.ion.server.features.multiblock.type.checklist.BattleCruiserReactorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.checklist.CruiserReactorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.defense.AntiAirCannonBaseMultiblock
import net.horizonsend.ion.server.features.multiblock.type.dockingtube.ConnectedDockingTubeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.dockingtube.DisconnectedDockingTubeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.farming.harvester.HarvesterMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.farming.harvester.HarvesterMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.farming.harvester.HarvesterMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.farming.planter.PlanterMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.farming.planter.PlanterMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.farming.planter.PlanterMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.gas.GasCollectorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.gas.GasPowerPlantMultiblock
import net.horizonsend.ion.server.features.multiblock.type.gas.VentMultiblock
import net.horizonsend.ion.server.features.multiblock.type.generator.GeneratorMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.generator.GeneratorMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.generator.GeneratorMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.gravitywell.AmplifiedGravityWellMultiblock
import net.horizonsend.ion.server.features.multiblock.type.gravitywell.StandardGravityWellMultiblock
import net.horizonsend.ion.server.features.multiblock.type.hyperdrive.HyperdriveMultiblockClass1
import net.horizonsend.ion.server.features.multiblock.type.hyperdrive.HyperdriveMultiblockClass2
import net.horizonsend.ion.server.features.multiblock.type.hyperdrive.HyperdriveMultiblockClass3
import net.horizonsend.ion.server.features.multiblock.type.hyperdrive.HyperdriveMultiblockClass4
import net.horizonsend.ion.server.features.multiblock.type.industry.CentrifugeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CircuitfabMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CompressorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.FabricatorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.GasFurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.PlatePressMultiblock
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier1Bottom
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier1Side
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier1Top
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier2Bottom
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier2Side
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier2Top
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier3Bottom
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier3Side
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier3Top
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier4Bottom
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier4Side
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier4Top
import net.horizonsend.ion.server.features.multiblock.type.misc.AirlockMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.CryoPodMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.DecomposerMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.DisposalMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.ExpandableAirlock
import net.horizonsend.ion.server.features.multiblock.type.misc.FuelTankMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.ItemSplitterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.LandingGearMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.LargeTractorBeamMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.MagazineMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.MobDefender
import net.horizonsend.ion.server.features.multiblock.type.misc.OdometerMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.ShipFactoryMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.TestMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.TractorBeamMultiblock
import net.horizonsend.ion.server.features.multiblock.type.navigationcomputer.HorizontalNavigationComputerMultiblockAdvanced
import net.horizonsend.ion.server.features.multiblock.type.navigationcomputer.NavigationComputerMultiblockBasic
import net.horizonsend.ion.server.features.multiblock.type.navigationcomputer.VerticalNavigationComputerMultiblockAdvanced
import net.horizonsend.ion.server.features.multiblock.type.particleshield.BoxShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.type.particleshield.EventShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass08Left
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass08Right
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass08i
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass20
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass30
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass65
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass85
import net.horizonsend.ion.server.features.multiblock.type.powerbank.PowerBankMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.powerbank.PowerBankMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.powerbank.PowerBankMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.powerbank.PowerCellMultiblock
import net.horizonsend.ion.server.features.multiblock.type.powerfurnace.PowerFurnaceMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.powerfurnace.PowerFurnaceMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.powerfurnace.PowerFurnaceMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.printer.ArmorPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.CarbonPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.CarbonProcessorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.GlassPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.TechnicalPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.cannon.LaserCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.cannon.PlasmaCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.cannon.PulseCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.CapitalBeamStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockBottom
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockSide
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockTop
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.FlamethrowerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.HorizontalPumpkinCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.MiniPhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.SonicMissileWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy.AIHeavyLaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy.AIPhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy.BottomArsenalStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy.DownwardRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy.HeavyLaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy.HorizontalRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy.PhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy.TopArsenalStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy.TorpedoStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy.UpwardRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.misc.PointDefenseStarshipWeaponMultiblockBottom
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.misc.PointDefenseStarshipWeaponMultiblockSide
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.misc.PointDefenseStarshipWeaponMultiblockTop
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.BottomHeavyTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.BottomIonTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.BottomLightTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.BottomQuadTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.BottomTriTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TopHeavyTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TopIonTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TopLightTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TopQuadTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TopTriTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.util.getBukkitBlockState
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import net.horizonsend.ion.server.miscellaneous.utils.toBlockKey
import org.bukkit.World
import org.bukkit.block.Sign
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

	private fun initMultiblocks() {
		multiblocks.putAll(listOf(
			CentrifugeMultiblock,
			CompressorMultiblock,
			FabricatorMultiblock,
			CircuitfabMultiblock,
			PlatePressMultiblock,
			GasFurnaceMultiblock,

			GeneratorMultiblockTier1,
			GeneratorMultiblockTier2,
			GeneratorMultiblockTier3,

			PowerFurnaceMultiblockTier1,
			PowerFurnaceMultiblockTier2,
			PowerFurnaceMultiblockTier3,

			PowerBankMultiblockTier1,
			PowerBankMultiblockTier2,
			PowerBankMultiblockTier3,

			PowerCellMultiblock,

			ChargerMultiblockTier1,
			ChargerMultiblockTier2,
			ChargerMultiblockTier3,

			HyperdriveMultiblockClass1,
			HyperdriveMultiblockClass2,
			HyperdriveMultiblockClass3,
			HyperdriveMultiblockClass4,

			NavigationComputerMultiblockBasic,
			VerticalNavigationComputerMultiblockAdvanced,
			HorizontalNavigationComputerMultiblockAdvanced,

			ShieldMultiblockClass08Right,
			ShieldMultiblockClass08Left,
			ShieldMultiblockClass20,
			ShieldMultiblockClass30,
			ShieldMultiblockClass65,
			ShieldMultiblockClass85,
			ShieldMultiblockClass08i,
			BoxShieldMultiblock,
			EventShieldMultiblock,

			CarbonProcessorMultiblock,

			CarbonPrinterMultiblock,
			TechnicalPrinterMultiblock,
			GlassPrinterMultiblock,
			ArmorPrinterMultiblock,

			DisconnectedDockingTubeMultiblock,
			ConnectedDockingTubeMultiblock,

			CryoPodMultiblock,
			FuelTankMultiblock,
			MagazineMultiblock,
			AirlockMultiblock,
			ExpandableAirlock,
			TractorBeamMultiblock,
			LargeTractorBeamMultiblock,

			ShipFactoryMultiblock,

			DrillMultiblockTier1,
			DrillMultiblockTier2,
			DrillMultiblockTier3,

			StandardGravityWellMultiblock,
			AmplifiedGravityWellMultiblock,

			AreaShield5,
			AreaShield10,
			AreaShield20,
			AreaShield30,

			MobDefender,

			StandardAmmoPressMultiblock,
			AmmoLoaderMultiblock,
			MissileLoaderMultiblock,

			LaserCannonStarshipWeaponMultiblock,
			PlasmaCannonStarshipWeaponMultiblock,
			PulseCannonStarshipWeaponMultiblock,
			HeavyLaserStarshipWeaponMultiblock,
			AIHeavyLaserStarshipWeaponMultiblock,
			AIPhaserStarshipWeaponMultiblock,
			TorpedoStarshipWeaponMultiblock,
			PointDefenseStarshipWeaponMultiblockTop,
			PointDefenseStarshipWeaponMultiblockSide,
			PointDefenseStarshipWeaponMultiblockBottom,
			TopLightTurretMultiblock,
			BottomLightTurretMultiblock,
			TopHeavyTurretMultiblock,
			BottomHeavyTurretMultiblock,
			TopTriTurretMultiblock,
			BottomTriTurretMultiblock,
			TopIonTurretMultiblock,
			BottomIonTurretMultiblock,
			TopQuadTurretMultiblock,
			BottomQuadTurretMultiblock,
			HorizontalRocketStarshipWeaponMultiblock,
			UpwardRocketStarshipWeaponMultiblock,
			DownwardRocketStarshipWeaponMultiblock,
			PhaserStarshipWeaponMultiblock,
			MiniPhaserStarshipWeaponMultiblock,
			SonicMissileWeaponMultiblock,
			DecomposerMultiblock,
			DisposalMultiblock,
			MiningLaserMultiblockTier1Top,
			MiningLaserMultiblockTier1Bottom,
			MiningLaserMultiblockTier1Side,
			MiningLaserMultiblockTier2Top,
			MiningLaserMultiblockTier2Bottom,
			MiningLaserMultiblockTier2Side,
			MiningLaserMultiblockTier3Top,
			MiningLaserMultiblockTier3Bottom,
			MiningLaserMultiblockTier3Side,
			MiningLaserMultiblockTier4Top,
			MiningLaserMultiblockTier4Bottom,
			MiningLaserMultiblockTier4Side,
			TopArsenalStarshipWeaponMultiblock,
			BottomArsenalStarshipWeaponMultiblock,

			ItemSplitterMultiblock,
			GasCollectorMultiblock,
			GasPowerPlantMultiblock,
			VentMultiblock,

			LandingGearMultiblock,

			AutoCrafterMultiblockTier1,
			AutoCrafterMultiblockTier2,
			AutoCrafterMultiblockTier3,

			HorizontalPumpkinCannonStarshipWeaponMultiblock,
			CthulhuBeamStarshipWeaponMultiblockBottom,
			CthulhuBeamStarshipWeaponMultiblockTop,
			CthulhuBeamStarshipWeaponMultiblockSide,
			FlamethrowerStarshipWeaponMultiblock,
			CapitalBeamStarshipWeaponMultiblock,

			PlanterMultiblockTier1,
			PlanterMultiblockTier2,
			PlanterMultiblockTier3,
			HarvesterMultiblockTier1,
			HarvesterMultiblockTier2,
			HarvesterMultiblockTier3,

			AntiAirCannonBaseMultiblock,
//			AntiAirCannonTurretMultiblock,

			BattleCruiserReactorMultiblock,
			CruiserReactorMultiblock,
			BargeReactorMultiBlock,
			OdometerMultiblock,
			TestMultiblock
		).associateBy { it.javaClass.simpleName })
	}

	val context = CoroutineScope(Dispatchers.Default + SupervisorJob())

	/**
	 * Map of world UUIDs to a map of block keys to Multiblock types
	 **/
	private val newMultiblockCache: MutableMap<UUID, MutableMap<Long, Multiblock>> = Object2ObjectOpenHashMap()

	override fun onEnable() {
		initMultiblocks()

		log.info("Loaded ${multiblocks.size} multiblocks")
	}

	fun all(): List<Multiblock> = multiblocks.values.toList()

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
	 * Get a multiblock by its identifying name
	 **/
	operator fun get(name: String) = multiblocks[name]

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
				if (!multiblock.matchesSign(sign.lines())) continue else Tasks.sync {
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
		val worldCache = newMultiblockCache[world.uid] ?: return null

		val key = toBlockKey(x, y, z)

		val possibleMultiblock = worldCache[key] ?: return null

		if (checkStructure && !possibleMultiblock.signMatchesStructureAsync(world, Vec3i(x, y, z), loadChunks)) return null

		return possibleMultiblock
	}

	fun getFromPDC(sign: Sign): Multiblock? {
		val pdc = sign.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING) ?: return null

		return multiblocks[pdc]
	}

	fun matchesPersistentDataContainer(persistentDataContainer: PersistentDataContainer, multiblock: Multiblock): Boolean {
		val value = persistentDataContainer.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING) ?: return false

		return value == multiblock::class.simpleName
	}

	/**
	 * The check for when someone right-clicks an undetected multiblock
	 **/
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onInteractMultiblockSign(event: PlayerInteractEvent) = context.launch {
		if (event.hand != EquipmentSlot.HAND || event.action != Action.RIGHT_CLICK_BLOCK) {
			return@launch
		}

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

		// Check all multiblocks
		for ((_, multiblock) in multiblocks) {
			// If it has the same sign text as the multiblock
			if (multiblock.matchesUndetectedSign(sign)) {
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
				} else {
					// Store the multi that last matched sign text
					lastMatch = multiblock
				}
			}
		}

		if (lastMatch != null) {
			player.userError("Improperly built ${lastMatch.name}. Make sure every block is correctly placed!")

			// Prompt the help command
			setupCommand(player, sign, lastMatch)
		}
	}

	@EventHandler
	fun onPlayerBreakBlock(event: BlockBreakEvent) = context.launch {
		val player = event.player
		if (getBlockTypeSafe(event.block.world, event.block.x, event.block.y, event.block.z)?.isSign == false) return@launch

		val sign = getBukkitBlockState(event.block, false) as? Sign ?: return@launch

		val multiblock = getFromSignPosition(
			sign.world,
			sign.x,
			sign.y,
			sign.z,
			checkStructure = true,
			loadChunks = false
		) ?: return@launch

		removeMultiblock(multiblock, sign, player)
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

		Tasks.sync {
			sign.update()
		}

		if (multiblock is EntityMultiblock<*>) {
			// Multiblock entities are stored inside the block that the sign is placed on
			val (x, _, z) = Multiblock.getOrigin(sign)

			val chunkX = x.shr(4)
			val chunkZ = z.shr(4)

			val chunk = sign.world.ion.getChunk(chunkX, chunkZ) ?: return@sync

			chunk.multiblockManager.addNewMultiblockEntity(multiblock, sign)
		}
	}

	/** Upon a multiblock being removed */
	fun removeMultiblock(multiblock: Multiblock, sign: Sign, player: Player) = Tasks.sync  {
		val (x, y, z) = Multiblock.getOrigin(sign)

		val chunkX = x.shr(4)
		val chunkZ = z.shr(4)

		val chunk = sign.world.ion.getChunk(chunkX, chunkZ) ?: return@sync

		chunk.multiblockManager.removeMultiblockEntity(x, y, z)
	}
}
