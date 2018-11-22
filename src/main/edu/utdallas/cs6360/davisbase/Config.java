package edu.utdallas.cs6360.davisbase;

/**
 * A Configuration class to hold settings
 * @author Charles Krol
 */
public final class Config {
	
	/**
	 * Private constructor so the class cannot be instantiated
	 */
	private Config() { throw new IllegalStateException("Config Utility Class");}
	
	public static final String DATA_DIRECTORY = "data";
	public static final String CATALOG_DIRECTORY = DATA_DIRECTORY + "/" + "catalog";
	public static final String USER_DATA_DIRECTORY = DATA_DIRECTORY + "/" + "user_data";
	public static final String CATALOG_TABLE = CATALOG_DIRECTORY + "/" + "davisbase_tables";
	public static final String CATALOG_COLUMN = CATALOG_DIRECTORY + "/" + "davisbase_columns";
	
	/**
	 * Page size for all files is 512 bytes by default.
	 * You may choose to make it user modifiable
	 */
	public static final int PAGE_SIZE = 512;
	public static final byte PAGE_HEADER_SIZE = 8;
	public static final byte TABLE_INTERIOR_CELL_HEADER_SIZE = 8;
	public static final short MAX_TEXT_FIELD_LENGTH = 127;
	public static final byte DATARECORD_KEY_SIZE = Integer.BYTES;
	
	
	public static final String COPYRIGHT = "Â©2016 Chris Irwin Davis";
	public static final String VERSION = "v1.0b(example)";
	
	public static final String CHARACTER_SET = "UTF-8";
}
