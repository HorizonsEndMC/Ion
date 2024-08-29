package net.horizonsend.ion.server.features.multiblock.newer

import com.google.common.collect.Multimap
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockBottom
import net.horizonsend.ion.server.features.multiblock.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockSide
import net.horizonsend.ion.server.features.multiblock.starshipweapon.event.CthulhuBeamStarshipWeaponMultiblockTop
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield10
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield20
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield30
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield5
import net.horizonsend.ion.server.features.multiblock.type.checklist.BargeReactorMultiBlock
import net.horizonsend.ion.server.features.multiblock.type.checklist.BattleCruiserReactorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.checklist.CruiserReactorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.dockingtube.ConnectedDockingTubeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.dockingtube.DisconnectedDockingTubeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.gas.PipedGasCollectorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.hyperdrive.HyperdriveMultiblockClass1
import net.horizonsend.ion.server.features.multiblock.type.hyperdrive.HyperdriveMultiblockClass2
import net.horizonsend.ion.server.features.multiblock.type.hyperdrive.HyperdriveMultiblockClass3
import net.horizonsend.ion.server.features.multiblock.type.hyperdrive.HyperdriveMultiblockClass4
import net.horizonsend.ion.server.features.multiblock.type.misc.AirlockMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.CryoPodMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.FuelTankMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.LandingGearMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.MagazineMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.MobDefender
import net.horizonsend.ion.server.features.multiblock.type.misc.OdometerMultiblock
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
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.cannon.LaserCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.cannon.PlasmaCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.cannon.PulseCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.CapitalBeamStarshipWeaponMultiblock
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

		// Misc
		registerMultiblock(DisconnectedDockingTubeMultiblock)
		registerMultiblock(ConnectedDockingTubeMultiblock)
		registerMultiblock(CryoPodMultiblock)
		registerMultiblock(AirlockMultiblock)
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

	private fun registerMultiblock(multiblock: Multiblock, alias: String) {
		if (multiblocks.containsKey(alias)) {
			throw IllegalArgumentException("Attempted to register duplicate multiblock name! Exisitng: ${multiblocks[alias]}, new: $multiblock")
		}

		multiblocks[alias] = multiblock
	}

	fun getAllMultiblocks() = multiblocks.values.toSet()

	fun getByDetectionName(name: String): List<Multiblock> {
		return byDetectionName[name].toList()
	}

	fun getByStorageName(name: String): Multiblock? {
		return multiblocks[name]
	}
}
