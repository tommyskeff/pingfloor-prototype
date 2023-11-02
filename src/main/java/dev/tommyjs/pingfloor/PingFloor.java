package dev.tommyjs.pingfloor;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import dev.tommyjs.pingfloor.config.MutablePingConfig;
import dev.tommyjs.pingfloor.inject.PingAdapter;
import org.bukkit.plugin.java.JavaPlugin;

public class PingFloor extends JavaPlugin {

    private MutablePingConfig config;
    private PacketAdapter adapter;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        config = new MutablePingConfig();
        config.setEnabled(getConfig().getBoolean("enabled"));
        config.setThreshold(getConfig().getInt("threshold"));

        adapter = PingAdapter.fromProtocolVersion(this, config);

//        AsynchronousManager manager = ProtocolLibrary.getProtocolManager().getAsynchronousManager();
//        AsyncListenerHandler handler = manager.registerAsyncHandler(adapter);
//        handler.start();

        ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
    }

    @Override
    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(adapter);
    }

}
