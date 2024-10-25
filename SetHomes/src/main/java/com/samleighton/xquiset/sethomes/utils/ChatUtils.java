package com.samleighton.xquiset.sethomes.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatUtils {
    /**
     * サーバー全体にメッセージを送信するために使用します
     *
     * @param msg, 送信するメッセージ
     */
    public static void broadcastMessage(String msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.YELLOW + msg);
        }
    }

    /**
     * コマンド送信者に「情報」形式のメッセージを送信するために使用します
     *
     * @param s,   CommandSenderオブジェクト
     * @param msg, 送信するメッセージ
     */
    public static void sendInfo(CommandSender s, String msg) {
        s.sendMessage(ChatColor.WHITE + msg);
    }

    /**
     * コマンド送信者に「エラー」形式のメッセージを送信するために使用します
     *
     * @param s,   CommandSenderオブジェクト
     * @param msg, 送信するメッセージ
     */
    public static void sendError(CommandSender s, String msg) {
        s.sendMessage(ChatColor.DARK_RED + msg);
    }

    /**
     * コマンド送信者に「成功」形式のメッセージを送信するために使用します
     *
     * @param s,   CommandSenderオブジェクト
     * @param msg, 送信するメッセージ
     */
    public static void sendSuccess(CommandSender s, String msg) {
        s.sendMessage(ChatColor.GOLD + msg);
    }

    /**
     * コマンド送信者に「プレイヤーではない」エラーメッセージを送信するために使用します
     *
     * @param s, CommandSenderオブジェクト
     */
    public static void notPlayerError(CommandSender s) {
        s.sendMessage(ChatColor.DARK_RED + "このコマンドを使用するにはプレイヤーである必要があります！");
    }

    /**
     * コマンド送信者に「権限がない」エラーメッセージを送信するために使用します
     *
     * @param s, CommandSenderオブジェクト
     */
    public static void permissionError(CommandSender s) {
        sendError(s, "その操作を行う権限がありません！");
    }

    /**
     * コマンド送信者に「引数が多すぎる」エラーメッセージを送信するために使用します
     *
     * @param s, CommandSenderオブジェクト
     */
    public static void tooManyArgs(CommandSender s) {
        sendError(s, "エラー: 引数が多すぎます！");
    }
