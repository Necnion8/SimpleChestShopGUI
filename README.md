# SimpleChestShopGUI
GUI操作でChestShopの店を作成できるBukkitプラグイン

![image](https://user-images.githubusercontent.com/62508399/205823598-96c068c3-cfe6-46d4-98a3-bc896ca158a6.png)

## 使い方
1. 看板に `newshop` を書いて設置
2. 表示されるGUIで、商品にするアイテムの選択 & 値段を変更
3. `ショップを作成！` ボタンで確定して完成 :+1:

※ `/editshop` コマンドを使えば、作成済みの店も編集できますよ :sunglasses:

## 前提
- Spigot 1.14 以上 (またはその派生)
- ChestShop v3.12 (または周辺でもOKかも)
- Vault (オプションですが、金額フォーマットに利用します)

## 設定
- 設定なし  (言語ファイルくらいは後々実装するかも･･･)

## コマンドと権限
- `simplechestshopgui.use` - ショップ作成GUIを使うための権限
- `simplechestshopgui.command.editshop` - `/editshop` コマンドの権限
- `simplechestshopgui.access-other` - 他人の編集を開くための運営用権限
- ※ ショップへのアクセス権限などはChestShopに依存します
