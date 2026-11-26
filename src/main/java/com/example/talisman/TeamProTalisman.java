package com.example.check;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamProCheck extends JavaPlugin implements Listener {

    // замороженный игрок -> UUID админа, который его вызвал
    Map<UUID, UUID> frozen = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Team Pro Проверка включён!");

        // каждые 5 секунд показываем замороженным экран проверки заново
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (UUID id : frozen.keySet()) {
                Player p = Bukkit.getPlayer(id);
                if (p != null) {
                    showScreen(p);
                }
            }
        }, 0L, 100L);
    }

    void showScreen(Player p) {
        p.sendTitle("§c§lПРОВЕРКА НА ЧИТЫ",
                "§fСкачай AnyDesk: §bhttps://anydesk.com/ru", 10, 130, 10);
    }

    void freeze(Player admin, Player target) {
        frozen.put(target.getUniqueId(), admin.getUniqueId());
        showScreen(target);
        target.sendMessage("§c§lВы вызваны на проверку на читы!");
        target.sendMessage("§fСкачай программу AnyDesk: §bhttps://anydesk.com/ru");
        target.sendMessage("§7Пиши в чат — твои сообщения увидит только проверяющий.");
        admin.sendMessage("§aИгрок " + target.getName() + " вызван на проверку.");
        admin.sendMessage("§7Его сообщения в чате будут приходить тебе.");
    }

    void unfreeze(Player admin, Player target) {
        frozen.remove(target.getUniqueId());
        target.sendTitle("§a§lПРОВЕРКА ПРОЙДЕНА", "§fПриятной игры!", 10, 60, 10);
        target.sendMessage("§aС тебя снята проверка. Приятной игры!");
        admin.sendMessage("§aИгрок " + target.getName() + " снят с проверки.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду можно использовать только в игре.");
            return true;
        }
        Player p = (Player) sender;

        if (!p.isOp()) {
            p.sendMessage("§cУ тебя нет прав для этой команды.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("freze")) {
            if (args.length < 1) {
                p.sendMessage("§cИспользование: /freze <ник>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                p.sendMessage("§cИгрок " + args[0] + " не найден или не в сети.");
                return true;
            }
            if (target == p) {
                p.sendMessage("§cНельзя вызвать на проверку самого себя.");
                return true;
            }
            if (frozen.containsKey(target.getUniqueId())) {
                p.sendMessage("§cЭтот игрок уже на проверке.");
                return true;
            }
            freeze(p, target);
            return true;
        }

        if (command.getName().equalsIgnoreCase("unfreze")) {
            if (args.length < 1) {
                p.sendMessage("§cИспользование: /unfreze <ник>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                p.sendMessage("§cИгрок " + args[0] + " не найден или не в сети.");
                return true;
            }
            if (!frozen.containsKey(target.getUniqueId())) {
                p.sendMessage("§cЭтот игрок не на проверке.");
                return true;
            }
            unfreeze(p, target);
            return true;
        }

        return false;
    }

    // замороженный игрок не может двигаться (поворачивать голову можно)
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (frozen.containsKey(e.getPlayer().getUniqueId())) {
            if (e.getFrom().getBlockX() != e.getTo().getBlockX()
                    || e.getFrom().getBlockY() != e.getTo().getBlockY()
                    || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
                e.setCancelled(true);
            }
        }
    }

    // сообщения замороженного игрока приходят только проверяющему
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID checkerId = frozen.get(p.getUniqueId());
        if (checkerId == null) return;

        e.setCancelled(true);
        Player checker = Bukkit.getPlayer(checkerId);
        if (checker != null) {
            checker.sendMessage("§8[§cПроверка§8] §e" + p.getName() + "§7: §f" + e.getMessage());
        }
        p.sendMessage("§7Сообщение отправлено проверяющему.");
    }

    // замороженный игрок не может писать команды (личные сообщения можно)
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!frozen.containsKey(p.getUniqueId())) return;

        String cmd = e.getMessage().toLowerCase().split(" ")[0];
        boolean allowed = cmd.equals("/msg") || cmd.equals("/tell")
                || cmd.equals("/r") || cmd.equals("/m") || cmd.equals("/w");
        if (!allowed) {
            e.setCancelled(true);
            p.sendMessage("§cВо время проверки команды запрещены.");
        }
    }

    // замороженному нельзя наносить урон
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (frozen.containsKey(p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    // игрок вышел во время проверки — сообщаем админу
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID checkerId = frozen.remove(e.getPlayer().getUniqueId());
        if (checkerId != null) {
            Player checker = Bukkit.getPlayer(checkerId);
            if (checker != null) {
                checker.sendMessage("§c§lИгрок " + e.getPlayer().getName()
                        + " вышел с сервера во время проверки!");
            }
        }
    }
}
