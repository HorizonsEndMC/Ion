package net.wesjd.anvilgui.version.special;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.AnvilMenu;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;

public class AnvilContainer1_19_2_R1 extends AnvilMenu {
	public AnvilContainer1_19_2_R1(org.bukkit.entity.Player player, int containerId, String guiTitle) {
		super(
			containerId,
			((CraftPlayer) player).getHandle().getInventory(),
			ContainerLevelAccess.create(((CraftWorld) player.getWorld()).getHandle(), new BlockPos(0, 0, 0))
		);
		this.checkReachable = false;
		setTitle(Component.nullToEmpty(guiTitle));
	}

	@Override public void createResult() {
		super.createResult();
		this.cost.set(0);
	}

	@Override public void removed(net.minecraft.world.entity.player.Player player) {}

	@Override protected void clearContainer(net.minecraft.world.entity.player.Player player, Container container) {}

	public int getContainerId() {
		return this.containerId;
	}
}