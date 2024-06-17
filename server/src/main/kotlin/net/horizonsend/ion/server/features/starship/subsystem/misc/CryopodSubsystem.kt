package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.server.features.multiblock.type.misc.CryoPodMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class CryopodSubsystem(starship: ActiveStarship, sign: Sign, multiblock: CryoPodMultiblock, var pod: Cryopod) :
	AbstractMultiblockSubsystem<CryoPodMultiblock>(starship, sign, multiblock)
