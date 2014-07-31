package tw.longcat.lab.PayHome;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.locations.HomesComponent;

//Code need organize...
public class PayHome extends JavaPlugin{
	public static CommandBook cmdbook = null;
	private static final Logger log = Logger.getLogger("Minecraft");
	public static Economy econ = null;
	double price;
	public void onEnable(){
		this.saveDefaultConfig();
		price = this.getConfig().getDouble("price");
		if (!setupEconomy() ) {
			log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
		}
		if (!setupCmdBook() ) {
			log.severe(String.format("[%s] - Disabled due to no CommandBook dependency found!", getDescription().getName()));
		}
	}
	private boolean setupEconomy(){
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
	private boolean setupCmdBook(){
		cmdbook = (CommandBook)getServer().getPluginManager().getPlugin("CommandBook");
		return true;
	}
	public void reload(){
		this.saveDefaultConfig();
		price = this.getConfig().getDouble("price");
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if(command.getLabel().equals("payhome")) {
			if(args.length == 0){
				if(!(sender instanceof Player)) {
					log.info("This command should only call by player.");
					return true;
				}
				Player player = (Player) sender;
				if(!(player.hasPermission("PayHome.teleport"))){
					player.sendMessage(ChatColor.YELLOW + "You have no PERMISSION to teleport.");
					return true;
				}
				EconomyResponse r = econ.withdrawPlayer(player.getDisplayName(), price);
				if(r.transactionSuccess()) {
					sender.sendMessage(String.format(ChatColor.YELLOW + "You have teleport to your home, cost " + ChatColor.WHITE + "%s" + ChatColor.YELLOW + ".", econ.format(r.amount)));
					sender.sendMessage(String.format(ChatColor.YELLOW + "Remain " + ChatColor.WHITE + "%s" + ChatColor.YELLOW + " in account.", econ.format(r.balance)));
					player.teleport(cmdbook.getComponentManager().getComponent(HomesComponent.class).getManager().get(getServer().getWorld("world"), player.getName()).getLocation());
				}else if(r.amount > r.balance){
					sender.sendMessage(ChatColor.YELLOW + "Insufficient funds.");
				}else{
					sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
				}
				return true;
			}else if(args[0].equalsIgnoreCase("reload")){
				if(!(sender instanceof Player)){
					reload();
					log.info("PayHome reloaded.");
					return true;
				}else{
					Player player = (Player) sender;
					if(player.hasPermission("PayHome.reload")){
						player.sendMessage(ChatColor.YELLOW + "PayHome reloaded.");
						reload();
						return true;
					}else{
						player.sendMessage(ChatColor.YELLOW + "You have no PERMISSION to reload");
						return true;
					}
				}
			}
		}
		return false;
	}
}