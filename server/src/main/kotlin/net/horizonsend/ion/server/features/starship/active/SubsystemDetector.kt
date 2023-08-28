package net.horizonsend.ion.server.features.starship.active

import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.drills.DrillMultiblock
import net.horizonsend.ion.server.features.multiblock.hyperdrive.HyperdriveMultiblock
import net.horizonsend.ion.server.features.multiblock.misc.CryoPodMultiblock
import net.horizonsend.ion.server.features.multiblock.misc.LandingGearMultiblock
import net.horizonsend.ion.server.features.multiblock.misc.MagazineMultiblock
import net.horizonsend.ion.server.features.multiblock.navigationcomputer.NavigationComputerMultiblock
import net.horizonsend.ion.server.features.multiblock.particleshield.BoxShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.particleshield.EventShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.particleshield.SphereShieldMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SubsystemMultiblock
import net.horizonsend.ion.server.features.starship.subsystem.CryoSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.HyperdriveSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.MagazineSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.NavCompSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.PlanetDrillSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.reactor.ReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.BoxShieldSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.SphereShieldSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.thruster.ThrusterSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.thruster.ThrusterType
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.isFroglight
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import java.util.LinkedList
import java.util.Locale

object SubsystemDetector {
	fun detectSubsystems(starship: ActiveControlledStarship) {
		// these has to be queued for after the loop so things like the bounds are detected first,
		// and so they are detected in the right order, e.g. weapons before weapon sets/signs
		val potentialThrusterBlocks = LinkedList<Block>()
		val potentialWeaponBlocks = LinkedList<Block>()
		val potentialSignBlocks = LinkedList<Block>()
		val potentialLandingGearBlocks = LinkedList<Block>()

		starship.iterateBlocks { x, y, z ->
			val block = starship.serverLevel.world.getBlockAt(x, y, z)
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
		}

		starship.reactor = ReactorSubsystem(starship)
		starship.subsystems += starship.reactor

		for (block in potentialThrusterBlocks) {
			detectThruster(starship, block)
		}
		for (block in potentialWeaponBlocks) {
			detectWeapon(starship, block)
		}
		for (block in potentialLandingGearBlocks) {
			detectLandingGear(starship, block)
		}
		for (block in potentialSignBlocks) {
			detectSign(starship, block)
		}

		filterSubsystems(starship)
	}

	private fun detectSign(starship: ActiveControlledStarship, block: Block) {
		val sign = block.state as Sign

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
				if (multiblock is EventShieldMultiblock && starship.lastPilot?.hasPermission("ion.core.eventship") == false) return
				starship.subsystems += SphereShieldSubsystem(starship, sign, multiblock)
			}

			is BoxShieldMultiblock -> {
				starship.subsystems += BoxShieldSubsystem(starship, sign, multiblock)
			}

			is HyperdriveMultiblock -> {
				starship.subsystems += HyperdriveSubsystem(starship, sign, multiblock)
			}

			is NavigationComputerMultiblock -> {
				starship.subsystems += NavCompSubsystem(starship, sign, multiblock)
			}

			is MagazineMultiblock -> {
				starship.subsystems += MagazineSubsystem(starship, sign, multiblock)
			}

			is DrillMultiblock -> {
				starship.drillCount++
				starship.subsystems += PlanetDrillSubsystem(starship, sign, multiblock)
			}

			is CryoPodMultiblock -> {
				val cryo = Cryopod[Vec3i(sign.location), sign.world.name] ?: return
				starship.subsystems += CryoSubsystem(starship, sign, multiblock, cryo)
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

	private fun detectWeapon(starship: ActiveControlledStarship, block: Block) {
		for (face: BlockFace in CARDINAL_BLOCK_FACES) {
			val multiblock = getWeaponMultiblock(block, face) ?: continue

			val pos = Vec3i(block.location)

			val subsystem = multiblock.createSubsystem(starship, pos, face)

			if (subsystem is PermissionWeaponSubsystem && starship.pilot?.hasPermission(subsystem.permission) == false) {
				continue
			}

			if (isDuplicate(starship, subsystem)) {
				continue
			}

			starship.subsystems += subsystem
		}
	}

	fun detectLandingGear(starship: ActiveControlledStarship, block: Block) {
		val matches = LandingGearMultiblock.blockMatchesStructure(block, BlockFace.NORTH)

		if (!matches) return

		starship.subsystems += LandingGearMultiblock.createSubsystem(starship, Vec3i(block.location), BlockFace.NORTH)
	}

	private fun isDuplicate(starship: ActiveControlledStarship, subsystem: WeaponSubsystem): Boolean {
		return subsystem is DirectionalSubsystem && starship.subsystems
			.filterIsInstance<WeaponSubsystem>()
			.filter { it.pos == subsystem.pos }
			.filterIsInstance<DirectionalSubsystem>()
			.any { it.face == subsystem.face }
	}

	private fun getWeaponMultiblock(block: Block, face: BlockFace): SubsystemMultiblock<*>? {
		return when {
			block.state is Sign -> getSignWeaponMultiblock(block, face)
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
	}
}
