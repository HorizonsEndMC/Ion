package net.starlegacy.listener.nations

import com.flowpowered.math.vector.Vector3i
import com.griefdefender.api.GriefDefender
import com.griefdefender.api.event.ChangeClaimEvent
import com.griefdefender.api.event.ClaimEvent
import com.griefdefender.api.event.CreateClaimEvent
import net.kyori.event.Cancellable
import net.kyori.event.method.annotation.Subscribe
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.database.schema.nations.NPCTerritoryOwner
import net.starlegacy.feature.nations.NATIONS_BALANCE
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionCapturableStation
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.Tasks
import net.starlegacy.util.d
import net.starlegacy.util.msg
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.Ellipse2D
import kotlin.math.max
import kotlin.math.min

object GriefDefenderListener : SLEventListener() {
    override fun supportsVanilla(): Boolean {
        return true
    }

    override fun onRegister() {
        Tasks.syncDelay(1) {
            if (!Bukkit.getPluginManager().isPluginEnabled("GriefDefender")) {
                return@syncDelay
            }
            registerWithGriefDefenderEventManager()
        }
    }

    private fun registerWithGriefDefenderEventManager() {
        GriefDefender.getEventManager().register(this)
    }

    @Subscribe
    fun onClaimCreate(event: CreateClaimEvent) {
        val rectangle = getRectangle(event.claim.lesserBoundaryCorner, event.claim.greaterBoundaryCorner)
        handleEvent(event, rectangle)
    }

    @Subscribe
    fun onClaimResize(event: ChangeClaimEvent.Resize) {
        val rectangle = getRectangle(event.startCorner, event.endCorner)
        handleEvent(event, rectangle)
    }

    private fun getRectangle(startCorner: Vector3i, endCorner: Vector3i): Rectangle {
        val minX = min(startCorner.x, endCorner.x)
        val minZ = min(startCorner.z, endCorner.z)
        val maxX = max(startCorner.x, endCorner.x)
        val maxZ = max(startCorner.z, endCorner.z)
        val rectangle = Rectangle()
        rectangle.setFrameFromDiagonal(Point(minX, minZ), Point(maxX, maxZ))
        return rectangle
    }

    private fun handleEvent(event: ClaimEvent, rectangle: Rectangle) {
        check(event is Cancellable)

        val claim = event.claim
        val player: Player = checkNotNull(Bukkit.getPlayer(claim.ownerUniqueId))
        val world: World = checkNotNull(Bukkit.getWorld(claim.worldUniqueId))

        checkTerritories(world, rectangle, event, player)

        preventClaimInStation(world, rectangle, event)
    }

    private fun checkTerritories(world: World, rectangle: Rectangle, event: Cancellable, player: Player) {
        for (territory in Regions.getAllOf<RegionTerritory>()) {
            if (territory.world != world.name || !territory.polygon.intersects(rectangle)) {
                continue
            }

            val npcOwner = territory.npcOwner
            if (npcOwner != null) {
                event.cancelled(true)

                Tasks.async {
                    val name = NPCTerritoryOwner.getName(npcOwner)
                    player msg "&cYou can't claim in $name"
                }
                return
            }

            val settlementId = territory.settlement

            if (settlementId != null) {
                val settlement = SettlementCache[settlementId]

                val name = settlement.name
                player msg "&cThis territory is claimed by the settlement $name, so you can't claim here."

                event.cancelled(true)
                return
            }

            val nationId = territory.nation

            if (nationId != null) {
                val nation = NationCache[nationId]
                val nationName: String? = nation.name
                player msg "&cThis territory is claimed by the nation $nationName so you cannot create a claim here."
                event.cancelled(true)
                return
            }
        }
        return
    }

    private fun preventClaimInStation(world: World, rectangle: Rectangle, event: Cancellable) {
        for (station in Regions.getAllOf<RegionCapturableStation>().filter { it.world == world.name }) {
            val x = station.x
            val z = station.z
            val radius = NATIONS_BALANCE.capturableStation.radius

            val shape = Ellipse2D.Double(x.d() - radius, z.d() - radius, radius * 2.0, radius * 2.0)

            if (shape.intersects(rectangle.x.d(), rectangle.y.d(), rectangle.width.d(), rectangle.height.d())) {
                event.cancelled(true)
            }
        }
    }
}
