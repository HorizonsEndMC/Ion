package net.starlegacy.feature.gear.blaster

import net.md_5.bungee.api.ChatColor
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.misc.getPower
import net.starlegacy.feature.misc.removePower
import net.starlegacy.util.updateMeta
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.util.Vector
import java.time.Instant
import java.util.HashMap
import java.util.Random
import java.util.UUID

object Blasters {
    fun getBlaster(item: ItemStack): CustomItems.BlasterItem? = CustomItems[item] as? CustomItems.BlasterItem

    private val typeMap = mutableMapOf<String, BlasterType>()
    fun getBlasterType(item: CustomItems.BlasterItem): BlasterType =
        typeMap.getOrPut(item.id) { BlasterType.values().single { it.item === item } }

    private val randomColorCache = mutableMapOf<UUID, Color>()
    fun getRandomColor(uuid: UUID): Color = randomColorCache.getOrElse(uuid) {
        Random(uuid.leastSignificantBits).let { r -> Color.fromRGB(r.nextInt(255), r.nextInt(255), r.nextInt(255)) }
    }

    fun getColor(player: Player): Color {
        if (player.world.name.toLowerCase().contains("arena")) {
            return getRandomColor(player.uniqueId)
        }

        val nation = PlayerCache[player].nation ?: return Color.BLUE
        return Color.fromRGB(NationCache[nation].color)
    }

    private val lastFired = HashMap<UUID, Long>()

    fun fireBlaster(entity: LivingEntity, blaster: ItemStack, type: BlasterType) {
        val uniqueId = entity.uniqueId
        if (Instant.now().toEpochMilli() - (lastFired[uniqueId] ?: 0) < type.cooldown) {
            return
        }
        if (entity is Player) {
            val powerUsage = type.power

            if (getPower(blaster) < powerUsage) {
                entity.sendMessage(ChatColor.RED.toString() + "Out of power!")
                return
            }

            if (!entity.getWorld().name.toLowerCase().contains("arena")) {
                removePower(blaster, powerUsage)
            }

            entity.setCooldown(blaster.type, (type.cooldown / 1000.0f * 20.0f).toInt())

            blaster.updateMeta {
                (it as Damageable).damage++
            }
        }
        lastFired[uniqueId] = Instant.now().toEpochMilli()
        BlasterProjectile.scheduler.submit {
            val location = entity.eyeLocation
            val lore = blaster.itemMeta.lore

            val color = if (lore != null && lore.size > 1) {
                DyeColor.valueOf(lore[1]).color
            } else when (entity) {
                is Player -> getColor(entity)
                else -> getRandomColor(entity.uniqueId)
            }

            val dmg = type.damage
            val range = type.range
            val thickness = type.thickness
            val speed = type.speed
            val expPow = type.explosionPower
            val sound = type.sound
            val pitchB = type.pitchBase
            val pitchR = type.pitchRange
            val dir = if (entity is Player) entity.location.direction
            else entity.location.direction
                .add(Vector((Math.random() - 0.5) / 10, (Math.random() - 0.5) / 10, (Math.random() - 0.5) / 10))
                .normalize()
            BlasterProjectile(entity, location, color, dmg, range, thickness, speed, expPow, sound, pitchB, pitchR, dir)
        }
    }
}
