package net.starlegacy.database.schema.nations

import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.objId
import net.starlegacy.database.trx
import net.starlegacy.util.SLTextStyle
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.setOnInsert
import org.litote.kmongo.upsert

/**
 * Referenced on: None
 */
data class NationRelation(
	override val _id: Oid<NationRelation> = objId(),
	val nation: Oid<Nation>,
	val other: Oid<Nation>,
	var wish: Level,
	var actual: Level
) : DbObject {
	companion object : OidDbObjectCompanion<NationRelation>(NationRelation::class, setup = {
		ensureUniqueIndex(NationRelation::nation, NationRelation::other)
		ensureIndex(NationRelation::nation)
		ensureIndex(NationRelation::other)
	}) {
		fun getRelationWish(nation: Oid<Nation>, other: Oid<Nation>): Level = when (nation) {
			other -> Level.NATION
			else -> findOneProp(
				and(NationRelation::nation eq nation, NationRelation::other eq other), NationRelation::wish
			) ?: Level.NONE
		}

		fun changeRelationWish(nation: Oid<Nation>, other: Oid<Nation>, wish: Level): Level = trx { sess ->
			fun set(nation: Oid<Nation>, other: Oid<Nation>, wish: Level, actual: Level) {
				col.updateOne(
					sess,
					and(NationRelation::nation eq nation, NationRelation::other eq other),
					combine(
						setOnInsert(NationRelation::nation, nation),
						setOnInsert(NationRelation::other, other),
						org.litote.kmongo.setValue(NationRelation::wish, wish),
						org.litote.kmongo.setValue(NationRelation::actual, actual)
					),
					upsert()
				)
			}

			val otherWish: Level = getRelationWish(other, nation)
			val actual: Level = wish.lowest(otherWish)
			set(nation, other, wish, actual)
			set(other, nation, otherWish, actual)

			return@trx actual
		}
	}

	/** Relation wishes nations can set to other nations */
	enum class Level(val textStyle: SLTextStyle) {
		ENEMY(SLTextStyle.RED),
		NONE(SLTextStyle.GRAY),
		NEUTRAL(SLTextStyle.LIGHT_PURPLE),
		ALLY(SLTextStyle.DARK_PURPLE),
		NATION(SLTextStyle.GREEN);

		fun lowest(other: Level): Level = when {
			other.ordinal > this.ordinal -> this
			else -> other
		}

		val coloredName = "$textStyle$name"
	}
}
