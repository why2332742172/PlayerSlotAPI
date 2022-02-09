package hooks;

import eos.moe.dragoncore.DragonCore;

public class DragonCoreHook {

    public DragonCore instance = null;

    public DragonCoreHook() {
        this.instance = DragonCore.getInstance();
    }

}
