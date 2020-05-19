package stegfs_dropbox;


/**
 * Metadata object to hold (filename, salt) pairs.
 */

public class metadata implements java.io.Serializable {
	
	
	private static final long serialVersionUID = 1L;
		
	    private static String salt;

	    
	    /**
	     * Create a DataPoint
	     * @param songId
	     * @param time
	     */
	    public metadata(String salt) {
	        metadata.salt = salt;
	    }
	    

	    /**
	     * Get salt
	     * @return time
	     */
	    public static String getSalt() {
	        return metadata.salt;
	    }


	}
