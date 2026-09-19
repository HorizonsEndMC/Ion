package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.configuration.starship.AutocannonBalancing
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getEnumSettingOrThrow
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsMainMenuGui
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.AutocannonMultiblock
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class AutocannonProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager,
	private val shotIndex: Int,
	private val multiblock: AutocannonMultiblock
) : LaserProjectile<AutocannonBalancing.AutocannonProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {
    override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		if (System.nanoTime() - this.firedAtNanos > shotIndex * TimeUnit.MILLISECONDS.toNanos(balancing.delayMillis.toLong())) {
			this.speed = balancing.speed
		}

        super.moveVisually(oldLocation, newLocation, travel)
    }
	override var speed = balancing.speed
    override fun fire() {
        super.fire()

        this.speed = 1.0
    }

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		toPlayersInRadius(location,512.0) { player ->
			when (player.getEnumSettingOrThrow<SettingsMainMenuGui.Companion.ParticleSettings>(PlayerSettings::autoCannons)) {
				SettingsMainMenuGui.Companion.ParticleSettings.ALL_PARTICLES ->	player.spawnPlayerSpecificParticle(x, y, z, force)
				SettingsMainMenuGui.Companion.ParticleSettings.HALF_PARTICLES -> when(shotIndex) {
					1-> player.spawnPlayerSpecificParticle(x, y, z, force)
					4-> player.spawnPlayerSpecificParticle(x, y, z, force)
					else -> {}
				}
				SettingsMainMenuGui.Companion.ParticleSettings.REDUCED_PARTICLES-> if(shotIndex == 1) player.spawnPlayerSpecificParticle(x, y, z, force)
			}
		}
	}
}
