package com.samleighton.xquiset.sethomes.commands;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteHome implements CommandExecutor {
    private final SetHomes pl;

    public DeleteHome(SetHomes plugin) {
        this.pl = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //コマンドの送信者がプレイヤーであることを確認する
        if (!(sender instanceof Player)) {
            //コマンドの送信者にプレイヤーではないというメッセージを送信する
            ChatUtils.notPlayerError(sender);
            return false;
        }

        //コマンドを送信したプレイヤー
        Player p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("delhome")) {
            //適切な権限があるか確認する
            if (!p.hasPermission("homes.delhome")) {
                ChatUtils.permissionError(p);
                return false;
            }

            //コマンドを送信したプレイヤーのUUID
            String uuid = p.getUniqueId().toString();

            //入力されたパラメータに基づいて、デフォルトのホームまたは名前付きホームを削除しようとしているかどうかを確認します。
            if (args.length < 1) {
                //削除する前に、Homeがあるかどうか確認します
                if (!pl.hasUnknownHomes(uuid)) {
                    ChatUtils.sendError(p, "現在、デフォルトのホームが設定されていません。");
                    return true;
                }

                //プレイヤーのデフォルトのホームを削除する
                pl.deleteUnknownHome(uuid);
                ChatUtils.sendSuccess(p, "デフォルトのホームが削除されました。");
                return true;
            } else if (args.length > 1) {
                //プレイヤーに、コマンドで引数が多すぎるかどうかを伝える
                ChatUtils.tooManyArgs(p);
                return false;
            } else {
                //名前の付いたホームや、指定された名前のホームがあるかどうかを確認します
                if (!(pl.hasNamedHomes(uuid)) || !(pl.getPlayersNamedHomes(uuid).containsKey(args[0]))) {
                    ChatUtils.sendError(p, "そんな名前の家はありません。");
                    return true;
                }

                //指定された名前のホームを削除します
                pl.deleteNamedHome(uuid, args[0]);
                //プレイヤーにどの家を削除したかを伝える
                ChatUtils.sendSuccess(p, "ホームを削除しました:" + args[0]);
                return true;
            }
        }

        if (cmd.getName().equalsIgnoreCase("delhome-of")) {
            //Check for proper permissions
            if (!p.hasPermission("homes.delhome-of")) {
                ChatUtils.permissionError(p);
                return false;
            }

            //引数の数値の範囲が正しいか確認する
            if (args.length < 1 || args.length > 2) {
                ChatUtils.sendError(p, "エラー: 引数の数が正しくありません。");
                return false;
            }

            //入力したプレーヤー名で新しいオフラインプレーヤーを作成する
            @SuppressWarnings({"deprecated"})
            OfflinePlayer targetP = Bukkit.getServer().getOfflinePlayer(args[0]);
            //プレイヤーが以前にサーバーでプレイしたことがあるかどうかを確認してください
            if (!targetP.hasPlayedBefore()) {
                ChatUtils.sendError(p, "このプレイヤーは" + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + "これまでにサーバーに参加していません。");
                return false;
            }

            String uuid = targetP.getUniqueId().toString();
            //プレイヤー名のみが指定されている場合は、デフォルトのホームを削除してみてください
            if (args.length == 1) {
                //削除する前に、プレーヤーにデフォルトのホームセットがあるかどうかを確認します。
                if (!pl.hasUnknownHomes(uuid)) {
                    ChatUtils.sendError(p, "このプレイヤーは" + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + "デフォルトのホームがありません。");
                    return false;
                } else {
                    //名前のないホームの削除を実行する
                    pl.deleteUnknownHome(uuid);
                    ChatUtils.sendSuccess(p, "プレーヤーのデフォルトのホームを削除しました。" + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.GOLD + "!");
                    return true;
                }
            } else {
                //選択したホームがあるかの確認をする
                String homeName = args[1];
                if (!(pl.hasNamedHomes(uuid)) || !(pl.getPlayersNamedHomes(uuid).containsKey(homeName))) {
                    ChatUtils.sendError(p, "このプレイヤーは" + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + "その名前のホームがありません。");
                    return false;
                } else {
                    //指定されたホームの削除を実行します
                    pl.deleteNamedHome(uuid, homeName);
                    ChatUtils.sendSuccess(p, "プレーヤーの'" + homeName + "'ホームを削除しました。" + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.GOLD + "!");
                    return true;
                }
            }
        }
        return false;
    }

}
