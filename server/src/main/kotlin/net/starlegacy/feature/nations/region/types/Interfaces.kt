package net.starlegacy.feature.nations.region.types

import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.nations.Nation
import java.awt.Polygon
import kotlin.math.roundToInt

/** Regions that should be searched every time regions in the world are searched,
 *  and aren't the child of another region, e.g. territories but not their zones */
interface RegionTopLevel

/** Regions that are parents of another kind of region.
 * Does not currently support children of children, RegionCache would have to be updated. */
interface RegionParent {
	val children: MutableSet<Region<*>>
}

interface TerritoryRegion {
	var name: String
	val nation: Oid<Nation>?
	var polygon: Polygon

	fun centerX() = polygon.xpoints.average().roundToInt()
	fun centerZ() = polygon.ypoints.average().roundToInt()
}
