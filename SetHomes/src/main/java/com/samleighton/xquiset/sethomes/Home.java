package com.samleighton.xquiset.sethomes;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;

public class Home {

    // 家に必要なデータ
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;
    private String world;
    private String homeName;
    private String desc = null;

    // データを初期化
    public Home(Location l) {
        setWorld(Objects.requireNonNull(l.getWorld()).getName());
        setX(l.getX());
        setY(l.getY());
        setZ(l.getZ());
        setYaw(l.getYaw());
        setPitch(l.getPitch());
    }

    public Home(String homeName, String world, double x, double y, double z, float pitch, float yaw, String homeDesc) {
        setHomeName(homeName);
        setWorld(world);
        setX(x);
        setY(y);
        setZ(z);
        setPitch(pitch);
        setYaw(yaw);
        setDesc(homeDesc);
    }

    /**
     * @return world
     */
    public String getWorld() {
        return world;
    }

    /**
     * @param w 世界を設定
     */
    public void setWorld(String w) {
        this.world = w;
    }

    /**
     * @return pitch
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * @param pitch プレイヤーの家のピッチを設定
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * @return yaw
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * @param yaw プレイヤーの家のヨーを設定
     */
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    /**
     * @return z
     */
    public double getZ() {
        return z;
    }

    /**
     * @param z プレイヤーの家のZ値を設定
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * @return x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x プレイヤーの家のX値を設定
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y プレイヤーの家のY値を設定
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return 説明
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc プレイヤーの家の説明を設定
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * @return homeName
     */
    public String getHomeName() {
        return homeName;
    }

    /**
     * @param homeName プレイヤーが付けた家の名前を設定
     */
    public void setHomeName(String homeName) {
        this.homeName = homeName;
    }

    /**
     * @return 家をLocationオブジェクトとして返す
     */
    public Location toLocation() {
        return new Location(Bukkit.getServer().getWorld(this.getWorld()), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    /**
     * @return 家の情報を文字列として返す
     */
    public String toString() {
        return "Home Name: " + getHomeName() + "\n" +
                "Home Desc: " + getDesc() + "\n" +
                "Location: " + toLocation().toString() + "\n";
    }
}

