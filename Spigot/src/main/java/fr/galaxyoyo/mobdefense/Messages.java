package fr.galaxyoyo.mobdefense;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.YamlConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Locale;

public abstract class Messages extends YamlConfig implements Serializable
{
	private static final Messages EN;
	private static final Messages FR;
	private static Messages DEFAULT_MESSAGES = null;

	static
	{
		EN = new MessagesEN();
		//noinspection StaticInitializerReferencesSubClass
		FR = new MessagesFR();
		DEFAULT_MESSAGES = getMessages(Locale.getDefault().getLanguage().toLowerCase());
	}

	public static Messages getMessages(CommandSender sender)
	{
		if (sender instanceof Player)
		{
			try
			{
				return getMessages(((CraftPlayer) sender).spigot().getLocale());
			}
			catch (Exception ex)
			{
				return getMessages();
			}
		}
		else
			return getMessages();
	}

	public static Messages getMessages(String language)
	{
		if (DEFAULT_MESSAGES != null && MobDefense.instance().getConfiguration() != null && MobDefense.instance().getConfiguration().isForcePreferredLanguage())
			return DEFAULT_MESSAGES;

		switch (language.split("_")[0].toLowerCase())
		{
			case "en":
				return EN;
			case "fr":
				return FR;
			default:
				if (DEFAULT_MESSAGES == null)
					DEFAULT_MESSAGES = EN;
				return DEFAULT_MESSAGES;
		}
	}

	public static Messages getMessages()
	{
		return DEFAULT_MESSAGES;
	}

	protected static void setPreferredLanguage(String language)
	{
		DEFAULT_MESSAGES = getMessages(language);
	}

	public static void broadcast(String msg, Object... args)
	{
		broadcast(new MessageProperty(msg, args));
	}

	public static void broadcast(MessageProperty property)
	{
		Bukkit.getOnlinePlayers().forEach(property::send);
		property.send(Bukkit.getConsoleSender());
	}

	public abstract String getGreetings();

	public abstract String getDownloadingMessage();

	public abstract String getDisablingPluginMessage();

	public abstract String getUnableToDownload();

	public abstract String getConfigLoaded();

	public abstract String getLocationConfigError();

	public abstract String getConfigLoadError();

	public abstract String getCheckingServerVersion();

	public abstract String getMinecraftRunningVersion();

	public abstract String getWarning18();

	public abstract String getUnsupportedServerVersion();

	public abstract String getOutdatedVersion();

	public abstract String getMobsConfigLoadError();

	public abstract String getWavesConfigLoadError();

	public abstract String getTowersConfigLoadError();

	public abstract String getUpgradesConfigLoadError();

	public abstract String getGeneralError();

	public abstract String getNotFoundMessage();

	public abstract String getGameAlreadyStarted();

	public abstract String getNoPlayerToStart();

	public abstract String getNoGame();

	@SuppressWarnings("unused")
	public abstract String getGameStarted();

	public abstract String[] getDemoLogging();

	@SuppressWarnings("unused")
	public abstract String getStartingWave();

	@SuppressWarnings("unused")
	public abstract String getGameEnded();

	@SuppressWarnings("unused")
	public abstract String[] getDemoEnded();

	public abstract String getNoItemType();

	public abstract String getNoPath();

	public abstract String getOnlyPlayers();

	public abstract String getLocationsSuccessDefined();

	public abstract String getNoPermission();

	public abstract String getNpcTowerName();

	public abstract String getNpcUpgradesName();

	public abstract String getNpcExchangeName();

	public abstract String getLives();

	public abstract String getWave();

	@SuppressWarnings("unused")
	public abstract String getBypassedTowers();

	@SuppressWarnings("unused")
	public abstract String getBypassedTowersOneLeft();

	@SuppressWarnings("unused")
	public abstract String getBypassedTowersEnd();

	public abstract String getPercentageUpgradeWarning();

	public abstract String getTowerClassNotFound();

	public abstract String getNotATowerClass();

	public abstract String getUpgradeClassNotFound();

	public abstract String getNotAnUpgradeClass();

/*	public abstract String getPlayerSpawnConfigComment();

	public abstract String getSpawnConfigComment();

	public abstract String getEndConfigComment();

	public abstract String getStartMoneyConfigComment();

	public abstract String getWaveTimeConfigComment();

	public abstract String getLivesConfigComment();

	public abstract String getNpcTowerLocConfigComment();

	public abstract String getNpcUpgradesLocConfigComment();

	public abstract String getNpcExchangeLocConfigComment();

	public abstract String getPreferredLanguageConfigComment();

	public abstract String getForcePreferredLanguageConfigComment();*/

	@Override
	public String toString()
	{
		return "Messages{" + getLocale().getDisplayName() + "}";
	}

	public abstract Locale getLocale();

	public static class MessageProperty
	{
		private final String field;
		private final Object[] args;

		public MessageProperty(String key, Object... args)
		{
			String[] split = key.split("-");
			if (split.length > 1)
			{
				key = "";
				for (String s : split)
				{
					if (!key.isEmpty() && s.length() == 1)
						s = s.toUpperCase();
					else if (!key.isEmpty())
						s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
					key += s;
				}
			}
			this.field = key;
			this.args = args;
		}

		public void send(CommandSender sender)
		{
			Messages msgs = Messages.getMessages(sender);
			try
			{
				Field f = msgs.getClass().getDeclaredField(field);
				f.setAccessible(true);
				Object obj = f.get(msgs);
				if (obj instanceof String[])
					obj = Strings.join((String[]) obj, "\n");
				sender.sendMessage("[MobDefense] " + ChatColor.translateAlternateColorCodes('&', String.format((String) obj, args)));
				f.setAccessible(false);
			}
			catch (Exception ex)
			{
				MobDefense.instance().getLogger().warning(String.format(getMessages().getNotFoundMessage(), field));
				ex.printStackTrace();
			}
		}
	}

	@EqualsAndHashCode(callSuper = true)
	@Data
	public static class MessagesEN extends Messages
	{
		@Getter
		private String greetings = "You're running the MobDefense plugin, by galaxyoyo. Thanks for buying it!";
		@Getter
		private String downloadingMessage = "Downloading version %s of %s ...";
		@Getter
		private String disablingPluginMessage = "Plugin will disable now.";
		@Getter
		private String unableToDownload = "Unable to download %s library. Make sure you have the latest version of MobDefense and have an Internet connection.";
		@Getter
		private String configLoaded = "Configuration Loaded! Messages console language: ";
		@Getter
		private String locationConfigError = "CONFIGURATION ERROR : Location must be provided as x:y:z:yaw:pitch, it seems that there are %d arguments, required 5";
		@Getter
		private String configLoadError = "An error occured while loading configuration file.";
		@Getter
		private String checkingServerVersion = "Checking server version ...";
		@Getter
		private String minecraftRunningVersion = "You're running Minecraft server %s, for Minecraft %s.";
		@Getter
		private String warning18 = "Warning: the 1.8 version of MobDefense contains less features as 1.9 and 1.10, like tipped arrows.\n" +
				"It mays contain some compatibility issues. I'm able to fix these, so please report them.\n" +
				"But please note that in a later update, this compatiblity will be removed.\n" +
				"If you want a better gameplay, please update your server to 1.9.4 or 1.10 (1.10 recomended).\n" +
				"There's no plugin update required.";
		@Getter
		private String unsupportedServerVersion = "You're running Minecraft server %s. This version is unsupported by MobDefense now.";
		@Getter
		private String outdatedVersion = "This plugin is outdated. The latest version is %s and you're running %s. Please update, there're maybe some fixes or new features.";
		@Getter
		private String mobsConfigLoadError = "Error while loading mobs config, please check this one.";
		@Getter
		private String wavesConfigLoadError = "Error while loading waves config, please check this one.";
		@Getter
		private String towersConfigLoadError = "Error while loading towers config, please check this one.";
		@Getter
		private String upgradesConfigLoadError = "Error while loading upgrades config, please check this one.";
		@Getter
		private String generalError = "An error occured while enabling Mob Defense. Please report it.";
		@Getter
		private String notFoundMessage = "The message \"%s\" wasn't found.";
		@Getter
		private String gameAlreadyStarted = "&cA game is already started!";
		@Getter
		private String noPlayerToStart = "&cAny player is connected to start the game!";
		@Getter
		private String noGame = "&cNo game is running!";
		@Getter
		private String gameStarted = "Game started!";
		@Getter
		private String startingWave = "Starting wave #%d (%s)";
		@Getter
		private String gameEnded = "Game ended.";
		@Getter
		private String noItemType = "No item type!";
		@Getter
		private String noPath = "&cNo path could be found. Please update the map.";
		@Getter
		private String onlyPlayers = "&cOnly players are able set positions.";
		@Getter
		private String locationsSuccessDefined = "Location successfully defined!";
		@Getter
		private String noPermission = "&cYou don't have the permission to run this command.";
		@Getter
		private String npcTowerName = "Towers";
		@Getter
		private String npcUpgradesName = "Upgrades";
		@Getter
		private String npcExchangeName = "Exchange";
		@Getter
		private String lives = "Lives";
		@Getter
		private String wave = "Wave";
		@Getter
		private String bypassedTowers = "%s bypassed the towers! %d lives left";
		@Getter
		private String bypassedTowersOneLeft = "%s bypassed the towers! One life left";
		@Getter
		private String bypassedTowersEnd = "%s bypassed the towers! &cYou survived %d waves.";
		@Getter
		private String percentageUpgradeWarning = "Warning: percentage %d must be included between 0 and 1. Considering 1 (100 %). Please check %d config.";
		@Getter
		private String towerClassNotFound = "Unable to find the tower class '%s'. Please update config.";
		@Getter
		private String notATowerClass = "The class '%s' was found but isn't a tower class. Please update config.";
		@Getter
		private String upgradeClassNotFound = "Unable to find the upgrade class '%s'. Please update config.";
		@Getter
		private String notAnUpgradeClass = "The class '%s' was found but isn't an upgrade class. Please update config.";
		@SuppressWarnings("MismatchedReadAndWriteOfArray")
		@Getter
		private transient String[] demoLogging = {"Hello!",
				"You're on the test server of Mob Defense.",
				"You are not opped, but you can run the /md command, which is normally provided for ops.",
				"You're able to run this demo whenever you want, but don't forget that players can join when they want.",
				"This server is made to test the plugin, not to play with it. So please leave the place to some other testers when you have finished to test it.",
				"The configuration couldn't be modified, and /md setloc isn't available.",
				"I hope you'll enjoy the plugin :)"};
		@Getter
		private transient String[] demoEnded = "The demo is now ended. I hope you enjoyed the plugin :)\nIn every case, please leave a comment on the forum :)".split("\n");
/*		@Getter
		private String playerSpawnConfigComment = "Location of player spawn (x:y:z:yaw:pitch)";
		@Getter
		private String spawnConfigComment = "Location of mob spawn (x:y:z:yaw:pitch)";
		@Getter
		private String endConfigComment = "Location of mob objective (x:y:z:yaw:pitch)";
		@Getter
		private String startMoneyConfigComment = "The amount of gold nuggets you start with";
		@Getter
		private String waveTimeConfigComment = "The time (in seconds) between each wave, after the last mob of\nlast wave spawned";
		@Getter
		private String livesConfigComment = "The number of mobs that can reach the end point before you loose";
		@Getter
		private String npcTowerLocConfigComment = "Location of towers seller (x:y:z:yaw:pitch)";
		@Getter
		private String npcUpgradesLocConfigComment = "Location of tower upgrades seller (x:y:z:yaw:pitch)";
		@Getter
		private String npcExchangeLocConfigComment = "Location of exchanger (x:y:z:yaw:pitch)";
		@Getter
		private String towerUpdateRateConfigComment = "Tower update rate, in ticks";
		@Getter
		private String preferredLanguageConfigComment = "Preferred language code for messages (default: en; supporteds: en, fr)";
		@Getter
		private String forcePreferredLanguageConfigComment = "If true, players will receive messages in the language of the server.\nIf false, players will receive messages in their
		 " +
				"own language (if available).";*/

		private MessagesEN()
		{
			CONFIG_FILE = new File(MobDefense.instance().getDataFolder().getAbsolutePath() + File.separatorChar + "messages", "en.yml");
			CONFIG_HEADER = new String[]{"##################################################",
					"####### Messages shown by english players ########",
					"##################################################"};
			try
			{
				init();
			}
			catch (InvalidConfigurationException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public Locale getLocale()
		{
			return Locale.US;
		}
	}

	@EqualsAndHashCode(callSuper = true)
	@Data
	public static class MessagesFR extends Messages
	{
		@Getter
		private String greetings = "Vous jouez avec le plugin MobDefense, créé par galaxyoyo. Merci de l'avoir acheté !";
		@Getter
		private String downloadingMessage = "Téléchargement de la version %s de %s ...";
		@Getter
		private String disablingPluginMessage = "Le plugin va désormais de désactiver.";
		@Getter
		private String unableToDownload = "Impossible de télécharger la bibliothèque %s. Vérifiez que vous possédiez bien la dernière version de MobDefense et que vous disposez " +
				"d'une connexion Internet.";
		@Getter
		private String configLoaded = "Configuration chargée ! Langue des messages dans la console : ";
		@Getter
		private String locationConfigError = "ERREUR DE CONFIGURATION : Un lieu doit être défini suivant la façon suivante : x:y:z:yaw:pitch. Il n'y a que %d arguments, pour 5 " +
				"requis.";
		@Getter
		private String configLoadError = "Une erreur est survenue lors du chargement du fichier de configuration.";
		@Getter
		private String checkingServerVersion = "Vérification de la version du serveur ...";
		@Getter
		private String minecraftRunningVersion = "Vous lancez un serveur Minecraft en version %s, pour Minecraft %s.";
		@Getter
		private String warning18 = "Attention : la version 1.8 de MobDefense contient moins de fonctionnalités que la 1.9 et la 1.10, comme les flèches de potions.\n" +
				"Il peut y avoir quelques problèmes de compatibilité, dont je serais apte de corriger, reportez-les donc s'il vous plaît.\n" +
				"Mais attention, dans une lointaine mise à jour, cette compatibilité sera supprimée.\n" +
				"Si vous voulez un meilleur gameplay, merci de mettre à jour votre serveur en 1.9.4 ou 1.10 (1.10.2 recommandée).\n" +
				"Aucune mise à jour du plugin n'est requise pour cela.";
		@Getter
		private String unsupportedServerVersion = "La version de votre serveur est %s. Cette version n'est actuellement pas supportée par MobDefense.";
		@Getter
		private String outdatedVersion = "Ce plugin n'est plus à jour. La dernière version est la %s et vous possèdez la %s. Merci de mettre à jour, il y a probablement de " +
				"nouvelles" +
				" " +
				"fonctionnalités ou de nouveaux fix.";
		@Getter
		private String mobsConfigLoadError = "Erreur lors du chargement de la configuration des mobs, merci de vérifier ce fichier.";
		@Getter
		private String wavesConfigLoadError = "Erreur lors du chargement de la configuration des vagues, merci de vérifier ce fichier.";
		@Getter
		private String towersConfigLoadError = "Erreur lors du chargement de la configuration des tours, merci de vérifier ce fichier.";
		@Getter
		private String upgradesConfigLoadError = "Erreur lors du chargement de la configuration des améliorations, merci de vérifier ce fichier.";
		@Getter
		private String generalError = "Une erreur est survenue lors de l'activatin de MobDefense. Merci de la reporter.";
		@Getter
		private String notFoundMessage = "Le message \"%s\" n'a pas été trouvé.";
		@Getter
		private String gameAlreadyStarted = "&cUne partie est déjà en cours !";
		@Getter
		private String noPlayerToStart = "&cAucun joueur n'est connecté pour démarrer la partie !";
		@Getter
		private String noGame = "&cAucune partie n'est en cours !";
		@Getter
		private String gameStarted = "Partie démarrée !";
		@Getter
		private String startingWave = "Démarrage de la vague n°%d (%s)";
		@Getter
		private String gameEnded = "Partie terminée.";
		@Getter
		private String noItemType = "Aucun type d'item défini !";
		@Getter
		private String noPath = "&cAucun chemin n'a pu être trouvé. Merci de mettre à jour votre carte.";
		@Getter
		private String onlyPlayers = "&cSeuls les joueurs sont capables de modifier les positions.";
		@Getter
		private String locationsSuccessDefined = "Position définie avec succès !";
		@Getter
		private String noPermission = "&cVous n'avez pas la permission d'utiliser cette commande.";
		@Getter
		private String npcTowerName = "Tours";
		@Getter
		private String npcUpgradesName = "Améliorations";
		@Getter
		private String npcExchangeName = "Échange";
		@Getter
		private String lives = "Vies";
		@Getter
		private String wave = "Vague";
		@Getter
		private String bypassedTowers = "%s a surmonté les tours ! %d vies restantes";
		@Getter
		private String bypassedTowersOneLeft = "%s a surmonté les tours ! Une vie restante";
		@Getter
		private String bypassedTowersEnd = "%s a surmonté les tours ! &cVous avez survécu %d vagues.";
		@Getter
		private String percentageUpgradeWarning = "Attention : la proportion %d doit être comprise entre 0 et 1. Définie à 1 (100 %) par défaut. Merci de vérifier la configuration" +
				" " +
				"de" +
				" %s.";
		@Getter
		private String towerClassNotFound = "Impossible de trouver la classe de tour '%s'. Merci de mettre à jour la configuration.";
		@Getter
		private String notATowerClass = "La classe '%s' a bien été trouvée, mais n'est pas une classe de tour. Merci de mettre à jour la configuration.";
		@Getter
		private String upgradeClassNotFound = "Impossible de trouver la classe d'amélioration '%s'. Merci de mettre à jour la configuration.";
		@Getter
		private String notAnUpgradeClass = "La classe '%s' a bien été trouvée, mais n'est pas une classe d'amélioration. Merci de mettre à jour la configuration.";
		@SuppressWarnings("MismatchedReadAndWriteOfArray")
		@Getter
		private transient String[] demoLogging = {"Bonjour !",
				"Vous êtes sur le serveur de test de Mob Defense.",
				"Vous n'êtes pas opérateur, mais vous pouvez lancer la commande /md, qui n'est réservée d'habitude uniquement aux opérateurs.",
				"Vous pouvez lancer cette démonstration quand vous le souhaitez, mais n'oubliez pas que des joueurs peuvent rejoindre ce serveur à tout moment.",
				"Ce serveur est réservé au test public du plugin, mais pas pour jouer avec. Donc veuillez s'il vous plaît laisser la place à d'autres testeurs quand vous aurez " +
						"terminé votre test.",
				"La configuration ne peut être modifiée, et la commande /md setloc n'est pas disponible.",
				"J'espère que vous apprécierez le plugin :)"};
		@Getter
		private transient String[] demoEnded = "La démo est désormais terminée. J'espère que ce plugin vous aura plu :)\nDans tous les cas, merci de laisser un post sur le forum :)"
				.split("\n");
/*		@Getter
		private String playerSpawnConfigComment = "Position du spawn des joueurs (x:y:z:yaw:pitch)";
		@Getter
		private String spawnConfigComment = "Position du spawn des mobs (x:y:z:yaw:pitch)";
		@Getter
		private String endConfigComment = "Position de l'objectif des mobs (x:y:z:yaw:pitch)";
		@Getter
		private String startMoneyConfigComment = "Combien de pépites d'or les joueurs reçoivent";
		@Getter
		private String waveTimeConfigComment = "Le temps (en secondes) entre chaque vague, après que\nla dernière vague soit terminée";
		@Getter
		private String livesConfigComment = "Le nombre de mobs qui peuvent atteindre la fin avant de perdre";
		@Getter
		private String npcTowerLocConfigComment = "Position du vendeur de tours (x:y:z:yaw:pitch)";
		@Getter
		private String npcUpgradesLocConfigComment = "Position du vendeur d'améliorations (x:y:z:yaw:pitch)";
		@Getter
		private String npcExchangeLocConfigComment = "Position du monnayeur (x:y:z:yaw:pitch)";
		@Getter
		private String towerUpdateRateConfigComment = "Délai entre chaque mise à jour des tours, en ticks";
		@Getter
		private String preferredLanguageConfigComment = "Langue préférée des messages (défaut : en; supportés : en, fr)";
		@Getter
		private String forcePreferredLanguageConfigComment = "Si oui, les joueurs vont recevoir des messages dans la langue ci-dessus.\n" +
				"Si non, les joueurs vont recevoir des messages dans leur propre langue (si disponible).";*/

		private MessagesFR()
		{
			CONFIG_FILE = new File(MobDefense.instance().getDataFolder().getAbsolutePath() + File.separatorChar + "messages", "fr.yml");
			CONFIG_HEADER = new String[]{"##################################################",
					"##### Messages affichés aux joueurs français #####",
					"##################################################"};

			try
			{
				init();
			}
			catch (InvalidConfigurationException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public Locale getLocale()
		{
			return Locale.FRANCE;
		}
	}
}
