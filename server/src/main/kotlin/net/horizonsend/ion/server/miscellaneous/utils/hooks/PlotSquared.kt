package net.horizonsend.ion.server.miscellaneous.utils.hooks

import com.plotsquared.bukkit.util.BukkitWorld
import com.plotsquared.core.PlotAPI
import com.plotsquared.core.PlotSquared
import com.plotsquared.core.plot.PlotArea
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

val plotAPI by lazy { runCatching { PlotAPI() }.getOrNull() }

fun Location.toPlotSquaredLocation(): com.plotsquared.core.location.Location {
	return com.plotsquared.core.location.Location.at(BukkitWorld.of(world), BlockVector3.at(x, y, z), yaw, pitch)
}

// Plot area is
fun getPlotArea(location: Location): PlotArea? {
	if (!Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) return null
	return PlotSquared.platform().plotAreaManager().getPlotArea(location.toPlotSquaredLocation())
}

fun isPlotDenied(player: Player, location: Location): Boolean {
	if (!Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) return false

	val plotManager = getPlotArea(location)
	if (plotManager != null) {
		val plot = plotManager.getPlot(location.toPlotSquaredLocation())

		if (plot != null) {
			if (!plot.hasOwner()) return !player.hasPermission("plots.admin.build.unowned")
			if (player.hasPermission("plots.admin.build.other")) return false

			if (plot.owner == player.uniqueId) return false
			if (plot.trusted.contains(player.uniqueId)) return false
			if (plot.members.contains(player.uniqueId)) return false

			return true
		}
	}

	if (location.toPlotSquaredLocation().isPlotRoad) {
		return !player.hasPermission("plots.admin.build.road")
	}

	return false
}
