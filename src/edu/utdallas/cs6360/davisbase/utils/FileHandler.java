package edu.utdallas.cs6360.davisbase.utils;

import edu.utdallas.cs6360.davisbase.Config;
import edu.utdallas.cs6360.davisbase.DatabaseType;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileHandler {
    private static final Logger LOGGER = Logger.getLogger(FileHandler.class.getName());
	
	/**
	 * Private constructor to override the implicit constructor
	 */
	private FileHandler() { throw new IllegalStateException("FileHandler Utility Class"); }
    
    /**
     * This static method creates the DavisBase data storage container
     * and then initializes two .tbl files to implement the two
     * system tables, davisbase_tables and davisbase_columns
     */
    public static void initializeDataStore() {

        /** Create data directory at the current OS location to hold */
        createDatabaseDirectory(Config.DATA_DIRECTORY);
        createDatabaseDirectory(Config.CATALOG_DIRECTORY);
        createDatabaseDirectory(Config.USER_DATA_DIRECTORY);

        /** Create davisbase_tables system catalog */
        createTableFile(getTableFileName(Config.CATALOG_TABLE, DatabaseType.CATALOG));

        /** Create davisbase_columns system catalog */
        createTableFile(getTableFileName(Config.CATALOG_COLUMN, DatabaseType.CATALOG));

    }


    /**
     * Creates directory if it doesn't exist
     *
     * @param name Directory name
     */
    private static void createDatabaseDirectory(String name) {
        try {
            File dataDir = new File(name);
            if (!dataDir.exists()) {
                while (true) {
                    if (dataDir.mkdir()) {
                        break;
                    }
                }
            }
        } catch (SecurityException se) {
	        LOGGER.log(Level.SEVERE, "Unable to create " + name + " container directory");
	        LOGGER.log(Level.SEVERE, se.toString());
        }
    }
	
	/**
	 * A static method to return a table file name given it's name
	 * @return
	 */
	static String getTableFileName(String tableName, DatabaseType type) {
		if(type == DatabaseType.USER) {
			return Config.USER_DATA_DIRECTORY + "/" + tableName + ".tbl";
		} else {
			return Config.CATALOG_DIRECTORY + "/" + tableName + ".tbl";
		}
	}
	
    /**
     * A static method that creates a file from a table name and checks
     * if it exists and is not a directory
     * @param fileName the full path for the table
     * @return boolean if the file exists and is not a directory
     */
    public static boolean doesTableExist(String fileName) {
        File f = new File(fileName);
        return f.exists() && !f.isDirectory();
    }
    
    public static boolean createTable(String tableName) {
    	return createTableFile(getTableFileName(tableName, DatabaseType.USER));
    }
    
    public static boolean createTableFile(String fileName) {
        if (!doesTableExist(fileName)) {
            try (RandomAccessFile file = new RandomAccessFile(fileName, "rw")) {
	            file.setLength(Config.PAGE_SIZE);
            } catch (IOException e1) {
	            LOGGER.log(Level.SEVERE, "Unable to create the " + fileName + " file");
	            LOGGER.log(Level.SEVERE, e1.toString());
            }
            return true;
        }else{
            return false;
        }
    }

}