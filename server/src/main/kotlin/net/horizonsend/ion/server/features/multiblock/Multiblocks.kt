package net.horizonsend.ion.server.features.multiblock

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.horizonsend.ion.server.features.multiblock.misc.DisposalMultiblock
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier1Bottom
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier1Top
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier2Bottom
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier2Top
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier3Bottom
import net.horizonsend.ion.server.features.multiblock.mininglasers.MiningLaserMultiblockTier3Top
import net.horizonsend.ion.server.features.multiblock.starshipweapon.cannon.MiniPhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.cannon.SonicMissileWeaponMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.ammopress.StandardAmmoPressMultiblock
import net.horizonsend.ion.server.features.multiblock.areashield.AreaShield10
import net.horizonsend.ion.server.features.multiblock.areashield.AreaShield20
import net.horizonsend.ion.server.features.multiblock.areashield.AreaShield30
import net.horizonsend.ion.server.features.multiblock.areashield.AreaShield5
import net.horizonsend.ion.server.features.multiblock.charger.ChargerMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.charger.ChargerMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.charger.ChargerMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.dockingtube.ConnectedDockingTubeMultiblock
import net.horizonsend.ion.server.features.multiblock.dockingtube.DisconnectedDockingTubeMultiblock
import net.horizonsend.ion.server.features.multiblock.drills.DrillMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.drills.DrillMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.drills.DrillMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.generator.GeneratorMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.generator.GeneratorMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.generator.GeneratorMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.gravitywell.AmplifiedGravityWellMultiblock
import net.horizonsend.ion.server.features.multiblock.gravitywell.StandardGravityWellMultiblock
import net.horizonsend.ion.server.features.multiblock.hyperdrive.HyperdriveMultiblockClass1
import net.horizonsend.ion.server.features.multiblock.hyperdrive.HyperdriveMultiblockClass2
import net.horizonsend.ion.server.features.multiblock.hyperdrive.HyperdriveMultiblockClass3
import net.horizonsend.ion.server.features.multiblock.hyperdrive.HyperdriveMultiblockClass4
import net.horizonsend.ion.server.features.multiblock.misc.AirlockMultiblock
import net.horizonsend.ion.server.features.cryopods.CryoPodMultiblock
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.horizonsend.ion.server.features.multiblock.misc.DecomposerMultiblock
import net.horizonsend.ion.server.features.multiblock.misc.ItemSplitterMultiblock
import net.horizonsend.ion.server.features.multiblock.misc.MagazineMultiblock
import net.horizonsend.ion.server.features.multiblock.misc.MobDefender
import net.horizonsend.ion.server.features.multiblock.misc.ShipFactoryMultiblock
import net.horizonsend.ion.server.features.multiblock.misc.TractorBeamMultiblock
import net.horizonsend.ion.server.features.multiblock.navigationcomputer.NavigationComputerMultiblockAdvanced
import net.horizonsend.ion.server.features.multiblock.navigationcomputer.NavigationComputerMultiblockBasic
import net.horizonsend.ion.server.features.multiblock.particleshield.BoxShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.particleshield.EventShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.particleshield.ShieldMultiblockClass08Left
import net.horizonsend.ion.server.features.multiblock.particleshield.ShieldMultiblockClass08Right
import net.horizonsend.ion.server.features.multiblock.particleshield.ShieldMultiblockClass08i
import net.horizonsend.ion.server.features.multiblock.particleshield.ShieldMultiblockClass20
import net.horizonsend.ion.server.features.multiblock.particleshield.ShieldMultiblockClass30
import net.horizonsend.ion.server.features.multiblock.particleshield.ShieldMultiblockClass65
import net.horizonsend.ion.server.features.multiblock.particleshield.ShieldMultiblockClass85
import net.horizonsend.ion.server.features.multiblock.powerbank.PowerBankMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.powerbank.PowerBankMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.powerbank.PowerBankMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.powerbank.PowerCellMultiblock
import net.horizonsend.ion.server.features.multiblock.powerfurnace.PowerFurnaceMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.powerfurnace.PowerFurnaceMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.powerfurnace.PowerFurnaceMultiblockTier3
import net.horizonsend.ion.server.features.multiblock.printer.ArmorPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.printer.CarbonPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.printer.CarbonProcessorMultiblock
import net.horizonsend.ion.server.features.multiblock.printer.GlassPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.printer.TechnicalPrinterMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.cannon.LaserCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.cannon.PlasmaCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.cannon.PulseCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy.DownwardRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy.HeavyLaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy.HorizontalRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy.PhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy.TorpedoStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy.UpwardRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.misc.PointDefenseStarshipWeaponMultiblockBottom
import net.horizonsend.ion.server.features.multiblock.starshipweapon.misc.PointDefenseStarshipWeaponMultiblockSide
import net.horizonsend.ion.server.features.multiblock.starshipweapon.misc.PointDefenseStarshipWeaponMultiblockTop
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.BottomHeavyTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.BottomLightTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.BottomTriTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.TopHeavyTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.TopLightTurretMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.TopTriTurretMultiblock
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
			NavigationComputerMultiblockAdvanced,

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
			MagazineMultiblock,
			AirlockMultiblock,
			TractorBeamMultiblock,

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

			LaserCannonStarshipWeaponMultiblock,
			PlasmaCannonStarshipWeaponMultiblock,
			PulseCannonStarshipWeaponMultiblock,
			HeavyLaserStarshipWeaponMultiblock,
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
			MiningLaserMultiblockTier2Top,
			MiningLaserMultiblockTier2Bottom,
			MiningLaserMultiblockTier3Top,
			MiningLaserMultiblockTier3Bottom,

			ItemSplitterMultiblock
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
			val matchesSign =
				if (pdc != null) pdc == cached::class.simpleName else cached.matchesSign(sign.lines().toTypedArray())

			// one was already cached before
			if (matchesSign && (!checkStructure || cached.signMatchesStructure(sign, loadChunks))) {
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
			if (matchesSign && (!checkStructure || multiblock.signMatchesStructure(sign, loadChunks))) {
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
					event.player.rewardAchievement(Achievement.DETECT_MULTIBLOCK)
					return multiblock.setupSign(player, sign)
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
