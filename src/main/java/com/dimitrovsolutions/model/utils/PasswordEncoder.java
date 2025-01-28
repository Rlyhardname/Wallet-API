package com.dimitrovsolutions.model.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password encoder and validator
 */
public class PasswordEncoder {

    public static String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    public static boolean decodeMatches(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}