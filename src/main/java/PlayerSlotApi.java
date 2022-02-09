import hooks.DragonCoreHook;
import hooks.GermPluginHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerSlotApi extends JavaPlugin {

    //依赖
    public GermPluginHook germPluginHook = null;
    public DragonCoreHook dragonCoreHook = null;

    @Override
    public void onEnable() {
        printInfo();
        loadHook();
    }

    @Override
    public void onDisable() {

    }

    private void printInfo(){
        Bukkit.getConsoleSender().sendMessage("§9==============================================================");
        Bukkit.getConsoleSender().sendMessage("§9Enabling PlayerSlotApi");
        Bukkit.getConsoleSender().sendMessage("§9==============================================================");
        Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f Loading...");
    }

    private void loadHook(){
        if (Bukkit.getPluginManager().getPlugin("GermPlugin") != null){
            //萌芽
            germPluginHook = new GermPluginHook();
            Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f 已加载GermPlugin作为前置!");
        }else if (Bukkit.getPluginManager().getPlugin("DragonCore") != null){
            //龙核
            dragonCoreHook = new DragonCoreHook();
            Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f 已加载DragonCore作为前置!");
        }else{
            //原版

            Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f 已选择原版模式!");
        }
    }
}
