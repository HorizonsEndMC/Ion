package net.horizonsend.ion

import net.horizonsend.ion.MobSpawning.MobSpawns.valueOf
import org.bukkit.entity.EntityType
import org.bukkit.entity.EntityType.BEE
import org.bukkit.entity.EntityType.BLAZE
import org.bukkit.entity.EntityType.CAVE_SPIDER
import org.bukkit.entity.EntityType.CHICKEN
import org.bukkit.entity.EntityType.COW
import org.bukkit.entity.EntityType.CREEPER
import org.bukkit.entity.EntityType.DROWNED
import org.bukkit.entity.EntityType.ELDER_GUARDIAN
import org.bukkit.entity.EntityType.ENDERMAN
import org.bukkit.entity.EntityType.ENDERMITE
import org.bukkit.entity.EntityType.EVOKER
import org.bukkit.entity.EntityType.GHAST
import org.bukkit.entity.EntityType.GUARDIAN
import org.bukkit.entity.EntityType.HOGLIN
import org.bukkit.entity.EntityType.HUSK
import org.bukkit.entity.EntityType.ILLUSIONER
import org.bukkit.entity.EntityType.MAGMA_CUBE
import org.bukkit.entity.EntityType.PHANTOM
import org.bukkit.entity.EntityType.PIG
import org.bukkit.entity.EntityType.PIGLIN_BRUTE
import org.bukkit.entity.EntityType.PILLAGER
import org.bukkit.entity.EntityType.RAVAGER
import org.bukkit.entity.EntityType.SHEEP
import org.bukkit.entity.EntityType.SHULKER
import org.bukkit.entity.EntityType.SILVERFISH
import org.bukkit.entity.EntityType.SKELETON
import org.bukkit.entity.EntityType.SLIME
import org.bukkit.entity.EntityType.SPIDER
import org.bukkit.entity.EntityType.SQUID
import org.bukkit.entity.EntityType.STRAY
import org.bukkit.entity.EntityType.VEX
import org.bukkit.entity.EntityType.WITCH
import org.bukkit.entity.EntityType.ZOMBIE
import org.bukkit.entity.EntityType.ZOMBIE_VILLAGER
import org.bukkit.entity.EntityType.ZOMBIFIED_PIGLIN
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.PATROL
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.RAID
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.REINFORCEMENTS

class MobSpawning: Listener {
	private val canceledSpawnReasons = enumSetOf(RAID, REINFORCEMENTS, PATROL)

	private val hostileMobs = enumSetOf(BLAZE, CREEPER, DROWNED, ELDER_GUARDIAN, ENDERMITE, EVOKER, GHAST, GUARDIAN, HOGLIN, HUSK, ILLUSIONER, MAGMA_CUBE, PHANTOM, PIGLIN_BRUTE, PILLAGER, RAVAGER, SHULKER, SILVERFISH, SKELETON, SLIME, STRAY, VEX, WITCH, ZOMBIE, ZOMBIE_VILLAGER)

	@EventHandler
	fun onMobSpawn(event: CreatureSpawnEvent) {
		if (canceledSpawnReasons.contains(event.spawnReason)) event.isCancelled = true // Prevent certain types of mob spawns.
		if (event.spawnReason != NATURAL) return // Only interfere with certain types of mob spawns.

		event.isCancelled = true

		val mobSpawn = valueOf(event.location.world.name)

		if (hostileMobs.contains(event.entityType))
			event.location.world.spawnEntity(event.location, mobSpawn.hostileMob)

		else
			mobSpawn.passiveMob?.let { event.location.world.spawnEntity(event.location, it) }
	}

	@Suppress("unused") // Hardcoded settings because I am lazy.
	private enum class MobSpawns(
		val passiveMob: EntityType?,
		val hostileMob: EntityType
	) {
		Space    (null   , ENDERMITE       ),
		Chandra  (null   , ENDERMAN        ),
		Ilius    (SHEEP  , SPIDER          ),
		Damkoth  (null   , CAVE_SPIDER     ),
		Herdoli  (null   , ZOMBIFIED_PIGLIN),
		Rubaciea (null   , STRAY           ),
		Aret     (COW    , HUSK            ),
		Aerach   (PIG    , SKELETON        ),
		Luxiterna(null   , SLIME           ),
		Gahara   (SQUID  , DROWNED         ),
		Isik     (null   , MAGMA_CUBE      ),
		Chimgara (CHICKEN, CREEPER         ),
		Vask     (BEE    , ZOMBIE          ),
		Krio     (null   , STRAY           )
	}
}