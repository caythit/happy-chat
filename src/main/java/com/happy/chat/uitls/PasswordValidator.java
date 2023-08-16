package com.happy.chat.uitls;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {
    private static PasswordValidator INSTANCE = new PasswordValidator();
    private static String pattern = null;

    /**
     * No one can make a direct instance
     */
    private PasswordValidator() {
        //do nothing
    }

    /**
     * Force the user to build a validator using this way only
     */
    public static PasswordValidator buildValidator(boolean forceSpecialChar,
                                                   boolean forceCapitalLetter,
                                                   boolean forceNumber,
                                                   int minLength,
                                                   int maxLength) {
        StringBuilder patternBuilder = new StringBuilder("((?=.*[a-z])");

        if (forceSpecialChar) {
            patternBuilder.append("(?=.*[@#$%])");
        }

        if (forceCapitalLetter) {
            patternBuilder.append("(?=.*[A-Z])");
        }

        if (forceNumber) {
            patternBuilder.append("(?=.*\\d)");
        }

        patternBuilder.append(".{" + minLength + "," + maxLength + "})");
        pattern = patternBuilder.toString();

        return INSTANCE;
    }

    /**
     * Here we will validate the password
     */
    public boolean validatePassword(final String password) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(password);
        return m.matches();
    }
}