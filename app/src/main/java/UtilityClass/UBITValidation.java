package UtilityClass;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by Rachel on 10/23/17.
 */

public class UBITValidation {

    /* parse user's UBIT from UB email */
    public static String parseUBIT(String email){
        return email.substring(0, email.indexOf("@"));
    }

    /* check if this account is UB email */
    public static boolean isUBEmail(GoogleSignInAccount account){
        return account != null && account.getEmail().contains("@buffalo.edu");
    }
}
