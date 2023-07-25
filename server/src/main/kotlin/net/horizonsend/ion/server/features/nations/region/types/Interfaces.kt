package net.horizonsend.ion.server.features.nations.region.types

/** Regions that should be searched every time regions in the world are searched,
 *  and aren't the child of another region, e.g. territories but not their zones */
interface RegionTopLevel

/** Regions that are parents of another kind of region.
 * Does not currently support children of children, RegionCache would have to be updated. */
interface RegionParent {
	val children: MutableSet<Region<*>>
}
