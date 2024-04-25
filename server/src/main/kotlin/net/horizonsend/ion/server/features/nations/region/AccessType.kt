package net.horizonsend.ion.server.features.nations.region

enum class AccessType {
	BLOCK_EDIT,       /* Break/place blocks */
	INVENTORY_ACCESS, /* Chest access */
	ANIMAL_DAMAGE,    /* Hurt passive mobs */
	ENTITY_EDIT,      /* Item frames, shear sheep, etc */
	SWITCH_INTERACT,  /* Levers, buttons, etc */
	DOORS             /* Doors, trapdoors */
}
