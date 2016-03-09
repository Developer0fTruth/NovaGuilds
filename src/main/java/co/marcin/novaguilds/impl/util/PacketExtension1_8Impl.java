/*
 *     NovaGuilds - Bukkit plugin
 *     Copyright (C) 2016 Marcin (CTRL) Wieczorek
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package co.marcin.novaguilds.impl.util;

import co.marcin.novaguilds.api.util.packet.PacketExtension;
import co.marcin.novaguilds.event.PacketReceiveEvent;
import co.marcin.novaguilds.util.reflect.Reflections;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PacketExtension1_8Impl implements PacketExtension {
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
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void registerPlayer(final Player player) {
		try {
			Channel c = getChannel(player);
			ChannelHandler handler = new ChannelDuplexHandler() {
				@Override
				public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
					if(msg == null) {
						return;
					}

					super.write(ctx, msg, promise);
				}

				@Override
				public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
					try {
						if(msg == null) {
							return;
						}

						PacketReceiveEvent event = callEvent(new PacketReceiveEvent(msg, player));

						if(event.isCancelled() || event.getPacket() == null) {
							return;
						}
						super.channelRead(ctx, event.getPacket());
					}
					catch(Exception e) {
						super.channelRead(ctx, msg);
					}
				}
			};
			ChannelPipeline cp = c.pipeline();
			if(cp.names().contains("packet_handler")) {
				if(cp.names().contains("NovaGuilds")) {
					cp.replace("NovaGuilds", "NovaGuilds", handler);
				}
				else {
					cp.addBefore("packet_handler", "NovaGuilds", handler);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void unregisterChannel() {

	}
	
	private static <E extends Event> E callEvent(E event) {
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}
}