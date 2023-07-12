package net.starlegacy

import net.horizonsend.ion.server.legacy.listeners.BowHitListener
import net.horizonsend.ion.server.legacy.listeners.PlayerJumpListener
import net.horizonsend.ion.server.legacy.listeners.PlayerToggleSneakListener
import net.starlegacy.listener.gear.DetonatorListener
import net.starlegacy.listener.gear.DoubleJumpListener
import net.starlegacy.listener.gear.PowerArmorListener
import net.starlegacy.listener.gear.PowerToolListener
import net.starlegacy.listener.gear.SwordListener
import net.starlegacy.listener.misc.BlockListener
import net.starlegacy.listener.misc.EntityListener
import net.starlegacy.listener.misc.FurnaceListener
import net.starlegacy.listener.misc.InteractListener
import net.starlegacy.listener.misc.InventoryListener
import net.starlegacy.listener.misc.JoinLeaveListener
import net.starlegacy.listener.misc.ProtectionListener
import net.starlegacy.listener.nations.FriendlyFireListener
import net.starlegacy.listener.nations.MovementListener

val listeners = listOf(
	JoinLeaveListener,
	MovementListener,
	FriendlyFireListener,
	ProtectionListener,

	BlockListener,
	EntityListener,
	FurnaceListener,
	InteractListener,
	InventoryListener,
	BowHitListener,
	PlayerToggleSneakListener,
	PlayerJumpListener,

	DetonatorListener,
	DoubleJumpListener,
	PowerArmorListener,
	PowerToolListener,
	SwordListener
)
