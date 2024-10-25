package com.samleighton.xquiset.sethomes.eventListeners;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.database.MySQLConnector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EventListener implements Listener {

    private SetHomes pl;

    // イベントリスナーを登録し、イベントの監視を開始する
    public EventListener(SetHomes plugin) {
        this.pl = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // プレイヤーがサーバーに参加したときのイベントハンドラー
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // データベースサポートが有効かどうかを確認
        if (pl.isDBConnected()) {
            // イベントを発生させたプレイヤーを取得
            Player p = event.getPlayer();
            // プレイヤーのユニークIDを取得
            String uuid = p.getUniqueId().toString();

            // プレイヤーがまだ登録されていない場合、プレイヤーズテーブルに追加する
            MySQLConnector dbConnector = pl.getDbConnector();
            dbConnector.executeSQL(String.format("INSERT IGNORE INTO %ssh_users (uuid) VALUES ('%s')", dbConnector.getPrefix(), uuid));
            dbConnector.close();
        }
    }

    // プレイヤーがブロックにインタラクトしたときのイベントハンドラー
    @EventHandler
    public void onPlayerInteractBlock(PlayerInteractEvent event) {
        // イベントを発生させたプレイヤーを取得
        Player p = event.getPlayer();
        // プレイヤーが手に持っているアイテムを取得
        ItemStack item = p.getInventory().getItemInMainHand();
        // アイテムのメタデータを取得
        ItemMeta itemMeta = item.getItemMeta();

        // プレイヤーが持っているアイテムが特定のロッドであれば、プレイヤーが見ている場所に雷を落とす
        if (itemMeta != null) {
            if (itemMeta.hasLocalizedName()) {
                if (itemMeta.getLocalizedName().equalsIgnoreCase("almighty")) {
                    p.getWorld().strikeLightning(p.getTargetBlock(null, 200).getLocation());
                }
            }
        }
    }
}
