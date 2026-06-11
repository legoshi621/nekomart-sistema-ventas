package com.nekomart.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase de utilidad para el manejo y encriptación de contraseñas utilizando SHA-256.
 * Todo el código está documentado y comentado en español según los requerimientos.
 */
public class PasswordUtils {

    /**
     * Encripta una contraseña en texto plano utilizando el algoritmo SHA-256.
     *
     * @param password Contraseña en texto plano a encriptar.
     * @return El hash SHA-256 resultante en formato hexadecimal de 64 caracteres en minúsculas.
     */
    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            // Obtener instancia del algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Convertir la contraseña a bytes y calcular el hash
            byte[] hashBytes = digest.digest(password.getBytes());
            
            // Convertir el arreglo de bytes en formato hexadecimal legible (string de 64 caracteres)
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0'); // Completar con cero a la izquierda si es de un dígito
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: No se pudo encontrar el algoritmo SHA-256 para la encriptación.", e);
        }
    }

    /**
     * Compara una contraseña en texto plano contra su equivalente en formato Hash SHA-256.
     *
     * @param password Contraseña en texto plano ingresada por el usuario.
     * @param hashedPassword Hash SHA-256 previamente almacenado en la base de datos.
     * @return true si el hash de la contraseña coincide con el hash almacenado, de lo contrario false.
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        String hash = hashPassword(password);
        return hash.equalsIgnoreCase(hashedPassword);
    }
}
