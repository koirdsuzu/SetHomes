# SetHomes
様々なホームの作成、削除、テレポートが可能なシンプルなホームプラグインです。プレイヤーがホームを設定することを制限されるワールドのブラックリストをコントロールする機能があります。設定ファイルで、ホームの最大数、テレポートのクールダウン、テレポートの遅延、およびユーザーに表示されるそれぞれのメッセージなどの設定を制御することができます。

# Installation
ダウンロードしたjarファイルをサーバーのpluginsフォルダに入れるだけです。
**注意！"MAX HOMES "が動作するためには、以下のソフト依存のいずれかを満たし、それぞれのパーミッションプラグインのグループを設定する必要があります。また、config.ymlでmax-homesを設定する必要があります。max-homesのセットアップの例は、デフォルトのコンフィグ設定にあります。

# Soft Dependencies
- [LuckPerms](https://luckperms.net/download)
- [Vault plugin](https://dev.bukkit.org/projects/vault)、と同様に、Vaultでサポートされているパーミッションプラグインです。

# Commands
- **/sethome [ホーム名] [ホームの説明]** - このコマンドは、プレイヤーが選んだ名前と説明を持つホームを、プレイヤーの立っている場所に設定することを可能にします。名前と説明が与えられていない場合、あなたの立っている場所にデフォルトのホームが設定されます。
- **/home [ホームの名前]** - このコマンドは、ユーザーが選択した[ホームの名前]にテレポートさせます。ホーム名が選択されていない場合、**/sethome**で設定されたデフォルトのホームにテレポートされます。
- **/home-of [プレイヤー名] [ホーム名]** - このコマンドは、「homes.home-of」という権限があるプレイヤーが、任意のプレイヤーの設定したホームにテレポートすることを許可します。ホーム名が提供されない場合、希望する目的地としてデフォルトのホームにテレポートされます。
- **/delhome [ホーム名]** - このコマンドは、選択されたホームを削除します。ホーム名が選択されていない場合は、デフォルトのホームを削除します。
- **/delhome-of [PlayerName] [HomeName]** - このコマンドは、「homes.delhome-of」という権限があるプレイヤーが、選択したプレイヤーが設定したホームを削除することを許可します。ホーム名が選択されていない場合、削除したいホームとしてデフォルトのホームを削除します。
- **/uhome [HomeName] [HomeDescription]** - このコマンドは、「homes.uhome-of」という権限があるプレイヤーが、任意のプレイヤーのホームを更新することを許可します。ホーム名が選択されない場合、選択したプレイヤーのデフォルトのホームを更新します。
- **/uhome-of [PlayerName] [HomeName]** - このコマンドは、現在設定されているホームがある場合、すべてのプレイヤーをリストアップします。「home.gethomes」という権限があるプレイヤーは、PlayerNameパラメータを使用して、特定のプレイヤーのホームをリストアップすることができます。
- **/homes [PlayerName]** - このコマンドは、現在設定されているホームがある場合、すべてのプレーヤーをリストアップします。「home.gethomes」という権限があるプレイヤーは、PlayerNameパラメータを使用して、特定のプレイヤーのホームをリストアップすることができます。
- **/blacklist [Add/Remove] [WorldName]** - このコマンドは、ワールドのブラックリストを操作するために使用されます。もしワールド名がブラックリストにあれば、プレイヤーはそのワールドにホームを設定することができなくなります。/blacklistでブラックリストに登録されているワールドを表示できます。
- **/setmax [GroupName] [Amount]** - このコマンドは、「homes.setmax」という権限があるプレーヤーが、特定の権限グループに対して許可されるホームの最大数を設定することを許可します。例えば、デフォルトのパーミッショングループに4つのホームだけを許可したい場合、「/setmax default 4」コマンドを実行します。

# Permissions
- **homes.*** - A player given this permission will be allowed all commands under the Set Homes plugin
- **homes.home** - A player with this permission is allowed to teleport to named homes
- **homes.sethome** - A player with this permission is allowed to set named homes
- **homes.strike** - Give the power to others!
- **homes.blacklist_list** - Give the ability to list worlds currently in the blacklist
- **homes.blacklist_add** - Give the ability to add worlds to the blacklist
- **homes.blacklist_remove** - Give the ability to remove worlds from the blacklist
- **homes.gethomes** - Give the ability to list any players active homes
- **homes.home-of** - Give the ability to teleport to one of any players active homes
- **homes.delhome-of** - Give the ability to delete one of any players active homes
- **homes.config_bypass** - A player given this permission can set homes in blacklisted worlds, and doesn't have to wait for cooldown or teleport delays. They will also be able to exceed the max home limit.
- **homes.uhome** - Give the ability to update homes
- **homes.uhome-of** - Give the ability to update other players homes
- **homes.setmax** - Give the ability to set a maximum number of homes for a permission group

# Default Config
```yaml
# --------------------------
# 	SetHomes Config	
# --------------------------
# Messages: 
# 	You can use chat colors in messages with this symbol §.
# 	I.E: §b will change any text after it to an aqua blue color.
# 	Color codes may be found here https://www.digminecraft.com/lists/color_list_pc.php
# Time: 
# 	Any time value is based in seconds.
# Things to Note: 
# 	Set any integer option to 0 for it to be ignored.
# 	The max-homes does not include the default un-named home.
# 	Use %s as the seconds variable in the cooldown message.

max-homes:
  default: 0
max-homes-msg: §4You have reached the maximum amount of saved homes!
tp-delay: 3
tp-cooldown: 0
tp-cancelOnMove: false
tp-cancelOnMove-msg: §4Movement detected! Teleporting has been cancelled!
tp-cooldown-msg: §4You must wait another %s second(s) before teleporting!
auto-update: true
```

# Example Max-Homes setup
```yaml
max-homes:
  default: 1
  free: 3
  subscriber: 5
  admin: 0
```

# F.A.Q
- **Q: How can I give players permission to set named homes?**
  **A:** You will need to install a permission plugin, either [LuckPerms](https://luckperms.net/download) or [Vault](https://dev.bukkit.org/projects/vault) & a Vault supported permissions plugin then apply the permission "homes.sethome" to the (player/group) you would like to allow the usage of multiple homes for.
  
# Change Log
- Added support for Minecraft/Craftbukkit V1.16.3.
- Added support for LuckPerms permission plugin
- Made LuckPerms default permission plugin, and set Vault as a rollback before disabling all together
- Changed colors, and layout of list homes message to be more readable
- Fixed error, where SetHomes could not load without Vault
- Removed auto-updater functionality because it was not working properly
- Added server log messages for permissions plugin hooking, and no perms plugin found

# Donations
I work on this plugin with the little amount of free time that I have. Please feel free to donate via [PayPal](https://www.paypal.com/donate/?return=https://dev.bukkit.org/projects/312833&cn=Add+special+instructions+to+the+addon+author()&business=sam%40samleighton.us&bn=PP-DonationsBF:btn_donateCC_LG.gif:NonHosted&cancel_return=https://dev.bukkit.org/projects/312833&lc=US&item_name=Set+Homes+(from+bukkit.org)&cmd=_donations&rm=1&no_shipping=1&currency_code=USD) any amount you desire to show your support, and help me stay motivated to keep this project going. Thank You!
