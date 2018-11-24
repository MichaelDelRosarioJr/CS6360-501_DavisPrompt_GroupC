package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.Config;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that stores configuration details for specific instances of Table Trees<br>
 *
 * This is helpful in that it can be passed around to the various page classes at
 * creation time with common set up
 * parameters
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class TableConfig {
	private static final Logger LOGGER = Logger.getLogger(TableConfig.class.getName());
	
	private static final int ZERO = 0;
	
	// DataRecord Size: different depending on the CREATE TABLE command
	private int dataMaxRecordSize;
	private int dataRecordSizeNoText;
	private int numOfColumns;
	
	private int treeOrder;
	private int leafPageDegree;
	
	private boolean hasTextFields;
	
	
	/**
	 * Default constructor for completeness
	 */
	public TableConfig() {
		this.dataMaxRecordSize = -1;
		this.dataRecordSizeNoText = -1;
		this.treeOrder = -1;
		this.leafPageDegree = -1;
		this.numOfColumns = -1;
		this.hasTextFields = false;
	}
	
	/**
	 * Constructor that initializes a TableTree when passed an array of DataType enum values
	 * @param columnTypes an array of DataType enums representing column data types
	 */
	public TableConfig(DataType[] columnTypes) {
		this.numOfColumns = columnTypes.length;
		this.dataMaxRecordSize = calculateMaxDataRecordSize(columnTypes);
		this.dataRecordSizeNoText = calculateMinDataRecordSize(columnTypes);
		this.treeOrder = calculateTreeOrder();
		this.leafPageDegree = calculateLeafPageDegree();
		this.hasTextFields = doesColHaveTextFields(columnTypes);
		logTreeConfig();
	}
	
	/**
	 * Constructor that initializes a TableTree when passed an array bytes representing data type codes
	 * @param columnTypeCodes an array of data type byte cods
	 */
	public TableConfig(byte[] columnTypeCodes) {
		this.numOfColumns = columnTypeCodes.length;
		this.dataMaxRecordSize = calculateMaxDataRecordSize(columnTypeCodes);
		this.dataRecordSizeNoText = calculateMinDataRecordSize(columnTypeCodes);
		this.treeOrder = calculateTreeOrder();
		this.leafPageDegree = calculateLeafPageDegree();
		this.hasTextFields = doesColHaveTextFields(columnTypeCodes);
		logTreeConfig();
	}
	
	
	/**
	 * Returns the page size from the Config class
	 * @return the page size in bytes
	 */
	public int pageSize() {
		return Config.PAGE_SIZE;
	}
	
	/**
	 * Getter for property 'dataMaxRecordSize'.
	 *
	 * @return Value for property 'dataMaxRecordSize'.
	 */
	public int getDataMaxRecordSize() {
		return this.dataMaxRecordSize;
	}
	
	/**
	 * Getter for property 'treeOrder'.
	 *
	 * @return Value for property 'treeOrder'.
	 */
	public int getTreeOrder() {
		return this.treeOrder;
	}
	
	/**
	 * Getter for property 'leafPageDegree'.
	 *
	 * @return Value for property 'leafPageDegree'.
	 */
	public int getLeafPageDegree() {
		return this.leafPageDegree;
	}
	
	/**
	 * Getter for property 'hasTextFields'.
	 *
	 * @return Value for property 'hasTextFields'.
	 */
	public boolean isHasTextFields() {
		return hasTextFields;
	}
	
	/**
	 * Getter for property 'dataRecordSizeNoText'.
	 *
	 * @return Value for property 'dataRecordSizeNoText'.
	 */
	public int getDataRecordSizeNoText() {
		return dataRecordSizeNoText;
	}
	
	/**
	 * Setter for property 'hasTextFields'.
	 *
	 * @param hasTextFields Value to set for property 'hasTextFields'.
	 */
	public void setHasTextFields(boolean hasTextFields) {
		this.hasTextFields = hasTextFields;
	}
	
	/**
	 * Calculates the size of the column data cells when passed an array of byte type codes
	 * @param columnTypeCodes an array of column type codes
	 * @return the size in bytes of any instantiated data records with this column configuration
	 */
	private int calculateMaxDataRecordSize(byte[] columnTypeCodes) {
		int size = 0;
		for (byte b : columnTypeCodes) {
			size += DataType.getMaxSize(b);
		}
		return size;
	}
	
	private int calculateMinDataRecordSize(byte[] columnTypeCodes) {
		int size = 0;
		for (byte b : columnTypeCodes) {
			if (DataType.getEnum(b) != DataType.TEXT_TYPE_CODE) {
				size += DataType.getMaxSize(b);
			}
		}
		return size;
	}
	
	/**
	 * Calculates the size of the column data cells when passed an array of DataType enum values
	 * @param columnTypes an array of column data types represented by DataType enum values
	 * @return the size in bytes of any instantiated data records with this column configuration
	 */
	private int calculateMaxDataRecordSize(DataType[] columnTypes) {
		int size = 0;
		for (DataType d : columnTypes) {
			size += DataType.getMaxSize(d.getTypeCode());
		}
		return size;
	}
	
	private int calculateMinDataRecordSize(DataType[] columnTypes) {
		int size = 0;
		for (DataType d : columnTypes) {
			if (d != DataType.TEXT_TYPE_CODE) {
				size += DataType.getMaxSize(d.getTypeCode());
			}
		}
		return size;
	}
	
	/**
	 * Calculates the Tree Order which is also the internal page degree
	 * pageHeaderSize = 8 Bytes
	 * offsetSize  = short = 2 Bytes
	 * tableInteriorCellSize = (4 Byte Key, 4 Byte Pointer) =  8 Bytes
	 *
	 * PageSize   = headerSize + offsets + dataCells
	 *            = interiorPageHeaderSize + (n*offsetSize) + (n*interiorCellSize)
	 *            = 8 + 2n + 8n
	 *        10n = PageSize - 8
	 * Tree Order = (PageSize - 8)/10
	 * Tree Order = (pageSize - pageHeaderSize) / (offsetSize + interiorCellSize)
	 * @return the Tree order or internal page degree which is the maximum number of cells that
	 * can fit on an interior page
	 */
	private int calculateTreeOrder() {
		return (Config.PAGE_SIZE - Config.PAGE_HEADER_SIZE) / (Short.BYTES + Config.TABLE_INTERIOR_CELL_SIZE);
	}
	
	
	/**
	 * Calculates the leafPageDegree or the maximum number of records that can be stored in a leaf cell <br>
	 *
	 * tableLeafCellSize = (2 Byte Payload Size, 4 Byte Key, Payload)<br>
	 * payloadSize = (1 byte #ofColumns, n byte type codes, column data)<br>
	 *
	 * PageSize = headerSize + offsets + dataCells<br>
	 *          = headerSize + offsetSize*degree + dataCells*degree<br>
	 *          = headerSize + degree(offsetSize + dataCells)<br>
	 *          = headerSize + degree[offsetSize + leafCellHeader + (1Byte for NumCol) + numberOfColForTypeCodes +
	 *          dataSize]<br>
	 *
	 *  degree =                                       (PageSize - headerSize) /
	 *                  [offsetSize + leafCellHeader + (1Byte for NumCol) + numberOfColForTypeCodes +dataSize]
	 *
	 *  degree =                                            (PageSize - 8)/
	 *                                               (2 + 6 + 1 + numCol + dataSize)
	 *
	 */
	private int calculateLeafPageDegree() {
		return (Config.PAGE_SIZE - Config.PAGE_HEADER_SIZE) /
				(Short.BYTES + Config.TABLE_LEAF_CELL_HEADER_SIZE + Byte.BYTES + this.numOfColumns +
						this.dataMaxRecordSize);
			
	}
	
	/**
	 * Used to ensure that this configuration will produce valid trees by making sure no interior/leaf page has a
	 * degree < 2
	 */
	private void checkTreeOrder() {
		if (treeOrder < Config.MIN_ORDER_OF_TREE || leafPageDegree < Config.MIN_ORDER_OF_TREE) {
			throw new IllegalStateException("Error this tree order < 2 or it's leaf nodes have degree < 2");
		}
	}
	
	/**
	 * Calculates and returns the maximum number of entries allowedin a table interior cell
	 * @return the maximum number of entries
	 */
	public int getMaxInteriorPageCells() {
		return (2*this.treeOrder) - 1;
	}
	
	/**
	 * Calculates and returns the minimum number of entries allowed in a table interior cell
	 * @return the minimum number of entries
	 */
	public int getMinInteriorPageCell() {
		return this.treeOrder - 1;
	}
	
	/**
	 * Calculates and returns the maximum number of entries allowed in a table leaf cell
	 * @return the maximum number of entries
	 */
	public int getMaxLeafPageRecords() {
		return (2*this.leafPageDegree) - 1;
	}
	
	/**
	 * Calculates and returns the minimum number pf entries allowed in a table leaf cell
	 * @return the minimum number of entries
	 */
	public int getMinLeafPageRecords() {
		return this.leafPageDegree - 1;
	}
	
	void logTreeConfig() {
		LOGGER.log(Level.INFO, "Tree Order: {0}", this.treeOrder);
		LOGGER.log(Level.INFO, "Leaf Page Degree: {0}", this.leafPageDegree);
		LOGGER.log(Level.INFO, "Data Record Size: {0}", this.dataMaxRecordSize);
	}
	
	public static boolean doesColHaveTextFields(byte[] colTypeCodes){
		for (byte b : colTypeCodes) {
			if (DataType.getEnum(b) == DataType.TEXT_TYPE_CODE) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean doesColHaveTextFields(DataType[] colTypes){
		for (DataType type : colTypes) {
			if (type == DataType.TEXT_TYPE_CODE) {
				return true;
			}
		}
		return false;
	}
}
