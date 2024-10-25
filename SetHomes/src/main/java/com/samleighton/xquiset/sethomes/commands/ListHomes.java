package com.samleighton.xquiset.sethomes.commands;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ListHomes implements CommandExecutor {

    private final SetHomes pl;
    private final String filler = StringUtils.repeat("-", 53);

    public ListHomes(SetHomes plugin) {
        this.pl = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //Make sure the sender of the command is a player
        if (!(sender instanceof Player)) {
            //Sends message to sender of command that they're not a player
            ChatUtils.notPlayerError(sender);
            return false;
        }

        if (cmd.getName().equalsIgnoreCase("homes")) {
            Player p = (Player) sender;

            if (args.length == 1) {
                if (p.hasPermission("homes.gethomes")) {
// プレイヤーが実際にサーバーに参加していることを確認
                    if (offlinePlayer.hasPlayedBefore()) {
                        UUID uuid = offlinePlayer.getUniqueId();
                        listHomes(uuid, p);
                    } else {
                        ChatUtils.sendError(p, "そのプレイヤーはこのサーバーに参加したことがありません!");
                        return true;
                    }
                } else {
                    // プレイヤーに適切な権限がないため、メッセージを送信
                    ChatUtils.permissionError(p);
                }
                return true;
            } else if (args.length == 0) {
                // コマンドを送信したプレイヤーのホームをリスト表示
                listHomes(p);
                return true;
            } else {
                // コマンドに引数が多すぎる場合、プレイヤーに通知
                ChatUtils.tooManyArgs(p);
                return false;
            }
        }
        return false;
    }

    /**
     * プレイヤーが名前付きのホームを持っているか確認し、
     * ホームがあればリストを送信するために使用
     *
     * @param p     ホームを確認するプレイヤーオブジェクト
     * @param uuid  プレイヤーオブジェクトのUUID（文字列）
     */
    private void checkForNamedHomes(Player p, String uuid) {
        if (pl.hasNamedHomes(uuid)) {
            // ホームとその説明をプレイヤーに表示
            for (String id : pl.getPlayersNamedHomes(uuid).keySet()) {
                // ホームが設定されているワールドの名前を取得
                String world = pl.getPlayersNamedHomes(uuid).get(id).getWorld();
                // ホームの説明を取得
                String desc = pl.getPlayersNamedHomes(uuid).get(id).getDesc();
                if (desc != null) {
                    // 説明付きメッセージを送信
                    p.sendMessage(ChatColor.DARK_AQUA + "名前: " + ChatColor.WHITE + id + ChatColor.DARK_GRAY + " | " + ChatColor.DARK_AQUA + "ワールド: " + ChatColor.WHITE + world + ChatColor.DARK_GRAY + " | " + ChatColor.DARK_AQUA + "説明: " + ChatColor.WHITE + desc);
                } else {
                    // 説明なしメッセージを送信
                    p.sendMessage(ChatColor.DARK_AQUA + "名前: " + ChatColor.WHITE + id + ChatColor.DARK_GRAY + " | " + ChatColor.DARK_AQUA + "ワールド: " + ChatColor.WHITE + world);
                }
            }
        }
        p.sendMessage(filler);
    }

    /**
     * コマンド送信者のホームリストを生成し、フォーマットして表示
     *
     * @param p コマンドを送信するプレイヤー
     */
    private void listHomes(Player p) {
        // プレイヤーpのUUID文字列
        String uuid = p.getUniqueId().toString();

        // プレイヤーのホームリストを表示開始
        p.sendMessage(ChatColor.BOLD + "現在設定されているホーム");
        p.sendMessage(filler);

        // デフォルトのホームが設定されているかをプレイヤーに伝える
        if (pl.hasUnknownHomes(uuid)) {
            // ホームが設定されているワールドの名前を取得
            String world = pl.getPlayersUnnamedHome(uuid).getWorld().getName();
            p.sendMessage(ChatColor.GOLD + "デフォルトホーム" + ChatColor.DARK_GRAY + " | " + ChatColor.DARK_AQUA + "ワールド: " + ChatColor.WHITE + world);
        }

        // プレイヤーがホームを持っているか確認
        checkForNamedHomes(p, uuid);
    }

    /**
     * 別のプレイヤーのホームリストを生成し、コマンド送信者に表示
     * 通常は管理者が使用
     *
     * @param playerUUID ホームリストを取得するプレイヤーのUUID
     * @param sender     コマンドリストを表示する送信者
     */
    private void listHomes(UUID playerUUID, Player sender) {
        String uuid = playerUUID.toString();

        sender.sendMessage(ChatColor.BOLD + "現在設定されているプレイヤーのホーム - " + Bukkit.getOfflinePlayer(playerUUID).getName());
        sender.sendMessage(filler);

        // デフォルトのホームが設定されているかを伝える
        if (pl.hasUnknownHomes(uuid)) {
            // ホームが設定されているワールドの名前を取得
            String world = pl.getPlayersUnnamedHome(uuid).getWorld().getName();
            sender.sendMessage(ChatColor.GOLD + "デフォルトホーム - ワールド: " + world);
        }

        // プレイヤーがホームを持っているか確認
        checkForNamedHomes(sender, uuid);
    }

}
