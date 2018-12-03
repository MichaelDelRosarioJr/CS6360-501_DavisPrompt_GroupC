package edu.utdallas.cs6360.davisbase.trees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;

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
	
	/**
	 * A logger that logs things for logging purposes
	 */
	private static final Logger LOGGER = Logger.getLogger(TableConfig.class.getName());
	
	// DataRecord Size: different depending on the CREATE TABLE command
	private int dataMaxRecordSize;
	private int dataRecordSizeNoText;
	private int numOfColumns;
	
	private int treeOrder;
	private int leafPageDegree;
	
	// True if the table has text columns false otherwise
	private boolean hasTextColumns;
	
	/**
	 * TODO: Link with metadata tables
	 */
	private ArrayList<DataType> colTypes;
	
	
	
	/**
	 * Default constructor for completeness
	 */
	public TableConfig() {
		this.dataMaxRecordSize = -1;
		this.dataRecordSizeNoText = -1;
		this.treeOrder = -1;
		this.leafPageDegree = -1;
		this.numOfColumns = -1;
		this.hasTextColumns = false;
		this.colTypes = new ArrayList<>();
	}
	
	/**
	 * Constructor that initializes a TableTree when passed an array of DataType enum values
	 * @param columnTypes an array of DataType enums representing column data types
	 */
	public TableConfig(ArrayList<DataType> columnTypes) {
		this.numOfColumns = columnTypes.size();
		this.dataMaxRecordSize = calculateMaxDataRecordSize(columnTypes);
		this.dataRecordSizeNoText = calculateMinDataRecordSize(columnTypes);
		this.treeOrder = calculateTreeOrder();
		this.leafPageDegree = calculateLeafPageDegree();
		this.hasTextColumns = doesColHaveTextFields(columnTypes);
		this.colTypes = columnTypes;
		logTreeConfig();
	}
	
	/**
	 * Constructor that initializes a TableTree when passed an array bytes representing data type codes
	 * @param columnTypeCodes an array of data type byte cods
	 */
	public TableConfig(DataType[] columnTypeCodes) {
		this.numOfColumns = columnTypeCodes.length;
		this.dataMaxRecordSize = calculateMaxDataRecordSize(columnTypeCodes);
		this.dataRecordSizeNoText = calculateMinDataRecordSize(columnTypeCodes);
		this.treeOrder = calculateTreeOrder();
		this.leafPageDegree = calculateLeafPageDegree();
		this.hasTextColumns = doesColHaveTextFields(columnTypeCodes);
		logTreeConfig();
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
	 * Getter for property 'hasTextColumns'.
	 *
	 * @return Value for property 'hasTextColumns'.
	 */
	public boolean hasTextColumns() {
		return hasTextColumns;
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
	 * Setter for property 'hasTextColumns'.
	 *
	 * @param hasTextColumns Value to set for property 'hasTextColumns'.
	 */
	public void setHasTextColumns(boolean hasTextColumns) {
		this.hasTextColumns = hasTextColumns;
	}
	
	/**
	 * Getter for property 'colTypes'.
	 *
	 * @return Value for property 'colTypes'.
	 */
	public ArrayList<DataType> getColTypes() {
		return colTypes;
	}
	
	/**
	 * Setter for property 'colTypes'.
	 *
	 * @param colTypes Value to set for property 'colTypes'.
	 */
	public void setColTypes(ArrayList<DataType> colTypes) {
		this.colTypes = colTypes;
	}
	
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *     Calculating Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * Calculates the Tree Order which is also the internal page degree<br>
	 * pageHeaderSize = 8 bytes<br>
	 * offsetSize  = short = 2 bytes<br>
	 * tableInteriorCellSize = (4 byte Key, 4 byte pointer) =  8 bytes<br>
	 * <br>
	 * PageSize   = headerSize + offsets + dataCells<br>
	 *            = interiorPageHeaderSize + (n*offsetSize) + (n*interiorCellSize)<br>
	 *            = 8 + 2n + 8n<br>
	 *        10n = PageSize - 8<br>
	 * Tree Order = (PageSize - 8)/10<br>
	 * Tree Order = (pageSize - pageHeaderSize) / (offsetSize + interiorCellSize)<br>
	 * @return the Tree order or internal page degree which is the maximum number of cells that
	 * can fit on an interior page
	 */
	private int calculateTreeOrder() {
		return (PAGE_SIZE - PAGE_HEADER_SIZE) / (Short.BYTES + TABLE_INTERIOR_CELL_SIZE);
	}
	
	/**
	 * Calculates the leafPageDegree or the maximum number of records that can be stored in a leaf cell <br>
	 *
	 * tableLeafCellSize = (2 byte payload size, 4 byte Key, payload)<br>
	 * payloadSize = (1 byte #ofColumns, n byte type codes, column data)<br>
	 * <br>
	 * PageSize = headerSize + offsets + dataCells<br>
	 *          = headerSize + offsetSize*degree + dataCells*degree<br>
	 *          = headerSize + degree(offsetSize + dataCells)<br>
	 *          = headerSize + degree[offsetSize + leafCellHeader + (1byte for NumCol) + numberOfColForTypeCodes +
	 *          dataSize]<br>
	 *
	 *  degree =                                       (PageSize - headerSize) /<br>
	 *                  [offsetSize + leafCellHeader + (1byte for NumCol) + numberOfColForTypeCodes +dataSize]<br>
	 *
	 *  degree =                                            (PageSize - 8)/<br>
	 *                                               (2 + 6 + 1 + numCol + dataSize)<br>
	 * @return the degree of a leaf node or the maximum number of cells that can be stored a leave node based on the
	 * column configuration
	 * 	 * can fit on an interior page
	 */
	private int calculateLeafPageDegree() {
		return (PAGE_SIZE - PAGE_HEADER_SIZE) /
				(Short.BYTES + TABLE_LEAF_CELL_HEADER_SIZE + Byte.BYTES + this.numOfColumns +
						this.dataMaxRecordSize);
	}
	
	/**
	 * Used to ensure that this configuration will produce valid trees by making sure no interior/leaf page has a
	 * degree < 2
	 */
	private void checkTreeOrder() {
		if (treeOrder < MIN_ORDER_OF_TREE || leafPageDegree < MIN_ORDER_OF_TREE) {
			throw new IllegalStateException("Error this tree order < 2 or it's leaf nodes have degree < 2");
		}
	}
	
	/**
	 * Calculates the size of the column data cells when passed an array of byte type codes<br>
	 *     For Text columns it returns the largest possible record size since Text columns make it variable in length
	 * @param columnTypeCodes an array of column type codes
	 * @return the size in bytes of any instantiated data records with this column configuration
	 */
	private int calculateMaxDataRecordSize(DataType[] columnTypeCodes) {
		int size = 0;
		for (DataType b : columnTypeCodes) {
			size += DataType.getMaxSize(b.getTypeCode());
		}
		return size;
	}
	
	/**
	 * Calculates the size of the column data cells when passed an array of byte type codes<br>
	 *     For Text columns it returns the largest possible record size since Text columns make it variable in length
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
	
	/**
	 * Calculates the size of the column data cells when passed an array of byte type codes<br>
	 *     For Text columns it does nothing, this is used when determining the size of inserting a new record
	 * @param columnTypeCodes an array of column type codes
	 * @return the size in bytes of any instantiated data records with this column configuration
	 */
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
	 * Calculates the size of the column data cells when passed an array of DataType enum values<br>
	 *     For Text columns it returns the largest possible record size since Text columns make it variable in length
	 * @param columnTypes an array of column type codes
	 * @return the size in bytes of any instantiated data records with this column configuration
	 */
	private int calculateMaxDataRecordSize(ArrayList<DataType> columnTypes) {
		int size = 0;
		for (DataType d : columnTypes) {
			size += DataType.getMaxSize(d.getTypeCode());
		}
		return size;
	}
	
	/**
	 * Calculates the size of the column data cells when passed an array of DataType enum values<br>
	 *     For Text columns it does nothing, this is used when determining the size of inserting a new record
	 * @param columnTypes an array of column type codes
	 * @return the size in bytes of any instantiated data records with this column configuration
	 */
	private int calculateMinDataRecordSize(ArrayList<DataType> columnTypes) {
		int size = 0;
		for (DataType d : columnTypes) {
			if (d != DataType.TEXT_TYPE_CODE) {
				size += DataType.getMaxSize(d.getTypeCode());
			}
		}
		return size;
	}
	
	/**
	 * Calculates the size of the column data cells when passed an array of byte type codes<br>
	 *     For Text columns it does nothing, this is used when determining the size of inserting a new record
	 * @param columnTypes an array of column type codes
	 * @return the size in bytes of any instantiated data records with this column configuration
	 */
	private int calculateMinDataRecordSize(DataType[] columnTypes) {
		return calculateMinDataRecordSize(new ArrayList<>(Arrays.asList(columnTypes)));
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
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *        Static Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	public static boolean doesColHaveTextFields(DataType[] colTypeCodes){
		for (DataType b : colTypeCodes) {
			if (b == DataType.TEXT_TYPE_CODE) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean doesColHaveTextFields(ArrayList<DataType> colTypes){
		for (DataType type : colTypes) {
			if (type == DataType.TEXT_TYPE_CODE) {
				return true;
			}
		}
		return false;
	}
}