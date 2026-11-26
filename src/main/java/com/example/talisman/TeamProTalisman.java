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
import java.util.List;

public class TeamProTalisman extends JavaPlugin {

    // ====== НАСТРОЙКИ (меняй эти числа) ======
    int refreshTicks = 20;       // раз в сколько тиков обновлять эффекты (20 тиков = 1 секунда)
    double sphereRadius = 5.0;   // радиус сферы из частиц
    // ==========================================

    @Override
    public void onEnable() {
        getLogger().info("Team Pro Talismans включён!");

        // каждые refreshTicks проверяем руки игроков и даём эффекты
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                applyHeld(p);
            }
        }, 0L, refreshTicks);
    }

    ItemStack makeTalisman(Material material, String name, String lore, String color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + name);
        List<String> l = new ArrayList<>();
        l.add("§7" + lore);
        l.add("§8Держи в руке — эффекты действуют сами");
        meta.setLore(l);
        item.setItemMeta(meta);
        return item;
    }

    void giveTalisman(Player p, String type) {
        ItemStack t;
        if (type.equalsIgnoreCase("luck")) {
            t = makeTalisman(Material.GHAST_TEAR, "Талисман удачи", "Скорость и прыгучесть", "§d");
        } else if (type.equalsIgnoreCase("power")) {
            t = makeTalisman(Material.FIRE_CHARGE, "Талисман силы", "Сила и регенерация", "§c");
        } else if (type.equalsIgnoreCase("speed")) {
            t = makeTalisman(Material.SNOWBALL, "Талисман скорости", "Ускорение и невесомость", "§b");
        } else if (type.equalsIgnoreCase("jump")) {
            t = makeTalisman(Material.SLIME_BALL, "Талисман прыжков", "Высокие прыжки и мягкое падение", "§a");
        } else if (type.equalsIgnoreCase("night")) {
            t = makeTalisman(Material.ENDER_PEARL, "Талисман ночного зрения", "Видно в темноте", "§5");
        } else if (type.equalsIgnoreCase("fire")) {
            t = makeTalisman(Material.MAGMA_CREAM, "Талисман огнеупорности", "Огонь не вредит", "§6");
        } else if (type.equalsIgnoreCase("water")) {
            t = makeTalisman(Material.HEART_OF_THE_SEA, "Талисман морей", "Дыхание под водой и плавание", "§3");
        } else {
            t = makeTalisman(Material.GHAST_TEAR, "Талисман удачи", "Скорость и прыгучесть", "§d");
        }
        p.getInventory().addItem(t);
    }

    void applyEffect(Player p, PotionEffectType type, int level) {
        // 400 тиков = 20 секунд с запасом, эффект обновляется каждую секунду
        p.addPotionEffect(new PotionEffect(type, 400, level, true, false, true));
    }

    void applyHeld(Player p) {
        checkHand(p, p.getInventory().getItemInMainHand());
        checkHand(p, p.getInventory().getItemInOffHand());
    }

    void checkHand(Player p, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        String name = item.getItemMeta().getDisplayName();
        if (name.contains("удачи")) {
            applyEffect(p, PotionEffectType.SPEED, 0);
            applyEffect(p, PotionEffectType.JUMP, 0);
        } else if (name.contains("силы")) {
            applyEffect(p, PotionEffectType.INCREASE_DAMAGE, 0);
            applyEffect(p, PotionEffectType.REGENERATION, 0);
        } else if (name.contains("скорости")) {
            applyEffect(p, PotionEffectType.SPEED, 1);
            applyEffect(p, PotionEffectType.SLOW_FALLING, 0);
        } else if (name.contains("прыжков")) {
            applyEffect(p, PotionEffectType.JUMP, 1);
            applyEffect(p, PotionEffectType.SLOW_FALLING, 0);
        } else if (name.contains("ночного")) {
            applyEffect(p, PotionEffectType.NIGHT_VISION, 0);
        } else if (name.contains("огнеупорности")) {
            applyEffect(p, PotionEffectType.FIRE_RESISTANCE, 0);
        } else if (name.contains("морей")) {
            applyEffect(p, PotionEffectType.WATER_BREATHING, 0);
            applyEffect(p, PotionEffectType.DOLPHINS_GRACE, 0);
        }
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
                p.sendMessage("§aТалисман выдан! Возьми его в руку — эффекты включатся сами.");
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
                giveTalisman(p, "jump");
                giveTalisman(p, "night");
                giveTalisman(p, "fire");
                giveTalisman(p, "water");
                p.sendMessage("§aТестовый набор выдан: все 7 талисманов!");
                return true;
            }

            if (sub.equals("luck") || sub.equals("power") || sub.equals("speed")
                    || sub.equals("jump") || sub.equals("night") || sub.equals("fire")
                    || sub.equals("water")) {
                giveTalisman(p, sub);
                p.sendMessage("§aТалисман выдан!");
                return true;
            }

            p.sendMessage("§cПодкоманды: /talisman | /talisman give <ник> [тип] | /talisman test");
            p.sendMessage("§7Типы: luck, power, speed, jump, night, fire, water");
            return true;
        }

        return false;
    }
}
