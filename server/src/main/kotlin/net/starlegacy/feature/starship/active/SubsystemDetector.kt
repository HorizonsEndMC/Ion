package net.starlegacy.feature.starship.active

import net.starlegacy.feature.misc.CryoPods
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.drills.DrillMultiblock
import net.starlegacy.feature.multiblock.hyperdrive.HyperdriveMultiblock
import net.starlegacy.feature.multiblock.misc.CryoPodMultiblock
import net.starlegacy.feature.multiblock.misc.MagazineMultiblock
import net.starlegacy.feature.multiblock.navigationcomputer.NavigationComputerMultiblock
import net.starlegacy.feature.multiblock.particleshield.BoxShieldMultiblock
import net.starlegacy.feature.multiblock.particleshield.EventShieldMultiblock
import net.starlegacy.feature.multiblock.particleshield.SphereShieldMultiblock
import net.starlegacy.feature.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.multiblock.starshipweapon.StarshipWeaponMultiblock
import net.starlegacy.feature.starship.subsystem.CryoSubsystem
import net.starlegacy.feature.starship.subsystem.DirectionalSubsystem
import net.starlegacy.feature.starship.subsystem.HyperdriveSubsystem
import net.starlegacy.feature.starship.subsystem.MagazineSubsystem
import net.starlegacy.feature.starship.subsystem.NavCompSubsystem
import net.starlegacy.feature.starship.subsystem.reactor.ReactorSubsystem
import net.starlegacy.feature.starship.subsystem.shield.BoxShieldSubsystem
import net.starlegacy.feature.starship.subsystem.shield.SphereShieldSubsystem
import net.starlegacy.feature.starship.subsystem.thruster.ThrusterSubsystem
import net.starlegacy.feature.starship.subsystem.thruster.ThrusterType
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.util.CARDINAL_BLOCK_FACES
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.isWallSign
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import java.util.LinkedList
import java.util.Locale

object SubsystemDetector {
	fun detectSubsystems(starship: ActivePlayerStarship) {
		// these has to be queued for after the loop so things like the bounds are detected first,
		// and so they are detected in the right order, e.g. weapons before weapon sets/signs
		val potentialThrusterBlocks = LinkedList<Block>()
		val potentialWeaponBlocks = LinkedList<Block>()
		val potentialSignBlocks = LinkedList<Block>()
		starship.iterateBlocks { x, y, z ->
			val block = starship.serverLevel.world.getBlockAt(x, y, z)
			val type = block.type

			if (type.isWallSign) {
				potentialSignBlocks.add(block)
			}

			potentialWeaponBlocks.add(block)

			if (type == Material.GLOWSTONE || type == Material.REDSTONE_LAMP || type == Material.SEA_LANTERN || type == Material.MAGMA_BLOCK) {
				potentialThrusterBlocks += block
			}
		}

		starship.reactor = ReactorSubsystem(starship)
		starship.subsystems += starship.reactor

		for (block in potentialThrusterBlocks) {
			detectThruster(starship, block)
		}
		for (block in potentialWeaponBlocks) {
			detectWeapon(starship, block)
		}
		for (block in potentialSignBlocks) {
			detectSign(starship, block)
		}

		filterSubsystems(starship)
	}

	private fun detectSign(starship: ActivePlayerStarship, block: Block) {
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
				if (multiblock is EventShieldMultiblock && starship.pilot?.hasPermission("ion.core.eventship") == false) return
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
			}

			is CryoPodMultiblock -> {
				val cryoPod = CryoPods[sign]
				if (cryoPod != null) {
					starship.subsystems += CryoSubsystem(starship, sign, multiblock, cryoPod)
				}
			}
		}
	}

	private fun detectThruster(starship: ActivePlayerStarship, block: Block) {
		for (face in CARDINAL_BLOCK_FACES) {
			val thrusterType: ThrusterType = ThrusterType.values()
				.firstOrNull { it.matchesStructure(starship, block.x, block.y, block.z, face) }
				?: continue
			val pos = Vec3i(block.blockKey)
			starship.subsystems += ThrusterSubsystem(starship, pos, face, thrusterType)
		}
	}

	private fun detectWeapon(starship: ActivePlayerStarship, block: Block) {
		for (face: BlockFace in CARDINAL_BLOCK_FACES) {
			val multiblock = getWeaponMultiblock(block, face) ?: continue

			val pos = Vec3i(block.location)

			val subsystem = multiblock.createSubsystem(starship, pos, face)

			if (isDuplicate(starship, subsystem)) {
				continue
			}

			starship.subsystems += subsystem
		}
	}

	private fun isDuplicate(starship: ActivePlayerStarship, subsystem: WeaponSubsystem): Boolean {
		return subsystem is DirectionalSubsystem && starship.subsystems
			.filterIsInstance<WeaponSubsystem>()
			.filter { it.pos == subsystem.pos }
			.filterIsInstance<DirectionalSubsystem>()
			.any { it.face == subsystem.face }
	}

	private fun getWeaponMultiblock(block: Block, face: BlockFace): StarshipWeaponMultiblock<*>? {
		return when {
			block.state is Sign -> getSignWeaponMultiblock(block, face)
			else -> getSignlessStarshipWeaponMultiblock(block, face)
		}
	}

	private fun getSignWeaponMultiblock(block: Block, face: BlockFace): StarshipWeaponMultiblock<*>? {
		val sign = block.state as Sign

		// avoid duplicates
		if (sign.getFacing() != face) {
			return null
		}

		val multiblock = Multiblocks[sign]

		if (multiblock !is StarshipWeaponMultiblock<*>) {
			return null
		}

		return multiblock
	}

	private fun getSignlessStarshipWeaponMultiblock(block: Block, face: BlockFace): StarshipWeaponMultiblock<*>? {
		return Multiblocks.all()
			.filterIsInstance<SignlessStarshipWeaponMultiblock<*>>()
			.firstOrNull { it.blockMatchesStructure(block, face) }
	}

	private fun filterSubsystems(starship: ActivePlayerStarship) {
		starship.subsystems.filterIsInstanceTo(starship.shields)
		starship.subsystems.filterIsInstanceTo(starship.weapons)
		starship.subsystems.filterIsInstanceTo(starship.turrets)
		starship.subsystems.filterIsInstanceTo(starship.hyperdrives)
		starship.subsystems.filterIsInstanceTo(starship.navComps)
		starship.subsystems.filterIsInstanceTo(starship.thrusters)
		starship.subsystems.filterIsInstanceTo(starship.magazines)
		starship.subsystems.filterIsInstanceTo(starship.gravityWells)
	}
}
