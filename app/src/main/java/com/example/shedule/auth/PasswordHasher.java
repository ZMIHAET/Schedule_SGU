package com.example.shedule.auth;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    // Метод для хэширования пароля
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    // Метод для проверки пароля
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
