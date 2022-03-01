# PlayerSlotAPI

Bukkit 1.9 - 1.18.1 异步装备缓存、信息读取、装备修改、技能触发API

## Introduction

本API功能

- 提供信息修改事件，实时捕获玩家装备槽位里物品的变化情况
  - 例如穿脱盔甲、切换主手物品、改变副手物品
  - 兼容原版六槽位萌芽/龙核和其它自定义槽位
- 提供装备异步修改和应用

## API Usage

- 将本API打包到你的插件内并relocate
- 获取API

```java
PlayerSlotAPI slotApi = PlayerSlotAPI.getAPI();
```

- 注册原版槽位

```
slotApi.registerVanilla();
```

- 注册龙核/萌芽槽位

```java
PlayerSlot petSlot = new DragonCoreSlot("宠物槽位");
manager.registerSlot(petSlot);
```
- 注册完毕以后，如果需要监听装备变动，只需监听两个事件

  - AsyncSlotUpdateEvent：**推荐**，异步装备更新事件，能确保装备准确更新
  - SlotUpdateEvent：同步装备更新事件，可以取消（取消对龙核萌芽无效）
    - 如果新装备为null，代表插件无法判断新装备会是什么

- 如果需要获取缓存的槽位装备，可以直接

  ```java
  PlayerSlotCache cache = slotApi.getSlotCache(player);
  ItemStack item = cache.getItem(petSlot);
  ```

- 如果要异步修改装备：

  ```java
  // 想要修改装备，必须先调用getModifiedItem
  ItemStack toModify = cache.getModifiedItem(slot);
  // 修改装备，比如把它变得无法破坏
  toModify.setUnbreakable(true);
  cache.setModifiedItem(toModify);
  // 上述流程可以反复进行, 最后应用槽位更改。 如果想要验证时忽略耐久就传入true
  cache.applyModification(false);
  // 修改不一定能成功，如果用户在异步操作期间改变了他的装备，修改就不会生效(防止刷物品)
  ```
  
- 另外，插件支持直接存取萌芽、龙核装备

  ```java
  petSlot.get(player, item->{
  	if(item==null){
  		// 获取失败
  		return;
  	}
  	Bukkit.runTask()->{
  		player.getInventory().addItem(item);
  	}
  })
  ```
  
- 高级用法：扩展本框架，读取其它储存的槽位

```java
public class MySQLSlot extends PlayerSlot{
    private String 槽位名;
    private static SQLDatasource sql = 你的数据源;
	// 实现 get set 方法
	public void get(Player player, Consumer<ItemStack> callback){
		// 获取物品
		// 我建议如果是MySQL或者Yml槽位的话这里用异步
		if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchroniously(MyPlugin.getInstance(),()->{
                ItemStack item = sql.get(player, 槽位名);
                callback.accept(item);
            });
		}else{
            ItemStack item = sql.get(player, 槽位名);
            callback.accept(item);
        }
	}
	public void set(Player player, ItemStack item, Consumer<Boolean> callback){
		// 设置物品
        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchroniously(MyPlugin.getInstance(),()->{
                bollean success = sql.set(player, 槽位名, item);
                callback.accept(success);
            });
		}else{
            bollean success = sql.set(player, 槽位名, item);
            callback.accept(success);
        }
	}
    
    // 实现AsyncSafe方法, 告知框架本slot可以异步设置和读取
    public boolean isAsyncSafe(){
        return true;
    }
}
// 由于是你自己的槽位，你应该在自己设置的时候callEvent
// 在你的设置API处callEvent
public class SQLDatasourcre{
    public void set(Player player, String 槽位名, ItemStack item){
        // 加入下列代码，让框架帮你缓存物品
        SlotUpdateEvent event = new SlotUpdateEvent(UpdateTrigger.CUSTOM, player, new MySQLSlot(槽位名), null, item);
        event.setUpdateImmediately();
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()){
            return;
        }
        // ...
        sql.setItem(player,槽位名,item);
    }
}
```

