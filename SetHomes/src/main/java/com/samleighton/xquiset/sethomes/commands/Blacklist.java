package com.samleighton.xquiset.sethomes.commands;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Blacklist implements CommandExecutor {

    private final SetHomes pl;

    public Blacklist(SetHomes plugin) {
        this.pl = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //コマンドの送信者がプレイヤーであることを確認してください
        if (!(sender instanceof Player)) {
            //コマンドの送信者にプレイヤーではないというメッセージを送信します
            ChatUtils.notPlayerError(sender);
            return false;
        }

        if (cmd.getName().equalsIgnoreCase("blacklist")) {
            Player p = (Player) sender;
            String filler = StringUtils.repeat("-", 53);

            //コマンドにパラメータを渡さない場合は、ブラックリストに登録されたワールドをリストするだけです
            if (args.length == 0) {
                //適切な権限があるか確認する
                if (p.hasPermission("homes.blacklist_list")) {
                    if (pl.getBlacklistedWorlds().size() > 0) {
                        p.sendMessage(ChatColor.DARK_RED + "All blacklisted worlds:");
                        p.sendMessage(filler);
                        for (String w : pl.getBlacklistedWorlds()) {
                            p.sendMessage(ChatColor.LIGHT_PURPLE + " - " + w);
                        }
                        return true;
                    } else {
                        //設定から返されたブラックリストのサイズは 0 でした
                        ChatUtils.sendInfo(p, "現在、ブラックリストにワールドはありません。");
                        return true;
                    }
                } else {
                    //適切な権限が見つかりませんでした
                    ChatUtils.permissionError(p);
                    return true;
                }
            } else {
                //ブラックリストに世界を追加する
                if (args[0].equalsIgnoreCase("add")) {
                    //適切な権限があるか確認する
                    if (p.hasPermission("homes.blacklist_add")) {
                        //このコマンドにはワールド名を指定する必要があります。
                        if (args.length == 2) {
                            //入力したものが実際に有効なワールドであり、ブラックリストにまだ含まれていないことを確認してください。
                            if (getAllWorlds().contains(args[1]) && !(pl.getBlacklistedWorlds().contains(args[1]))) {
                                //設定リストに世界を追加する
                                List<String> temp = pl.getBlacklistedWorlds();
                                temp.add(args[1]);
                                pl.getBlacklist().getConfig().set("blacklisted_worlds", temp);
                                pl.getBlacklist().save();

                                ChatUtils.sendSuccess(p, "ワールド '" + args[1] + "' をブラックリストに追加しました。");
                                return true;
                            } else {
                                ChatUtils.sendError(p, "その名前の世界は見つかりませんでした。");
                                return true;
                            }
                        } else {
                            ChatUtils.sendError(p, "ブラックリストに追加するワールド名を指定する必要があります。");
                            return true;
                        }
                    } else {
                        //適切な権限が見つかりませんでした
                        ChatUtils.permissionError(p);
                        return true;
                    }
                    //ブラックリストから世界を削除する
                } else if (args[0].equalsIgnoreCase("remove")) {
                    //適切な権限があるか確認する
                    if (p.hasPermission("homes.blacklist_remove")) {
                        //このコマンドにはワールド名を指定する必要があります。
                        if (args.length == 2) {
                            //世界が実際にブラックリストに含まれているか確認する
                            if (pl.getBlacklistedWorlds().contains(args[1])) {
                                //構成リストから世界を削除する構成リストから世界を削除する構成リストから世界を削除する
                                List<String> temp = pl.getBlacklistedWorlds();
                                temp.remove(args[1]);
                                pl.getBlacklist().getConfig().set("blacklisted_worlds", temp);
                                pl.getBlacklist().save();

                                ChatUtils.sendSuccess(p, "ブラックリストからワールド '" + args[1] + "' を削除しました。");
                                return true;
                            } else {
                                ChatUtils.sendError(p, "ブラックリストにその名前の世界は見つかりませんでした。");
                                return true;
                            }
                        } else {
                            ChatUtils.sendError(p, "ブラックリストから削除するワールド名を指定する必要があります。");
                            return true;
                        }
                    } else {
                        //適切な権限が見つかりませんでした
                        ChatUtils.permissionError(p);
                        return true;
                    }
                } else {
                    ChatUtils.sendError(p, args[0] + "'ブラックリストアクションはありません！");
                    return false;
                }
            }
        }
        return false;
    }

    //現在サーバーにインストールされているすべてのワールドを名前で取得します
    private List<String> getAllWorlds() {
        List<String> worldNames = new ArrayList<String>();

        for (World w : Bukkit.getWorlds()) {
            worldNames.add(w.getName());
        }

        return worldNames;
    }

}
