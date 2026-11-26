package com.example.talisman;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeamProTalisman extends JavaPlugin {

    // ====== НАСТРОЙКИ (меняй эти числа) ======
    int effectSeconds = 30;      // сколько секунд действует эффект талисмана
    int cooldownSeconds = 10;    // перезарядка использования в секундах
    double sphereRadius = 5.0;   // радиус сферы из частиц
    // ==========================================

    Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Team Pro Talismans включён!");
    }

    ItemStack makeTalisman(Material material, String name, String lore, String color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + name);
        List<String> l = new ArrayList<>();
        l.add("§7" + lore);
        l.add("§8ПКМ — активировать");
        meta.setLore(l);
        item.setItemMeta(meta);
        return item;
    }

    void giveTalisman(Player p, String type) {
        ItemStack t;
        if (type.equalsIgnoreCase("luck")) {
            t = makeTalisman(Material.AMETHYST_SHARD, "Талисман удачи", "Скорость и прыгучесть", "§d");
        } else if (type.equalsIgnoreCase("power")) {
            t = makeTalisman(Material.BLAZE_POWDER, "Талисман силы", "Сила и регенерация", "§c");
        } else if (type.equalsIgnoreCase("speed")) {
            t = makeTalisman(Material.SUGAR, "Талисман скорости", "Ускорение и невесомость", "§b");
        } else {
            t = makeTalisman(Material.AMETHYST_SHARD, "Талисман удачи", "Скорость и прыгучесть", "§d");
        }
        p.getInventory().addItem(t);
    }

    void activate(Player p, ItemStack item) {
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(p.getUniqueId());
        if (last != null && now - last < cooldownSeconds * 1000L) {
            long left = (cooldownSeconds * 1000L - (now - last)) / 1000L + 1;
            p.sendMessage("§cПерезарядка: ещё " + left + " сек.");
            return;
        }
        cooldowns.put(p.getUniqueId(), now);

        String name = item.getItemMeta().getDisplayName();
        if (name.contains("удачи")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effectSeconds * 20, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, effectSeconds * 20, 1));
            p.sendMessage("§dТалисман удачи активирован!");
        } else if (name.contains("силы")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, effectSeconds * 20, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, effectSeconds * 20, 0));
            p.sendMessage("§cТалисман силы активирован!");
        } else if (name.contains("скорости")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effectSeconds * 20, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, effectSeconds * 20, 0));
            p.sendMessage("§bТалисман скорости активирован!");
        }
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        p.getWorld().spawnParticle(Particle.TOTEM, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду можно использовать только в игре.");
            return true;
        }
        Player p = (Player) sender;

        if (command.getName().equalsIgnoreCase("sphere")) {
            int segments = 24;
            double step = (2 * Math.PI) / segments;
            for (int i = 0; i < segments; i++) {
                double theta = i * step;
                for (int j = 0; j < segments; j++) {
                    double phi = j * step;
                    double x = sphereRadius * Math.sin(phi) * Math.cos(theta);
                    double y = sphereRadius * Math.sin(phi) * Math.sin(theta) + 2;
                    double z = sphereRadius * Math.cos(phi);
                    p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().clone().add(x, y, z), 1);
                }
            }
            p.sendMessage("§aСфера создана!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("talisman")) {
            if (args.length == 0) {
                giveTalisman(p, "luck");
                p.sendMessage("§aТалисман выдан!");
                return true;
            }
            String sub = args[0].toLowerCase();

            if (sub.equals("give")) {
                if (args.length < 2) {
                    p.sendMessage("§cИспользование: /talisman give <ник> [тип]");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    p.sendMessage("§cИгрок " + args[1] + " не найден или не в сети.");
                    return true;
                }
                String type = args.length >= 3 ? args[2] : "luck";
                giveTalisman(target, type);
                p.sendMessage("§aТалисман выдан игроку " + target.getName() + ".");
                target.sendMessage("§dТебе выдали талисман!");
                return true;
            }

            if (sub.equals("test")) {
                giveTalisman(p, "luck");
                giveTalisman(p, "power");
                giveTalisman(p, "speed");
                p.sendMessage("§aТестовый набор выдан: удача, сила, скорость.");
                return true;
            }

            if (sub.equals("luck") || sub.equals("power") || sub.equals("speed")) {
                giveTalisman(p, sub);
                p.sendMessage("§aТалисман выдан!");
                return true;
            }

            p.sendMessage("§cПодкоманды: /talisman | /talisman give <ник> [тип] | /talisman test");
            return true;
        }

        return false;
    }
}
