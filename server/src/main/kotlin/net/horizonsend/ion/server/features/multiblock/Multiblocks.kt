package net.horizonsend.ion.server.features.multiblock

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier4Bottom
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier4BottomMirrored
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier4Side
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier4SideMirrored
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier4Top
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier4TopMirrored
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.BottomIonTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.TopIonTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.AmmoLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.MissileLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.StandardAmmoPressMultiblock
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield10
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield20
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield30
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield5
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier1Mirrored
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier2Mirrored
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.autocrafter.AutoCrafterMultiblockTier3Mirrored
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
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblockTier1Mirrored
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblockTier2Mirrored
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblockTier3Mirrored
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
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier1BottomMirrored
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier1Side
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier1SideMirrored
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier1Top
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier1TopMirrored
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier2Bottom
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier2BottomMirrored
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier2Side
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier2SideMirrored
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier2Top
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier2TopMirrored
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier3Bottom
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier3BottomMirrored
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier3Side
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier3SideMirrored
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier3Top
import net.horizonsend.ion.server.features.multiblock.type.mininglasers.MiningLaserMultiblockTier3TopMirrored
import net.horizonsend.ion.server.features.multiblock.type.misc.AirlockMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.CryoPodMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.DecomposerMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.DisposalMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.DisposalMultiblockMirrored
import net.horizonsend.ion.server.features.multiblock.type.misc.ExpandableAirlock
import net.horizonsend.ion.server.features.multiblock.type.misc.FuelTankMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.ItemSplitterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.LandingGearMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.MagazineMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.MagazineMultiblockMirrored
import net.horizonsend.ion.server.features.multiblock.type.misc.MobDefender
import net.horizonsend.ion.server.features.multiblock.type.misc.OdometerMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.ShipFactoryMultiblock
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
import net.horizonsend.ion.server.features.multiblock.type.printer.ArmorPrinterMultiblockMirrored
import net.horizonsend.ion.server.features.multiblock.type.printer.CarbonPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.CarbonPrinterMultiblockMirrored
import net.horizonsend.ion.server.features.multiblock.type.printer.CarbonProcessorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.GlassPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.GlassPrinterMultiblockMirrored
import net.horizonsend.ion.server.features.multiblock.type.printer.TechnicalPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.type.printer.TechnicalPrinterMultiblockMirrored
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.cannon.LaserCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.cannon.PlasmaCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.cannon.PulseCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.CapitalBeamStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockBottom
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockSide
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockTop
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.FireWaveWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.FlamethrowerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.GazeStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.MiniPhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.PumpkinCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.SkullThrowerStarshipWeaponMultiblock
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
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.BottomLightTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.BottomQuadTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.BottomTriTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TopHeavyTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TopLightTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TopQuadTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TopTriTurretMultiblock
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType

object Multiblocks : IonServerComponent() {
	private lateinit var multiblocks: List<Multiblock>

	private fun initMultiblocks() {
		multiblocks = listOf(
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
			CarbonPrinterMultiblockMirrored,
			TechnicalPrinterMultiblock,
			TechnicalPrinterMultiblockMirrored,
			GlassPrinterMultiblock,
			GlassPrinterMultiblockMirrored,
			ArmorPrinterMultiblock,
			ArmorPrinterMultiblockMirrored,

			DisconnectedDockingTubeMultiblock,
			ConnectedDockingTubeMultiblock,

			CryoPodMultiblock,
			FuelTankMultiblock,
			MagazineMultiblock,
			MagazineMultiblockMirrored,
			AirlockMultiblock,
			ExpandableAirlock,
			TractorBeamMultiblock,

			ShipFactoryMultiblock,

			DrillMultiblockTier1,
			DrillMultiblockTier1Mirrored,
			DrillMultiblockTier2,
			DrillMultiblockTier2Mirrored,
			DrillMultiblockTier3,
			DrillMultiblockTier3Mirrored,

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
			FireWaveWeaponMultiblock,
			DecomposerMultiblock,
			DisposalMultiblock,
			DisposalMultiblockMirrored,
			MiningLaserMultiblockTier1Top,
			MiningLaserMultiblockTier1TopMirrored,
			MiningLaserMultiblockTier1Bottom,
			MiningLaserMultiblockTier1BottomMirrored,
			MiningLaserMultiblockTier1Side,
			MiningLaserMultiblockTier1SideMirrored,
			MiningLaserMultiblockTier2Top,
			MiningLaserMultiblockTier2TopMirrored,
			MiningLaserMultiblockTier2Bottom,
			MiningLaserMultiblockTier2BottomMirrored,
			MiningLaserMultiblockTier2Side,
			MiningLaserMultiblockTier2SideMirrored,
			MiningLaserMultiblockTier3Top,
			MiningLaserMultiblockTier3TopMirrored,
			MiningLaserMultiblockTier3Bottom,
			MiningLaserMultiblockTier3BottomMirrored,
			MiningLaserMultiblockTier3Side,
			MiningLaserMultiblockTier3SideMirrored,
			MiningLaserMultiblockTier4Top,
			MiningLaserMultiblockTier4TopMirrored,
			MiningLaserMultiblockTier4Bottom,
			MiningLaserMultiblockTier4BottomMirrored,
			MiningLaserMultiblockTier4Side,
			MiningLaserMultiblockTier4SideMirrored,
			TopArsenalStarshipWeaponMultiblock,
			BottomArsenalStarshipWeaponMultiblock,

			ItemSplitterMultiblock,
			GasCollectorMultiblock,
			GasPowerPlantMultiblock,
			VentMultiblock,

			LandingGearMultiblock,

			AutoCrafterMultiblockTier1,
			AutoCrafterMultiblockTier1Mirrored,
			AutoCrafterMultiblockTier2,
			AutoCrafterMultiblockTier2Mirrored,
			AutoCrafterMultiblockTier3,
			AutoCrafterMultiblockTier3Mirrored,

			PumpkinCannonStarshipWeaponMultiblock,
			CthulhuBeamStarshipWeaponMultiblockBottom,
			CthulhuBeamStarshipWeaponMultiblockTop,
			CthulhuBeamStarshipWeaponMultiblockSide,
			FlamethrowerStarshipWeaponMultiblock,
			CapitalBeamStarshipWeaponMultiblock,
			SkullThrowerStarshipWeaponMultiblock,
			GazeStarshipWeaponMultiblock,

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
			OdometerMultiblock
		)
	}

	private val multiblockCache: MutableMap<Location, Multiblock> = Object2ObjectOpenHashMap()

	override fun onEnable() {
		initMultiblocks()

		log.info("Loaded ${multiblocks.size} multiblocks")
	}

	fun all(): List<Multiblock> = multiblocks

	@JvmStatic
	@JvmOverloads
	operator fun get(sign: Sign, checkStructure: Boolean = true, loadChunks: Boolean = true): Multiblock?  {
		val location: Location = sign.location
		val pdc = sign.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING)

		val cached: Multiblock? = multiblockCache[location]
		if (cached != null) {
			val matchesSign = if (pdc != null) pdc == cached::class.simpleName else cached.matchesSign(sign.lines().toTypedArray())

			// one was already cached before
			if (matchesSign && (!checkStructure || cached.signMatchesStructure(sign, loadChunks = loadChunks))) {
				if (pdc == null) {
					sign.persistentDataContainer.set(
						NamespacedKeys.MULTIBLOCK,
						PersistentDataType.STRING,
						cached::class.simpleName!!
					)
					sign.update(false, false)
				}

				// it still matches so returned the cached one
				return cached
			} else {
				// it no longer matches so remove it, and re-detect it afterwards
				multiblockCache.remove(location)
			}
		}

		for (multiblock in multiblocks) {
			val matchesSign =
				if (pdc != null) pdc == multiblock::class.simpleName else multiblock.matchesSign(sign.lines().toTypedArray())
			if (matchesSign && (!checkStructure || multiblock.signMatchesStructure(sign, loadChunks = loadChunks))) {
				if (pdc == null) {
					sign.persistentDataContainer.set(
						NamespacedKeys.MULTIBLOCK,
						PersistentDataType.STRING,
						multiblock::class.simpleName!!
					)
					sign.update(false, false)
				}

				if (checkStructure) {
					multiblockCache[location] = multiblock
				}
				return multiblock
			}
		}

		return null
	}

	fun getFromPDC(sign: Sign): Multiblock? {
		val pdc = sign.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING) ?: return null

		return multiblocks.firstOrNull { it::class.simpleName == pdc }
	}

	fun matchesPersistentDataContainer(sign: Sign, multiblock: Multiblock): Boolean {
		return sign.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING)  == multiblock::class.simpleName
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onInteractMultiblockSign(event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND || event.action != Action.RIGHT_CLICK_BLOCK) {
			return
		}

		val sign = event.clickedBlock?.state as? Sign ?: return
		var lastMatch: Multiblock? = null
		val player = event.player

		if (!player.hasPermission("starlegacy.multiblock.detect")) {
			player.userError("You don't have permission to detect multiblocks!")
			return
		}

		for (multiblock in multiblocks) {
			if (multiblock.matchesUndetectedSign(sign)) {
				if (multiblock.signMatchesStructure(sign, particles = true)) {
					multiblock.requiredPermission?.let {
						if (!player.hasPermission(it)) return player.userError("You don't have permission to use that multiblock!")
					}

					event.player.rewardAchievement(Achievement.DETECT_MULTIBLOCK)

					multiblock.setupSign(player, sign)
					sign.persistentDataContainer.set(
						NamespacedKeys.MULTIBLOCK,
						PersistentDataType.STRING,
						multiblock::class.simpleName!!
					)
					sign.isWaxed = true
					sign.update()
					return
				} else {
					lastMatch = multiblock
				}
			}
		}

		if (lastMatch != null) {
			player.userError(
				"Improperly built ${lastMatch.name}. Make sure every block is correctly placed!"
			)

			setupCommand(player, sign, lastMatch)
		}
	}

	private fun setupCommand(player: Player, sign: Sign, lastMatch: Multiblock) {
		val multiblockType = lastMatch.name

		val possibleTiers = multiblocks.filter { it.name == multiblockType }

		val message = text()
			.append(text("Which type of $multiblockType are you trying to build? (Click one)"))
			.append(newline())

		for (tier in possibleTiers) {
			val tierName = tier.javaClass.simpleName

			val command = "/multiblock check $tierName ${sign.x} ${sign.y} ${sign.z}"

			val tierText = text().color(NamedTextColor.GRAY)
				.append(text("["))
				.append(text(tierName).color(NamedTextColor.DARK_GREEN).decorate(TextDecoration.BOLD))
				.append(text("]"))
				.clickEvent(ClickEvent.runCommand(command))
				.hoverEvent(text(command).asHoverEvent())

			if (possibleTiers.indexOf(tier) != possibleTiers.size - 1) tierText.append(text(", "))

			message.append(tierText)
		}

		player.sendMessage(message.build())
	}
}
