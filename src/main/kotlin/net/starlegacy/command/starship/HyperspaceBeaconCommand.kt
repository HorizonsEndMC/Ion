package net.starlegacy.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import java.util.Locale
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.space.CachedStar
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.feature.starship.hyperspace.HyperspaceBeacons
import net.starlegacy.util.Vec3i
import net.starlegacy.util.loadSchematic
import net.starlegacy.util.msg
import net.starlegacy.util.paste
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender

@CommandAlias("hyperspacebeacon")
@CommandPermission("starships.createbeacon")
object HyperspaceBeaconCommand : SLCommand() {
	@Subcommand("regen")
	fun regen(sender: CommandSender) {
		for (beacon in HyperspaceBeacons.getBeacons()) {
			val world = Bukkit.getWorld(beacon.world) ?: fail { "Couldn't load world ${beacon.world}" }

			val schematic = loadSchematic(null, beacon.schem) ?: fail { "Couldn't load schematic for beacon $beacon" }

			val origin = Vec3i(beacon.x, 128, beacon.z)

			createRegion(world, beacon.id, schematic, origin)

			schematic.paste(world, origin.x, origin.y, origin.z, false)
		}
	}

	@Subcommand("create")
	fun create(p: CommandSender, world: World, from: CachedStar, to: CachedStar, x: Int, z: Int, schemName: String) {
		failIf(!SpaceWorlds.contains(world)) {
			"${world.name} is not a space world."
		}

		val schematic = loadSchematic(null, schemName) ?: fail { "Could not load schematic $schemName" }

		val id = (from.name + "_" + to.name).lowercase(Locale.getDefault())
		val pairedId = (to.name + "_" + from.name).lowercase(Locale.getDefault())

		failIf(HyperspaceBeacons[id] != null) {
			"That hyperspace beacon already exists. Delete it to re-make it."
		}

		val originVec = Vec3i(x, 128, z)

		schematic.paste(world, originVec.x, originVec.y, originVec.z, false)

		createRegion(world, id, schematic, originVec)

		HyperspaceBeacons.add(id, world.name, x, z, pairedId, schemName)
		p msg "&aSuccessfully created hyperspace beacon at $x, $z in ${world.name} " +
			"going from ${from.name} to ${to.name}."
		if (HyperspaceBeacons[pairedId] == null) {
			p msg "&eWarning: Hyperspace beacon going from ${to.name} to ${from.name} not found. One must be created for this one to work."
		} else {
			p msg "&6Successfully linked beacons."
		}
		HyperspaceBeacons.reloadDynmap()
	}

	private fun createRegion(world: World, id: String, schem: Clipboard, origin: Vec3i): ProtectedCuboidRegion {
		val regionManager = WorldGuard.getInstance().platform.regionContainer[BukkitAdapter.adapt(world)]
			?: fail { "Couldn't get region data for world!" }
		regionManager.removeRegion(id)
		val min = schem.minimumPoint.subtract(schem.origin).add(origin.x, origin.y, origin.z)
		val max = schem.maximumPoint.subtract(schem.origin).add(origin.x, origin.y, origin.z)
		val region = ProtectedCuboidRegion(id, min, max)
		region.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY)
		region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY)
		region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY)
		region.setFlag(Flags.PVP, StateFlag.State.ALLOW)
		regionManager.addRegion(region)
		return region
	}
}
