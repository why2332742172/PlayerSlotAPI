package hooks;

import com.germ.germplugin.api.GermKeyAPI;

public class GermPluginHook {

    public GermKeyAPI germKeyAPI = null;

    public GermPluginHook(){
        this.germKeyAPI = new GermKeyAPI();
    }

}
