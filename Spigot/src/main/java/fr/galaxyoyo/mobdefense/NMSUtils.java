package fr.galaxyoyo.mobdefense;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import fr.galaxyoyo.mobdefense.events.GameStartedEvent;
import fr.galaxyoyo.mobdefense.events.GameStoppedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static fr.galaxyoyo.spigot.nbtapi.ReflectionUtils.invokeBukkitMethod;
import static fr.galaxyoyo.spigot.nbtapi.ReflectionUtils.invokeNMSMethod;

public class NMSUtils
{
	private static ServerVersion VERSION;

	static
	{
		String version = Bukkit.getServer().getClass().getName().split("\\.")[3];
		try
		{
			VERSION = ServerVersion.valueOf(version);
		}
		catch (IllegalArgumentException ex)
		{
			throw new UnsupportedClassVersionError(version);
		}
	}

	public static void addEntityNameProtocolLibListener(Villager villager, int type)
	{
		PacketListener listener = new PacketAdapter(MobDefense.instance(), PacketType.Play.Client.SETTINGS)
		{
			@Override
			public void onPacketReceiving(PacketEvent event)
			{
				try
				{
					PacketContainer pkt = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
					pkt.getIntegers().write(0, villager.getEntityId());
					WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(villager);
					Messages msgs = Messages.getMessages(event.getPacket() == null ? event.getPlayer().spigot().getLocale() : event.getPacket().getStrings().read(0));
					String name = type == 2 ? msgs.getNpcExchangeName() : type == 1 ? msgs.getNpcUpgradesName() : msgs.getNpcTowerName();
					watcher.setObject(2, name);
					pkt.getWatchableCollectionModifier().write(0, Lists.newArrayList(watcher));
					ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), pkt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};

		ProtocolLibrary.getProtocolManager().addPacketListener(listener);

		Bukkit.getPluginManager().registerEvents(new Listener()
		{
			private boolean unregistered = false;

			@EventHandler
			public void onGameEnded(GameStoppedEvent event)
			{
				if (unregistered)
					return;

				unregistered = true;

				ProtocolLibrary.getProtocolManager().removePacketListener(listener);
			}

			@EventHandler
			public void onGameStarted(GameStartedEvent event)
			{
				if (unregistered)
					return;

				Bukkit.getOnlinePlayers().forEach(player -> listener.onPacketReceiving(PacketEvent.fromClient(player, null, null, player)));
			}
		}, MobDefense.instance());
	}

	public static void registerSBListeners()
	{
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(MobDefense.instance(), PacketType.Play.Client.SETTINGS, PacketType.Play.Server.SCOREBOARD_SCORE)
		{
			@Override
			public void onPacketSending(PacketEvent event)
			{
				String objectiveName = event.getPacket().getStrings().read(1);
				if (!objectiveName.equals("mobdefense"))
					return;

				Messages srvMsgs = Messages.getMessages();
				Messages playerMsgs = Messages.getMessages(event.getPlayer());

				if (srvMsgs == playerMsgs)
					return;

				String scoreName = event.getPacket().getStrings().read(0);

				if (scoreName.equals(srvMsgs.getLives()))
					event.getPacket().getStrings().write(0, playerMsgs.getLives());
				else if (scoreName.equals(srvMsgs.getWave()))
					event.getPacket().getStrings().write(0, playerMsgs.getWave());
			}

			@Override
			public void onPacketReceiving(PacketEvent event)
			{
				try
				{
					Messages srvMsgs = Messages.getMessages();
					Messages oldMsgs = Messages.getMessages(event.getPlayer());
					Messages newMsgs = Messages.getMessages(event.getPacket().getStrings().read(0));

					PacketContainer pkt = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
					pkt.getStrings().write(0, oldMsgs.getLives());
					pkt.getStrings().write(0, "mobdefense");
					pkt.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.REMOVE);
					ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), pkt);

					pkt = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
					pkt.getStrings().write(0, oldMsgs.getWave());
					pkt.getStrings().write(0, "mobdefense");
					pkt.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.REMOVE);
					ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), pkt);

					pkt = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
					pkt.getStrings().write(0, newMsgs.getLives());
					pkt.getStrings().write(1, "mobdefense");
					pkt.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.CHANGE);
					pkt.getIntegers().write(0, event.getPlayer().getScoreboard().getObjective("mobdefense").getScore(srvMsgs.getLives()).getScore());
					ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), pkt);

					pkt = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
					pkt.getStrings().write(0, newMsgs.getWave());
					pkt.getStrings().write(1, "mobdefense");
					pkt.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.CHANGE);
					pkt.getIntegers().write(0, event.getPlayer().getScoreboard().getObjective("mobdefense").getScore(srvMsgs.getWave()).getScore());
					ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), pkt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public static void setEntityYaw(Entity e, float yaw)
	{
		yaw %= 360.0F;
		Object handle = invokeBukkitMethod("getHandle", e);
		if (VERSION.isAfter1_9())
		{
			invokeNMSMethod("EntityLiving", "h", handle, new Class[]{float.class}, yaw);
			invokeNMSMethod("EntityLiving", "i", handle, new Class[]{float.class}, yaw);
		}
		else
			invokeNMSMethod("EntityLiving", "f", handle, new Class[]{float.class}, yaw);
	}

	public static ServerVersion getServerVersion()
	{
		return VERSION;
	}

	@SuppressWarnings("unused")
	public enum ServerVersion
	{
		v1_8_R1("1.8 -> 1.8.2"),
		v1_8_R2("1.8.3 -> 1.8.5"),
		v1_8_R3("1.8.6 -> 1.8.9"),
		v1_9_R1("1.9 -> 1.9.2"),
		v1_9_R2("1.9.4"),
		v1_10_R1("1.10 -> 1.10.2");

		private String name;

		ServerVersion(String name)
		{
			this.name = name;
		}

		public String getServerName()
		{
			return name;
		}

		public boolean isBefore1_9()
		{
			return !isAfter1_9();
		}

		public boolean isAfter1_9()
		{
			return compareTo(v1_9_R1) >= 0;
		}

		public boolean isAfter1_10()
		{
			return compareTo(v1_10_R1) >= 0;
		}
	}
}
