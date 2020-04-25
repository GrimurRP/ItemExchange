package com.untamedears.itemexchange.commands;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.glue.CitadelGlue;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.ExchangeRule.Type;
import com.untamedears.itemexchange.utility.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import vg.civcraft.mc.civmodcore.api.BlockAPI;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.command.AikarCommand;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

@CommandAlias(CreateCommand.ALIAS)
public class CreateCommand extends AikarCommand {

	public static final String ALIAS = "iec|iecreate";

	public static final String ALIAS_INPUT_TYPES = "input|i|in|inputs";

	public static final String ALIAS_OUTPUT_TYPES = "output|o|out|outputs";

	public static final String HELD_DESCRIPTION = "Creates an exchange rule based on a held item.";

	// ------------------------------------------------------------
	// Creating from held item
	// ------------------------------------------------------------

	public static final String DETAILS_SYNTAX = "<material> [amount]";

	public static final String DETAILS_DESCRIPTION = "Sets the material of an exchange rule.";

	public static final String DETAILS_COMPLETION = "@itemMaterials";

	@Default
	@Description("Creates an exchange rule based on a shop block.")
	public void createFromShop(Player player) {
		BlockIterator ray = new BlockIterator(player, 6);
		while (ray.hasNext()) {
			Block block = ray.next();
			if (!BlockAPI.isValidBlock(block) || !block.getType().isBlock()) {
				continue;
			}
			if (!ItemExchangePlugin.SHOP_BLOCKS.contains(block.getType())) {
				break;
			}
			if (CitadelGlue.isEnabled()) {
				if (CitadelGlue.hasAccessToChest(block, player)) {
					player.sendMessage(ChatColor.RED + "You do not have access to that.");
					return;
				}
			}
			Inventory inventory = NullCoalescing.chain(() -> ((InventoryHolder) block.getState()).getInventory());
			if (inventory == null) {
				throw new InvalidCommandArgument("You do not have access to that.");
			}
			ItemStack inputItem = null;
			ItemStack outputItem = null;
			for (ItemStack item : inventory.getContents()) {
				if (!ItemAPI.isValidItem(item)) {
					continue;
				}
				if (inputItem == null) {
					inputItem = item.clone();
				}
				else if (inputItem.isSimilar(item)) {
					inputItem.setAmount(inputItem.getAmount() + item.getAmount());
				}
				else if (outputItem == null) {
					outputItem = item.clone();
				}
				else if (outputItem.isSimilar(item)) {
					outputItem.setAmount(outputItem.getAmount() + item.getAmount());
				}
				else {
					throw new InvalidCommandArgument("Inventory should only contain two types of items!");
				}
			}
			if (inputItem == null) {
				throw new InvalidCommandArgument("Inventory should have at least one type of item.");
			}
			if (Utilities.isExchangeRule(inputItem)) {
				throw new InvalidCommandArgument("You cannot exchange rule blocks!");
			}
			ExchangeRule inputRule = new ExchangeRule();
			inputRule.setType(Type.INPUT);
			inputRule.trace(inputItem);
			if (outputItem == null) {
				Utilities.giveItemsOrDrop(inventory, inputRule.toItem());
			}
			else {
				if (Utilities.isExchangeRule(outputItem)) {
					throw new InvalidCommandArgument("You cannot exchange rule blocks!");
				}
				ExchangeRule outputRule = new ExchangeRule();
				outputRule.setType(Type.OUTPUT);
				outputRule.trace(outputItem);
				Utilities.giveItemsOrDrop(inventory, inputRule.toItem(), outputRule.toItem());
			}
			player.sendMessage(ChatColor.GREEN + "Created exchange successfully.");
			return;
		}
		throw new InvalidCommandArgument("No block in view is a suitable shop block.");
	}

	// ------------------------------------------------------------
	// Creating from explicit details
	// ------------------------------------------------------------

	private void createFromHeld(Player player, Type type) {
		ItemStack held = player.getInventory().getItemInMainHand();
		if (!ItemAPI.isValidItem(held)) {
			throw new InvalidCommandArgument("You must be holding an item to do that.");
		}
		ExchangeRule rule = new ExchangeRule();
		rule.setType(type);
		rule.trace(held);
		Utilities.givePlayerExchangeRule(player, rule);
		player.sendMessage(ChatColor.GREEN + "Created exchange successfully.");
	}

	@Subcommand(ALIAS_INPUT_TYPES)
	@Description(HELD_DESCRIPTION)
	public void createInputFromHeld(Player player) {
		createFromHeld(player, Type.INPUT);
	}

	@Subcommand(ALIAS_OUTPUT_TYPES)
	@Description(HELD_DESCRIPTION)
	public void createOutputFromHeld(Player player) {
		createFromHeld(player, Type.OUTPUT);
	}

	private void createFromDetails(Player player, Type type, String slug, int amount) {
		Material material = MaterialAPI.getMaterial(slug);
		if (!MaterialAPI.isValidItemMaterial(material)) {
			throw new InvalidCommandArgument("You must enter a valid item material.");
		}
		ExchangeRule rule = new ExchangeRule();
		rule.setType(type);
		rule.setMaterial(material);
		if (amount <= 0) {
			throw new InvalidCommandArgument("You must enter a valid amount.");
		}
		rule.setAmount(amount);
		Utilities.givePlayerExchangeRule(player, rule);
		player.sendMessage(ChatColor.GREEN + "Created exchange successfully.");
	}

	@Subcommand(ALIAS_INPUT_TYPES)
	@Syntax(DETAILS_SYNTAX)
	@Description(DETAILS_DESCRIPTION)
	@CommandCompletion(DETAILS_COMPLETION)
	public void createInputFromDetails(Player player, String slug, @Default("1") int amount) {
		createFromDetails(player, Type.INPUT, slug, amount);
	}

	@Subcommand(ALIAS_OUTPUT_TYPES)
	@Syntax(DETAILS_SYNTAX)
	@Description(DETAILS_DESCRIPTION)
	@CommandCompletion(DETAILS_COMPLETION)
	public void createOutputFromDetails(Player player, String slug, @Default("1") int amount) {
		createFromDetails(player, Type.OUTPUT, slug, amount);
	}

}