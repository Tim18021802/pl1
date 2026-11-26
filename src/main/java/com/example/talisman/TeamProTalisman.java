package com.example.talisman;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class TeamProTalisman extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Team Pro Talismans включён!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду можно использовать только в игре.");
            return true;
        }
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("sphere")) {
            double radius = 5.0;
            int segments = 24;
            double step = (2 * Math.PI) / segments;

            for (int i = 0; i < segments; i++) {
                double theta = i * step;
                for (int j = 0; j < segments; j++) {
                    double phi = j * step;
                    double x = radius * Math.sin(phi) * Math.cos(theta);
                    double y = radius * Math.sin(phi) * Math.sin(theta) + 2;
                    double z = radius * Math.cos(phi);
                    Location point = player.getLocation().clone().add(x, y, z);
                    player.getWorld().spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
                }
            }
            player.sendMessage("§aСфера из частиц создана вокруг тебя.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("talisman")) {
            ItemStack talisman = new ItemStack(Material.AMETHYST_SHARD);
            ItemMeta meta = talisman.getItemMeta();
            meta.setDisplayName("§dТалисман удачи §7(Team Pro)");
            List<String> lore = new ArrayList<>();
            lore.add("§7ПКМ — даёт скорость и прыгучесть.");
            meta.setLore(lore);
            talisman.setItemMeta(meta);

            player.getInventory().addItem(talisman);
            player.sendMessage("§aТалисман выдан!");
            return true;
        }

        return false;
    }
}
