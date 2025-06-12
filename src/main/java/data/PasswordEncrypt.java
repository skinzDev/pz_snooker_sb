package data;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Password Encryption Utility
 * <p>
 * This utility class provides static methods for handling password security.
 * It uses the BCrypt algorithm to create a salted hash of a user's password for storage
 * and to verify a submitted password against the stored hash.
 * </p>
 *
 * @author Andrija Milovanovic
 * @version 1.0
 */
public class PasswordEncrypt {

    /**
     * Hashes a plain-text password using BCrypt.
     *
     * @param password The plain-text password to hash.
     * @return A salted and hashed password string.
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Checks if a plain-text password matches a hashed password.
     *
     * @param password       The plain-text password to check.
     * @param hashedPassword The hashed password from the database to compare against.
     * @return {@code true} if the password matches the hash, {@code false} otherwise.
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
