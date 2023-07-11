// package net.starlegacy.feature.machine
//
// import java.util.concurrent.TimeUnit
// import net.horizonsend.ion.server.IonServerComponent
// import net.starlegacy.feature.multiblock.Multiblocks
// import net.starlegacy.feature.multiblock.starshipweapon.turret.TurretMultiblock
// import net.starlegacy.feature.starship.active.ActiveStarships
// import net.starlegacy.feature.starship.control.StarshipControl
// import net.starlegacy.util.PerPlayerCooldown
// import net.horizonsend.ion.server.miscellaneous.Vec3i
// import net.starlegacy.util.isGlass
// import org.bukkit.Location
// import org.bukkit.block.Sign
// import org.bukkit.event.EventPriority
// import org.bukkit.event.block.Action
// import org.bukkit.event.player.PlayerInteractEvent
// import org.bukkit.event.player.PlayerMoveEvent
// import org.bukkit.inventory.EquipmentSlot
//
// object Turrets : IonServerComponent() {
// 	private lateinit var turretMultiblocks: List<TurretMultiblock>
//
// 	override fun onEnable() {
// 		turretMultiblocks = Multiblocks.all().filterIsInstance<TurretMultiblock>()
//
// 		val cooldown = PerPlayerCooldown(1000)
//
// 		subscribe<PlayerInteractEvent>(EventPriority.LOWEST) { event ->
// 			// prevent double call
// 			if (event.hand != EquipmentSlot.HAND) {
// 				return@subscribe
// 			}
// 			val player = event.player
//
//
// 			if (!StarshipControl.isHoldingController(player)) {
// 				return@subscribe
// 			}
// 			val sign = event.clickedBlock?.state as? Sign ?: return@subscribe
// 			val multiblock = Multiblocks[sign] as? TurretMultiblock ?: return@subscribe
// 			val face = multiblock.getFacing(sign)
//
// 			// this is just to insert them into the cooldown to prevent accidentally shooting right when boardingm
// 			// and to prevent leaving right after entering
// 			cooldown.tryExec(player) {
// 				player.teleport(multiblock.getPilotLoc(sign, face))
// 			}
//
// 			event.isCancelled = true
// 		}
//
// 		subscribe<PlayerInteractEvent>(EventPriority.NORMAL) { event ->
// 			// prevent double call
// 			if (event.hand != EquipmentSlot.HAND) {
// 				return@subscribe
// 			}
// 			val player = event.player
// 			val block = player.location.block
// 			if (!block.type.isGlass) {
// 				return@subscribe
// 			}
// 			var sign: Sign? = null
// 			var multiblock: TurretMultiblock? = null
// 			for (turret in turretMultiblocks) {
// 				sign = turret.getSignFromPilot(player) ?: continue
// 				multiblock = turret
// 				break
// 			}
// 			if (sign == null || multiblock == null) {
// 				return@subscribe
// 			}
// 			when (event.action) {
// 				Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
// 					cooldown.tryExec(player) {
// 						player.teleport(sign.location.add(0.5, 0.0, 0.5))
// 					}
// 				}
// 				Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> {
// 					if (!StarshipControl.isHoldingController(player)) {
// 						return@subscribe
// 					}
//
// 					cooldown.tryExec(player, multiblock.cooldownNanos, TimeUnit.NANOSECONDS) {
// 						val oldFace = multiblock.getFacing(sign)
// 						val newFace = player.facing
//
// 						// only fire if it can align properly
// 						if (multiblock.rotate(sign, oldFace, newFace) == newFace) {
// 							val world = player.world
// 							val pos = Vec3i(sign.location.toBlockKey())
// 							val dir = player.location.direction
// 							val starship = ActiveStarships.findByPassenger(player)
// 							multiblock.shoot(world, pos, newFace, dir, starship, player)
// 						}
// 					}
// 				}
// 				else -> return@subscribe
// 			}
// 			event.isCancelled = true
// 		}
//
// 		subscribe<PlayerMoveEvent> { event ->
// 			val player = event.player
//
// 			if (!event.from.block.type.isGlass) {
// 				return@subscribe
// 			}
//
// 			for (turret in turretMultiblocks) {
// 				val sign = turret.getSignFromPilot(player) ?: continue
// 				handleTurretMovement(event, turret, sign)
// 				return@subscribe
// 			}
// 		}
// 	}
//
// 	private fun handleTurretMovement(event: PlayerMoveEvent, turret: TurretMultiblock, sign: Sign): Location {
// 		val newTo = event.to
//
// 		if (event.from.distanceSquared(event.to) > 0) {
// 			newTo.x = newTo.blockX + 0.5
// 			newTo.y = newTo.blockY + 0.0
// 			newTo.z = newTo.blockZ + 0.5
// 		}
//
// 		val oldFace = turret.getFacing(sign)
// 		val newFace = event.player.facing
// 		if (oldFace != newFace) {
// 			val finalFace = turret.rotate(sign, oldFace, newFace)
// 			if (finalFace != oldFace) {
// 				val pilotLocation = turret.getPilotLoc(sign, finalFace)
// 				newTo.x = pilotLocation.x
// 				newTo.y = pilotLocation.y
// 				newTo.z = pilotLocation.z
// 			}
// 		}
//
// 		return newTo
// 	}
// }
