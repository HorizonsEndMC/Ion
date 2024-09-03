package net.horizonsend.ion.server.features.multiblock

import com.google.common.collect.Multimap
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockBottom
import net.horizonsend.ion.server.features.multiblock.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockSide
import net.horizonsend.ion.server.features.multiblock.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockTop
import net.horizonsend.ion.server.features.multiblock.type.defense.passive.areashield.AreaShield10
import net.horizonsend.ion.server.features.multiblock.type.defense.passive.areashield.AreaShield20
import net.horizonsend.ion.server.features.multiblock.type.defense.passive.areashield.AreaShield30
import net.horizonsend.ion.server.features.multiblock.type.defense.passive.areashield.AreaShield5
import net.horizonsend.ion.server.features.multiblock.type.dockingtube.ConnectedDockingTubeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.dockingtube.DisconnectedDockingTubeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.ElectrolysisMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.collector.PipedGasCollectorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.storage.FluidTankMedium
import net.horizonsend.ion.server.features.multiblock.type.fluid.storage.FluidTankSmall
import net.horizonsend.ion.server.features.multiblock.type.misc.AirlockMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.CryoPodMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.ExpandableAirlock
import net.horizonsend.ion.server.features.multiblock.type.misc.FuelTankMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.MagazineMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.MobDefender
import net.horizonsend.ion.server.features.multiblock.type.misc.ShipFactoryMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.TestMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.TractorBeamMultiblock
import net.horizonsend.ion.server.features.multiblock.type.particleshield.BoxShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.type.particleshield.EventShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass08Left
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass08Right
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass08i
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass20
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass30
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass65
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblockClass85
import net.horizonsend.ion.server.features.multiblock.type.power.storage.PowerBankMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.power.storage.PowerBankMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.power.storage.PowerBankMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.type.power.storage.PowerCellMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.LandingGearMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.OdometerMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.checklist.BargeReactorMultiBlock
import net.horizonsend.ion.server.features.multiblock.type.starship.checklist.BattleCruiserReactorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.checklist.CruiserReactorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.hyperdrive.HyperdriveMultiblockClass1
import net.horizonsend.ion.server.features.multiblock.type.starship.hyperdrive.HyperdriveMultiblockClass2
import net.horizonsend.ion.server.features.multiblock.type.starship.hyperdrive.HyperdriveMultiblockClass3
import net.horizonsend.ion.server.features.multiblock.type.starship.hyperdrive.HyperdriveMultiblockClass4
import net.horizonsend.ion.server.features.multiblock.type.starship.navigationcomputer.HorizontalNavigationComputerMultiblockAdvanced
import net.horizonsend.ion.server.features.multiblock.type.starship.navigationcomputer.NavigationComputerMultiblockBasic
import net.horizonsend.ion.server.features.multiblock.type.starship.navigationcomputer.VerticalNavigationComputerMultiblockAdvanced
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.LaserCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.PlasmaCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.PulseCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.CapitalBeamStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.FlamethrowerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.HorizontalPumpkinCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.MiniPhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.SonicMissileWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.AIHeavyLaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.AIPhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.BottomArsenalStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.DownwardRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.HeavyLaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.HorizontalRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.PhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.TopArsenalStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.TorpedoStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.UpwardRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.misc.PointDefenseStarshipWeaponMultiblockBottom
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.misc.PointDefenseStarshipWeaponMultiblockSide
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.misc.PointDefenseStarshipWeaponMultiblockTop
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.BottomHeavyTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.BottomIonTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.BottomLightTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.BottomQuadTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.BottomTriTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TopHeavyTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TopIonTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TopLightTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TopQuadTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TopTriTurretMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf

object MultiblockRegistration : IonServerComponent() {
	private val multiblocks: MutableMap<String, Multiblock> = mutableMapOf()

	/**
	 * The multiblocks grouped by their sign text.
	 *
	 * E.g. powerbank would contain all tiers of Power Banks
	 **/
	private val byDetectionName : Multimap<String, Multiblock> = multimapOf()

	override fun onEnable() {
		initMultiblocks()
		sortMultiblocks()

		log.info("Loaded ${multiblocks.size} multiblocks")
	}

	private fun initMultiblocks() {
        registerMultiblock(TestMultiblock)

		// Power
		registerMultiblock(PowerBankMultiblockTier1)
		registerMultiblock(PowerBankMultiblockTier2)
		registerMultiblock(PowerBankMultiblockTier3)
		registerMultiblock(PowerBankMultiblockTier1, "NewPowerBankMultiblockTier1") //TODO testing only - remove on live
		registerMultiblock(PowerBankMultiblockTier2, "NewPowerBankMultiblockTier2") //TODO testing only - remove on live
		registerMultiblock(PowerBankMultiblockTier3, "NewPowerBankMultiblockTier3") //TODO testing only - remove on live
		registerMultiblock(PowerCellMultiblock)

		// Gas
		registerMultiblock(PipedGasCollectorMultiblock)
		registerMultiblock(ElectrolysisMultiblock)
		registerMultiblock(FluidTankSmall)
		registerMultiblock(FluidTankMedium)
//		registerMultiblock(FluidTankLarge)

		// Defenses
		registerMultiblock(AreaShield5)
		registerMultiblock(AreaShield10)
		registerMultiblock(AreaShield20)
		registerMultiblock(AreaShield30)

		registerMultiblock(MobDefender)

		// Starship weapons
		registerMultiblock(LaserCannonStarshipWeaponMultiblock)
		registerMultiblock(PlasmaCannonStarshipWeaponMultiblock)
		registerMultiblock(PulseCannonStarshipWeaponMultiblock)
		registerMultiblock(HeavyLaserStarshipWeaponMultiblock)
		registerMultiblock(AIHeavyLaserStarshipWeaponMultiblock)
		registerMultiblock(AIPhaserStarshipWeaponMultiblock)
		registerMultiblock(TorpedoStarshipWeaponMultiblock)
		registerMultiblock(PointDefenseStarshipWeaponMultiblockTop)
		registerMultiblock(PointDefenseStarshipWeaponMultiblockSide)
		registerMultiblock(PointDefenseStarshipWeaponMultiblockBottom)
		registerMultiblock(TopLightTurretMultiblock)
		registerMultiblock(BottomLightTurretMultiblock)
		registerMultiblock(TopHeavyTurretMultiblock)
		registerMultiblock(BottomHeavyTurretMultiblock)
		registerMultiblock(TopTriTurretMultiblock)
		registerMultiblock(BottomTriTurretMultiblock)
		registerMultiblock(TopIonTurretMultiblock)
		registerMultiblock(BottomIonTurretMultiblock)
		registerMultiblock(TopQuadTurretMultiblock)
		registerMultiblock(BottomQuadTurretMultiblock)
		registerMultiblock(HorizontalRocketStarshipWeaponMultiblock)
		registerMultiblock(UpwardRocketStarshipWeaponMultiblock)
		registerMultiblock(DownwardRocketStarshipWeaponMultiblock)
		registerMultiblock(PhaserStarshipWeaponMultiblock)
		registerMultiblock(MiniPhaserStarshipWeaponMultiblock)

		registerMultiblock(TopArsenalStarshipWeaponMultiblock)
		registerMultiblock(BottomArsenalStarshipWeaponMultiblock)

		// Starship event weapons
		registerMultiblock(SonicMissileWeaponMultiblock)
		registerMultiblock(HorizontalPumpkinCannonStarshipWeaponMultiblock)
		registerMultiblock(CthulhuBeamStarshipWeaponMultiblockBottom)
		registerMultiblock(CthulhuBeamStarshipWeaponMultiblockTop)
		registerMultiblock(CthulhuBeamStarshipWeaponMultiblockSide)
		registerMultiblock(FlamethrowerStarshipWeaponMultiblock)
		registerMultiblock(CapitalBeamStarshipWeaponMultiblock)

		// Starship utilities
		registerMultiblock(BattleCruiserReactorMultiblock)
		registerMultiblock(CruiserReactorMultiblock)
		registerMultiblock(BargeReactorMultiBlock)
		registerMultiblock(FuelTankMultiblock)

		registerMultiblock(OdometerMultiblock)
		registerMultiblock(LandingGearMultiblock)
		registerMultiblock(MagazineMultiblock)

		registerMultiblock(NavigationComputerMultiblockBasic)
		registerMultiblock(VerticalNavigationComputerMultiblockAdvanced)
		registerMultiblock(HorizontalNavigationComputerMultiblockAdvanced)
		registerMultiblock(HorizontalNavigationComputerMultiblockAdvanced, "NavigationComputerMultiblockAdvanced")

		registerMultiblock(HyperdriveMultiblockClass1)
		registerMultiblock(HyperdriveMultiblockClass2)
		registerMultiblock(HyperdriveMultiblockClass3)
		registerMultiblock(HyperdriveMultiblockClass4)

		// Starship shields
		registerMultiblock(ShieldMultiblockClass08Right)
		registerMultiblock(ShieldMultiblockClass08Left)
		registerMultiblock(ShieldMultiblockClass20)
		registerMultiblock(ShieldMultiblockClass30)
		registerMultiblock(ShieldMultiblockClass65)
		registerMultiblock(ShieldMultiblockClass85)
		registerMultiblock(ShieldMultiblockClass08i)
		registerMultiblock(BoxShieldMultiblock)
		registerMultiblock(EventShieldMultiblock)

		// Machine
		registerMultiblock(ShipFactoryMultiblock)

		// Misc
		registerMultiblock(DisconnectedDockingTubeMultiblock)
		registerMultiblock(ConnectedDockingTubeMultiblock)
		registerMultiblock(CryoPodMultiblock)
		registerMultiblock(AirlockMultiblock)
		registerMultiblock(ExpandableAirlock)
		registerMultiblock(TractorBeamMultiblock)
	}

	private fun sortMultiblocks() {
		for (multi in getAllMultiblocks()) {
			byDetectionName[multi.name].add(multi)

			if (multi.alternativeDetectionNames.isEmpty()) continue

			for (altName in multi.alternativeDetectionNames) {
				byDetectionName[multi.name].add(multi)
			}
		}
	}

	private fun registerMultiblock(multiblock: Multiblock) {
		val name = multiblock.javaClass.simpleName ?: throw IllegalArgumentException("Provided anonymous multiblock class!")

		if (multiblocks.containsKey(name)) {
			throw IllegalArgumentException("Attempted to register duplicate multiblock name! Exisitng: ${multiblocks[name]}, new: $multiblock")
		}

		multiblocks[name] = multiblock
	}

	/**
	 * Registers a multiblock under a different storage identifier. This is to be used in the case a class has to be renamed, or similar.
	 **/
	private fun registerMultiblock(multiblock: Multiblock, storageAlias: String) {
		if (multiblocks.containsKey(storageAlias)) {
			throw IllegalArgumentException("Attempted to register duplicate multiblock name! Exisitng: ${multiblocks[storageAlias]}, new: $multiblock")
		}

		multiblocks[storageAlias] = multiblock
	}

	fun getAllMultiblocks() = multiblocks.values.toSet()

	fun getByDetectionName(name: String): List<Multiblock> {
		return byDetectionName[name].toList()
	}

	fun getByStorageName(name: String): Multiblock? {
		return multiblocks[name]
	}
}
