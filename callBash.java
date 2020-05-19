package stegfs_dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;




/**
 * Relay to call bash commands
 * 
 */

public class callBash {
	
	   
	
	/** Issue a string of commands to the bash
     */
    public static ArrayList<String> command(final String cmdline) {
        try {
            Process process = 
                new ProcessBuilder(new String[] {"bash", "-c", cmdline})
                    .redirectErrorStream(true)
                    .start();

            ArrayList<String> output = new ArrayList<String>();
            BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line = null;
            while ( (line = br.readLine()) != null )
                output.add(line);

            //There should really be a timeout here.
            if (0 != process.waitFor())
                return null;

            return output;

        } catch (Exception e) {
          System.out.println("Error");

            return null;
        }
    }


    
    /**
     * Create a RAM disk at /mnt/StegDrop
     * 
     */
	public static void createRamDisk() {
			
			command("mkdir /mnt/StegDrop; "
				  + "mount -t ramfs ramfs /mnt/StegDrop");
		}
	
	
	 /**
     * Erase content on the ram disk using the Gutmann method
     * 
     */
	public static void destroyRamDisk() {
			
			// Gutmann file erase: find all content on /mnt/ramdisk, force to write 50 times randomly, then all zeros
			// http://manpages.ubuntu.com/manpages/trusty/man1/shred.1.html
			command("find /mnt/StegDrop -type f -print0 | xargs -0 shred -fuz n- 50 -u;"
					+ "rm -r /mnt/StegDrop/*"); // remove empty sub-directories
		}
	
	
	 /**
     * Write a file to steganographic filesystem
     * file_auth must be in the format of String (file:authentication)
     */
	public static void writeToStegFS(String file_auth) {
			
			String filename = file_auth.split(":")[0];
			command("cp /mnt/StegDrop/" + filename + " /mnt/stegfs-2/" + file_auth);
			System.out.println("Write to stegfs: " + file_auth);
				
		}
	
	
	/**
     * Read a file from steganographic filesystem
     * file_auth must be in the format of String (file:authentication)
     */
	public static void readFromStegFS(String file_auth) {
		System.out.println(file_auth);
			String filename = file_auth.split(":")[0];
			command("cp /mnt/stegfs-2/" + file_auth + " /mnt/StegDrop/" + filename);
			System.out.println(filename + " fetched from stegfs");
				
		}
	
	

	

	
	 
	


}
