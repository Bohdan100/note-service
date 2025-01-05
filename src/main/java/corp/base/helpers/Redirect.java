package corp.base.helpers;

import static corp.base.constants.Constants.VERSION;

public class Redirect {
    public static String buildRedirectUrl(String path) {
        return "redirect:/" + VERSION + path;
    }
}
