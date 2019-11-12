public class settings {
    //Default Realm and Region
    public static final String WOW_REGION = "eu";
    public static final String WOW_REALM = "Twisting-Nether";
    public static final String LOCALE = "en-GB";
    //World of Warcraft API Settings
    public static final String WOW_CLIENT_ID = tokenStore.apiKey();
    public static final String WOW_CLIENT_SECRET = tokenStore.apiSecret();
    //API Connection Errors - for when things break
    public static final String NOT_FOUND_ERROR = "Could not find a character with that name, realm or region combination. Type `!armory help` for a list of valid commands. :hammer_pick:";
    public static final String CONNECTION_ERROR = "There was an issue establishing a connection to the Blizzard API. Please try again. :electric_plug:";
    public static final String CREDENTIAL_ERROR = "There was an error generating the auth token. Either the Blizzard auth API was not reachable or your Blizzard API credentials are not correct. :fire:";
    public static final String UNKNOWN_ERROR = "An unknown error occurred while attempting to retrieve this character. If this error continues to persist please create a bug report on Github. :space_invader:";
    public static final String GOLD_ERROR = "There was an error retreiving the price of the WoW token for this region. Please try again. :electric_plug:";

}
