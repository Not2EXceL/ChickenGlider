package com.not2excel.chickenglider;

import java.io.File;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ChickenGlider extends JavaPlugin implements Listener
{
	private double glideSpeed = 0;
	private double glideDrop = 0;
	private String prefix = ChatColor.LIGHT_PURPLE + "[ChickenGlider] ";

	@Override
	public void onEnable()
	{
		this.getServer().getPluginManager().registerEvents(this, this);
		File config = new File(getDataFolder(), "config.yml");
		if (!config.exists())
		{
			saveDefaultConfig();
		}
		reloadConfig();
		glideSpeed = this.getConfig().getDouble("glide_speed");
		glideDrop = this.getConfig().getDouble("glide_drop");
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerInteract(PlayerInteractEntityEvent event)
	{
		if (!event.getPlayer().hasPermission("chickenglider.glide"))
		{
			return;
		}
		if (event.getRightClicked() instanceof Chicken)
		{
			if (event.getPlayer().getPassenger() == null)
			{
				if(event.getPlayer().getItemInHand().getType() == Material.AIR)
				{
					event.getPlayer().sendMessage(prefix + ChatColor.GREEN + "Equipping ChickenGlider.");
					event.getPlayer().setPassenger(event.getRightClicked());
				}
			}
			else if(event.getRightClicked().equals(event.getPlayer().getPassenger()))
			{
				event.getPlayer().sendMessage(prefix + ChatColor.RED + "Unequpping ChickenGlider.");
				event.getPlayer().eject();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if (!player.hasPermission("chickenglider.glide"))
		{
			return;
		}
		if(player.isFlying())
		{
			return;
		}
		if (player.getPassenger() != null)
		{
			if (player.getLocation().getBlock().getRelative(BlockFace.DOWN)
					.getType() == Material.AIR && !hovering(player.getLocation()))
			{
				Vector dir = player.getLocation().getDirection()
						.multiply(glideSpeed);
				player.setVelocity(new Vector(dir.getX(), player.getVelocity()
						.getY() * glideDrop, dir.getZ()));
			}
		}
	}
	
	private boolean hovering(Location loc)
	{
		Block lower = loc.getBlock().getRelative(BlockFace.DOWN);
		if(lower.getRelative(BlockFace.NORTH).getType() != Material.AIR || 
				lower.getRelative(BlockFace.SOUTH).getType() != Material.AIR || 
				lower.getRelative(BlockFace.EAST).getType() != Material.AIR || 
				lower.getRelative(BlockFace.WEST).getType() != Material.AIR ||
				lower.getRelative(BlockFace.DOWN).getType() != Material.AIR)
		{
			return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSpawn(ItemSpawnEvent event)
	{
		ItemStack itemStack = event.getEntity().getItemStack();
		if (itemStack.getType() != Material.EGG)
		{
			return;
		}
		List<Entity> nearby = event.getEntity().getNearbyEntities(0.01D, 0.3D,
				0.01D);

		for (int i = 0; i < nearby.size(); i++)
		{
			if (nearby.get(i) instanceof Player)
			{
				Player player = (Player) nearby.get(i);
				if (player.getPassenger() != null
						&& player.getPassenger() instanceof Chicken)
				{
					event.setCancelled(true);
					return;
				}
			}

		}
	}
}
