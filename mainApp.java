package stegfs_dropbox;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;




public class mainApp {
	
	static String authToken ="";

	
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
	
	
	public static void processFile(File file) throws Exception {
		
		// check if a file is already stored in metaStorage, add if not
		metaStorage.loadDecrypt("C:/KEYSTORE/metaStorage.db");
		
		if (metaStorage.contains(file.getName())){
			System.out.println("already stored");
		}
		
		else {
			// generate a random salt, then add the metadata object (filename, salt) to metadata storage
			metaStorage.add(file.getName(), new metadata (Auth.getRandomSalt()));
			System.out.println("file " + file.getName() + " added to storage");
			
			// update metadata storage to disk
			metaStorage.saveEncrypted("C:/KEYSTORE/metaStorage.db");
			
			//write the file to stegfs
			//stegfs write  filename:
			
			String passPerFile = Auth.calcPassPerFile(authToken, Auth.getRandomSalt());
			System.out.println("Write to stegfs: " + file.getName() + ":" + passPerFile);
		}
	
	}
	
	
	
	public static void printFiles(){
		
		List<String> listOfFiles = metaStorage.getAllFiles();
		System.out.println("List of files:");
		for (int i=0; i<listOfFiles.size(); i++) {
			System.out.println("Filename: " + listOfFiles.get(i));
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {

		// AUTHENTICATION
		// password authentication
		//String password = Auth.getPassword();
		String password = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b";
		
		// key-file authentication
		URI uri = new URI("file:///C:/KEYSTORE/keyfile.txt");
		String key = Auth.getKeyFile(uri);
		
		// 2FA authentication
		authToken = Auth.calculateAuthToken(password ,key);
		//System.out.println(authToken);
		
		
		
		// STORAGE
		// scan drop-directory regularly, add new files to metadata storage, ignore existing files
		
		
		// metaStorage.erase(); // erase storage for testing
		
		while (1==1) { // daemon to run in background
		scanDirectory("C:/stegdrop/");
		TimeUnit.SECONDS.sleep(10);
		
	}
	
	}
		
		
		

}
