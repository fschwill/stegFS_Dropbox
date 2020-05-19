package stegfs_dropbox;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Random;

public class Auth {
	
	
	/**
	 * Get the key file 
	  @param input The path to the key file
	  @return A string containing the hashed key (SHA256) in hex
	 * @throws IOException 
	*/
	
	public static String getKeyFile(URI input){
			
			// return the key if a key file exists
			try {
				
				// read the key from file
				String key = new String(Files.readAllBytes(Paths.get(input)), StandardCharsets.UTF_8);
				
				// create SHA256(key), stored in hex
				String hashedKey = org.apache.commons.codec.digest.DigestUtils.sha256Hex(key);   
				return hashedKey;
			} 
			
			// output an error if no key file was found, return an empty string
			catch (IOException e) {
				System.out.println("Error: 2FA - key file not found");
				return new String ();
			}
	}
	
	
	/**
	 * Read the user password from console
	  
	  @return A string containing the hashed password (SHA256) in hex
	 * 
	*/
	public static String getPassword() {
		
		System.out.println("Please enter your password");
		
		// read password silently from console
		char[] input = System.console().readPassword();
		String password = String.copyValueOf(input);
		
		// create SHA256(password), stored in hex
		String hashedPassword = org.apache.commons.codec.digest.DigestUtils.sha256Hex(password);   
		return hashedPassword;
	}
	
	
	/**
	 * Calculate the authentication token to be used to encrypt a file with stegFS
	 * authToken = (SHA256(password) bitwise-XOR SHA256(2FAkey))
	 * 
	  @param password SHA256 of the user password
	  @param key SHA256 of the 2FA key
	  @return A string containing the X bit authentication token
	*/
	
	public static String calculateAuthToken(String password, String key) {
		
		char[] chars = new char[password.length()];
		
		// for every character: XOR
		for (int i = 0; i < chars.length; i++) {
				  chars[i] = toHex(fromHex(password.charAt(i)) ^ fromHex(key.charAt(i)));
			 }
				
		return new String(chars);
	}
	
	/**
	 * Convert char to hex
	 * 
	  @param c character input
	  @return c integer
	*/
	public static int fromHex(char c) {
	    if (c >= '0' && c <= '9') {
	        return c - '0';
	    }
	    if (c >= 'a' && c <= 'f') {
	        return c - 'a' + 10;
	    }
	    throw new IllegalArgumentException();
	}
	
	
	
	/**
	 * Convert int to hex
	 * 
	  @param nybble integer input
	  @return hex character
	*/
	public static char toHex(int nybble) {
	    if (nybble < 0 || nybble > 15) {
	        throw new IllegalArgumentException();
	    }
	    return "0123456789abcdef".charAt(nybble);
	}
	
	
	
	/**
	 * Generate a random salt of 256 bit
	 * 
	  @return  random salt in hex
	*/
	
	public static String getRandomSalt() {
		
		final Random rand = new SecureRandom();
		byte[] s = new byte[32];
		rand.nextBytes(s);
		
		String salt = org.apache.commons.codec.digest.DigestUtils.sha256Hex(s);
		return salt;
		
	}
	
	
	
	/**
	 * Generate a per-file password used to store each file
	 * password = (authtoken XOR salt)
	 * 
	  @param authToken the token to authenticate the user
	  @param salt random salt
	  @return pass a per-file password used to write  file to stegfs
	*/
	
	public static String calcPassPerFile(String authToken, String salt){
		
		char[] chars = new char[authToken.length()];
		
		// for every character: XOR
		for (int i = 0; i < chars.length; i++) {
				  chars[i] = toHex(fromHex(authToken.charAt(i)) ^ fromHex(salt.charAt(i)));
			 }
				
		return new String(chars);	
	}
	

}
