package com.samleighton.xquiset.sethomes.commands;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class GoHome implements CommandExecutor, Listener {

    private final SetHomes pl;
    private int taskId;
    private final int cooldown;
    private final Map<String, Long> cooldownList = new HashMap<>();
    private final boolean cancelOnMove;
    private Location locale = null;
    private Player p;

    public GoHome(SetHomes plugin) {
        pl = plugin;
        cooldown = pl.getConfig().getInt("tp-cooldown");
        cancelOnMove = pl.getConfig().getBoolean("tp-cancelOnMove");
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    //コマンド送信者がプレイヤーであることを確認
    if (!(sender instanceof Player)) {
        //コマンド送信者に対してプレイヤーではないことを通知
        ChatUtils.notPlayerError(sender);
        return false;
    }

    p = (Player) sender;
    if (cmd.getName().equalsIgnoreCase("home")) {
        if (isTeleporting()) {
            ChatUtils.sendError(p, "テレポート中はこのコマンドを使用できません!");
            return true;
        }

        final String uuid = p.getUniqueId().toString();

        //クールダウンが設定で有効の場合、プレイヤーがクールダウンリストにあるか確認
        if (!(cooldownList.containsKey(uuid)) || cooldown <= 0 || p.hasPermission("homes.config_bypass")) {
            //プレイヤーがクールダウンリストにいないので、テレポートを試みる
            //テレポートが成功したのでtrueを返す
            return teleportHome(p, uuid, args);
            //テレポート失敗
        } else {
            //プレイヤーがクールダウンリストにいる
            //再度コマンドを実行できるまでの残り時間を計算
            long timeLeft = ((cooldownList.get(uuid) / 1000) + cooldown) - (System.currentTimeMillis() / 1000);
            //プレイヤーが必要な時間を経過していない
            if (timeLeft > 0) {
                //プレイヤーに待つ必要があることを通知
                ChatUtils.sendInfo(p, StringUtils.replace(pl.getConfig().getString("tp-cooldown-msg"), "%s", String.valueOf(timeLeft)));
                return true;
            } else {
                //プレイヤーが十分に待ったのでリストから削除し、テレポートを試みる
                cooldownList.remove(uuid);
                if (teleportHome(p, uuid, args)) {
                    //プレイヤーが正常にテレポートした
                    return true;
                }
            }
        }
    }

    if (cmd.getName().equalsIgnoreCase("home-of")) {
        if (!p.hasPermission("homes.home-of")) {
            ChatUtils.permissionError(p);
            return false;
        }

        if (isTeleporting()) {
            ChatUtils.sendError(p, "テレポート中はこのコマンドを使用できません!");
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            ChatUtils.sendError(p, "エラー: 引数の数が正しくありません!");
            return false;
        }

        //現在のプレイヤーの位置を取得
        locale = p.getLocation();
        //ターゲットプレイヤーのオフラインプレイヤーを作成
        @SuppressWarnings({"deprecated"})
        OfflinePlayer targetP = Bukkit.getServer().getOfflinePlayer(args[0]);

        //オフラインプレイヤーが以前にサーバーでプレイしたか確認
        if (!targetP.hasPlayedBefore()) {
            ChatUtils.sendError(p, "プレイヤー " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + " はこれまでにプレイしたことがありません!");
            return false;
        }

        //オフラインプレイヤーのUUIDを文字列として保存
        String uuid = targetP.getUniqueId().toString();
        //他のプレイヤーのホームにプレイヤーをテレポートしようとする
        return teleportHomeOf(p, uuid, args);
    }
    return false;
}

/**
 * @param p    テレポートしようとしているプレイヤー
 * @param args プレイヤーがコマンドで渡した引数
 * @return テレポートが成功した場合はtrue、それ以外はfalse
 */
private boolean teleportHome(final Player p, final String uuid, String[] args) {
    //コマンド実行時のプレイヤーの位置
    locale = p.getLocation();
    if (args.length < 1) {
        //ホームがない場合、プレイヤーに通知
        if (!(pl.hasUnknownHomes(uuid))) {
            ChatUtils.sendError(p, "デフォルトのホームがありません!");
            return false;
        } else {
            //プレイヤーをホームにテレポートし、通知を表示
            if (pl.getConfig().getInt("tp-delay") > 0 && !p.hasPermission("homes.config_bypass")) {
                //テレポート遅延時間をカウントダウンし、ユーザーの画面にメッセージを表示するタイマーを実行
                taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
                    int delay = pl.getConfig().getInt("tp-delay");

                    public void run() {
                        if (delay == 0) {
                            //この繰り返しタスクをキャンセル
                            pl.cancelTask(taskId);
                            //プレイヤーをホームにテレポート
                            p.teleport(pl.getPlayersUnnamedHome(uuid));
                            //テレポート後にプレイヤーの足元にパーティクルを表示
                            p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                            //テレポート時に音を再生
                            p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                            //プレイヤーをクールダウンリストに追加
                            cooldownList.put(uuid, System.currentTimeMillis());
                        } else {
                            //プレイヤーに毎秒タイトルを送信
                            p.sendTitle(ChatColor.GOLD + "テレポートまで " + delay + "...", null, 0, 20, 0);
                            //毎秒音を再生
                            p.playNote(p.getLocation(), Instrument.DIDGERIDOO, Note.sharp(2, Note.Tone.F));
                            //残り時間を1秒ずつ減少
                            delay--;
                        }
                    }
                }, 0L, 20L);
            } else {
                //設定でテレポート遅延が無効なため、繰り返しタスクを開始せずにテレポート
                p.teleport(pl.getPlayersUnnamedHome(uuid));
                //テレポート後にプレイヤーの足元にパーティクルを表示
                p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                //テレポート時に音を再生
                p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                //テレポート成功を通知
                ChatUtils.sendSuccess(p, "ホームにテレポートしました!");
                //プレイヤーをクールダウンリストに追加
                cooldownList.put(uuid, System.currentTimeMillis());
            }
            return true;
        }
    } else if (args.length > 1) {
        //コマンドに引数が多すぎる場合にプレイヤーに通知
        ChatUtils.tooManyArgs(p);
        return false;
    } else {
        //指定された名前のホームがあるか確認
        if (!(pl.hasNamedHomes(uuid)) || !(pl.getPlayersNamedHomes(uuid).containsKey(args[0]))) {
            ChatUtils.sendError(p, "その名前のホームはありません!");
            return false;
        }
        final String homeName = args[0];
        //プレイヤーをホームにテレポートし、通知を表示
        if (pl.getConfig().getInt("tp-delay") > 0 && !p.hasPermission("homes.config_bypass")) {
            //テレポート遅延時間をカウントダウンし、ユーザーの画面にメッセージを表示するタイマーを実行
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
                int delay = pl.getConfig().getInt("tp-delay");

                public void run() {
                    if (delay == 0) {
                        pl.cancelTask(taskId);
                        //プレイヤーをホームにテレポート
                        p.teleport(pl.getNamedHomeLocal(uuid, homeName));
                        //テレポート後にプレイヤーの足元にパーティクルを表示
                        p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                        //テレポート時に音を再生
                        p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                        //プレイヤーをクールダウンリストに追加
                        cooldownList.put(uuid, System.currentTimeMillis());
                    } else {
                        //毎秒プレイヤーにタイトルを送信
                        p.sendTitle(ChatColor.GOLD + "テレポートまで " + delay + "...", null, 5, 5, 5);
                        //毎秒音を再生
                        p.playNote(p.getLocation(), Instrument.DIDGERIDOO, Note.sharp(2, Note.Tone.F));
                        //残り時間を減少
                        delay--;
                    }
                }
            }, 0L, 20L);
        } else {
            //テレポート遅延が無効なため、即座にテレポート
            p.teleport(pl.getNamedHomeLocal(uuid, homeName));
            //テレポート後にプレイヤーの足元にパーティクルを表示
            p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
            //テレポート時に音を再生
            p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
            //テレポート成功を通知
            ChatUtils.sendSuccess(p, "ホーム '" + homeName + "' にテレポートしました!");
            //プレイヤーをクールダウンリストに追加
            cooldownList.put(uuid, System.currentTimeMillis());
        }
        return true;
    }
}

/**
 * @param p    テレポートしようとしているプレイヤー
 * @param uuid テレポート先のホームを持つプレイヤーのUUID
 * @param args プレイヤーがコマンドで渡した引数
 * @return テレポートが成功した場合はtrue、それ以外はfalse
 */
private boolean teleportHomeOf(final Player p, final String uuid, String[] args) {
    locale = p.getLocation();
    // 引数が1つだけ渡された場合、デフォルトのホームを検索
    if (args.length == 1) {
        // デフォルトのホームがあるか確認
        if (!pl.hasUnknownHomes(uuid)) {
            ChatUtils.sendError(p, "プレイヤー " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + " はデフォルトのホームを設定していません!");
            return false;
        } else {
            // プレイヤーをホームにテレポートし、通知を表示
            if (pl.getConfig().getInt("tp-delay") > 0 && !p.hasPermission("homes.config_bypass")) {
                // テレポート遅延時間をカウントダウンし、ユーザーの画面にメッセージを表示するタイマーを実行
                taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
                    int delay = pl.getConfig().getInt("tp-delay");

                    public void run() {
                        if (delay == 0) {
                            // この繰り返しタスクをキャンセル
                            pl.cancelTask(taskId);
                            // プレイヤーをホームにテレポート
                            p.teleport(pl.getPlayersUnnamedHome(uuid));
                            // テレポート後にプレイヤーの足元にパーティクルを表示
                            p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                            // テレポート時に音を再生
                            p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                            // プレイヤーをクールダウンリストに追加
                            cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
                        } else {
                            // 毎秒タイトルを送信
                            p.sendTitle(ChatColor.GOLD + "テレポートまで " + delay + "...", null, 0, 20, 0);
                            // 毎秒音を再生
                            p.playNote(p.getLocation(), Instrument.DIDGERIDOO, Note.sharp(2, Note.Tone.F));
                            // 残り時間を1秒減少
                            delay--;
                        }
                    }
                }, 0L, 20L);
            } else {
                // 設定でテレポート遅延が無効なため、繰り返しタスクを開始せずにテレポート
                p.teleport(pl.getPlayersUnnamedHome(uuid));
                // テレポート後にプレイヤーの足元にパーティクルを表示
                p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                // テレポート時に音を再生
                p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                // テレポート成功を通知
                ChatUtils.sendSuccess(p, "テレポートしました!");
                // プレイヤーをクールダウンリストに追加
                cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
            }
            return true;
        }
    } else {
        final String homeName = args[1];
        if (!(pl.hasNamedHomes(uuid)) || !(pl.getPlayersNamedHomes(uuid).containsKey(homeName))) {
            ChatUtils.sendError(p, "その名前のホームはありません!");
            return false;
        }

        // プレイヤーをホームにテレポートし、通知を表示
        if (pl.getConfig().getInt("tp-delay") > 0 && !p.hasPermission("homes.config_bypass")) {
            // テレポート遅延時間をカウントダウンし、ユーザーの画面にメッセージを表示するタイマーを実行
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
                int delay = pl.getConfig().getInt("tp-delay");

                public void run() {
                    if (delay == 0) {
                        pl.cancelTask(taskId);
                        // プレイヤーをホームにテレポート
                        p.teleport(pl.getNamedHomeLocal(uuid, homeName));
                        // テレポート後にプレイヤーの足元にパーティクルを表示
                        p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                        // テレポート時に音を再生
                        p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                        // プレイヤーをクールダウンリストに追加
                        cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
                    } else {
                        // 毎秒プレイヤーにタイトルを送信
                        p.sendTitle(ChatColor.GOLD + "テレポートまで " + delay + "...", null, 5, 5, 5);
                        // 毎秒音を再生
                        p.playNote(p.getLocation(), Instrument.DIDGERIDOO, Note.sharp(2, Note.Tone.F));
                        // 残り時間を減少
                        delay--;
                    }
                }
            }, 0L, 20L);
        } else {
            // プレイヤーをホームにテレポート
            p.teleport(pl.getNamedHomeLocal(uuid, args[1]));
            // テレポート後にプレイヤーの足元にパーティクルを表示
            p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
            // テレポート時に音を再生
            p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
            // テレポート成功を通知
            ChatUtils.sendSuccess(p, "テレポートしました!");
            // プレイヤーをクールダウンリストに追加
            cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
        }
    }
    return true;
}

public boolean isTeleporting() {
    // 実行中/保留中のすべてのタスクをループ
    for (BukkitTask t : Bukkit.getScheduler().getPendingTasks()) {
        // リスト内のtaskIdを見つけようとする
        if (t.getTaskId() == taskId) {
            // タスクIDが見つかった場合はtrueを返す
            return true;
        }
    }
    // タスクIDが見つからなかった場合はfalseを返す
    return false;
}

@EventHandler
public void onPlayerMove(PlayerMoveEvent e) {
    // 移動イベントをトリガーしているプレイヤーが現在テレポートしようとしているプレイヤーであることを確認
    if (e.getPlayer() == p) {
        // プレイヤーのために作成された実行中の遅延タスクを取得
        if (isTeleporting()) {
            // プレイヤーがテレポートを開始した位置から移動したかを確認
            if (e.getPlayer().getLocation().getX() != locale.getX() || e.getPlayer().getLocation().getY() != locale.getY()) {
                // configの変数を確認して、タスクのキャンセルを続けるか、プレイヤーにバイパス権限があるかを確認
                if (cancelOnMove && !e.getPlayer().hasPermission("homes.config_bypass")) {
                    // タスクをキャンセル
                    pl.cancelTask(taskId);
                    // テレポートがキャンセルされたことを通知
                    ChatUtils.sendInfo(e.getPlayer(), pl.getConfig().getString("tp-cancelOnMove-msg"));
                    // テレポート遅延中にプレイヤーが移動した場合にスネア音を再生
                    e.getPlayer().playNote(e.getPlayer().getLocation(), Instrument.SNARE_DRUM, Note.natural(0, Note.Tone.F));
                }
            }
        }
    }
}
