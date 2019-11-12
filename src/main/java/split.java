import java.net.*;

public class split {
//This splits the message so i can do shit with it
    public static String splitQuery(String message, String content) throws MalformedURLException {
        String[] splitMessage = message.split("\\s+");
        System.out.println(splitMessage[0]);
        String rtnstr;
        String[] splitopt = new String[0];
        if (message.contains("worldofwarcraft") || message.contains("battle.net")) {
            String dataURL = splitMessage[2];
            System.out.println(dataURL);
            URL infoURL = new URL(dataURL);
            //Checks what info i can get from the URL if one is passed
            System.out.println("protocol = " + infoURL.getProtocol());
            System.out.println("authority = " + infoURL.getAuthority());
            System.out.println("host = " + infoURL.getHost());
            System.out.println("filename = " + infoURL.getFile());
            //Check if a region was provided, otherwise use the region setting.
            try {
                rtnstr = infoURL.getHost() + " " + content + " " + splitMessage[3];
                System.out.println(rtnstr);
            } catch (ArrayIndexOutOfBoundsException e) {
                rtnstr = infoURL.getHost() + " " + content + " " + settings.WOW_REGION;
                System.out.println(rtnstr);
            }
            System.out.println(rtnstr);
            return rtnstr;
        // Assumes it's not a url path, and splits the string normally.
        } else {
            if (content == "wow_token") {
                String region;
                try {
                    region = splitMessage[2];
                    System.out.println(region);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(settings.WOW_REGION);
                    region = settings.WOW_REGION;
                }
                System.out.println(region);
                return region;
            } else {
                String defreturn;
                try {
                    defreturn = splitMessage[2] + " " + splitMessage[3] + " " + content + " " + splitMessage[4];
                    System.out.println(defreturn);
                } catch (ArrayIndexOutOfBoundsException e) {
                    defreturn = splitMessage[2] + " " + splitMessage[3] + " " + content + " " + settings.WOW_REGION;
                    System.out.println(defreturn);
                }
                return defreturn;
            }
        }
    }
}
