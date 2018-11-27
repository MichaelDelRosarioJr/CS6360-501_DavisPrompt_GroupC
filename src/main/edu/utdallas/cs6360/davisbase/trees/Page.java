package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.shortToBytes;

/**
 * Abstract class containing common tasks associated with managing pages and their data cells
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public abstract class Page {
	private int pageNumber;
	private PageType pageType;
	
	/**
	 * The maximum number of cells on a page, a 1 byte
	 * signed integer. Maximum number of records on a page
	 * is 127
	 */
	private byte numOfCells;
	
	/**
	 * The start of the data cell pointers.
	 * 2^16 - valueFromDisk = startOfCellPointers
	 * Assuming we cannot have a negative value then<br>
	 * Range: [0, 2^15-1],
	 * Range of Possible starting places for data cells:
	 *        [65536, 32769]
	 */
	private short startOfCellPointers;
	
	private ArrayList<DataCell> dataCells;
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *        Constructors
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * Default constructor
	 */
	Page() {
		this.pageType = null;
		this.pageNumber = -ONE;
		this.numOfCells = ZERO;
		this.dataCells = new ArrayList<>();
	}
	
	/**
	 * Constructor that accepts the page type and the number of the file within the page
	 * @param pageType the page type
	 * @param pageNumber the number of the page within the file
	 */
	Page(PageType pageType, int pageNumber) {
		this.pageType = pageType;
		this.pageNumber = pageNumber;
		this.numOfCells = ZERO;
		this.dataCells = new ArrayList<>();
	}
	
	/**
	 * Constructor that accepts a pageType, pageNumber, and List of dataCells<br>
	 * @param pageType the type of page
	 * @param pageNumber the page number and pointer location of the page in the file
	 * @param dataCells the data cells to store in the page
	 */
	Page(PageType pageType, int pageNumber, ArrayList<DataCell> dataCells) {
		this.pageType = pageType;
		this.pageNumber = pageNumber;
		this.dataCells = new ArrayList<>(dataCells);
		this.numOfCells = (byte)this.dataCells.size();
	}
	
	/**
	 * Constructor recreates a page from the file using it's byte representation and page number
	 * @param data the raw bytes of the page in the file
	 * @param pageNumber the ordering of the page within the file
	 */
	Page(byte[] data, int pageNumber) {
		// TODO Check page size here
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		byte tmpPageType = byteBuffer.get();
		
		this.dataCells = new ArrayList<>();
		this.numOfCells = byteBuffer.get();
		this.startOfCellPointers = byteBuffer.getShort();
		
		// Throw away extra pointer, subclasses can get it if they need it
		byteBuffer.getInt();
		
		// If pageNumber = 0 then it is a root page
		if(pageNumber != ZERO) {
			this.pageType = PageType.getEnum(tmpPageType);
		} else {
			PageType tmpType = PageType.getEnum(tmpPageType);
			if (tmpType == PageType.TABLE_LEAF_PAGE) { this.pageType = PageType.TABLE_LEAF_ROOT; }
			if (tmpType == PageType.TABLE_INTERIOR_PAGE) { this.pageType = PageType.TABLE_INTERIOR_ROOT; }
		}
		
		short[] offsets = new short[this.numOfCells];
		
		for(int i = ZERO; i < this.numOfCells; i++) {
			offsets[i] = byteBuffer.getShort();
		}
		initDataCellsFromFile(data);
	}
	
	private void initDataCellsFromFile(byte[] data) {
		byte[] inOrderBytes = ByteHelpers.reverseByteArray(data);
		int byteArrayPointer = ZERO;
		
		if(isLeaf()) {
			for(int i = 0; i < numOfCells; i++) {
				int startOfDataCell = byteArrayPointer;
				int dataCellSize = ByteBuffer.wrap(inOrderBytes).getShort(startOfDataCell) + TABLE_LEAF_CELL_HEADER_SIZE;
				
				addDataCell(Arrays.copyOfRange(inOrderBytes, startOfDataCell, dataCellSize));
				byteArrayPointer += dataCellSize;
			}
		} else {
			int startOfDataCell = byteArrayPointer;
			for(int i = 0; i < numOfCells; i++) {
				addDataCell(Arrays.copyOfRange(inOrderBytes, startOfDataCell, TABLE_INTERIOR_CELL_SIZE));
				startOfDataCell += TABLE_INTERIOR_CELL_SIZE;
			}
		}
	}
	
	/**
	 * Sorts the DataCells based on their location within the Page file
	 */
	void sortDataCellsByOffset() {
		this.dataCells.sort((o1, o2) -> o1.getPageOffset() - o2.getPageOffset());
	}
	
	/**
	 * Retrieves and recreates DataCells from the page file with the offsets
	 * @param data an array of bytes that make up a page
	 * @param offsets the location from the end of the page where the cells are located
	 * @return an ArrayList of DataCell objects
	 */
	/* TODO: REMOVE AFTER TESTING writePage() and updated byte constructor
	private ArrayList<DataCell> getDataCellsFromFileData(byte[] data, short[] offsets) {
		ArrayList<DataCell> tmpDataCells = new ArrayList<>();
		
		for (short s : offsets) {
			tmpDataCells.add(getDataCellAtOffsetInFile(data, s));
		}
		return tmpDataCells;
	}*/
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *      Getters and Setters
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * Adds a new cell to the data store array
	 * @param data an array of bytes representing a data cell
	 */
	private void addDataCell(byte[] data) {
		if(isLeaf()) {
			this.dataCells.add(new TableLeafCell(data));
		} else {
			this.dataCells.add(new TableInteriorCell(data));
		}
	}
	
	/**
	 * Getter for property 'dataCells'.
	 *
	 * @return Value for property 'dataCells'.
	 */
	public List<DataCell> getDataCells() {
		return dataCells;
	}
	
	/**
	 * Return the page type
	 * @return the type of the page
	 */
	PageType getPageType() { return this.pageType; }
	
	/**
	 * Getter for property 'startOfCellPointers'.
	 *
	 * @return Value for property 'startOfCellPointers'.
	 */
	public short getStartOfCellPointers() {
		return startOfCellPointers;
	}
	
	/**
	 * Getter for property 'numOfCells'.
	 *
	 * @return Value for property 'numOfCells'.
	 */
	public byte getNumOfCells() {
		return numOfCells;
	}
	
	/**
	 * Getter for property 'dataCells'.
	 *
	 * @return Value for property 'dataCells'.
	 */
	public List<Byte> getDataCellOffsetsBytes() {
		ArrayList<Byte> output = new ArrayList<>();
		for (DataCell offset : this.dataCells) {
			for (byte b : shortToBytes(offset.getPageOffset())) {
				output.add(b);
			}
		}
		return output;
	}
	
	/**
	 * Return the byte code of the page type to store in the page header on the file
	 * @return the byte codes specified in the DavisBase requirements
	 */
	byte getPageTypeCode() { return this.pageType.getByteCode(); }
	
	/**
	 * Change the type of the current page<br>
	 *
	 * If a table page then it can only switch between it's standard version and root status<br>
	 *     i.e. TABLE_LEAF_ROOT can only become TABLE_LEAF_PAGE and
	 *          TABLE_INTERIOR_ROOT can only become TABLE_INTERIOR_PAGE
	 * @param pageType the new type for this page
	 */
	void setPageType(PageType pageType) {
		// if table page check if this type change is allowed
		if (isTablePage()) {
			if (isLeaf()) {
				this.pageType = pageType;
				if (isInterior()) {
					throw new IllegalArgumentException("Cannot convert Table Leaf Page to Table Interior Page");
				}
			} else {
				this.pageType = pageType;
				if (isLeaf()) {
					throw new IllegalArgumentException("Cannot convert Table Leaf Page to Table Interior Page");
				}
			}
		} else {
			// Else Index page and we don't care about them
			this.pageType = pageType;
		}
	}
	
	/**
	 * Return the page number within the file
	 * @return the page number
	 */
	int getPageNumber() { return this.pageNumber; }
	
	/**
	 * Update the page number
	 * @param pageNumber new number of the page in the file
	 */
	void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
	
	/**
	 * Check if the page is full and needs splitting<br>
	 * 
	 * A Page is considered full when the header and cell areas grow
	 * and meet each other
	 * @return true if the page is full, false it it is not
	 */
	boolean isFull(TableConfig config) {
		if (isLeaf()) {
			return config.getMaxLeafPageRecords() == this.numOfCells;
		} else {
			return config.getMaxInteriorPageCells() == this.numOfCells;
		}
	}
	
	boolean needMerge(TableConfig config) {
		if(isRoot()) { return this.numOfCells <= ONE; }
		else if (isLeaf()) {
			return config.getMinLeafPageRecords() >= this.numOfCells;
		} else {
			return config.getMinInteriorPageCell() >= this.numOfCells;
		}
	}
	
	/**
	 * Increases the number of cells on the page
	 */
	void incrementNumOfCells() {
		numOfCells++;
	}
	
	/**
	 * Decreases the number of cells on the page
	 */
	void decrementNumOfCells() {
		numOfCells--;
	}
	
	/**
	 * Sorts the DataCell ArrayList based on rowId
	 */
	public void sort() {
		Collections.sort(this.dataCells);
	}
	
	/**
	 * Check if the page is empty and needs merging
	 *
	 * @return true if the page is empty, false otherwise
	 */
	boolean isEmpty() { return this.numOfCells == ZERO; }
	
	/**
	 * Check if the page is a leaf page
	 * @return true if a leaf page, false otherwise
	 */
	boolean isLeaf() { return this.pageType == PageType.TABLE_LEAF_ROOT || this.pageType == PageType.TABLE_LEAF_PAGE ||
			this.pageType == PageType.INDEX_LEAF_PAGE || this.pageType == PageType.INDEX_LEAF_ROOT; }
	
	/**
	 * Check if the page is an interior page
	 * @return true if an interior page, false otherwise
	 */
	boolean isInterior() { return this.pageType == PageType.TABLE_INTERIOR_PAGE ||
			this.pageType == PageType.TABLE_INTERIOR_ROOT || this.pageType == PageType.INDEX_INTERIOR_PAGE ||
			this.pageType == PageType.INDEX_INTERIOR_ROOT; }
	
	/**
	 * Check if the page is a root page
	 * @return true if a root page, false otherwise
	 */
	boolean isRoot() { return this.pageType == PageType.TABLE_LEAF_ROOT ||
			this.pageType == PageType.TABLE_INTERIOR_ROOT || this.pageType == PageType.INDEX_INTERIOR_ROOT ||
			this.pageType == PageType.INDEX_LEAF_ROOT; }
	
	/**
	 * Check if the page is a table page
	 * @return true if a table page, false otherwise
	 */
	boolean isTablePage() { return this.pageType == PageType.TABLE_LEAF_PAGE ||
			this.pageType == PageType.TABLE_LEAF_ROOT || this.pageType == PageType.TABLE_INTERIOR_PAGE ||
			this.pageType == PageType.TABLE_INTERIOR_ROOT;}
	
	/**
	 * Check if the page is a index page
	 * @return true if a index page, false otherwise
	 */
	boolean isIndexPage() { return this.pageType == PageType.INDEX_LEAF_PAGE ||
			this.pageType == PageType.INDEX_LEAF_ROOT || this.pageType == PageType.INDEX_INTERIOR_PAGE ||
			this.pageType == PageType.INDEX_INTERIOR_ROOT; }
	
	/**
	 * A method to write the update page data to the file, each subclass uses their own getBytes() method which contains
	 * class specific instructions on how to prepare the bytes for the file
	 * @param treeFile the RandomAccessFile associated with the page's database file
	 * @throws IOException is thrown when an I/O operation fails
	 */
	void writePage(RandomAccessFile treeFile) throws IOException {
		// Pointers into the table file
		// The beginning of this page and the start the next and the
		// beginning of the data cell area at the end of the file
		long addressPointer = ((long)this.getPageNumber() * PAGE_SIZE);
		long startOfNextPage = addressPointer + PAGE_SIZE;
		int startOfDataCells = (int)(startOfNextPage - this.startOfCellPointers);
		
		// Expand the file if needed
		if(treeFile.length() < startOfNextPage) {
			treeFile.setLength(startOfNextPage);
		}
		
		// Get page bytes and initialize pointers into the array
		byte[] pageBytes = ByteHelpers.byteArrayListToArray(getBytes());
		int headerSize = 8 + 2*this.numOfCells;
		int freeSpaceSize = PAGE_SIZE - this.startOfCellPointers - headerSize;
		
		
		// Seek to first address of this page and
		// write the header and data cell offset values
		treeFile.seek(addressPointer);
		treeFile.write(Arrays.copyOfRange(pageBytes, ZERO, headerSize));
		
		
		// Fill the center of the file with null values
		treeFile.write(new byte[freeSpaceSize], startOfDataCells, freeSpaceSize);
		
		// Fill the remaining bytes in the page with the rest of the byte array
		treeFile.write(Arrays.copyOfRange(pageBytes, headerSize, this.startOfCellPointers));
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *       Abstract Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	/**
	 * Abstract method all subclasses must implement that contains instructions for outputting itself to an array of
	 * bytes to store itself on disk
	 * @return an ArrayList containing the bytes that make up the page
	 */
	public abstract List<Byte> getBytes();
	
	/**
	 * Abstract method all subclasses must implement that when given an array of bytes making up a page and an offset
	 * in that page from the end of the page it recreates the DataCell from it's byte representation
	 * @param data an array of bytes that make up a page in a file
	 * @param offset an offset from the end of the page that acts as a pointer to a data cell
	 * @return the DataCell that was represented by the bytes
	 */
	public abstract DataCell getDataCellAtOffsetInFile(byte[] data, short offset);
	
	/**
	 * An abstract method that gets the best location to insert a cell into a page depending on
	 * the page type and if its a leaf with text columns of variable length
	 * @param newEntrySumOfTextFields the length of all the text fields of the new entry, if 0
	 *                                it is assumed null or no text columns.
	 *                                Ignored if a TableInteriorNode
	 * @param config a TableConfig class representing the configuration of the tree based on the table's columns
	 * @return the address of the best location for an insertion
	 */
	public abstract int getFreeCellLocation(int newEntrySumOfTextFields, TableConfig config);
	
	/**
	 * Abstract class method that all subclasses must implement that contains instructions specific to each page type on
	 * how to log information about itself
	 */
	public abstract void printPage();
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *        Static Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	/**
	 * A static helper method that accepts an 8 digit page header and if all bytes are null
	 * then it returns true, else it returns false
	 * @param data a page header (8 bytes)
	 * @return true if the page is not being used, false otherwise
	 */
	public static boolean isFreePage(byte[] data) {
		for (byte b : data) {
			if (b != ZERO) {
				return false;
			}
		}
		return true;
	}
}