package edu.utdallas.cs6360.davisbase;

/**
 * A Configuration class to hold settings
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public final class Config {
	/**
	 * Private constructor so the class cannot be instantiated
	 */
	private Config() { throw new IllegalStateException("Config Utility Class");}
	
	/**
	 * Database file storage options
	 */
	public static final String DATA_DIRECTORY = "data";
	public static final String CATALOG_DIRECTORY = DATA_DIRECTORY + "/" + "catalog";
	public static final String USER_DATA_DIRECTORY = DATA_DIRECTORY + "/" + "user_data";
	public static final String CATALOG_TABLE = CATALOG_DIRECTORY + "/" + "davisbase_tables";
	public static final String CATALOG_COLUMN = CATALOG_DIRECTORY + "/" + "davisbase_columns";
	
	
	/**
	 * Page, page header, and data cell size options
	 */
	public static final int PAGE_SIZE = 512;
	public static final byte PAGE_HEADER_SIZE = 8;
	public static final byte TABLE_INTERIOR_CELL_SIZE = 8;
	public static final byte TABLE_LEAF_CELL_HEADER_SIZE = 6;
	public static final int MIN_ORDER_OF_TREE = 2;
	
	/**
	 * Numerical constants
	 */
	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final int TWO = 2;
	
	
	/**
	 * Misc Options
	 */
	public static final String COPYRIGHT = "Â©2016 Chris Irwin Davis";
	public static final String VERSION = "v1.0b(example)";
	public static final String CHARACTER_SET = "UTF-8";
}
