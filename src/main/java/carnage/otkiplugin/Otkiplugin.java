package carnage.otkiplugin;

import carnage.otkiplugin.commands.ClassCommand;
import carnage.otkiplugin.gui.ClassGUI;
import carnage.otkiplugin.items.*;
import carnage.otkiplugin.listeners.ClassGUIListener;
import carnage.otkiplugin.listeners.classitem.*;
import carnage.otkiplugin.managers.ActionBarManager;
import carnage.otkiplugin.classes.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class Otkiplugin extends JavaPlugin {

    private static Otkiplugin plugin;
    private ActionBarManager actionBarManager;

    @Override
    public void onEnable() {
        plugin = this;

        // Initialize commands
        getCommand("class").setExecutor(new ClassCommand());

        // Register event listeners
        getServer().getPluginManager().registerEvents(new ClassGUIListener(), this);
        getServer().getPluginManager().registerEvents(new InfernoItemListener(), this);
        getServer().getPluginManager().registerEvents(new AetheriItemListener(), this);
        getServer().getPluginManager().registerEvents(new HydronItemListener(), this);
        getServer().getPluginManager().registerEvents(new CavernonItemListener(), this);
        getServer().getPluginManager().registerEvents(new TerrarorItemListener(), this);
        getServer().getPluginManager().registerEvents(new DraculoxItemListener(), this);

        // Initialize classes
        Inferno.init(this);
        getServer().getPluginManager().registerEvents(new Inferno(), this);
        Aetheri.init(this);
        getServer().getPluginManager().registerEvents(new Aetheri(), this);
        Hydron.init(this);
        getServer().getPluginManager().registerEvents(new Hydron(), this);
        Cavernon.init(this);
        getServer().getPluginManager().registerEvents(new Cavernon(), this);
        Terraror.init(this);
        getServer().getPluginManager().registerEvents(new Terraror(), this);
        Draculox.init(this);
        getServer().getPluginManager().registerEvents(new Draculox(), this);

        // Initialize classes items
        InfernoItem.init(this);
        AetheriItem.init(this);
        HydronItem.init(this);
        CavernonItem.init(this);
        TerrarorItem.init(this);
        DraculoxItem.init(this);

        // Initialize ActionBar manager
        actionBarManager = new ActionBarManager(this);

        getLogger().info("Otkiplugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Shutdown ActionBar manager
        if (actionBarManager != null) {
            actionBarManager.shutdown();
        }

        getLogger().info("Otkiplugin has been disabled!");
    }

    public static Otkiplugin getInstance() {
        return plugin;
    }
}
