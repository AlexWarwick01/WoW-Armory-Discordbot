import com.ning.http.client.AsyncHttpClient;
import org.apache.http.HttpHeaders;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class warcraft {

    //This needs to be broken into smaller methods cus what the fuck is this.
    public static String getData(String content, String regionreturn, String name, String realm, String field){

        //This code creates the access token and makes some vars
        String accesstoken = getAToken(regionreturn);
        String base_api_path;
        String api_path = null;
        String connerror = "Error: Connection error occurred when retrieving game data.";
        String tokenCost = null;

        //Code to access the API and get the wow token data
        if (accesstoken == "credential_error"){
            return accesstoken;
        } else {

            if (regionreturn == "cn"){
                base_api_path = "https://gateway.battlenet.com.cn";
                System.out.println(base_api_path);
            } else {
                base_api_path = "https://" + regionreturn + ".api.blizzard.com";
                System.out.println(base_api_path);
            }

            try(AsyncHttpClient asyncClient = new AsyncHttpClient()){

                //Seperate code for WoW token as it is handled differently.
                if (content == "wow_token"){
                    api_path = base_api_path + "/data/wow/token/?namespace=dynamic-" + regionreturn + "&access_token=" + accesstoken;
                    System.out.println(api_path);
                    //REMOVE REPEATING CODE ASAP
                    final String fetchURL = api_path;
                    final String body = asyncClient
                            .prepareGet(fetchURL)
                            .execute()
                            .get()
                            .getResponseBody(StandardCharsets.UTF_8.name());
                    System.out.println(body);
                    //Change the variable names probably
                    String[] fuck = body.split(",");
                    String[] shit = fuck[2].split(":");
                    String tokenCostunf = shit[1];
                    tokenCost = tokenCostunf.substring(0, tokenCostunf.length()-1);
                    return tokenCost;

                } else {
                    api_path =  base_api_path + "/wow/character/" + realm + "/" + name + "?fields=" + field + "&locale=" + settings.LOCALE + "&access_token=" + accesstoken;
                    System.out.println(api_path);
                    //Getting the status code
                    final String fetchURL = api_path;
                    final int body = asyncClient
                            .prepareGet(fetchURL)
                            .execute()
                            .get()
                            .getStatusCode();
                    System.out.println(body);

                    //Gets data if status code is 200
                    if (body == 200){
                        //Getting the status code
                        final String dataURL = api_path;
                        final String data = asyncClient
                                .prepareGet(dataURL)
                                .execute()
                                .get()
                                .getResponseBody(StandardCharsets.UTF_8.name());
                        return data;

                    //Returns an error if status code is 404
                    }else if (body == 404){
                        System.out.println("Error: Character not found");
                        return "not_found";
                    }
                }
            } catch(Exception error){
                System.out.println(connerror);
                return "connection_error";
            }
        }
        return accesstoken;
    }

    public static JSONObject getCharacterInfo(String region, String realm, String name, String content) throws ParseException {
        //This could implode at any time.
        String field = "items";
        String info = getData(null, region, realm, name, field);
        if (info == "not_found" || info == "connection_error" || info == "credential_error"){
            //Write some jank code to make it a JSONObject for returning easy
            JSONParser error = new JSONParser();
            JSONObject infoError = (JSONObject) error.parse(info);
            System.out.println(infoError);
            return infoError;
        //If the returned data is not an error, assume the character inputted exists.
        } else {
            //parses the string into a JSON object for me to use
            JSONParser parser = new JSONParser();
            JSONObject info_json = (JSONObject) parser.parse(info);
            JSONObject items = (JSONObject) info_json.get("items");
            System.out.println(info_json);
            try {
                //Gets basic character data - faction and class
                String classID = String.valueOf(info_json.get("class"));
                JSONObject classData = classDetails(classID);
                System.out.println(classData);
                String factionID = String.valueOf(info_json.get("faction"));
                String factionName = factionDetails(factionID);
                System.out.println(factionName);

                //Gets all character achivements
                String achievementData = getData(null, region, realm, name, "achievements");
                JSONParser achiParser = new JSONParser();
                JSONObject achievements_json = (JSONObject) achiParser.parse(achievementData);
                JSONObject achivements = charAchievements(achievements_json, factionName);
                System.out.println(achivements);
                System.out.println("Done");

                //Gets the talents of the character
                String talentData = getData(null, region, realm, name, "talents");
                System.out.println(talentData);
                JSONParser talentParser = new JSONParser();
                JSONObject talents_json = (JSONObject) talentParser.parse(talentData);
                String activeSpec = charTalents(talents_json);

                //Creates a char sheet depending on the query
                //This is mega scummy
                System.out.println(content);
                if (content.equals("pve")) {
                    JSONObject pve_character_sheet = new JSONObject();
                    String progressionData = getData(content, region, realm, name, "progression");
                    JSONParser progressParser = new JSONParser();
                    JSONObject allProgress_json = (JSONObject) progressParser.parse(progressionData);
                    System.out.println(allProgress_json);
                    JSONObject raidProg = charProgress(allProgress_json);
                    System.out.println(raidProg);
                    System.out.println("Creating character sheet...");

                    pve_character_sheet.put("name", info_json.get("name"));
                    pve_character_sheet.put("level", info_json.get("level"));
                    pve_character_sheet.put("realm", info_json.get("realm"));
                    pve_character_sheet.put("faction", factionName);
                    pve_character_sheet.put("spec", activeSpec);
                    pve_character_sheet.put("battlegroup", info_json.get("battlegroup"));
                    pve_character_sheet.put("faction", factionName);
                    pve_character_sheet.put("classcolour", classData.get("classcolour"));
                    pve_character_sheet.put("classtype", classData.get("classname"));
                    pve_character_sheet.put("armory", "http://" + region + ".battle.net/wow/en/character/" + name + "/" + realm);
                    pve_character_sheet.put("charportrait", info_json.get("thumbnail"));
                    pve_character_sheet.put("ilvl", items.get("averageItemLevelEquipped"));
                    pve_character_sheet.put("keystone_master", achivements.get("keystone_season_master"));
                    pve_character_sheet.put("keystone_conqueror", achivements.get("keystone_season_conqueror"));
                    pve_character_sheet.put("uldirFeat", achivements.get("ud_feat"));
                    pve_character_sheet.put("uldirProg", raidProg.get("uldir"));
                    pve_character_sheet.put("battleFeat", achivements.get("bod_feat"));
                    pve_character_sheet.put("battleProg", raidProg.get("battle"));
                    pve_character_sheet.put("crucibleFeat", achivements.get("cos_feat"));
                    pve_character_sheet.put("crucibleProg", raidProg.get("crucible"));
                    pve_character_sheet.put("palaceFeat", achivements.get("tep_feat"));
                    pve_character_sheet.put("palaceProg", raidProg.get("eternalPalace"));

                    System.out.println(pve_character_sheet);
                    return pve_character_sheet;
                }
                if(content.equals("pvp")){
                    //Creates the PvP charsheet object
                    JSONObject pvp_character_sheet = new JSONObject();
                    //Gets PvP data from the API and gives it to a function
                    String pvpData = getData(content, region, realm, name, "pvp");
                    JSONParser pvpParse = new JSONParser();
                    JSONObject pvpData_JSON = (JSONObject) pvpParse.parse(pvpData);
                    System.out.println(pvpData_JSON);
                    JSONObject pvpRatings = charProgesspvp(pvpData_JSON);
                    System.out.println("Creating character sheet...");

                    pvp_character_sheet.put("name", info_json.get("name"));
                    pvp_character_sheet.put("level", info_json.get("level"));
                    pvp_character_sheet.put("realm", info_json.get("realm"));
                    pvp_character_sheet.put("faction", factionName);
                    pvp_character_sheet.put("spec", activeSpec);
                    pvp_character_sheet.put("battlegroup", info_json.get("battlegroup"));
                    pvp_character_sheet.put("faction", factionName);
                    pvp_character_sheet.put("classcolour", classData.get("classcolour"));
                    pvp_character_sheet.put("classtype", classData.get("classname"));
                    pvp_character_sheet.put("armory", "http://" + region + ".battle.net/wow/en/character/" + name + "/" + realm);
                    pvp_character_sheet.put("charportrait", info_json.get("thumbnail"));
                    pvp_character_sheet.put("ilvl", items.get("averageItemLevelEquipped"));
                    pvp_character_sheet.put("arena_challenger", achivements.get("arena_challenger"));
                    pvp_character_sheet.put("arena_gladiator", achivements.get("arena_gladiator"));
                    pvp_character_sheet.put("arena_rival", achivements.get("arena_rival"));
                    pvp_character_sheet.put("arena_duelist", achivements.get("arena_duelist"));
                    pvp_character_sheet.put("2v2", pvpRatings.get("2v2"));
                    pvp_character_sheet.put("2v2s", pvpRatings.get("2v2s"));
                    pvp_character_sheet.put("3v3", pvpRatings.get("3v3"));
                    pvp_character_sheet.put("rbg", pvpRatings.get("rbg"));
                    pvp_character_sheet.put("kills", pvpRatings.get("kills"));
                    pvp_character_sheet.put("rbg2400Name", achivements.get("rbg_2400_name"));
                    pvp_character_sheet.put("rbg2400", achivements.get("rbg_2400"));
                    pvp_character_sheet.put("rbg2000Name", achivements.get("rbg_2000_name"));
                    pvp_character_sheet.put("rbg2000", achivements.get("rbg_2000"));
                    pvp_character_sheet.put("rbg1500Name", achivements.get("rbg_1500_name"));
                    pvp_character_sheet.put("rbg1500", achivements.get("rbg_1500"));

                    return pvp_character_sheet;

                }
            } catch (Exception e) {

            }

        }
        return null;
    }

    public static JSONObject charProgesspvp(JSONObject pvpData){
        JSONObject pvpPure = (JSONObject) pvpData.get("pvp");
        JSONObject bracketData = (JSONObject) pvpPure.get("brackets");
        JSONObject twovtwo = (JSONObject) bracketData.get("ARENA_BRACKET_2v2");
        Object twovtwoRating = twovtwo.get("rating");
        JSONObject twovtwoskirm = (JSONObject) bracketData.get("ARENA_BRACKET_2v2_SKIRMISH");
        Object twovtwoskrimRating = twovtwoskirm.get("rating");
        JSONObject threevthree = (JSONObject) bracketData.get("ARENA_BRACKET_3v3");
        Object threevthreeRating = threevthree.get("rating");
        JSONObject ratedbgs = (JSONObject) bracketData.get("ARENA_BRACKET_RBG");
        Object ratedbgRating = ratedbgs.get("rating");
        Object honorableKills = pvpData.get("totalHonorableKills");
        //Adds the data to the JSONObject
        JSONObject pvpRatings = new JSONObject();
        pvpRatings.put("2v2", twovtwoRating);
        pvpRatings.put("2v2s", twovtwoskrimRating);
        pvpRatings.put("3v3", threevthreeRating);
        pvpRatings.put("rbg", ratedbgRating);
        pvpRatings.put("kills", honorableKills);
        return pvpRatings;
    }

    public static JSONObject charProgress(JSONObject progressionData){
        JSONObject uldir = null;
        JSONObject battle = null;
        JSONObject crucible = null;
        JSONObject eternalPalace = null;

        System.out.println("Getting this expansion raid progress");
        JSONObject allRaidprog = (JSONObject) progressionData.get("progression");
        JSONArray allRaids = (JSONArray) allRaidprog.get("raids");
        System.out.println(allRaids);
        JSONObject uldirStats = null;
        JSONObject battleStats = null;
        JSONObject crucibleStats = null;
        JSONObject palaceStats = null;

        //Loops through the enitre JSONArray does some bad things
        for (int i=0; i < allRaids.size(); i++) {
            JSONObject raid = (JSONObject) allRaids.get(i);
            if (Integer.valueOf(Math.toIntExact((Long) raid.get("id"))).equals(constants.RAID_UD)){
                uldir = raid;
                uldirStats = calcBossKills(uldir);
                System.out.println(uldir);
            }
            if (Integer.valueOf(Math.toIntExact((Long) raid.get("id"))).equals(constants.RAID_BOD)){
                battle = raid;
                battleStats = calcBossKills(battle);
                System.out.println(battle);
            }
            if (Integer.valueOf(Math.toIntExact((Long) raid.get("id"))).equals(constants.RAID_COS)){
                crucible = raid;
                crucibleStats = calcBossKills(crucible);
                System.out.println(crucible);
            }
            if (Integer.valueOf(Math.toIntExact((Long) raid.get("id"))).equals(constants.RAID_TEP)){
                eternalPalace = raid;
                palaceStats = calcBossKills(eternalPalace);
                System.out.println(eternalPalace);
            }
        }

        JSONObject raidStats = new JSONObject();
        raidStats.put("uldir", uldirStats);
        raidStats.put("battle", battleStats);
        raidStats.put("crucible", crucibleStats);
        raidStats.put("eternalPalace", palaceStats);

        return raidStats;
    }

    public static JSONObject calcBossKills(JSONObject raidData){
        //Gets raid clearance
        int lfrKills = 0;
        int normalKills = 0;
        int heroicKills = 0;
        int mythicKills = 0;
        int raidSize;
        JSONArray bosses = (JSONArray) raidData.get("bosses");
        raidSize = bosses.size();
        System.out.println("Getting Boss Data");
        System.out.println(raidSize);
        for (int i=0; i < bosses.size(); i++) {
            JSONObject boss = (JSONObject) bosses.get(i);
            if (Integer.valueOf(Math.toIntExact((Long) boss.get("lfrKills"))) > 0){
                lfrKills = lfrKills + 1;
            }
            if (Integer.valueOf(Math.toIntExact((Long) boss.get("normalKills"))) > 0){
                normalKills = normalKills + 1;
            }
            if (Integer.valueOf(Math.toIntExact((Long) boss.get("heroicKills"))) > 0){
                heroicKills = heroicKills + 1;
            }
            if (Integer.valueOf(Math.toIntExact((Long) boss.get("mythicKills"))) > 0){
                mythicKills = mythicKills + 1;
            }

        }
        System.out.println("LFR: " + lfrKills);
        System.out.println("Normal: " + normalKills);
        System.out.println("Heroic: " + heroicKills);
        System.out.println("Mythic: " + mythicKills);

        JSONObject raidKills = new JSONObject();
        raidKills.put("lfr", lfrKills);
        raidKills.put("normal", normalKills);
        raidKills.put("heroic", heroicKills);
        raidKills.put("mythic", mythicKills);
        raidKills.put("bosses", raidSize);


        return raidKills;
    }

    public static String charTalents(JSONObject talentData){
        System.out.println("Getting Talent and Spec data");
        //Gets all possible talents for the class and puts it into a JSONArray
        JSONArray allTalents = (JSONArray) talentData.get("talents");
        //Gets all selected talents aswell as the spec that is currently being played as a JSONObject
        JSONObject selectedTalents = (JSONObject) allTalents.get(0);
        JSONObject activeSpec = (JSONObject) selectedTalents.get("spec");
        //Gets the name of the active spec -- THIS IS EXTREMELY MESSY AND NEEDS TO BE REFACTORED
        String activeName = (String) activeSpec.get("name");
        System.out.println(activeName);
        return activeName;
    }

    public static JSONObject charAchievements(JSONObject achievements_json, String factionName) throws ParseException {
        //Gets achivements from the object
        //This code is a mess but it works
        System.out.println("Getting Achievements");
        JSONObject allAchivements = (JSONObject) achievements_json.get("achievements");
        JSONArray completedAchivementsjson = (JSONArray) allAchivements.get("achievementsCompleted");

        //Converts the JSONArray into a Java Array List.
        ArrayList<String> completedAchivements = new ArrayList<>();
        if (completedAchivementsjson == null) {
            System.out.println("json is empty");
        }
        else
        {
            int length = completedAchivementsjson.size();
            for (int i=0;i<length;i++){
                completedAchivements.add(completedAchivementsjson.get(i).toString());
            }
        }

        //Setting notable achivements.
        String keystone_season_master = "In Progress";
        String keystone_season_conqueror = "In Progress";
        String arena_challenger = "In Progress";
        String arena_rival = "In Progress";
        String arena_duelist = "In Progress";
        String arena_gladiator = "In Progress";
        String rbg_2400 = "In Progress";
        String rbg_2000 = "In Progress";
        String rbg_1500 = "In Progress";
        String rbg2400n = "";
        String rbg2000n = "";
        String rbg1500n = "";
        String ud_feat = "";
        String bod_feat = "";
        String cos_feat = "";
        String tep_feat = "";

        //Checks for Mythic Plus feats of strength
        if(completedAchivements.contains(constants.AC_SEASON_KEYSTONE_MASTER)){
            keystone_season_master = "Completed";
        }
        if(completedAchivements.contains(constants.AC_SEASON_KEYSTONE_CONQUEROR)){
            keystone_season_conqueror = "Completed";
        }

        //Checks for PvP feats of strength
        if(completedAchivements.contains(constants.AC_ARENA_CHALLENGER)){
            arena_challenger = "Completed";
        }
        if(completedAchivements.contains(constants.AC_ARENA_RIVAL)){
            arena_rival = "Completed";
        }
        if(completedAchivements.contains(constants.AC_ARENA_DUELIST)){
            arena_duelist = "Completed";
        }
        if(completedAchivements.contains(constants.AC_ARENA_GLADIATOR)){
            arena_gladiator = "Completed";
        }

        //Checks for Raid feats of strength(AoTC or CE)
        if(completedAchivements.contains(constants.AC_AOTC_UD)){
            ud_feat = "Ahead of the Curve";
        }
        if(completedAchivements.contains(constants.AC_CE_UD)){
            ud_feat = "Cutting Edge";
        }
        if(completedAchivements.contains(constants.AC_AOTC_BOD)){
            bod_feat = "Ahead of the Curve";
        }
        if(completedAchivements.contains(constants.AC_CE_BOD)){
            bod_feat = "Cutting Edge";
        }
        if(completedAchivements.contains(constants.AC_AOTC_COS)){
            cos_feat = "Ahead of the Curve";
        }
        if(completedAchivements.contains(constants.AC_CE_COS)){
            cos_feat = "Cutting Edge";
        }
        if(completedAchivements.contains(constants.AC_AOTC_TEP)){
            tep_feat = "Ahead of the Curve";
        }
        if(completedAchivements.contains(constants.AC_CE_TEP)){
            tep_feat = "Cutting Edge";
        }

        //Checks for faction specific PvP achivements
        //Alliance
        if(factionName.equals("Alliance")){
            rbg2400n = constants.AC_GRAND_MARSHALL_NAME;
            rbg2000n = constants.AC_LIEAUTENANT_COMMANDER_NAME;
            rbg1500n = constants.AC_SERGEANT_MAJOR_NAME;
        }
        if(completedAchivements.contains(constants.AC_GRAND_MARSHALL)){
            rbg_2400 = "Completed";
        }
        if(completedAchivements.contains(constants.AC_LIEUTENANT_COMMANDER)){
            rbg_2000 = "Completed";
        }
        if(completedAchivements.contains(constants.AC_SERGEANT_MAJOR)){
            rbg_1500 = "Completed";
        }

        //Horde
        if(factionName.equals("Horde")){
            rbg2400n = constants.AC_HIGH_WARLORD_NAME;
            rbg2000n = constants.AC_CHAMPION_NAME;
            rbg1500n = constants.AC_FIRST_SERGEANT_NAME;
        }
        if(completedAchivements.contains(constants.AC_HIGH_WARLORD)){
            rbg_2400 = "Completed";
        }
        if(completedAchivements.contains(constants.AC_CHAMPION)){
            rbg_2000 = "Completed";
        }
        if(completedAchivements.contains(constants.AC_FIRST_SERGEANT)){
            rbg_1500 = "Completed";
        }

        //Creates the JSONObject
        JSONObject achievement_list = new JSONObject();
        achievement_list.put("keystone_season_master", keystone_season_master);
        achievement_list.put("keystone_season_conqueror", keystone_season_conqueror);
        achievement_list.put("arena_challenger", arena_challenger);
        achievement_list.put("arena_rival", arena_rival);
        achievement_list.put("arena_duelist", arena_duelist);
        achievement_list.put("arena_gladiator", arena_gladiator);
        achievement_list.put("rbg_2400_name", rbg2400n);
        achievement_list.put("rbg_2000_name", rbg2000n);
        achievement_list.put("rbg_1500_name", rbg1500n);
        achievement_list.put("rbg_2400", rbg_2400);
        achievement_list.put("rbg_2000", rbg_2000);
        achievement_list.put("rbg_1500", rbg_1500);
        achievement_list.put("ud_feat", ud_feat);
        achievement_list.put("bod_feat", bod_feat);
        achievement_list.put("cos_feat", cos_feat);
        achievement_list.put("tep_feat", tep_feat);

        return achievement_list;
    }


    public static JSONObject classDetails(String classID){
        //Takes the class ID and returns the name
        System.out.println("Getting Class Data");
        int class_colour = 0;
        String class_name = null;
        //Warrior
        if (classID.equals("1")){
            class_colour = constants.CLASS_WARRIOR_COLOUR;
            class_name = constants.CLASS_WARRIOR_NAME;
        }
        //Paladin
        if (classID.equals("2")){
             class_colour = constants.CLASS_PALADIN_COLOUR;
             class_name = constants.CLASS_PALADIN_NAME;
        }
        //Hunter
        if (classID.equals("3")){
             class_colour = constants.CLASS_HUNTER_COLOUR;
             class_name = constants.CLASS_HUNTER_NAME;
        }
        //Rogue
        if (classID.equals("4")){
             class_colour = constants.CLASS_ROGUE_COLOUR;
             class_name = constants.CLASS_ROGUE_NAME;
        }
        //Priest
        if (classID.equals("5")){
             class_colour = constants.CLASS_PRIEST_COLOUR;
             class_name = constants.CLASS_PRIEST_NAME;
        }
        //Death Knight
        if (classID.equals("6")){
             class_colour = constants.CLASS_DEATH_KNIGHT_COLOUR;
             class_name = constants.CLASS_DEATH_KNIGHT_NAME;
        }
        //Shaman
        if (classID.equals("7")){
             class_colour = constants.CLASS_SHAMAN_COLOUR;
             class_name = constants.CLASS_SHAMAN_NAME;
        }
        //Mage
        if (classID.equals("8")){
             class_colour = constants.CLASS_MAGE_COLOUR;
             class_name = constants.CLASS_MAGE_NAME;
        }
        //Warlock
        if (classID.equals("9")){
             class_colour = constants.CLASS_WARLOCK_COLOUR;
             class_name = constants.CLASS_WARLOCK_NAME;
        }
        //Monk
        if (classID.equals("10")){
             class_colour = constants.CLASS_MONK_COLOUR;
             class_name = constants.CLASS_MONK_NAME;
        }
        //Druid
        if (classID.equals("11")){
             class_colour = constants.CLASS_DRUID_COLOUR;
             class_name = constants.CLASS_DRUID_NAME;
        }
        //Demon Hunter
        if (classID.equals("12")){
            class_colour = constants.CLASS_DEMON_HUNTER_COLOUR;
            class_name = constants.CLASS_DEMON_HUNTER_NAME;
        }
        JSONObject classData = new JSONObject();
        classData.put("classcolour", class_colour);
        classData.put("classname", class_name);
        return classData;

    }

    public static String factionDetails(String factionID){
        //Takes the faction ID and returns the name
        System.out.println("Getting Faction data");
        String factionName = null;
        int i = Integer.parseInt(factionID);
        if (i == constants.FACTION_HORDE){
            factionName = constants.FACTION_HORDE_NAME;
        }
        if (i == constants.FACTION_ALLIANCE){
            factionName =  constants.FACTION_ALLIANCE_NAME;
        }
        return factionName;
    }

    public static String getAToken(String regionreturn){
        String accesstoken = null;
        try (AsyncHttpClient asyncClient = new AsyncHttpClient()){
            final String user = settings.WOW_CLIENT_ID;
            final String password = settings.WOW_CLIENT_SECRET;
            final String fetchURL = "https://" + regionreturn + ".battle.net/oauth/token";
            final String encoded = Base64.getEncoder().encodeToString((user + ':' + password).getBytes(StandardCharsets.UTF_8));
            final String body = asyncClient
                    .prepareGet(fetchURL)
                    .addHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                    .addQueryParam("grant_type", "client_credentials")
                    .execute()
                    .get()
                    .getResponseBody(StandardCharsets.UTF_8.name());
            //this was not a fun time to figure out
            String[] fuck = body.split(",");
            String[] shit = fuck[0].split(":");
            String accesstoken1 = shit[1];
            accesstoken = accesstoken1.substring(1, accesstoken1.length()-1);
        } catch(Exception e) {
            accesstoken = "credential_error";
            System.out.println(e);
        }
        return accesstoken;
    }

    //Method goes to getData and returns the price of a WoWToken in copper
    //Botcls.java handles converting the price into gold by dividing by 10000
    public static String wow_token_price(String content, String regionreturn) {
       String info =  getData(regionreturn, content, null, null, null);
       return info;
    }
}
