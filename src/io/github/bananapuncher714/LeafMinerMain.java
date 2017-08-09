package io.github.bananapuncher714;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class LeafMinerMain extends JavaPlugin implements Listener {
	private final double version = 3.2;
	private LeafLocation location;
	private Location loc, tempLoc, origin;
	private Material block;
	private Random rand = new Random();
	
	private TreeSet< LeafLocation > l, ls;
	private HashSet< Material > fortunableBlocks = new HashSet< Material >();
	private HashMap< UUID, TreeSet< LeafLocation > > activePlayerLocations = new HashMap< UUID, TreeSet< LeafLocation > >();
	private HashMap< UUID, Boolean > activePlayers = new HashMap< UUID, Boolean >() ;
	
	private int radius = 6;
	private int maxAmount = 40;
	private int maxLimit = 600;
	private int hungerMax = 20;
	private int drop = 0;
	
	@Override
	public void onEnable () {
		saveDefaultConfig();
		reloadLeafConfig();
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				if ( !activePlayerLocations.isEmpty() ) {
					for ( Iterator<UUID> i = activePlayerLocations.keySet().iterator(); i.hasNext(); ){
						UUID ud = i.next();
						for ( Player p: getServer().getOnlinePlayers() ) {
							if ( p.getUniqueId().equals( ud ) ) {
								destroyLeaves( p );
								break;
							}
						}
					}
				}
				// TODO Auto-generated method stub
			}
		}, 1, 1 );
		Bukkit.getPluginManager().registerEvents(this, this);
		
		fortunableBlocks.add( Material.LAPIS_ORE );
		fortunableBlocks.add( Material.REDSTONE_ORE );
		fortunableBlocks.add( Material.DIAMOND_ORE );
		fortunableBlocks.add( Material.EMERALD_ORE );
		fortunableBlocks.add( Material.COAL_ORE );
		fortunableBlocks.add( Material.CARROT );
		fortunableBlocks.add( Material.POTATO );
		fortunableBlocks.add( Material.LEAVES );
		fortunableBlocks.add( Material.LONG_GRASS );
		fortunableBlocks.add( Material.QUARTZ_ORE );
		fortunableBlocks.add( Material.MELON );
	}
	
	@Override
	public void onDisable() {
		saveLeafConfig();
		activePlayerLocations.clear();
		activePlayers.clear();
	}
	
	public void reloadLeafConfig() {
		try {
			maxAmount = getConfig().getInt( "speed" );
			maxLimit = getConfig().getInt( "max-limit" );
			hungerMax = getConfig().getInt( "hunger-rate" );
			radius = getConfig().getInt( "radius" );
			drop = getConfig().getInt( "drop-mode" );
		} catch ( Exception e ) {
			getLogger().info( ChatColor.RED + "There has been a problem with the config. Assuming default values" + ChatColor.RESET );
			radius = 6;
			maxLimit = 600;
			maxAmount = 40;
			hungerMax = 20;
			drop = 0;
		}
	}
	
	public void saveLeafConfig() {
		FileConfiguration config = getConfig();
		config.set( "speed", maxAmount );
		config.set( "max-limit", maxLimit );
		config.set( "hunger-rate", hungerMax );
		config.set( "radius", radius );
		config.set( "drop-mode", drop );
		saveConfig();
	}
		
	@SuppressWarnings("deprecation")
	@EventHandler( priority = EventPriority.HIGH )
	public void onBlockBreakEvent ( BlockBreakEvent e ) {
		if ( e.isCancelled() ) return;
		if ( e.getPlayer().hasPermission( "leafminer.mine" ) && e.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR ) {
			UUID playeruuid = e.getPlayer().getUniqueId();
			if ( activePlayers.containsKey( playeruuid ) && activePlayers.get( playeruuid ) ) {
				if ( activePlayerLocations.containsKey( playeruuid ) ) {
					activePlayerLocations.get( playeruuid ).add( new LeafLocation( e.getBlock().getLocation(), e.getPlayer().getInventory().getItemInMainHand(), e.getBlock().getLocation(), e.getBlock().getType(), e.getBlock().getData(), true ) );
				} else {
					ls = new TreeSet< LeafLocation >();
					ls.add( new LeafLocation( e.getBlock().getLocation(), e.getPlayer().getInventory().getItemInMainHand(), e.getBlock().getLocation(), e.getBlock().getType(), e.getBlock().getData(), true ) );
					activePlayerLocations.put( playeruuid, ls );
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerToggleSneakEvent( PlayerToggleSneakEvent e ) {
		if ( activePlayers.containsKey( e.getPlayer().getUniqueId() ) && e.getPlayer().hasPermission( "leafminer.mine" ) ) {
			if ( e.getPlayer().isSneaking() ) { 
				activePlayers.put( e.getPlayer().getUniqueId(), false );
			} else {
				activePlayers.put( e.getPlayer().getUniqueId(), true );
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuitEvent ( PlayerQuitEvent e ) {
		activePlayers.remove( e.getPlayer().getUniqueId() );
		activePlayerLocations.remove( e.getPlayer().getUniqueId() );
	}
	
	@Override
	public boolean onCommand( CommandSender s, Command c, String l, String[] args ) {
		if ( c.getName().equalsIgnoreCase( "leafminer" ) ) {
			if ( args.length == 0 ) {
				if ( checkSender( s, "leafminer.mine" ) ) {
					if ( s instanceof Player ) {
						Player p = ( Player ) s;
						UUID pud = p.getUniqueId();
						if ( activePlayers.containsKey( pud ) ) {
							activePlayers.remove( pud );
							activePlayerLocations.remove( pud );
							s.sendMessage( ChatColor.GREEN + "LeafMiner has now been disabled!" + ChatColor.RESET );
							return true;
						} else {
							activePlayers.put( pud, false );
							s.sendMessage( ChatColor.GREEN + "LeafMiner has now been enabled!" + ChatColor.RESET );
							s.sendMessage( ChatColor.GREEN + "Sneak to activate LeafMiner" + ChatColor.RESET );
							return true;
						}
					} else {
						s.sendMessage( ChatColor.RED + "You do not have permission to activate/deactivate LeafMiner!" + ChatColor.RESET );
						return false;
					}
				} else {
					s.sendMessage( ChatColor.RED + "This command cannot be run from the console!" + ChatColor.RESET );
					return false;
				}
			} else if ( args.length == 1 ) {
				if ( args[ 0 ].equalsIgnoreCase( "help" ) && checkSender( s, "leafminer.help" ) ) {
					showHelp( s );
					return true;
				} else if ( args[ 0 ].equalsIgnoreCase( "version" ) && checkSender( s, "leafminer.version" ) ) {
					s.sendMessage( "Version " + Double.toString( version ) );
					return true;
				} else if ( args[ 0 ].equalsIgnoreCase( "reload" ) && checkSender( s, "leafminer.reload" ) ) {
					reloadLeafConfig();
					s.sendMessage( "Config reloaded!" );
					return true;
				} else if ( args[ 0 ].equalsIgnoreCase( "clear" ) && checkSender( s, "leafminer.clear" ) ) {
					if ( s instanceof Player ) {
						activePlayerLocations.remove( ( ( Entity ) s ).getUniqueId() );
						s.sendMessage( ChatColor.BOLD + ChatColor.GREEN.toString() + "Your block list has been cleared!" + ChatColor.RESET );
						return true;
					} else {
						s.sendMessage( ChatColor.RED + "This message can only be sent by a player!" + ChatColor.RESET );
						return false;
					}
				} else {
					s.sendMessage( ChatColor.BOLD + ChatColor.RED.toString() + args[ 0 ] + ChatColor.RESET + ChatColor.RED +" is not a valid option! Valid options are: " + ChatColor.BOLD + ChatColor.RED.toString() + "help version reload clear set" );
					return false;
				}
			} else 
				try {
					if ( args.length == 3 ) {
						if ( args[ 1 ].equalsIgnoreCase( "mode" ) && checkSender( s, "leafminer.set.mode" ) ) {
							drop = Integer.parseInt( args[ 2 ] );
							s.sendMessage( ChatColor.BOLD + ChatColor.AQUA.toString() + Integer.toString( drop ) + ChatColor.RESET + ChatColor.GREEN + " has been set as the mode!" + ChatColor.RESET );
							saveLeafConfig();
							return true;
						} else if ( args[ 1 ].equalsIgnoreCase( "radius" ) && checkSender( s, "leafminer.set.radius" ) ) {
							radius = Integer.parseInt( args[ 2 ] );
							s.sendMessage( ChatColor.BOLD + ChatColor.AQUA.toString() + Integer.toString( radius ) + ChatColor.RESET + ChatColor.GREEN + " has been set as the radius!" + ChatColor.RESET );
							saveLeafConfig();
							return true;
						} else if ( args[ 1 ].equalsIgnoreCase( "speed" ) && checkSender( s, "leafminer.set.speed" ) ) {
							maxAmount = Integer.parseInt( args[ 2 ] );
							s.sendMessage( ChatColor.BOLD + ChatColor.AQUA.toString() + Integer.toString( maxAmount ) + ChatColor.RESET + ChatColor.GREEN + " has been set as the speed!" + ChatColor.RESET );
							saveLeafConfig();
							return true;
						} else if ( args[ 1 ].equalsIgnoreCase( "hunger" ) && checkSender( s, "leafminer.set.hunger" ) ) {
							hungerMax = Integer.parseInt( args[ 2 ] );
							s.sendMessage( ChatColor.BOLD + ChatColor.AQUA.toString() + Integer.toString( hungerMax ) + ChatColor.RESET + ChatColor.GREEN + " has been set as the hunger-rate!" + ChatColor.RESET );
							saveLeafConfig();
							return true;
						} else  if ( args[ 1 ].equalsIgnoreCase( "limit" ) && checkSender( s, "leafminer.set.limit" ) ) {
							maxLimit = Integer.parseInt( args[ 2 ] );
							s.sendMessage( ChatColor.BOLD + ChatColor.AQUA.toString() + Integer.toString( maxLimit ) + ChatColor.RESET + ChatColor.GREEN + " has been set as the limit!" + ChatColor.RESET );
							saveLeafConfig();
							return true;
						} else {
							s.sendMessage( ChatColor.RED + "That is not a valid option! Valid options are: " + ChatColor.BOLD + ChatColor.RED.toString() + "speed limit radius mode hunger" );
							return false;
						}
					} else {
						s.sendMessage( ChatColor.RED + "Invalid Command!" + ChatColor.RESET );
						return false;
					}
				} catch ( Exception E ) {
					s.sendMessage( ChatColor.RED + ChatColor.BOLD.toString() + "Invalid Argument!" + ChatColor.RESET );
					reloadLeafConfig();
					return false;
				}
		} else {
			return false;
		}
	}
	
	private void showHelp( CommandSender s ) {
		s.sendMessage( ChatColor.BLUE.toString() + ChatColor.BOLD + "LeafMiner Help Page:" + ChatColor.RESET );
		s.sendMessage( ChatColor.GREEN.toString() + ChatColor.BOLD + "/leafminer " + ChatColor.AQUA + "- Enables/Disables LeafMiner" + ChatColor.RESET);
		s.sendMessage( ChatColor.GREEN.toString() + ChatColor.BOLD + "/leafminer help " + ChatColor.AQUA + "- Display this help page" + ChatColor.RESET);
		s.sendMessage( ChatColor.GREEN.toString() + ChatColor.BOLD + "/leafminer version " + ChatColor.AQUA + "- Tells you the version" + ChatColor.RESET);
		s.sendMessage( ChatColor.GREEN.toString() + ChatColor.BOLD + "/leafminer reload " + ChatColor.AQUA + "- Reloads the configuration file" + ChatColor.RESET);
		s.sendMessage( ChatColor.GREEN.toString() + ChatColor.BOLD + "/leafminer clear " + ChatColor.AQUA + "- Clears the LeafMiner blocks to be destroyed" + ChatColor.RESET);
		s.sendMessage( ChatColor.GREEN.toString() + ChatColor.BOLD + "/leafminer set mode < integer > " + ChatColor.AQUA + "- Sets the mode of how the items drop" + ChatColor.RESET);
		s.sendMessage( ChatColor.GREEN.toString() + ChatColor.BOLD + "/leafminer set hunger < integer > " + ChatColor.AQUA + "- Sets the amont of blocks that must be broken before you lose 1 point of hunger" + ChatColor.RESET);
		s.sendMessage( ChatColor.GREEN.toString() + ChatColor.BOLD + "/leafminer set radius < integer > " + ChatColor.AQUA + "- Sets the maximum radius of blocks to break" + ChatColor.RESET);
		s.sendMessage( ChatColor.GREEN.toString() + ChatColor.BOLD + "/leafminer set speed < integer > " + ChatColor.AQUA + "- Sets the maximum blocks to destroy per tick" + ChatColor.RESET);
		s.sendMessage( ChatColor.GREEN.toString() + ChatColor.BOLD + "/leafminer set limit < integer > " + ChatColor.AQUA + "- Sets the maximum amount of blocks to be destroyed" + ChatColor.RESET);
	}

	public boolean checkSender( CommandSender s, String p ) {
		return ( !( s instanceof Player ) || s.hasPermission( p ) );
	}
	
	public void spawnLeafItem( ItemStack i, Location l1, Location l2, Player p) {
		if ( drop == 1 ) {
			l2.getWorld().dropItem( l2, i );
		} else if ( drop == 2 ) {
			for ( Iterator<ItemStack> it = p.getInventory().addItem( i ).values().iterator(); it.hasNext(); ) {
				p.getWorld().dropItem( p.getLocation(), it.next() );
			}
			
		} else {
			l1.getWorld().dropItem( tempLoc, i );
		}
	}
	
	public void destroyLeaves( Player p ) {
		int hungerLevel = 0;
		l = activePlayerLocations.get( p.getUniqueId() );
		for ( int count = 0; count <= maxAmount; count++ ) {
			location = ( LeafLocation ) l.pollFirst();
			loc = location.getLocation();
			origin = location.getOrigin();
			block = location.getBlock();
			for ( int i = -1; i <= 1; i++ ) {
				for ( int j = -1; j <= 1; j++ ) {
					for ( int k = -1; k <= 1; k++ ) {
						tempLoc = loc.clone().add( i, j, k);
						if ( tempLoc.distance( origin ) > radius && radius > 0 ) continue;
						if ( tempLoc.getBlock().getType().equals( block ) && tempLoc.getBlock().getData() == location.getData() ) {
							l.add( new LeafLocation( tempLoc, location.getItem(), location.getOrigin(), tempLoc.getBlock().getType(), tempLoc.getBlock().getData() ) );
							if ( location.getItem().containsEnchantment( Enchantment.SILK_TOUCH ) ) {
								spawnLeafItem( new ItemStack( block, 1, tempLoc.getBlock().getData() ) , tempLoc, location.getOrigin(), p );
							} else if ( location.getItem().containsEnchantment( Enchantment.LOOT_BONUS_BLOCKS ) && fortunableBlocks.contains( block ) ) {
								int level = location.getItem().getEnchantmentLevel( Enchantment.LOOT_BONUS_BLOCKS );
								int drops = rand.nextInt(level + 2) - 1;
								if ( drops < 0 ) {
									drops = 0;
								}
								for ( Iterator< ItemStack > iterator = tempLoc.getBlock().getDrops( location.getItem() ).iterator(); iterator.hasNext(); ) {
									ItemStack items = iterator.next();
									items.setAmount( items.getAmount() * ( drops + 1 ) );
									spawnLeafItem( items, tempLoc, location.getOrigin(), p );
								}
							} else {
								for ( Iterator< ItemStack > iterator = tempLoc.getBlock().getDrops( location.getItem() ).iterator(); iterator.hasNext(); ) {
									ItemStack items = iterator.next();
									spawnLeafItem( items, tempLoc, location.getOrigin(), p );
								}
							}
							if ( !( hungerMax <= 0 ) && !p.hasPermission( "leafminer.avoidhunger" ) ) {
								if ( hungerLevel >= hungerMax ) {
									p.setFoodLevel( (int) (p.getFoodLevel() - 1) );
									hungerLevel = 0;
								} else {
								hungerLevel++;
								}
							}
							tempLoc.getBlock().setType( Material.AIR );
							if ( p.getFoodLevel() <= 0 && hungerMax > 0 && !checkSender( p, "leafminer.avoidhunger" ) ) {
								activePlayerLocations.remove( p.getUniqueId() );
								p.sendMessage( "You are to hungry to use LeafMiner!" );
								return;
							}
						}
					}
				}
			}
			if ( ( l.size() >= maxLimit && maxLimit > 0 ) || l.isEmpty() ) {
				activePlayerLocations.remove( p.getUniqueId() );
				break;
			}
		}
	}

}
