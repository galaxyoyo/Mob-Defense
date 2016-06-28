package fr.galaxyoyo.mobdefense;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.YamlConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.Serializable;
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
			return getMessages(((Player) sender).spigot().getLocale().split("_")[0]);
		else
			return getMessages();
	}

	public static Messages getMessages(String language)
	{
		switch (language.toLowerCase())
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
				sender.sendMessage("[MobDefense] " + String.format((String) msgs.getClass().getDeclaredField(field).get(msgs), args));
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
		protected String greetings = "You're running the MobDefense plugin, by galaxyoyo. Thanks for buying it!";
		@Getter
		protected String downloadingMessage = "Downloading version %s of %s ...";
		@Getter
		protected String disablingPluginMessage = "Plugin will disable now.";
		@Getter
		protected String unableToDownload = "Unable to download %s library. Make sure you have the latest version of MobDefense and have an Internet connection.";
		@Getter
		protected String configLoaded = "Configuration Loaded! Messages console language: ";
		@Getter
		protected String locationConfigError = "CONFIGURATION ERROR : Location must be provided as x:y:z:yaw:pitch, it seems that there are %d arguments, required 5";
		@Getter
		protected String configLoadError = "An error occured while loading configuration file.";
		@Getter
		protected String checkingServerVersion = "Checking server version ...";
		@Getter
		protected String minecraftRunningVersion = "You're running Minecraft server %s, for Minecraft %s.";
		@Getter
		protected String warning18 = "Warning: the 1.8 version of MobDefense contains less features as 1.9 and 1.10, like tipped arrows.\n" +
				"It mays contain some compatibility issues. I'm able to fix these, so please report them.\n" +
				"But please note that in a later update, this compatiblity will be removed.\n" +
				"If you want a better gameplay, please update your server to 1.9.4 or 1.10 (1.10 recomended).\n" +
				"There's no plugin update required.";
		@Getter
		protected String unsupportedServerVersion = "You're running Minecraft server %s. This version is unsupported by MobDefense now.";
		@Getter
		protected String outdatedVersion = "This plugin is outdated. The latest version is %s and you're running %s. Please update, there're maybe some fixes or new features.";
		@Getter
		protected String mobsConfigLoadError = "Error while loading mobs config, please check this one.";
		@Getter
		protected String wavesConfigLoadError = "Error while loading waves config, please check this one.";
		@Getter
		protected String towersConfigLoadError = "Error while loading towers config, please check this one.";
		@Getter
		protected String upgradesConfigLoadError = "Error while loading upgrades config, please check this one.";
		@Getter
		protected String generalError = "An error occured while enabling Mob Defense. Please report it.";
		@Getter
		protected String notFoundMessage = "The message \"%s\" wasn't found.";
		@Getter
		protected String gameAlreadyStarted = "&cA game is already started!";
		@Getter
		protected String noPlayerToStart = "&cAny player is connected to start the game!";
		@Getter
		protected String noGame = "&cNo game is running!";
		@Getter
		protected String gameStarted = "Game started!";
		@Getter
		protected String startingWave = "Starting wave #%d (%s)";
		@Getter
		protected String gameEnded = "Game ended.";
		@Getter
		protected String noItemType = "No item type!";
		@Getter
		protected String noPath = "&cNo path could be found. Please update the map.";
		@Getter
		protected String onlyPlayers = "&cOnly players are able set positions.";
		@Getter
		protected String locationsSuccessDefined = "Location successfully defined!";
		@Getter
		protected String noPermission = "&cYou don't have the permission to run this command.";
		@Getter
		protected String npcTowerName = "Towers";
		@Getter
		protected String npcUpgradesName = "Upgrades";
		@Getter
		protected String npcExchangeName = "Exchange";
		@Getter
		protected String bypassedTowers = "%s bypassed the towers! %d lives left";
		@Getter
		protected String bypassedTowersOneLeft = "%s bypassed the towers! One life left";
		@Getter
		protected String bypassedTowersEnd = "%s bypassed the towers! &cYou survived %d waves.";
		@Getter
		protected String percentageUpgradeWarning = "Warning: percentage %d must be included between 0 and 1. Considering 1 (100 %). Please check %d config.";
		@Getter
		protected String towerClassNotFound = "Unable to find the tower class '%s'. Please update config.";
		@Getter
		protected String notATowerClass = "The class '%s' was found but isn't a tower class. Please update config.";
		@Getter
		protected String upgradeClassNotFound = "Unable to find the upgrade class '%s'. Please update config.";
		@Getter
		protected String notAnUpgradeClass = "The class '%s' was found but isn't an upgrade class. Please update config.";
		@SuppressWarnings("MismatchedReadAndWriteOfArray")
		@Getter
		protected transient String[] demoLogging = {"Hello!",
				"You're on the test server of Mob Defense.",
				"You are not opped, but you can run the /md command, which is normally provided for ops.",
				"You're able to run this demo whenever you want, but don't forget that players can join when they want.",
				"This server is made to test the plugin, not to play with it. So please leave the place to some other testers when you have finished to test it.",
				"The configuration couldn't be modified, and /md setloc isn't available.",
				"I hope you'll enjoy the plugin :)"};
		@Getter
		protected transient String[] demoEnded = "The demo is now ended. I hope you enjoyed the plugin :)\nIn every case, please leave a comment on the forum :)".split("\n");
/*		@Getter
		protected String playerSpawnConfigComment = "Location of player spawn (x:y:z:yaw:pitch)";
		@Getter
		protected String spawnConfigComment = "Location of mob spawn (x:y:z:yaw:pitch)";
		@Getter
		protected String endConfigComment = "Location of mob objective (x:y:z:yaw:pitch)";
		@Getter
		protected String startMoneyConfigComment = "The amount of gold nuggets you start with";
		@Getter
		protected String waveTimeConfigComment = "The time (in seconds) between each wave, after the last mob of\nlast wave spawned";
		@Getter
		protected String livesConfigComment = "The number of mobs that can reach the end point before you loose";
		@Getter
		protected String npcTowerLocConfigComment = "Location of towers seller (x:y:z:yaw:pitch)";
		@Getter
		protected String npcUpgradesLocConfigComment = "Location of tower upgrades seller (x:y:z:yaw:pitch)";
		@Getter
		protected String npcExchangeLocConfigComment = "Location of exchanger (x:y:z:yaw:pitch)";
		@Getter
		protected String towerUpdateRateConfigComment = "Tower update rate, in ticks";
		@Getter
		protected String preferredLanguageConfigComment = "Preferred language code for messages (default: en; supporteds: en, fr)";
		@Getter
		protected String forcePreferredLanguageConfigComment = "If true, players will receive messages in the language of the server.\nIf false, players will receive messages in their
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
		protected String greetings = "Vous jouez avec le plugin MobDefense, créé par galaxyoyo. Merci de l'avoir acheté !";
		@Getter
		protected String downloadingMessage = "Téléchargement de la version %s de %s ...";
		@Getter
		protected String disablingPluginMessage = "Le plugin va désormais de désactiver.";
		@Getter
		protected String unableToDownload = "Impossible de télécharger la bibliothèque %s. Vérifiez que vous possédiez bien la dernière version de MobDefense et que vous disposez " +
				"d'une connexion Internet.";
		@Getter
		protected String configLoaded = "Configuration chargée ! Langue des messages dans la console : ";
		@Getter
		protected String locationConfigError = "ERREUR DE CONFIGURATION : Un lieu doit être défini suivant la façon suivante : x:y:z:yaw:pitch. Il n'y a que %d arguments, pour 5 " +
				"requis.";
		@Getter
		protected String configLoadError = "Une erreur est survenue lors du chargement du fichier de configuration.";
		@Getter
		protected String checkingServerVersion = "Vérification de la version du serveur ...";
		@Getter
		protected String minecraftRunningVersion = "Vous lancez un serveur Minecraft en version %s, pour Minecraft %s.";
		@Getter
		protected String warning18 = "Attention : la version 1.8 de MobDefense contient moins de fonctionnalités que la 1.9 et la 1.10, comme les flèches de potions.\n" +
				"Il peut y avoir quelques problèmes de compatibilité, dont je serais apte de corriger, reportez-les donc s'il vous plaît.\n" +
				"Mais attention, dans une lointaine mise à jour, cette compatibilité sera supprimée.\n" +
				"Si vous voulez un meilleur gameplay, merci de mettre à jour votre serveur en 1.9.4 ou 1.10 (1.10.2 recommandée).\n" +
				"Aucune mise à jour du plugin n'est requise pour cela.";
		@Getter
		protected String unsupportedServerVersion = "La version de votre serveur est %s. Cette version n'est actuellement pas supportée par MobDefense.";
		@Getter
		protected String outdatedVersion = "Ce plugin n'est plus à jour. La dernière version est la %s et vous possèdez la %s. Merci de mettre à jour, il y a probablement de " +
				"nouvelles" +
				" " +
				"fonctionnalités ou de nouveaux fix.";
		@Getter
		protected String mobsConfigLoadError = "Erreur lors du chargement de la configuration des mobs, merci de vérifier ce fichier.";
		@Getter
		protected String wavesConfigLoadError = "Erreur lors du chargement de la configuration des vagues, merci de vérifier ce fichier.";
		@Getter
		protected String towersConfigLoadError = "Erreur lors du chargement de la configuration des tours, merci de vérifier ce fichier.";
		@Getter
		protected String upgradesConfigLoadError = "Erreur lors du chargement de la configuration des améliorations, merci de vérifier ce fichier.";
		@Getter
		protected String generalError = "Une erreur est survenue lors de l'activatin de MobDefense. Merci de la reporter.";
		@Getter
		protected String notFoundMessage = "Le message \"%s\" n'a pas été trouvé.";
		@Getter
		protected String gameAlreadyStarted = "&cUne partie est déjà en cours !";
		@Getter
		protected String noPlayerToStart = "&cAucun joueur n'est connecté pour démarrer la partie !";
		@Getter
		protected String noGame = "&cAucune partie n'est en cours !";
		@Getter
		protected String gameStarted = "Partie démarrée !";
		@Getter
		protected String startingWave = "Démarrage de la vague n°%d (%s)";
		@Getter
		protected String gameEnded = "Partie terminée.";
		@Getter
		protected String noItemType = "Aucun type d'item défini !";
		@Getter
		protected String noPath = "&cAucun chemin n'a pu être trouvé. Merci de mettre à jour votre carte.";
		@Getter
		protected String onlyPlayers = "&cSeuls les joueurs sont capables de modifier les positions.";
		@Getter
		protected String locationsSuccessDefined = "Position définie avec succès !";
		@Getter
		protected String noPermission = "&cVous n'avez pas la permission d'utiliser cette commande.";
		@Getter
		protected String npcTowerName = "Tours";
		@Getter
		protected String npcUpgradesName = "Améliorations";
		@Getter
		protected String npcExchangeName = "Échange";
		@Getter
		protected String bypassedTowers = "%s a surmonté les tours ! %d vies restantes";
		@Getter
		protected String bypassedTowersOneLeft = "%s a surmonté les tours ! Une vie restante";
		@Getter
		protected String bypassedTowersEnd = "%s a surmonté les tours ! &cVous avez survécu %d vagues.";
		@Getter
		protected String percentageUpgradeWarning = "Attention : la proportion %d doit être comprise entre 0 et 1. Définie à 1 (100 %) par défaut. Merci de vérifier la configuration" +
				" " +
				"de" +
				" %s.";
		@Getter
		protected String towerClassNotFound = "Impossible de trouver la classe de tour '%s'. Merci de mettre à jour la configuration.";
		@Getter
		protected String notATowerClass = "La classe '%s' a bien été trouvée, mais n'est pas une classe de tour. Merci de mettre à jour la configuration.";
		@Getter
		protected String upgradeClassNotFound = "Impossible de trouver la classe d'amélioration '%s'. Merci de mettre à jour la configuration.";
		@Getter
		protected String notAnUpgradeClass = "La classe '%s' a bien été trouvée, mais n'est pas une classe d'amélioration. Merci de mettre à jour la configuration.";
		@SuppressWarnings("MismatchedReadAndWriteOfArray")
		@Getter
		protected transient String[] demoLogging = {"Bonjour !",
				"Vous êtes sur le serveur de test de Mob Defense.",
				"Vous n'êtes pas opérateur, mais vous pouvez lancer la commande /md, qui n'est réservée d'habitude uniquement aux opérateurs.",
				"Vous pouvez lancer cette démonstration quand vous le souhaitez, mais n'oubliez pas que des joueurs peuvent rejoindre ce serveur à tout moment.",
				"Ce serveur est réservé au test public du plugin, mais pas pour jouer avec. Donc veuillez s'il vous plaît laisser la place à d'autres testeurs quand vous aurez " +
						"terminé votre test.",
				"La configuration ne peut être modifiée, et la commande /md setloc n'est pas disponible.",
				"J'espère que vous apprécierez le plugin :)"};
		@Getter
		protected transient String[] demoEnded = "La démo est désormais terminée. J'espère que ce plugin vous aura plu :)\nDans tous les cas, merci de laisser un post sur le forum :)"
				.split("\n");
/*		@Getter
		protected String playerSpawnConfigComment = "Position du spawn des joueurs (x:y:z:yaw:pitch)";
		@Getter
		protected String spawnConfigComment = "Position du spawn des mobs (x:y:z:yaw:pitch)";
		@Getter
		protected String endConfigComment = "Position de l'objectif des mobs (x:y:z:yaw:pitch)";
		@Getter
		protected String startMoneyConfigComment = "Combien de pépites d'or les joueurs reçoivent";
		@Getter
		protected String waveTimeConfigComment = "Le temps (en secondes) entre chaque vague, après que\nla dernière vague soit terminée";
		@Getter
		protected String livesConfigComment = "Le nombre de mobs qui peuvent atteindre la fin avant de perdre";
		@Getter
		protected String npcTowerLocConfigComment = "Position du vendeur de tours (x:y:z:yaw:pitch)";
		@Getter
		protected String npcUpgradesLocConfigComment = "Position du vendeur d'améliorations (x:y:z:yaw:pitch)";
		@Getter
		protected String npcExchangeLocConfigComment = "Position du monnayeur (x:y:z:yaw:pitch)";
		@Getter
		protected String towerUpdateRateConfigComment = "Délai entre chaque mise à jour des tours, en ticks";
		@Getter
		protected String preferredLanguageConfigComment = "Langue préférée des messages (défaut : en; supportés : en, fr)";
		@Getter
		protected String forcePreferredLanguageConfigComment = "Si oui, les joueurs vont recevoir des messages dans la langue ci-dessus.\n" +
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
