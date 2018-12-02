package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.utils.ByteHelpers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;
import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.*;

/**
 * Abstract class containing common tasks associated with managing pages and their data cells
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public abstract class Page {
	private static final Logger LOGGER = Logger.getLogger(Page.class.getName());
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
	
	private int nextPagePointer;
	
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
		int numCells = byteBuffer.get();
		short startOfDataCellPointers = byteBuffer.getShort();
		
		// Store next page pointer, if Index Leaf page the value will be 0
		this.nextPagePointer = byteBuffer.getInt();
		
		// If pageNumber = 0 then it is a root page
		if(pageNumber != ZERO) {
			this.pageType = PageType.getEnum(tmpPageType);
		} else {
			PageType tmpType = PageType.getEnum(tmpPageType);
			if (tmpType == PageType.TABLE_LEAF_PAGE) { this.pageType = PageType.TABLE_LEAF_ROOT; }
			if (tmpType == PageType.TABLE_INTERIOR_PAGE) { this.pageType = PageType.TABLE_INTERIOR_ROOT; }
		}
		
		initDataCellsFromBytes(Arrays.copyOfRange(data, data.length - startOfDataCellPointers, data.length),
				numCells, startOfDataCellPointers);
	}
	
	/**
	 * A helper method that strips out the data cells from the end of the page file and reverses the bytes
	 * before returning
	 * @param data anarray of bytes representing the data cell area within the page
	 */
	
	private void initDataCellsFromBytes(byte[] data, int numOfCells, int startOfCellPointers) {
		if(startOfCellPointers != ZERO) {
			// Grab only the DataCell bytes and reverse them
			byte[] inOrderBytes = ByteHelpers.reverseByteArray(data);
			
			// Keep track of our position within the array of bytes
			int byteArrayPointer = ZERO;
			if(isLeaf()) {
				for(int i = 0; i < numOfCells; i++) {
					int dataCellSize = ByteBuffer.wrap(inOrderBytes).getShort(byteArrayPointer) + TABLE_LEAF_CELL_HEADER_SIZE;
					
					addDataCell(Arrays.copyOfRange(inOrderBytes, byteArrayPointer, byteArrayPointer + dataCellSize));
					byteArrayPointer += dataCellSize;
				}
			} else {
				for(int i = 0; i < numOfCells; i++) {
					addDataCell(Arrays.copyOfRange(inOrderBytes, byteArrayPointer, TABLE_INTERIOR_CELL_SIZE +
							byteArrayPointer));
					byteArrayPointer += TABLE_INTERIOR_CELL_SIZE;
				}
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
			incrementNumOfCells();
		} else {
			incrementNumOfCells();
			this.dataCells.add(new TableInteriorCell(data));
		}
	}
	
	void addDataCell(DataCell data) {
		if(isLeaf()) {
			if (data instanceof TableLeafCell) {
				this.dataCells.add(data);
				incrementNumOfCells();
				this.startOfCellPointers += data.size();
			} else {
				throw new IllegalArgumentException("Error: Cannot add non-Leaf data cell to a Leaf page");
			}
		} else {
			if (data instanceof  TableInteriorCell) {
				this.dataCells.add(data);
				incrementNumOfCells();
				this.startOfCellPointers += TABLE_INTERIOR_CELL_SIZE;
			} else {
				throw new IllegalArgumentException("Error: Cannot add an Interior data cell to a Leaf page");
			}
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
	 * Setter for property 'startOfCellPointers'.
	 *
	 * @param startOfCellPointers Value to set for property 'startOfCellPointers'.
	 */
	public void setStartOfCellPointers(short startOfCellPointers) {
		this.startOfCellPointers = startOfCellPointers;
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
	 * Getter for property 'nextPagePointer'.
	 *
	 * @return Value for property 'nextPagePointer'.
	 */
	public int getNextPagePointer() {
		return nextPagePointer;
	}
	
	/**
	 * Setter for property 'nextPagePointer'.
	 *
	 * @param nextPagePointer Value to set for property 'nextPagePointer'.
	 */
	public void setNextPagePointer(int nextPagePointer) {
		this.nextPagePointer = nextPagePointer;
	}
	
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
	 *  ____ pageStartAddress = (PAGE_SIZE * pageNum)
	 * |    |
	 * |    |
	 * |    |
	 * |----|startOfFreeSpace = pageStartAddress + headerSize = pageStartAddress + 8 + 2*numOfCells
	 * |    |
	 * |    |
	 * |    |
	 * |    |
	 * |    |
	 * |    |
	 * |    |
	 * |----|startOfDataCells = startOfNextPage - startOfCellPointers
	 * |    |
	 * |    |
	 * |    |
	 * |____|
	 * |    |startOfNextPage = pageStartAddress + PAGE_SIZE
	 * |    |
	 * |....|
	 * @param treeFile the RandomAccessFile associated with the page's database file
	 * @throws IOException is thrown when an I/O operation fails
	 */
	void writePage(RandomAccessFile treeFile) throws IOException {
		// Get the bytes to write to file
		byte[] pageBytes = byteArrayListToArray(getBytes());
		if(this.startOfCellPointers == -ONE) {
			this.startOfCellPointers = getSizeOfDataCells();
		}
		
		// Pointers into the table file
		// The beginning of this page and the start the next and the
		// beginning of the data cell area at the end of the file
		long pageStartAddress = ((long)this.getPageNumber() * PAGE_SIZE);
		long startOfNextPage = pageStartAddress + PAGE_SIZE;
		
		// The remaining values are calculated for logging purposes
		// Sizes of the page segments
		// Header + data cell offsets, the free space in the middle of the page, and the size of the data cell area
		int headerSize = PAGE_HEADER_SIZE + (Short.BYTES * this.numOfCells);
		
		// Data Cell Area Size = this.startOfCellPointers
		int freeSpaceSize = PAGE_SIZE - this.startOfCellPointers - headerSize;
		
		long startOfFreeSpace = pageStartAddress + headerSize;
		long startOfDataCells = startOfNextPage - (long)this.startOfCellPointers;
		
		// Information about the writing operation that is about to take place
		LOGGER.log(Level.INFO, "Page number: {0}", this.pageNumber);
		LOGGER.log(Level.INFO, "RandomAccessFile.length(): {0}", treeFile.length());
		LOGGER.log(Level.INFO, "Page header/offset size: {0}", headerSize);
		LOGGER.log(Level.INFO, "Free space size: {0}", freeSpaceSize);
		LOGGER.log(Level.INFO, "Page start address: {0}", pageStartAddress);
		LOGGER.log(Level.INFO, "Free space start address: {0}", startOfFreeSpace);
		LOGGER.log(Level.INFO, "Start of data cells: {0}", startOfDataCells);
		
		
		// Expand the file if needed
		if(treeFile.length() < startOfNextPage) {
			LOGGER.log(Level.INFO, "Expanding file");
			treeFile.setLength(startOfNextPage);
		}
		
		treeFile.seek(pageStartAddress);
		treeFile.write(pageBytes);
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
	 * A method to return the ArrayList containing DataRecord formatted in bytes to the specified format to
	 * store into the table/index file. <br>
	 *
	 * The format being: [pageHeader, dataCellOffsets, freeSpace, reversedDataCells]<br>
	 *
	 * Originally function does not add the freeSpace cells between the dataCellOffsets, but to simplify the decision
	 * was made to include the the NULL bytes in the center. Thus this method will output an ArrayList that is always
	 * side PAGE_SIZE. This can be done for error checking purposes
	 *
	 * TODO: if time, switch to array
	 * @return an ArrayList containing the bytes that make up the page
	 */
	public List<Byte> getBytes() {
		ArrayList<Byte> output = new ArrayList<>(PAGE_SIZE);
		
		// Store the first 2 bytes of the header the page type and number of data cells
		output.add(getPageType().getByteCode());
		output.add(getNumOfCells());
		
		// Placeholders for the dataCellOffsets and the dataCellBytes that need to be reversed
		ArrayList<Byte> dataCellOffsets = new ArrayList<>();
		ArrayList<Byte> dataCellBytes = new ArrayList<>();
		
		// Initialize the first offset to 0, this is also going to count the total bytes taken up by the data cells.
		// 2 birds 1 for loop
		short dataCellOffset = (short)ZERO;
		
		for(DataCell c: getDataCells()) {
			// Get the offset for this data cell
			for(byte offsetBytes : shortToBytes(dataCellOffset)) {
				dataCellOffsets.add(offsetBytes);
			}
			
			// Get the byte representation of this data cell
			ArrayList<Byte> bytes = (ArrayList<Byte>)c.getBytes();
			
			// Use size of array to determine the cell offset for the next data cell
			dataCellOffset += (short)bytes.size();
			
			// Add bytes to list of data cell bytes
			dataCellBytes.addAll(bytes);
		}
		
		// Save the startOfCellPointers for the writePage method to use
		this.startOfCellPointers = dataCellOffset;
		
		
		// Add to master output list
		for (byte b : shortToBytes(this.startOfCellPointers)) {
			output.add(b);
		}
		
		// Add the value of the nextPagePointer to the list
		for (byte b : intToBytes(getNextPagePointer())) {
			output.add(b);
		}
		
		// Append the 2*n offset array after the header
		output.addAll(dataCellOffsets);
		
		// Fill the free space with NULL values
		for(int i = output.size(); i < PAGE_SIZE - this.startOfCellPointers; i++) {
			output.add(NULL_BYTE);
		}
		
		// Reverse the data cell bytes and add them to the en of the array
		Collections.reverse(dataCellBytes);
		output.addAll(dataCellBytes);
		
		return output;
	}
	
	/**
	 * Abstract method that all subclasses should implement that returns the number of bytes taken up by the data cells.
	 * This value should already be calculated when preparing the page to be written to the file.<br>
	 *
	 * This is method is mainly used as a last resort if that mistakenly not saved, not calculated, or not set for some
	 * other reason.<br>
	 *
	 * @return the number of bytes taken up by the DataCell storage area
	 */
	abstract short getSizeOfDataCells();
	
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