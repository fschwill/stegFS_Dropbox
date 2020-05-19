package stegfs_dropbox;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;




public class mainApp {
	
	static String authToken ="";

	
	/**
	 * Get all files that are present in a directory
	  @param input The path to the directory
	  @return An array holding all files of a specific type
	 * 
	*/
	public static File[] getFiles(File input){
        
		// if input is a directory, return all files
		if (input.isDirectory()) {
        	
            return input.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                	return filename.endsWith(".txt");
                }
            });
        }
        else {
            // else return an empty array
            return new File[0];
            
        }
	}
	
	
	/**
	 * Scan a directory for files
	  @param directory the path were to scan
	 * 
	*/
	public static void scanDirectory(String directory) throws Exception{
		
		File dropDirectory = new File (directory);
		File[] files = getFiles(dropDirectory);
		if(files.length!=0) {
			System.out.println("Found " + files.length + " files");
		}
		
		// loop through every file
			for (int i=0; i<files.length; i++) {
					
				//process file
				processFile(files[i]);	
			}
	}
	
	
	
	/**
	 * Process a file
	 * check if it is already stored. 
	 * 		if yes: ignore. 
	 * 		if not: generate a random salt, add it to the metadata store, generate a per-file authenticator, then write the file to stegFS
	  
	  @param input The path to the directory
	 * 
	*/
	public static void processFile(File file) throws Exception {
		
		// check if a file is already stored in metaStorage, add if not
		metaStorage.loadDecrypt("/mnt/share/metaStorage.db");
		
		if (metaStorage.contains(file.getName())){
			System.out.println("already stored");
		}
		
		else {
			// generate a random salt, add it to the metadata store, encrypt the metadata store , generate a per-file authenticator from (authToken XOR salt), then write the file to stegFS
			String salt = Auth.getRandomSalt();
			metaStorage.add(file.getName(),new metadata (salt));
			System.out.println("file " + file.getName() + " added to metadata storage");
			
			// update metadata storage to disk
			metaStorage.saveEncrypted("/mnt/share/metaStorage.db");
			
			//generate a per-file write the file to stegfs
			String passPerFile = Auth.calcPassPerFile(authToken, salt);
			callBash.writeToStegFS(file.getName() + ":" + authToken); //TODO: change to passperfile
		}
	
	}
	

	/**
	 * Print a list of all files stored in the metadata storage
	 * 
	*/
	public static void printFiles(){
		
		List<String> listOfFiles = metaStorage.getAllFiles();
		System.out.println("List of files:");
		for (int i=0; i<listOfFiles.size(); i++) {
			System.out.println("Filename: " + listOfFiles.get(i));
		}
		
	}
	
	
	/**
	 * Get all files from the steganographic file system
	 * Used at launch in order to copy all files from stegFS to the DropSteg folder on ram-disk
	 * 
	*/
	public static void readAllFromFS() throws InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IOException {
		
		metaStorage.loadDecrypt("/mnt/share/metaStorage.db");
		List<String> listOfFiles = metaStorage.getAllFiles();
		for (int i=0; i<listOfFiles.size(); i++) {
			String filename = listOfFiles.get(i);
			
			/* TODO: change to salt
			String salt = metaStorage.getSalt(filename);
			System.out.println("exists " + metaStorage.contains(listOfFiles.get(i)) + " filename " + filename + "salt" + salt);
			
			// only proceed if salt and authtoken are available
			if (salt !=null) {
			String passPerFile = Auth.calcPassPerFile(authToken, salt);
			*/
			callBash.readFromStegFS(filename + ":" + authToken);
			
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {

		// AUTHENTICATION
		// password authentication
		//String password = Auth.getPassword();
		String password = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
		
		// key-file authentication
		//URI uri = new URI("/mnt/share/key.file");
		//String key = Auth.getKeyFile(uri);
		String key = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e14c";
		
		// 2FA authentication
		authToken = Auth.calculateAuthToken(password ,key);
		//System.out.println(authToken);
		
		
		
		// STORAGE
		// scan drop-directory regularly, add new files to metadata storage, ignore existing files
		
		
		//metaStorage.erase(); // erase storage for testing
		//createRamDisk();
		readAllFromFS();
		
		while (1==1) { // daemon to run in background
		scanDirectory("/mnt/StegDrop");
		TimeUnit.SECONDS.sleep(10);
		
	}
	
	}
		
		
		

}
