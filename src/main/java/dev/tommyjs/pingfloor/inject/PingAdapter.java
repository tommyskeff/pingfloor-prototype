package dev.tommyjs.pingfloor.inject;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import dev.tommyjs.pingfloor.config.MutablePingConfig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PingAdapter extends PacketAdapter {

    private static final List<PacketType> EXCLUDED = Arrays.asList(
            PacketType.Play.Client.KEEP_ALIVE,
            PacketType.Play.Server.KEEP_ALIVE,
            PacketType.Play.Client.CHAT_COMMAND,
            PacketType.Play.Client.CLIENT_COMMAND,
            PacketType.Play.Client.TAB_COMPLETE,
            PacketType.Play.Client.CHAT
    );

    private final ScheduledExecutorService service;
    private final MutablePingConfig config;

    protected PingAdapter(Plugin plugin, MutablePingConfig config, Iterable<PacketType> packets, ScheduledExecutorService service) {
        super(plugin, packets);
        this.plugin = plugin;
        this.config = config;
        this.service = service;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        handle(event, delay -> {
            ProtocolLibrary.getProtocolManager().receiveClientPacket(event.getPlayer(), event.getPacket(), false);
        });
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        handle(event, delay -> {
            ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), event.getPacket(), false);
        });
    }
    public void handle(PacketEvent event, Consumer<Integer> runner) {
        if (!config.isEnabled() || EXCLUDED.contains(event.getPacketType())) {
            return;
        }

        Player player = event.getPlayer();
        if (event.isPlayerTemporary() || !player.isOnline()) {
            return;
        }

        int latency = getLatency(player);
        int delay = Math.max(config.getThreshold() / 2 - latency, 0);

        service.schedule(() -> runner.accept(delay), delay, TimeUnit.MILLISECONDS);
        event.setCancelled(true);
    }

    public int getLatency(Player player) {
        int latency;
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            latency = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            latency = 0;
        }

        return latency;
    }

    public static PingAdapter fromProtocolVersion(Plugin plugin, MutablePingConfig config) {
        int threads = Runtime.getRuntime().availableProcessors();
        return new PingAdapter(plugin, config, PacketType.values(), Executors.newScheduledThreadPool(threads));
    }

}
