# PlayerSlotAPI

Bukkit 1.9 - 1.18.1 异步装备缓存、信息读取、装备修改、技能触发API

## Introduction

本API功能

- 实时捕获玩家装备槽位里物品的变化情况
  - 例如穿脱盔甲、切换主手物品、改变副手物品
  - 兼容原版六槽位萌芽/龙核和其它自定义槽位
- 提供装备信息的异步读取
  - 支持自定义信息类
- 提供装备异步修改和应用
- 下一步更新：触发器系统

## API Usage

- 获取公共/私有缓存管理器之一
- 公共缓存管理器会自动注册并监听原版六个槽位
- 私有缓存管理器只属于你的插件，它不会注册任何槽位

```java
PlayerCacheManager manager = PlayerSlotAPI.getPublicManager();
// 或者注册私有管理器
PlayerCacheManager myManager = PlayerSlotAPI.getPrivateManager();
```

- 对于私有管理器的原版槽位，或者自定义槽位，监听的槽位需要自行注册

```java
PlayerSlot slot = new DragonCoreSlot("宠物槽位");
manager.registerSlot(slot);
// 或者
myManager.registerVanillaEvents();
```
- 注册完毕以后，如果需要监听装备变动，只需监听两个事件

  - AsyncSlotUpdateEvent：异步装备更新事件，能确保装备准确更新
  - SlotUpdateEvent：同步装备更新事件，可以取消（取消对龙核萌芽无效）。
    - 如果新装备为null，代表插件无法判断新装备会是什么

- 如果需要获取缓存的槽位装备，可以直接

  ```java
  PlayerSlotCache cache = manager.getPlayerCache(player);
  DragonCoreSlot slot = new DragonCoreSlot("龙核属性槽位1")
  ItemStack item = cache.getCachedItem(slot);
  ```

- 如果需要从装备上读取信息，可以注册你自己的信息读取器

  ```java
  // 比如读取装备上的自定义属性
  public class Attribute{
      private final Map<String,Double> values = new ConcurrentHashMap();
      //...
  }
  // 随后注册dataReader
  manager.registerDataReader(Attribute.class, item->{
      Attribute newAttribute = new Attribute();
      if(hasLore(item,"无敌宝石")){
          newAttribute.add("物理伤害",100000);
      }
      return newAttribute;
  });
  // 框架将自动从槽位物品读取信息
  // 然后在需要的时候
  PlayerSlotCache cache = manager.getPlayerCache(player);
  Attribute handAttribute = cache.getCachedData(VanillaEquipSlot.MAINHAND, Attribute.class);
  // 需要判空，为空表明没读到
  if(handAttribute != null){
      event.setDamage(handAttribute.get("物理伤害")+event.getDamage());
  }
  ```

  - 对于此处属性案例而言，框架没有提供将槽位信息融合的方法，相对不方便
  - 你可以不使用这里的信息读取器，改为自己监听AsyncSlotUpdateEvent，自己做异步融合
  - 但是考虑到信息都已经预读好，而遍历几个槽位的时间代价远远低于从物品上getMeta、getLore的代价，在这里遍历可以接受

- 如果要异步修改装备：

  ```java
  // 想要修改装备，必须先调用getModifiedItem
  ItemStack toModify = cache.getModifiedItem(slot);
  toModify.setUnbreakable(true);
  cache.setModifiedItem(toModify);
  // 上述流程可以反复进行, 最后应用槽位更改。 如果想要验证时忽略耐久就传入true
  cache.applyModification(false);
  // 修改不一定能成功，如果用户在异步操作期间改变了他的装备
  ```

- 另外，插件支持直接存取萌芽、龙核装备

  ```java
  DragonCoreSlot slot = new DragonCoreSlot("宠物槽位");
  slot.get(player, item->{
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
public void setMySQLItem(Player player, String 槽位名, ItemStack item){
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
```

