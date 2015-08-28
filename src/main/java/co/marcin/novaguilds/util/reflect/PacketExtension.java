package co.marcin.novaguilds.util.reflect;

import co.marcin.novaguilds.event.PacketReceiveEvent;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelPipeline;
import net.minecraft.util.io.netty.channel.ChannelPromise;
import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PacketExtension {
	private static Reflections.FieldAccessor<Channel> clientChannel;
	private static Field playerConnection;
	private static Field networkManager;
	private static Method handleMethod;

	static {
		try {
			clientChannel = Reflections.getField(Reflections.getCraftClass("NetworkManager"), Channel.class, 0);
			playerConnection = Reflections.getField(Reflections.getCraftClass("EntityPlayer"), "playerConnection");
			networkManager = Reflections.getField(Reflections.getCraftClass("PlayerConnection"), "networkManager");
			handleMethod = Reflections.getMethod(Reflections.getBukkitClass("entity.CraftEntity"), "getHandle");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static Channel getChannel(Player p) {
		try {
			Object eP = handleMethod.invoke(p);
			return clientChannel.get(networkManager.get(playerConnection.get(eP)));
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static void registerPlayer(final Player p) {
		Channel c = getChannel(p);
		ChannelHandler handler = new ChannelDuplexHandler() {
//			@Override
//			public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception{
//				PacketSendEvent event = new PacketSendEvent(msg, p);
//				Bukkit.getPluginManager().callEvent(event);
//				if(event.isCancelled() || event.getPacket() == null) {
//					return;
//				}
//
//				super.write(ctx, event.getPacket(), promise);
//			}

			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				PacketReceiveEvent event = new PacketReceiveEvent(msg, p);
				Bukkit.getPluginManager().callEvent(event);
				if(event.isCancelled() || event.getPacket() == null) {
					return;
				}

				super.channelRead(ctx, event.getPacket());
			}

		};
		ChannelPipeline cp = c.pipeline();
		cp.addBefore("packet_handler", "NovaGuilds", handler);
	}

	public static void unregisterNovaGuildsChannel() {
		for(Player player : Bukkit.getOnlinePlayers()){
			getChannel(player).pipeline().remove("NovaGuilds");
		}
	}

}