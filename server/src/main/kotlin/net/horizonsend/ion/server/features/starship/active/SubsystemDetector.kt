package net.horizonsend.ion.server.features.starship.active

import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customBlock
import net.horizonsend.ion.server.features.multiblock.old.Multiblocks
import net.horizonsend.ion.server.features.multiblock.type.areashield.AreaShield
import net.horizonsend.ion.server.features.multiblock.type.checklist.BargeReactorMultiBlock
import net.horizonsend.ion.server.features.multiblock.type.checklist.BattleCruiserReactorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.checklist.CruiserReactorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblock
import net.horizonsend.ion.server.features.multiblock.type.gravitywell.GravityWellMultiblock
import net.horizonsend.ion.server.features.multiblock.type.hyperdrive.HyperdriveMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.AbstractMagazineMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.CryoPodMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.FuelTankMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.LandingGearMultiblock
import net.horizonsend.ion.server.features.multiblock.type.navigationcomputer.NavigationComputerMultiblock
import net.horizonsend.ion.server.features.multiblock.type.particleshield.BoxShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.type.particleshield.EventShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.type.particleshield.SphereShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.SubsystemMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TurretBaseMultiblock
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.BargeReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.BattlecruiserReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.CruiserReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.FuelTankSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.CryopodSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.GravityWellSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.HyperdriveSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.MagazineSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.NavCompSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.PlanetDrillSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.reactor.ReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.BoxShieldSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.EventShieldSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.SphereShieldSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.thruster.ThrusterSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.thruster.ThrusterType
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.isFroglight
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import net.kyori.adventure.audience.Audience
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.EAST
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.block.BlockFace.NORTH_EAST
import org.bukkit.block.BlockFace.NORTH_WEST
import org.bukkit.block.BlockFace.SOUTH
import org.bukkit.block.BlockFace.SOUTH_EAST
import org.bukkit.block.BlockFace.SOUTH_WEST
import org.bukkit.block.BlockFace.WEST
import org.bukkit.block.HangingSign
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import java.util.EnumSet
import java.util.LinkedList
import java.util.Locale

object SubsystemDetector {
	fun detectSubsystems(feedbackDestination: Audience, starship: ActiveControlledStarship) {
		// these has to be queued for after the loop so things like the bounds are detected first,
		// and so they are detected in the right order, e.g. weapons before weapon sets/signs
		val potentialThrusterBlocks = LinkedList<Block>()
		val potentialWeaponBlocks = LinkedList<Block>()
		val potentialSignBlocks = LinkedList<Block>()
		val potentialLandingGearBlocks = LinkedList<Block>()
		val potentialTurretBases = LinkedList<Block>()

		starship.iterateBlocks { x, y, z ->
			val block = starship.world.getBlockAt(x, y, z)
			val type = block.type

			if (type.isWallSign) potentialSignBlocks.add(block)

			potentialWeaponBlocks.add(block)

			if (
				type == Material.GLOWSTONE ||
				type == Material.REDSTONE_LAMP ||
				type == Material.SEA_LANTERN ||
				type == Material.MAGMA_BLOCK ||
				type.isFroglight
			) {
				potentialThrusterBlocks += block
			}

			if (type == Material.OBSERVER) potentialLandingGearBlocks.add(block)

			if (type == Material.LOOM) potentialTurretBases.add(block)
		}

		val oversizeModifier = if (starship.initialBlockCount > starship.type.maxSize) ReactorSubsystem.OVERSIZE_POWER_PENALTY else 1.0
		starship.reactor = ReactorSubsystem(starship, oversizeModifier)
		starship.subsystems += starship.reactor

		for (block in potentialThrusterBlocks) {
			detectThruster(starship, block)
		}
		for (block in potentialWeaponBlocks) {
			detectWeapon(feedbackDestination, starship, block)
		}
		for (block in potentialLandingGearBlocks) {
			detectLandingGear(starship, block)
		}
		for (block in potentialTurretBases) {
			detectCustomTurretBase(starship, block)
		}
		for (block in potentialSignBlocks) {
			try {
				detectSign(starship, block)
			} catch (e: NumberFormatException) {
				feedbackDestination.userError("Box shield at ${Vec3i(block.location)} could not be parsed!")
				continue
			}
		}

		filterSubsystems(starship)

		// Do this after all subsystems are detected so that they can be captured
		starship.customTurrets.forEach { it.detectTurret() }
	}

	private fun detectSign(starship: ActiveControlledStarship, block: Block) {
		val sign = block.state as Sign

		if (Multiblocks.getFromPDC(sign) is AreaShield) {
			throw ActiveStarshipFactory.StarshipActivationException("Starships cannot fly with area shields!")
		}

		if (sign.type.isWallSign && sign.getLine(0).lowercase(Locale.getDefault()).contains("node")) {
			val inwardFace = sign.getFacing().oppositeFace
			val location = sign.block.getRelative(inwardFace).location
			val pos = Vec3i(location)
			val weaponSubsystems = starship.subsystems
				.filterIsInstance<WeaponSubsystem>()
				.filter { it.pos == pos }

			for (weaponSubsystem in weaponSubsystems) {
				val nodes = sign.lines.slice(1..3)
					.filter { it.isNotEmpty() }
					.map { it.lowercase(Locale.getDefault()) }
				for (node in nodes) {
					starship.weaponSets[node].add(weaponSubsystem)
				}
			}
			return
		}

		val multiblock = Multiblocks[sign] ?: return

		when (multiblock) {
			is SphereShieldMultiblock -> {
				starship.subsystems += SphereShieldSubsystem(starship, sign, multiblock)
			}

			is EventShieldMultiblock -> {
				if (starship.playerPilot?.hasPermission("ion.core.eventship") == false) return
				starship.subsystems += EventShieldSubsystem(starship, sign)
			}

			is BoxShieldMultiblock -> {
				starship.subsystems += BoxShieldSubsystem(starship, sign, multiblock)
			}

			is BattleCruiserReactorMultiblock -> {
				starship.subsystems += BattlecruiserReactorSubsystem(starship, sign, multiblock)
			}

			is CruiserReactorMultiblock -> {
				starship.subsystems += CruiserReactorSubsystem(starship, sign, multiblock)
			}

			is BargeReactorMultiBlock -> {
				starship.subsystems += BargeReactorSubsystem(starship, sign, multiblock)
			}

			is FuelTankMultiblock -> {
				starship.subsystems += FuelTankSubsystem(starship, sign, multiblock)
			}

			is HyperdriveMultiblock -> {
				starship.subsystems += HyperdriveSubsystem(starship, sign, multiblock)
			}

			is NavigationComputerMultiblock -> {
				starship.subsystems += NavCompSubsystem(starship, sign, multiblock)
			}

			is AbstractMagazineMultiblock -> {
				starship.subsystems += MagazineSubsystem(starship, sign, multiblock)
			}

			is DrillMultiblock -> {
				starship.drillCount++
				starship.subsystems += PlanetDrillSubsystem(starship, sign, multiblock)
			}

			is CryoPodMultiblock -> {
				val cryo = Cryopod[Vec3i(sign.location), sign.world.name] ?: return
				starship.subsystems += CryopodSubsystem(starship, sign, multiblock, cryo)
			}

			is GravityWellMultiblock -> {
				starship.subsystems += GravityWellSubsystem(starship, sign, multiblock)
			}
		}
	}

	private fun detectThruster(starship: ActiveControlledStarship, block: Block) {
		for (face in CARDINAL_BLOCK_FACES) {
			val thrusterType: ThrusterType = ThrusterType.values()
				.firstOrNull { it.matchesStructure(starship, block.x, block.y, block.z, face) }
				?: continue
			val pos = Vec3i(block.blockKey)
			starship.subsystems += ThrusterSubsystem(starship, pos, face, thrusterType)
		}
	}

	private fun detectWeapon(feedbackDestination: Audience, starship: ActiveControlledStarship, block: Block) {
		for (face: BlockFace in CARDINAL_BLOCK_FACES) {
			val multiblock = getWeaponMultiblock(block, face) ?: continue

			val pos = Vec3i(block.location)

			val subsystem = multiblock.createSubsystem(starship, pos, face)

			if (subsystem is PermissionWeaponSubsystem && starship.playerPilot?.hasPermission(subsystem.permission) == false) {
				throw ActiveStarshipFactory.StarshipActivationException("You don't have permission ${subsystem.permission} to use ${subsystem::class.simpleName}")
			}

			if (isDuplicate(starship, subsystem)) {
				continue
			}

			if (subsystem is WeaponSubsystem && !subsystem.canCreateSubsystem()) {
//				feedbackDestination.userError("Could not create subsystem ${subsystem.name}!") TODO wait for preference system
				continue
			}

			starship.subsystems += subsystem
		}
	}

	fun detectLandingGear(starship: ActiveControlledStarship, block: Block) {
		val matches = LandingGearMultiblock.blockMatchesStructure(block, NORTH)

		if (!matches) return

		starship.subsystems += LandingGearMultiblock.createSubsystem(starship, Vec3i(block.location), NORTH)
	}

	fun detectCustomTurretBase(starship: ActiveControlledStarship, block: Block) {
		val matches = EnumSet.of(NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST).map { block.getRelative(it) }.all {
			it.customBlock == CustomBlocks.TITANIUM_BLOCK
		}

		if (!matches) return

		val facing = (block.blockData as Directional).facing

		starship.subsystems += TurretBaseMultiblock.createSubsystem(starship, Vec3i(block.x, block.y, block.z), facing)
	}

	private fun isDuplicate(starship: ActiveControlledStarship, subsystem: StarshipSubsystem): Boolean {
		return subsystem is DirectionalSubsystem && starship.subsystems
			.filterIsInstance<WeaponSubsystem>()
			.filter { it.pos == subsystem.pos }
			.filterIsInstance<DirectionalSubsystem>()
			.any { it.face == subsystem.face }
	}

	private fun getWeaponMultiblock(block: Block, face: BlockFace): SubsystemMultiblock<*>? {
		return when {
			block.state is Sign && block.state !is HangingSign -> getSignWeaponMultiblock(block, face)
			else -> getSignlessStarshipWeaponMultiblock(block, face)
		}
	}

	private fun getSignWeaponMultiblock(block: Block, face: BlockFace): SubsystemMultiblock<*>? {
		val sign = block.state as Sign

		// avoid duplicates
		if (sign.getFacing() != face) {
			return null
		}

		val multiblock = Multiblocks[sign]

		if (multiblock !is SubsystemMultiblock<*>) {
			return null
		}

		return multiblock
	}

	private fun getSignlessStarshipWeaponMultiblock(block: Block, face: BlockFace): SubsystemMultiblock<*>? {
		return Multiblocks.all()
			.filterIsInstance<SignlessStarshipWeaponMultiblock<*>>()
			.firstOrNull { it.blockMatchesStructure(block, face) }
	}

	private fun filterSubsystems(starship: ActiveControlledStarship) {
		starship.subsystems.filterIsInstanceTo(starship.shields)
		starship.subsystems.filterIsInstanceTo(starship.weapons)
		starship.subsystems.filterIsInstanceTo(starship.turrets)
		starship.subsystems.filterIsInstanceTo(starship.hyperdrives)
		starship.subsystems.filterIsInstanceTo(starship.navComps)
		starship.subsystems.filterIsInstanceTo(starship.thrusters)
		starship.subsystems.filterIsInstanceTo(starship.magazines)
		starship.subsystems.filterIsInstanceTo(starship.gravityWells)
		starship.subsystems.filterIsInstanceTo(starship.drills)
		starship.subsystems.filterIsInstanceTo(starship.fuelTanks)
		starship.subsystems.filterIsInstanceTo(starship.customTurrets)
	}
}
