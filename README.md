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
- **homes.*** - この権限を持つプレイヤーは Set Homes プラグインのすべてのコマンドが使用可能
- **homes.home** - この権限を持つプレイヤーは名前付きホームへテレポートできる
- **homes.sethome** - この権限を持つプレイヤーは名前付きホームを設定できる
- **homes.strike** - 他のプレイヤーに対して権限を与える権限
- **homes.blacklist_list** - 現在ブラックリストにあるワールドをリストできる権限
- **homes.blacklist_add** - ワールドをブラックリストに追加できる権限
- **homes.blacklist_remove** - ブラックリストからワールドを削除できる権限
- **homes.gethomes** - 任意のプレイヤーのアクティブなホームをリストできる権限
- **homes.home-of** - 任意のプレイヤーのアクティブなホームにテレポートできる権限
- **homes.delhome-of** - 任意のプレイヤーのアクティブなホームを削除できる権限
- **homes.config_bypass** - この権限を持つプレイヤーはブラックリストに登録されているワールドにホームを設定でき、クールダウンやテレポートの遅延を待たずに済む。また、最大ホーム数を超えることができる。
- **homes.uhome** - ホームを更新できる権限
- **homes.uhome-of** - 他のプレイヤーのホームを更新できる権限
- **homes.setmax** - 権限グループに最大ホーム数を設定できる権限

# Default Config
```yaml
# --------------------------
# 	SetHomes 設定	
# --------------------------
# メッセージ: 
# 	この記号 § を使ってメッセージ内でチャットカラーを設定できます。
# 	例: §b を入れるとその後のテキストがアクアブルーの色に変わります。
# 	カラーコードは https://www.digminecraft.com/lists/color_list_pc.php で確認できます。
# 時間: 
# 	すべての時間は秒単位です。
# 注意事項: 
# 	整数オプションを 0 に設定すると無視されます。
# 	max-homes はデフォルトの無名のホームを含みません。
# 	クールダウンメッセージ内で %s を秒数の変数として使用します。

max-homes:
  default: 0
max-homes-msg: §4保存できるホームの最大数に達しました！
tp-delay: 3
tp-cooldown: 0
tp-cancelOnMove: false
tp-cancelOnMove-msg: §4動作が検出されました！テレポートがキャンセルされました！
tp-cooldown-msg: §4テレポートするにはあと %s 秒お待ちください！
auto-update: true


# Example Max-Homes setup
```yaml
max-homes:
  default: 1
  free: 3
  subscriber: 5
  admin: 0
```

# F.A.Q
- **Q: プレイヤーに名前付きホームを設定する権限を与えるにはどうすれば良いですか？**
  **A:**[LuckPerms](https://luckperms.net/download)または[Vault](https://dev.bukkit.org/projects/vault) & Vault 対応の権限プラグインをインストールし、 "homes.sethome" 権限を付与したいプレイヤーやグループに適用してください。
  
# Change Log
 - Minecraft/Craftbukkit V1.16.3 のサポートを追加
 - LuckPerms 権限プラグインのサポートを追加
 - LuckPerms をデフォルトの権限プラグインに設定し、Vault はフェールオーバーとして設定
 - ホームリストメッセージの色とレイアウトをより読みやすく変更
 - Vault なしで SetHomes がロードできなかったエラーを修正
 - 正常に動作していなかったため、自動アップデート機能を削除
 - 権限プラグインのフックや、権限プラグインが見つからなかった場合のサーバーログメッセージを追加

# Donations
このプラグインは私の限られた自由時間で開発しています。もし支援の意思がある方は、[PayPal](https://www.paypal.com/donate/?return=https://dev.bukkit.org/projects/312833&cn=Add+special+instructions+to+the+addon+author()&business=sam%40samleighton.us&bn=PP-DonationsBF:btn_donateCC_LG.gif:NonHosted&cancel_return=https://dev.bukkit.org/projects/312833&lc=US&item_name=Set+Homes+(from+bukkit.org)&cmd=_donations&rm=1&no_shipping=1&currency_code=USD)経由で任意の金額を寄付してください。このプロジェクトの継続に向けた支援とモチベーション維持に大変感謝いたします。Thank You!
