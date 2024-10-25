package com.samleighton.xquiset.sethomes;

import com.samleighton.xquiset.sethomes.commands.*;
import com.samleighton.xquiset.sethomes.configurations.Database;
import com.samleighton.xquiset.sethomes.configurations.Homes;
import com.samleighton.xquiset.sethomes.configurations.WorldBlacklist;
import com.samleighton.xquiset.sethomes.database.MySQLConnector;
import com.samleighton.xquiset.sethomes.eventListeners.EventListener;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * @author Xquiset
 * @version 1.3.1
 */
public class SetHomes extends JavaPlugin {

    public FileConfiguration config;
    private FileConfiguration homesCfg;
    private Permission vaultPerms = null;
    private LuckPerms luckPermsApi = null;
    private MySQLConnector dbConnector = null;
    private final WorldBlacklist blacklist = new WorldBlacklist(this);
    private final Homes homes = new Homes(this);
    private final Database db = new Database(this);
    public final String LOG_PREFIX = "[SetHomes] ";
    private final String configHeader = StringUtils.repeat("-", 26)
            + "\n\tSetHomesの設定\t\n" + StringUtils.repeat("-", 26) + "\n"
            + "メッセージ: \n\tメッセージでチャットカラーを使用するには、この記号 § を使用します。\n"
            + "\t例えば: §b はその後のテキストをアクアブルーに変更します。\n"
            + "\t色コードはここで見つけられます https://www.digminecraft.com/lists/color_list_pc.php\n"
            + "時間: \n\t任意の時間値は秒単位です。\n"
            + "注意すべきこと: \n\t任意の整数オプションを0に設定すると無視されます。\n"
            + "\tmax-homesはデフォルトの名前のないホームには含まれません。\n"
            + "\tクールダウンメッセージ内の秒数変数には%sを使用します。\n";

    @Override
    public void onEnable() {
        // LuckPermsの設定を試みる
        if (!setupLuckPerms()) {
            // LuckPermsがない場合はVaultの権限を設定
            if (!setupVaultPermissions()) {
                Bukkit.getServer().getLogger().log(Level.WARNING, LOG_PREFIX + "権限プラグインへの接続に失敗しました！設定 \"max-homes\" は無視されます！");
            }
        }

        // 有効化またはリロード時に設定ファイルを読み込む
        loadConfigurationFiles();
        // コマンド実行者を初期化
        registerCommands();
        // イベントリスナーを登録
        new EventListener(this);

        // 設定から有効化されている場合はデータベース接続を登録
        if (isDBEnabled()) {
            registerDB();
        }
    }

    @Override
    public void onDisable() {
        // 既存のMySQL接続を閉じる
        if (dbConnector != null) {
            dbConnector.closeConnection();
        }
    }

    /**
     * 設定ファイルが存在する場合は読み込み、
     * 存在しない場合はデフォルト設定で作成します。
     */
    private void loadConfigurationFiles() {
        // 設定を取得
        homesCfg = getHomes().getConfig();
        FileConfiguration blacklistCfg = getBlacklist().getConfig();
        FileConfiguration dbCfg = getDb().getConfig();

        // ブラックリストのデフォルト設定パスを確立
        if (!(blacklistCfg.isSet("blacklisted_worlds"))) {
            blacklistCfg.addDefault("blacklisted_worlds", new ArrayList<String>());
        }

        // デフォルトを保存
        blacklistCfg.options().copyDefaults(true);
        getBlacklist().save();

        // ホームのデフォルトパスを確立
        if (!(homesCfg.isSet("allNamedHomes") || homesCfg.isSet("unknownHomes"))) {
            homesCfg.addDefault("allNamedHomes", new HashMap<String, HashMap<String, Home>>());
            homesCfg.addDefault("unknownHomes", new HashMap<String, Home>());
        }

        // デフォルトを保存
        homesCfg.options().copyDefaults(true);
        getHomes().save();

        // データベースのデフォルトパスを確立
        dbCfg.addDefault("enabled", false);
        dbCfg.addDefault("host", "localhost");
        dbCfg.addDefault("database", "sethomes");
        dbCfg.addDefault("username", "root");
        dbCfg.addDefault("password", "root");
        dbCfg.addDefault("port", 3306);
        dbCfg.addDefault("db_prefix", "mc_");

        // データベースのデフォルトを保存
        dbCfg.options().copyDefaults(true);
        getDb().save();

        // 古い設定からホームをコピーし、デフォルト設定から削除
        config = getConfig();
        copyHomes(config, getHomes());

        // 設定のデフォルトを設定
        if (!config.isSet("max-homes") || !config.isSet("max-homes-msg") || !config.isSet("tp-delay")
                || !config.isSet("tp-cooldown") || !config.isSet("tp-cancelOnMove")
                || !config.isSet("tp-cancelOnMove-msg") || !config.isSet("tp-cooldown-msg")
                || !config.isSet("auto-update")) {
            // デフォルトでは最大ホームを無制限に設定

            if (!config.isSet("max-homes")) {
                config.set("max-homes.default", 0);
            }
            if (!config.isSet("max-homes-msg")) {
                config.set("max-homes-msg", "§4保存されたホームの最大数に達しました！");
            }
            if (!config.isSet("tp-delay")) {
                config.set("tp-delay", 3);
            }
            if (!config.isSet("tp-cooldown")) {
                config.set("tp-cooldown", 0);
            }
            if (!config.isSet("tp-cancelOnMove")) {
                config.set("tp-cancelOnMove", false);
            }
            if (!config.isSet("tp-cancelOnMove-msg")) {
                config.set("tp-cancelOnMove-msg", "§4動きが検出されました！テレポートがキャンセルされました！");
            }
            if (!config.isSet("tp-cooldown-msg")) {
                config.set("tp-cooldown-msg", "§4テレポートする前にあと%s秒待つ必要があります！");
            }
        }

        if (config.isSet("max-homes")) {
            if (config.getInt("max-homes") != 0) {
                int maxHomes = config.getInt("max-homes");
                Bukkit.getServer().getLogger().log(Level.WARNING, "[SetHomes] config.yml内で以前に最大ホームを設定したことが検出されました。設定が更新されましたので、\n" +
                        "プラグインページで権限グループの正しい設定方法を確認することをお勧めします: https://dev.bukkit.org/projects/set-homes");
                config.set("max-homes.default", maxHomes);
            }
        }

        config.options().header(configHeader);
        config.options().copyDefaults(true);
        saveConfig();

        getHomes().reloadConfig();
        getBlacklist().reloadConfig();
        getDb().reloadConfig();
    }

    /**
     * コマンドクラスを登録し、
     * これらのコマンドの実行を処理します。
     */
    private void registerCommands() {
        Objects.requireNonNull(this.getCommand("sethome")).setExecutor(new SetHome(this));
        Objects.requireNonNull(this.getCommand("homes")).setExecutor(new ListHomes(this));
        Objects.requireNonNull(this.getCommand("delhome")).setExecutor(new DeleteHome(this));
        Objects.requireNonNull(this.getCommand("home")).setExecutor(new GoHome(this));
        Objects.requireNonNull(this.getCommand("strike")).setExecutor(new Strike(this));
        Objects.requireNonNull(this.getCommand("blacklist")).setExecutor(new Blacklist(this));
        Objects.requireNonNull(this.getCommand("home-of")).setExecutor(new GoHome(this));
        Objects.requireNonNull(this.getCommand("delhome-of")).setExecutor(new DeleteHome(this));
        Objects.requireNonNull(this.getCommand("uhome")).setExecutor(new UpdateHome(this));
        Objects.requireNonNull(this.getCommand("uhome-of")).setExecutor(new UpdateHome(this));
        Objects.requireNonNull(this.getCommand("setmax")).setExecutor(new SetMaxHomes(this));
    }

    /**
     * プラグイン用のMySQLConnectorオブジェクトを作成します。
     */
    private void registerDB() {
        this.dbConnector = new MySQLConnector(this);
    }

    /**
     * VaultAPIを使用して権限サービスを初期化します。
     *
     * @return Vaultがセットアップされた場合はtrue、それ以外の場合はfalse
     */
    private boolean setupVaultPermissions() {
        try {
            // サービスプロバイダーを取得しようとします。
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);

            // サービスプロバイダーが正常に取得されました。
            if (rsp != null) {
                vaultPerms = rsp.getProvider();
                Bukkit.getServer().getLogger().info(LOG_PREFIX + "Vaultに接続しました！");
                return true;
            }
        } catch (NoClassDefFoundError ignored) {
            Bukkit.getServer().getLogger().info(LOG_PREFIX + "Vaultが見つかりませんでした。");
        }
        return false;
    }

    /**
     * LuckPermsAPIを使用して権限サービスを初期化します。
     *
     * @return LuckPermsがセットアップされた場合はtrue、それ以外の場合はfalse
     */
    private boolean setupLuckPerms() {
        try {
            // サービスプロバイダーを取得しようとします。
            RegisteredServiceProvider<LuckPerms> rsp = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

            // サービスプロバイダーが正常に取得されました。
            if (rsp != null) {
                luckPermsApi = rsp.getProvider();
                Bukkit.getServer().getLogger().info(LOG_PREFIX + "LuckPermsに接続しました！");
                return true;
            }
        } catch (NoClassDefFoundError ignored) {
            Bukkit.getServer().getLogger().info(LOG_PREFIX + "LuckPermsが見つかりませんでした！Vaultに戻ります...");
        }

        return false;
    }

    /**
     * 特定のプレイヤーUUIDのためにホームのマップを作成します。
     *
     * @param uuid ホームを取得しようとしているプレイヤーのUUID
     * @return プレイヤーの全てのホームのハッシュマップ
     */
    public HashMap<String, Home> getPlayersNamedHomes(String uuid) {
        HashMap<String, Home> playersNamedHomes = new HashMap<>();

        // データベースまたはファイルに保存しているか確認
        if (isDBEnabled()) {
            try {
                ResultSet rs = getDbConnector().executeSQL(
                        String.format(
                                "SELECT * FROM `%ssh_homes` sh JOIN `%ssh_users` su ON sh.uuid = su.id WHERE su.uuid = '%s' AND sh.home_name != ''",
                                getDbConnector().getPrefix(),
                                getDbConnector().getPrefix(),
                                uuid
                        )
                );

                if (rs != null) {
                    while (rs.next()) {
                        Home home = new Home(
                                rs.getString("home_name"),
                                rs.getString("world"),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z"),
                                rs.getFloat("pitch"),
                                rs.getFloat("yaw"),
                                rs.getString("home_desc")
                        );

                        playersNamedHomes.put(home.getHomeName(), home);
                    }
                }
            } catch (SQLException e) {
                printDBError(e);
            } finally {
                getDbConnector().close();
            }
        } else {
            String homesPath = "allNamedHomes." + uuid;
            homesCfg = getHomes().getConfig();

            // プレイヤーのホームリストをループして、ホーム名をキー、ホームを値とするハッシュマップを作成
            for (String id : Objects.requireNonNull(homesCfg.getConfigurationSection(homesPath)).getKeys(false)) {
                String path = homesPath + "." + id + ".";

                // ホームオブジェクトを作成して、説明を追加できるようにする
                Location home = getHomeLocaleFromConfig(path);
                Home h = new Home(home);

                // 説明が設定されているか確認
                if (homesCfg.isSet(path + ".desc")) {
                    h.setDesc(homesCfg.getString(path + ".desc"));
                }

                playersNamedHomes.put(id, h);
            }
        }

        return playersNamedHomes;
    }

    /**
     * プレイヤーのホームマップから、指定されたホームをロケーションとして取得します。
     *
     * @param uuid     ホームを取得するプレイヤーのUUID
     * @param homeName 作成するホームの名前
     * @return プレイヤーの指定されたホームのロケーション
     */
    public Location getNamedHomeLocal(String uuid, String homeName) {
        Home h = getPlayersNamedHomes(uuid).get(homeName);
        return h.toLocation();
    }

    /**
     * 設定からグループの最大ホーム数を取得します。
     *
     * @return グループをキー、最大ホーム数を値とするハッシュマップ
     */
    public HashMap<String, Integer> getMaxHomes() {
        HashMap<String, Integer> maxHomes = new HashMap<>();
        String maxHomesPath = "max-homes";

        for (String id : Objects.requireNonNull(config.getConfigurationSection(maxHomesPath)).getKeys(false)) {
            maxHomes.put(id, config.getInt(maxHomesPath + "." + id));
        }

        return maxHomes;
    }

    /**
     * @param uuid ホームが設定されているか確認するプレイヤーのUUID
     * @return true または false
     */
    public boolean hasNamedHomes(String uuid) {
        boolean hasHomes = false;

        // データベースまたはファイルに保存しているか確認
        if (isDBConnected()) {
            try {
                String sql = String.format(
                        "SELECT COUNT(*) AS `home_num` FROM `%ssh_homes` sh JOIN `%ssh_users` su ON sh.uuid = su.id WHERE su.uuid = '%s' AND sh.home_name != ''",
                        getDbConnector().getPrefix(),
                        getDbConnector().getPrefix(),
                        uuid
                );
                // このUUIDに属する行をデータベースからクエリします。
                ResultSet rs = getDbConnector().executeSQL(sql);

                if (rs != null) {
                    while (rs.next()) {
                        hasHomes = rs.getInt("home_num") > 0;
                    }
                }
            } catch (SQLException e) {
                // データベースエラーを表示
                printDBError(e);
            } finally {
                getDbConnector().close();
            }
        } else {
            // ホームの設定を取得
            homesCfg = getHomes().getConfig();
            // このUUIDに名前のあるホームが設定されているか確認
            hasHomes = homesCfg.contains("allNamedHomes." + uuid) && homesCfg.isSet("allNamedHomes." + uuid);
        }

        return hasHomes;
    }

    /**
     * 名前のあるホームを設定に保存します。
     *
     * @param uuid プレイヤーのUUID
     * @param home 保存するホームオブジェクト
     */
    public void saveNamedHome(String uuid, Home home) {
        if (isDBConnected()) {
            int userID = getDbConnector().getPlayerID(uuid);
            String sql = String.format(
                    "INSERT INTO `%ssh_homes` (uuid, home_name, world, x, y, z, pitch, yaw, home_desc) " +
                            "VALUES (%d, '%s', '%s', %f, %f, %f, %f, %f, '%s')",
                    getDbConnector().getPrefix(),
                    userID,
                    home.getHomeName() == null ? "" : home.getHomeName(),
                    home.getWorld(),
                    home.getX(),
                    home.getY(),
                    home.getZ(),
                    home.getPitch(),
                    home.getYaw(),
                    home.getDesc() == null ? "" : home.getDesc()
            );
            getDbConnector().executeSQL(sql);
            getDbConnector().close();
        } else {
            String path = "allNamedHomes." + uuid + "." + home.getHomeName();
            saveHomeToConfig(home, path);
            homesCfg.set(path + ".desc", home.getDesc());
            getHomes().save();
        }
    }

    /**
     * プレイヤーの名前付きホームを設定から削除するために使用されます。
     *
     * @param uuid プレイヤーのUUID
     * @param homeName 削除するホームの名前
     */
    public void deleteNamedHome(String uuid, String homeName) {
        if (isDBConnected()) {
            int userID = getDbConnector().getPlayerID(uuid);
            String sql = String.format("DELETE FROM `%ssh_homes` WHERE uuid = %d AND home_name LIKE '%s'", getDbConnector().getPrefix(), userID, homeName);
            getDbConnector().executeSQL(sql);
            getDbConnector().close();
        } else {
            String path = "allNamedHomes." + uuid + "." + homeName;
            getHomes().getConfig().set(path, null);
            getHomes().save();
            getHomes().reloadConfig();
        }
    }

    /**
     * 名前のないホームの位置オブジェクトを取得するために使用されます。
     *
     * @param uuid ホームを取得するプレイヤーのUUID
     * @return プレイヤーをテレポートするホーム
     */
    public Location getPlayersUnnamedHome(String uuid) {
        Location homeLocation = null;
        if (isDBConnected()) {
            try {
                String sql = String.format(
                        "SELECT * FROM `%ssh_homes` sh JOIN `%ssh_users` su ON sh.uuid = su.id WHERE su.uuid = '%s' AND sh.home_name = '' AND sh.home_desc = ''",
                        getDbConnector().getPrefix(),
                        getDbConnector().getPrefix(),
                        uuid
                );
                ResultSet rs = getDbConnector().executeSQL(sql);

                if (rs != null) {
                    while (rs.next()) {
                        homeLocation = new Location(
                                Bukkit.getWorld(rs.getString("world")),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z"),
                                rs.getFloat("yaw"),
                                rs.getFloat("pitch")
                        );
                    }
                }
            } catch (SQLException e) {
                printDBError(e);
            } finally {
                getDbConnector().close();
            }
        } else {
            // 設定ファイルからすべてのデータを取得
            String path = "unknownHomes." + uuid;
            homesCfg = getHomes().getConfig();
            homeLocation = getHomeLocaleFromConfig(path);
        }

        // ホームを位置として返す
        return homeLocation;
    }

    /**
     * プレイヤーが名前のないホームを持っているかどうかを確認するために使用されます。
     *
     * @param uuid 名前のないホームを確認するプレイヤーのUUID
     * @return true || false
     */
    public boolean hasUnknownHomes(String uuid) {
        boolean hasHomes = false;
        if (isDBConnected()) {
            try {
                String sql = String.format(
                        "SELECT COUNT(*) AS `home_num` FROM `%ssh_homes` sh JOIN `%ssh_users` su ON sh.uuid = su.id WHERE su.uuid = '%s' AND sh.home_name = '' AND sh.home_desc = ''",
                        getDbConnector().getPrefix(),
                        getDbConnector().getPrefix(),
                        uuid
                );
                // このUUIDに属する行があるかデータベースをクエリ
                ResultSet rs = getDbConnector().executeSQL(sql);

                if (rs != null) {
                    while (rs.next()) {
                        hasHomes = rs.getInt("home_num") > 0;
                    }
                }
            } catch (SQLException e) {
                // データベースエラーを表示
                printDBError(e);
            } finally {
                getDbConnector().close();
            }
        } else {
            homesCfg = getHomes().getConfig();
            hasHomes = homesCfg.contains("unknownHomes." + uuid);
        }

        return hasHomes;
    }

    /**
     * 名前のないホームを設定に保存するために使用されます。
     *
     * @param uuid ホームを保存するプレイヤーのUUID
     * @param home 保存するホーム
     */
    public void saveUnknownHome(String uuid, Home home) {
        if (isDBConnected()) {
            saveNamedHome(uuid, home);
        } else {
            // ホームの位置を構築するための変数を設定ファイルに保存
            String path = "unknownHomes." + uuid;
            saveHomeToConfig(home, path);
            getHomes().save();
        }
    }

    /**
     * ホームオブジェクトを設定ファイルに保存するためのヘルパーメソッド
     *
     * @param home 保存するホームオブジェクト
     * @param path 保存先の設定のパス
     */
    private void saveHomeToConfig(Home home, String path) {
        homesCfg = getHomes().getConfig();
        homesCfg.set(path + ".world", home.getWorld());
        homesCfg.set(path + ".x", home.getX());
        homesCfg.set(path + ".y", home.getY());
        homesCfg.set(path + ".z", home.getZ());
        homesCfg.set(path + ".pitch", home.getPitch());
        homesCfg.set(path + ".yaw", home.getYaw());
    }

    /**
     * 設定ファイルからホームの位置を取得するためのヘルパーメソッド
     *
     * @param path ホームデータへのパス
     * @return ホームの位置オブジェクト
     */
    private Location getHomeLocaleFromConfig(String path) {
        World world = getServer().getWorld(Objects.requireNonNull(homesCfg.getString(path + ".world")));
        double x = homesCfg.getDouble(path + ".x");
        double y = homesCfg.getDouble(path + ".y");
        double z = homesCfg.getDouble(path + ".z");
        float pitch = Float.parseFloat(Objects.requireNonNull(homesCfg.getString(path + ".pitch")));
        float yaw = Float.parseFloat(Objects.requireNonNull(homesCfg.getString(path + ".yaw")));

        return new Location(world, x, y, z, pitch, yaw);
    }

    /**
     * 設定から名前のないホームを削除するために使用されます。
     *
     * @param uuid デフォルトホームを削除するプレイヤーのUUID
     */
    public void deleteUnknownHome(String uuid) {
        if (isDBConnected()) {
            int userID = getDbConnector().getPlayerID(uuid);
            String sql = String.format("DELETE FROM `%ssh_homes` WHERE uuid = %d AND home_name = '' AND home_desc = ''", getDbConnector().getPrefix(), userID);
            getDbConnector().executeSQL(sql);
            getDbConnector().close();
        } else {
            // プレイヤーのIDのパスをnullに設定
            String path = "unknownHomes." + uuid;
            getHomes().getConfig().set(path, null);
            getHomes().save();
            getHomes().reloadConfig();
        }
    }

    /**
     * WorldBlacklist設定ファイルを操作するために使用されます。
     *
     * @return WorldBlacklistオブジェクト
     */
    public WorldBlacklist getBlacklist() {
        return blacklist;
    }

    /**
     * ブラックリスト設定からワールド名を読み取るために使用されます。
     *
     * @return ワールド名のリスト
     */
    public List<String> getBlacklistedWorlds() {
        return getBlacklist().getConfig().getStringList("blacklisted_worlds");
    }

    /**
     * ホームを取得するために使用されます。
     *
     * @return Homesオブジェクト
     */
    public Homes getHomes() {
        return homes;
    }

    /**
     * デフォルトの設定から新しいホーム設定にホームをコピーするために使用されます。
     *
     * @param config 元の古い設定
     * @param homeConfig 新しいホーム設定
     */
    private void copyHomes(FileConfiguration config, Homes homeConfig) {
        if (config.contains("allNamedHomes")) {
            if (config.isSet("allNamedHomes")) {
                homeConfig.getConfig().set("allNamedHomes", config.get("allNamedHomes"));
                config.set("allNamedHomes", null);
                homeConfig.save();
                saveConfig();
            }
        }

        if (config.contains("unknownHomes")) {
            if (config.isSet("unknownHomes")) {
                homeConfig.getConfig().set("unknownHomes", config.get("unknownHomes"));
                config.set("unknownHomes", null);
                homeConfig.save();
                saveConfig();
            }
        }
    }

    /**
     * データベースの設定パラメータを取得するために使用されます。
     *
     * @return データベース設定オブジェクト
     */
    public Database getDb() {
        return db;
    }

    /**
     * データベース通信のためのMySQLConnectorオブジェクトを取得するために使用されます。
     *
     * @return MySQLConnectorオブジェクト
     */
    public MySQLConnector getDbConnector() {
        return this.dbConnector;
    }

    /**
     * 設定でデータベースサポートが有効になっているかどうかを判断するために使用されます。
     *
     * @return boolean | データベースサポートが有効な場合はtrue、そうでない場合はfalse
     */
    public boolean isDBEnabled() {
        return getDb().getConfig().getBoolean("enabled");
    }

    /**
     * DB接続が確立されているかどうかを確認します。
     *
     * @return MySQLConnectorオブジェクトがonEnable()で登録されている場合はtrue
     */
    public boolean isDBConnected() {
        return dbConnector != null;
    }

    /**
     * Bukkitのランナブルタスクをキャンセルするために使用されます。
     *
     * @param taskId キャンセルするタスクのID
     */
    public void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    /**
     * サーバーの権限を取得するために使用されます。
     *
     * @return 権限ハンドラ
     */
    public Permission getVaultPermissions() {
        return this.vaultPerms;
    }

    /**
     * LuckPerms APIのインスタンスを取得するために使用されます。
     *
     * @return LuckPermsのインスタンス
     */
    public LuckPerms getLuckPermsApi() {
        return this.luckPermsApi;
    }

    /**
     * 発生したデータベースエラーを表示するために使用されます。
     *
     * @param e このエラーを引き起こしたSQLExceptionオブジェクト
     */
    public void printDBError(SQLException e) {
        getLogger().log(Level.SEVERE, LOG_PREFIX + "SQLException: " + e.getMessage());
        getLogger().log(Level.SEVERE, LOG_PREFIX + "SQLState: " + e.getSQLState());
        getLogger().log(Level.SEVERE, LOG_PREFIX + "VendorError: " + e.getErrorCode());
    }
}
