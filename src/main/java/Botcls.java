//DISCORD BOT IMPORTS
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
//Various exceptions
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.MalformedURLException;


public class Botcls extends ListenerAdapter{

    //Method to initialize an instance of the bot
    public static void buildBot() throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(tokenStore.BotToken());
        builder.addEventListeners(new Botcls());
        builder.build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        /*This if statement just removes clutter from the console - before it would output even if the message read in chat
        was from a bot. */
        if(event.getAuthor().isBot()){
        }else{
            System.out.println("We recieved a message from " +
                    event.getAuthor().getName() + ": " +
                    event.getMessage().getContentDisplay()
            );
        }

        //It will ignore any other bots
        if(event.getAuthor().isBot()){
            return;
        }

        //This gets the price of a WoW token for any given region - eu us kr cn and the others
        if(event.getMessage().getContentRaw().startsWith("!armory token")) {
            String message = event.getMessage().getContentRaw();
            String regionreturn = new String();
            String info = new String();
            String msg = new String();
            String content = "wow_token";
            try {
               regionreturn = split.splitQuery(message, "wow_token");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            System.out.println("Region is " + regionreturn);
            info = warcraft.wow_token_price(regionreturn, content);
            //Returns a message to the channel if there is an error fetching the info.
            if(info == "not_found"){
                msg = String.format(settings.GOLD_ERROR, message);
                event.getChannel().sendMessage(msg).queue();
            } else if(info == "connection_error"){
                msg = String.format(settings.CONNECTION_ERROR, message);
                event.getChannel().sendMessage(msg).queue();
            } else if(info == "credential_error"){
                msg = String.format(settings.CREDENTIAL_ERROR, message);
                event.getChannel().sendMessage(msg).queue();
            } else {
                System.out.println(info);
                try {
                    //doubles have to be used rather than int as int has a max store limit of 2.147 Billion and EU price is 2.5 Billion.
                    double i = Double.parseDouble(info.trim());
                    double j = i/10000;
                    int x = (int) j;
                    msg = "The current price of a WoW Token on " + regionreturn + " realms is " + x + " gold.";
                    event.getChannel().sendMessage(msg).queue();
                } catch (NumberFormatException e) {
                    // Output expected NumberFormatException.
                    System.out.println(e);
                }
            }
        }
        //This returns all PvE stats for the character
        if(event.getMessage().getContentRaw().startsWith("!armory pve")) {
            String message = event.getMessage().getContentRaw();
            String returnSplit = null;
            String region = null;
            String name = null;
            String realm = null;
            String content = "pve";
            JSONObject info = new JSONObject();
            String msg = null;

                try {
                    returnSplit = split.splitQuery(message, "pve");
                    String[] dataArray = returnSplit.split("\\s+");
                    region = dataArray[3];
                    System.out.println(region);
                    name = dataArray[1];
                    System.out.println(name);
                    realm = dataArray[0];
                    System.out.println(realm);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                event.getChannel().sendMessage("Retrieving Character Data... Please Wait :construction:").queue();
            try {
                info = warcraft.getCharacterInfo(region, name, realm, content);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //There should be code here to check error messages but it wasnt working so...
            String uldirFeat = "";
            String battleFeat = "";
            String crucibleFeat = "";
            String palaceFeat = "";
            JSONObject uldirProg = (JSONObject) info.get("uldirProg");
            JSONObject battleProg = (JSONObject) info.get("battleProg");
            JSONObject crucibleProg = (JSONObject) info.get("crucibleProg");
            JSONObject palaceProg = (JSONObject) info.get("palaceProg");

            if(info.get("uldirFeat") != ""){
                uldirFeat = "**" + info.get("uldirFeat") + "**";
            }
            if(info.get("battleFeat") != ""){
                battleFeat = "**" + info.get("battleFeat") + "**";
            }
            if(info.get("crucibleFeat") != ""){
                crucibleFeat = "**" + info.get("crucibleFeat") + "**";
            }
            if(info.get("palaceFeat") != ""){
                palaceFeat = "**" + info.get("palaceFeat") + "**";
            }

            EmbedBuilder eb = new EmbedBuilder();
            //Getting dumb casting errors if i dont do it this way \\\\ QUICK FIX
            String level = String.valueOf(info.get("level"));
            String faction = (String) info.get("faction");
            String spec = (String) info.get("spec");
            String classtype = (String) info.get("classtype");
            String desc = level + " " + faction + " " + spec + " " + classtype;
            eb.setTitle((String) info.get("name"));
            eb.setColor((Integer) info.get("classcolour"));
            eb.setDescription(desc);
            eb.setThumbnail("https://render-" + region + ".worldofwarcraft.com/character/" + info.get("charportrait") + "?_" + epochtime.getEpochTime());
            eb.setFooter("!armory help for more commands");
            eb.addField("Character","**`Name`:** " + info.get("name") + "\n**`Realm:`** " + info.get("realm") + "(" + region.toUpperCase() + ")" +
                    "\n**`Item Level:`** " + info.get("ilvl"), true);
            eb.addField("Keystone Achivements (Current Season)", "**`Keystone Conqueror (Mythic +10)`: **" + info.get("keystone_conqueror")
            + "\n**`Keystone Master (Mythic +15)`: **" + info.get("keystone_master"), true);
            //Uldir
            eb.addField("Uldir", "**`Normal`:** " + uldirProg.get("normal") + "/" + uldirProg.get("bosses") +
                        "\n**`Heroic`:** " + uldirProg.get("heroic") + "/" + uldirProg.get("bosses") +
                        "\n**`Mythic`:** " + uldirProg.get("mythic") + "/" + uldirProg.get("bosses") + "\n" + uldirFeat, true);
            //BOD
            eb.addField("Battle of Dazar'alor", "**`Normal`:** " + battleProg.get("normal") + "/" + battleProg.get("bosses") +
                    "\n**`Heroic`:** " + battleProg.get("heroic") + "/" + battleProg.get("bosses") +
                    "\n**`Mythic`:** " + battleProg.get("mythic") + "/" + battleProg.get("bosses") + "\n" + battleFeat, true);
            //COS
            eb.addField("Crucible of Storms", "**`Normal`:** " + crucibleProg.get("normal") + "/" + crucibleProg.get("bosses") +
                    "\n**`Heroic`:** " + crucibleProg.get("heroic") + "/" + crucibleProg.get("bosses") +
                    "\n**`Mythic`:** " + crucibleProg.get("mythic") + "/" + crucibleProg.get("bosses") + "\n" + crucibleFeat, true);
            //TEP
            eb.addField("The Eternal Palace", "**`Normal`:** " + palaceProg.get("normal") + "/" + palaceProg.get("bosses") +
                    "\n**`Heroic`:** " + palaceProg.get("heroic") + "/" + palaceProg.get("bosses") +
                    "\n**`Mythic`:** " + palaceProg.get("mythic") + "/" + palaceProg.get("bosses") + "\n" + palaceFeat, true);
            //Nyalotha - Commented as not in game yet
//            eb.addField("Nyalotha", "**`Normal`:** " + nyProg.get("normal") + "/" + nyProg.get("bosses") +
//                    "\n**`Heroic`:** " + nyProg.get("heroic") + "/" + nyProg.get("bosses") +
//                    "\n**`Mythic`:** " + nyProg.get("mythic") + "/" + nyProg.get("bosses") + "\n" + nyFeat, true);

            event.getChannel().sendMessage(eb.build()).queue();
        }
        //This returns all PvP stats for the character
        if(event.getMessage().getContentRaw().startsWith("!armory pvp")) {
            String message = event.getMessage().getContentRaw();
            String returnSplit = null;
            String region = null;
            String name = null;
            String realm = null;
            String content = "pvp";
            JSONObject info = new JSONObject();
            String msg = null;

            try {
                returnSplit = split.splitQuery(message, "pvp");
                String[] dataArray = returnSplit.split("\\s+");
                region = dataArray[3];
                System.out.println(region);
                name = dataArray[1];
                System.out.println(name);
                realm = dataArray[0];
                System.out.println(realm);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            event.getChannel().sendMessage("Retrieving Character Data... Please Wait :construction:").queue();
            try {
                info = warcraft.getCharacterInfo(region, name, realm, content);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            EmbedBuilder eb = new EmbedBuilder();
            //Getting dumb casting errors if i dont do it this way \\\\ QUICK FIX
            System.out.println(info);
            String level = String.valueOf(info.get("level"));
            String faction = (String) info.get("faction");
            String spec = (String) info.get("spec");
            String classtype = (String) info.get("classtype");
            String desc = level + " " + faction + " " + spec + " " + classtype;
            eb.setTitle((String) info.get("name"));
            eb.setColor((Integer) info.get("classcolour"));
            eb.setDescription(desc);
            eb.setThumbnail("https://render-" + region + ".worldofwarcraft.com/character/" + info.get("charportrait") + "?_" + epochtime.getEpochTime());
            eb.setFooter("!armory help for more commands");
            eb.addField("Character","**`Name`:** " + info.get("name") + "\n**`Realm:`** " + info.get("realm") + "(" + region.toUpperCase() + ")" +
                    "\n**`Item Level:`** " + info.get("ilvl"), true);

            eb.addField("Arena Achievements", "**`Challenger`:**" + info.get("arena_challenger") + "\n**`Rival`:**" + info.get("arena_rival")
                    + "\n**`Duelist`:**" + info.get("arena_duelist") + "\n**`Gladiator`:**" + info.get("arena_gladiator"), true);
            eb.addField("RBG Achievements", "**`" + info.get("rbg2400Name")+ "`:**" + info.get("rbg2400") +
                        "\n**`" + info.get("rbg2000Name")+ "`:**" + info.get("rbg2000") +
                        "\n**`" + info.get("rbg1500Name")+ "`:**" + info.get("rbg1500"), true);
            eb.addField("Rated 2v2", "**`Rating`:**" + info.get("2v2"),true);
            eb.addField("Rated 3v3", "**`Rating`:**" + info.get("3v3"),true);
            eb.addField("Rated Battlegrounds", "**`Rating`:**" + info.get("rbg"),true);
            eb.addField("2v2 Skirmish", "**`Rating`:**" + info.get("2v2s"),true);
            eb.addField("Lifetime Honorable Kills", String.valueOf(info.get("kills")),true);

            event.getChannel().sendMessage(eb.build()).queue();

        }

        //Gives all the commands when !armory help is entered
        if(event.getMessage().getContentRaw().startsWith("!armory help")) {
            String msg = "\n" +
                    "The following commands can be entered:\n" +
                    "# Displays a players PVE progression, dungeon kills, keystone achievements, etc.\n" +
                    "            !armory pve <name> <realm>\n" +
                    "            !armory pve <armory-link>\n" +
                    "# Displays a players PVP progression, arena ratings, honorable kills, etc.\n" +
                    "            !armory pvp <name> <realm>\n" +
                    "            !armory pvp <armory-link>\n" +
                    "            # Displays the WoW token price\n" +
                    "            !armory token\n" +
                    "# You can also provide an optional region to each query to display players from other WoW regions outside of the bot default, for example EU, US, etc.\n" +
                    "            !armory pve <name> <realm> <region>\n" +
                    "            !armory pvp <armory-link> <region>\n" +
                    "            !armory token <region>\n" +
                    "            ";

            event.getChannel().sendMessage(msg).queue();

        }
    }

    public static void main(String[] args) throws LoginException, IOException {
        buildBot();
    }
}
