package com.samleighton.xquiset.sethomes.commands;

import com.samleighton.xquiset.sethomes.Home;
import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Objects;

public class SetHome implements CommandExecutor {
    private final SetHomes pl;
    private final HashMap<String, Integer> maxHomesList;
    private final Permission vaultPerms;
    private final LuckPerms luckPerms;
    private final boolean permissions;

    public SetHome(SetHomes plugin) {
        pl = plugin;
        maxHomesList = pl.getMaxHomes();
        luckPerms = pl.getLuckPermsApi();
        vaultPerms = pl.getVaultPermissions();
        permissions = luckPerms != null || vaultPerms != null;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // コマンドの送信者がプレイヤーであることを確認
        if (!(sender instanceof Player)) {
            // コマンドの送信者に、プレイヤーではない旨を伝えるメッセージを送信
            ChatUtils.notPlayerError(sender);
            return false;
        }

// コマンドが /sethome であるかどうかを確認
        if (cmd.getName().equalsIgnoreCase("sethome")) {
            // 送信者がプレイヤーであることが確認できたので、キャストする
            Player p = (Player) sender;
            String uuid = p.getUniqueId().toString();
            Location home = p.getLocation();

            // ホームのワールドがブラックリストに載っていないことを確認
            if (pl.getBlacklistedWorlds().contains(Objects.requireNonNull(home.getWorld()).getName()) && !p.hasPermission("homes.config_bypass")) {
                ChatUtils.sendError(p, "このワールドではホームを設定できません！");
                return true;
            }

            // プレイヤーの位置にホームを作成
            Home playersHome = new Home(home);

            // ホーム名が指定されていない場合
            if (args.length < 1) {
                // ホームを保存
                pl.saveUnknownHome(uuid, playersHome);

                ChatUtils.sendSuccess(p, "デフォルトのホームが設定されました！");
                // ホーム名およびおそらく説明も指定されている場合
            } else {
                if (p.hasPermission("homes.sethome")) {
                    // プレイヤーのホーム数と設定されている最大ホーム数を確認
                    if (pl.hasNamedHomes(uuid)) {
                        int maxHomes = getMaxHomesAllowed(p);
                        Bukkit.getServer().getLogger().info("最大ホーム数: " + maxHomes);
                        if ((pl.getPlayersNamedHomes(uuid).size() >= maxHomes && maxHomes != 0) && !p.hasPermission("homes.config_bypass")) {
                            ChatUtils.sendInfo(p, pl.config.getString("max-homes-msg"));
                            return true;
                        }
                        // 指定されたホーム名で既にホームが存在しているか確認
                        if (pl.getPlayersNamedHomes(uuid).containsKey(args[0])) {
                            ChatUtils.sendError(p, "その名前のホームは既に存在しています。別の名前を試してください！");
                            return true;
                        }
                    }

                    // 入力されたホーム名からアルファベットや数字以外の文字を除去
                    String homeName = args[0].replaceAll("[^a-zA-Z0-9]", "");

                    // 除去後もホーム名が残っているか確認
                    if (homeName.length() > 0) {
                        // 指定された名前でホーム名を設定
                        playersHome.setHomeName(homeName);
                    } else {
                        ChatUtils.sendError(p, "有効なホーム名を使用してください！使用可能な文字は a-z & 0-9 です。");
                        return true;
                    }

                    // 他の引数を結合して説明として設定
                    StringBuilder desc = new StringBuilder();
                    for (int i = 1; i <= args.length - 1; i++) {
                        desc.append(args[i]).append(" ");
                    }

                    if (!desc.toString().equals("")) {
                        playersHome.setDesc(desc.substring(0, desc.length() - 1));
                    }

                    // 新しいホームを保存
                    pl.saveNamedHome(uuid, playersHome);

                    ChatUtils.sendSuccess(p, "ホーム '" + playersHome.getHomeName() + "' が設定されました！");
                    return true;
                }
                // プレイヤーに適切な権限がないため、メッセージを送信
                ChatUtils.permissionError(p);
            }
            return true;
        }
        return false;
    }

    /**
     * プレイヤーに許可されている最大のホーム数を取得。
     * プレイヤーが複数のグループに所属している場合は最も高い値を使用
     *
     * @param p ホーム数を取得したいプレイヤー
     * @return プレイヤーに許可されている最大ホーム数
     */
    private int getMaxHomesAllowed(Player p) {
        int maxHomes = 0;

        // 権限が有効か確認
        if (permissions) {
            // 最初に LuckPerms を試す
            if (luckPerms != null) {
                // 設定リストのグループをループ
                for (String group : maxHomesList.keySet()) {
                    if (p.hasPermission("group." + group)) {
                        int max_home_val = maxHomesList.get(group);
                        if (maxHomes < max_home_val) {
                            maxHomes = max_home_val;
                        }
                    }
                }
            } else {
                // Vaultで見つかったグループをループ

                for (String group : vaultPerms.getPlayerGroups(p)) {
                    for (String g : maxHomesList.keySet()) {
                        if (group.equalsIgnoreCase(g)) {
                            if (maxHomesList.get(g) > maxHomes) {
                                maxHomes = maxHomesList.get(g);
                            }
                        }
                    }
                }
            }
        }

        return maxHomes;
    }
}
