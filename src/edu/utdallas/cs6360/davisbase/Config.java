package edu.utdallas.cs6360.davisbase;

/**
 * A Configuration class to hold settings
 * @author Charles Krol
 */
final class Config {
	
	/**
	 * Private constructor so the class cannot be instantiated
	 */
	private Config() { throw new IllegalStateException("Config Utility Class");}
	
	static final String DATA_DIRECTORY = "data";
	static final String CATALOG_DIRECTORY = DATA_DIRECTORY + "/" + "catalog";
	static final String USER_DATA_DIRECTORY = DATA_DIRECTORY + "/" + "user_data";
	static final String CATALOG_TABLE = CATALOG_DIRECTORY + "/" + "davisbase_tables";
	static final String CATALOG_COLUMN = CATALOG_DIRECTORY + "/" + "davisbase_columns";
	
	/**
	 * Page size for all files is 512 bytes by default.
	 * You may choose to make it user modifiable
	 */
	static final int PAGE_SIZE = 512;
	static final String COPYRIGHT = "Â©2016 Chris Irwin Davis";
	static final String VERSION = "v1.0b(example)";
	
	static final String CHARACTER_SET = "UTF-8";
}
