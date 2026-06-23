package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import org.litote.kmongo.setValue

/**
 * Migration utility to convert legacy SLTextStyle color names to RGB format
 */
object RoleColorMigration {

	/**
	 * Migrate all nation roles from legacy color format to RGB format
	 */
	fun migrateNationRoles() {
		var count = 0
		for (role in NationRole.all()) {
			val oldColor = role.color

			// Skip if already in RGB format
			if (oldColor.contains(",")) continue

			// Convert to RGB
			val newColor = migrateLegacyColor(oldColor)
			if (newColor != oldColor) {
				NationRole.updateById(role._id, setValue(NationRole::color, newColor))
				count++
			}
		}
		println("Migrated $count nation roles from legacy color format to RGB")
	}

	/**
	 * Migrate all settlement roles from legacy color format to RGB format
	 */
	fun migrateSettlementRoles() {
		var count = 0
		for (role in SettlementRole.all()) {
			val oldColor = role.color

			// Skip if already in RGB format
			if (oldColor.contains(",")) continue

			// Convert to RGB
			val newColor = migrateLegacyColor(oldColor)
			if (newColor != oldColor) {
				SettlementRole.updateById(role._id, setValue(SettlementRole::color, newColor))
				count++
			}
		}
		println("Migrated $count settlement roles from legacy color format to RGB")
	}

	/**
	 * Migrate all roles (both nation and settlement)
	 */
	fun migrateAll() {
		migrateNationRoles()
		migrateSettlementRoles()
	}
}

