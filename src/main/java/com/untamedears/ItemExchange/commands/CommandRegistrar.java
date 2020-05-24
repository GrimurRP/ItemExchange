package com.untamedears.itemexchange.commands;

import com.untamedears.itemexchange.ItemExchangePlugin;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;

/**
 * Registers all of ItemExchange's commands
 */
public class CommandRegistrar extends AikarCommandManager {

	public CommandRegistrar(ItemExchangePlugin plugin) {
		super(plugin, false);
	}

	@Override
	public void registerCommands() {
		registerCommand(new CreateCommand());
		registerCommand(new InfoCommand());
		registerCommand(new ReloadCommand(getPlugin()));
		registerCommand(new SetCommand(getPlugin()));
	}

	/**
	 * Note: Don't try to remove this in favour of a private field as registerCommands() is called from the super
	 * constructor so the field will not yet be assigned.
	 *
	 * @return Returns the plugin attached to this registrar.
	 */
	public ItemExchangePlugin getPlugin() {
		return (ItemExchangePlugin) getInternalManager().getPlugin();
	}

	// ------------------------------------------------------------
	// Contexts
	// ------------------------------------------------------------

//	public static RuleHandler ruleHandlerContext(BukkitCommandExecutionContext context) {
//		if (context.getPlayer() == null) {
//			throw new InvalidCommandArgument("You must be a player to execute that.", false);
//		}
//		return new RuleHandler(context.getPlayer());
//	}

}
