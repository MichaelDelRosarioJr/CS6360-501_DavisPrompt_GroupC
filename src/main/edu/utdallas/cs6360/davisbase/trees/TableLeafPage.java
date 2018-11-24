package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.Config;

import javax.xml.crypto.Data;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.*;

/**
 * Class to represent a leaf page in a file and it's cells
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class TableLeafPage extends Page{
	private static final Logger LOGGER = Logger.getLogger(TableLeafPage.class.getName());
	private static final int LARGEST_PAGE_SIZE = 65536;
	private static final int ZERO = 0;
	
	private int nextPagePointer;
	
	
	public TableLeafPage() {
		super();
		this.nextPagePointer = 0;
	}
	
	TableLeafPage(PageType pageType, int pageNumber, int nextPagePointer) {
		super(pageType, pageNumber);
		if(pageType == PageType.TABLE_LEAF_ROOT && nextPagePointer > ZERO) { throw new
				IllegalArgumentException("Table root leaf page with non-null next page pointer"); }
		this.nextPagePointer = nextPagePointer;
		
	}
	
	/**
	 * A constructor to recreate a TableLeafPage object from it's byte representation stored in the file.
	 *
	 * @param data an array of bytes representing an entire page from a file
	 */
	public TableLeafPage(byte[] data, int pageNumber) {
		super(data, pageNumber);
		ByteBuffer headerBuffer = ByteBuffer.wrap(data);
		
		// Throw away page type, numOfCells, and startOfCellPointers since already assigned by call to super()
		headerBuffer.getInt();
		
		// Get next 4 bytes from ByteBuffer,
		// else store the 2's complement value of the 4 bytes
		this.nextPagePointer = headerBuffer.getInt();
	}
	
	/**
	 *
	 * @param tableLeafCell
	 */
	void addDataCell(TableLeafCell tableLeafCell) {
		addDataCell(tableLeafCell);
		sort();
	}
	
	/**
	 *
	 * @param rowId
	 * @return
	 */
	TableLeafCell getDataCell(int rowId) {
		for(DataCell c : getDataCells()) {
			if (c.getRowId() == rowId) {
				return (TableLeafCell)c;
			}
		}
		throw new IllegalArgumentException("DataCell not found");
	}
	
	@Override
	public void printPage() {
	
	}
	
	/**
	 * Gets the best location to insert a cell into a page depending using the size of a leaf cell<br>
	 *
	 * If the cells are constant size (no text columns) then the cells are sorted by their location within
	 * the page, normally they are sorted by rowId, the first available location is chosen,
	 * if there is none then it is added at the end of the page<br>
	 *
	 * If this table has text columns then it gathers cells with free space between them that are large enough
	 * to hold this entry. The difference between the actual size of the new entry and each portion of space
	 * and the smallest value is picked.
	 *
	 * @param newEntrySumOfTextFields the length of all the text fields of the new entry, if 0
	 *                                it is assumed null or no text columns.
	 * @param config a TableConfig class representing the configuration of the tree based on the
	 *                  table's columns
	 * @return the address of the best location for an insertion
	 */
	@Override
	public int getFreeCellLocation(int newEntrySumOfTextFields, TableConfig config) {
		if (getNumOfCells() == ZERO) {
			return ZERO;
		}
		
		sortDataCellsByOffset();
		
		int entrySize;
		
		if(!config.isHasTextFields()) {
			int lastCellPosition = 0;
			int currentCellPosition = 0;
			entrySize = config.getDataMaxRecordSize();
			
			// Sort array by their location within the page
			// normally they are sorted by rowId
			sortDataCellsByOffset();
			for(DataCell c : getDataCells()) {

				currentCellPosition = c.getPageOffset() / config.getDataMaxRecordSize();
				// If more than 1 position away from last cell then lastCellPosition+1 is free
				if (currentCellPosition - lastCellPosition > ONE) {
					// Fix the array order
					sort();
					return lastCellPosition + ONE;
				}
				lastCellPosition = currentCellPosition;
			}
			// Fix the array order
			sort();
			// Else all are contiguous and we fill in the next free slot
			return getNumOfCells();
			
		} else {
			int currentCellOffset = 0;
			int lastCellOffset = 0;
			int lastCellSize = 0;
			entrySize = config.getDataRecordSizeNoText() + newEntrySumOfTextFields;
			
			HashMap<Integer, Integer> freeSpaceList = new HashMap<>();
			
			for(DataCell c : getDataCells()) {
				currentCellOffset = c.getPageOffset();
				// If more than 1 position away from last cell then lastCellPosition+1 is free
				int freeSpaceBetweenCells = currentCellOffset - (lastCellOffset + lastCellSize);
				if (freeSpaceBetweenCells >= entrySize) {
					freeSpaceList.put((lastCellOffset + lastCellSize), freeSpaceBetweenCells);
				}
				lastCellOffset = currentCellOffset;
				lastCellSize = c.size();
			}
			// Else no free space fits, so append to end
			if(freeSpaceList.isEmpty()) {
				DataCell tmp = getDataCells().get(getNumOfCells() - 1);
				sort();
				return tmp.getPageOffset() + tmp.size();
			}
			
			sort();
			return getClosestSizeOffset(entrySize, freeSpaceList);
		}
		
		
	}
	
	private int getClosestSizeOffset(int entrySize, HashMap<Integer, Integer> map) {
		HashMap<Integer, Integer> difference = new HashMap<>();
		
		for(Map.Entry<Integer, Integer> entry : map.entrySet()) {
			difference.put(entry.getKey(), (entry.getValue() - entrySize));
		}
		
		int min = Collections.min(difference.values());
		for(Map.Entry<Integer, Integer> entry: map.entrySet()) {
			if(entry.getValue() == min) {
				return entry.getKey();
			}
		}
		throw new IllegalStateException("Error can't find min");
	}
	
	@Override
	public DataCell getDataCellAtOffsetInFile(byte[] data, short offset) {
		int beginningOfFirstCell = Config.PAGE_SIZE - 1 - offset;
		
		// Get number of bytes in payload to determine record length
		// Can't use TableConfig.getDataMaxRecordSize because of variable length Text fields
		byte[] payloadSize = new byte[2];
		payloadSize[0] = data[beginningOfFirstCell];
		payloadSize[1] = data[beginningOfFirstCell - 1];
		int totalCellBytes = 6 + ByteBuffer.wrap(payloadSize).getShort();
		
		// Create array and grab data cell bytes
		byte[] payloadBytes = new byte[totalCellBytes];
		for(int i = beginningOfFirstCell; i > beginningOfFirstCell - totalCellBytes; i--) {
			payloadBytes[beginningOfFirstCell - i] = data[i];
		}
		
		// Return new data cell
		return new TableLeafCell(payloadBytes, offset);
	}
	
	/**
	 * A method to return the byte array containing
	 * DataRecord formatted into the specified format
	 * @return a byte array containing the correctly formatted data
	 */
	@Override
	public List<Byte> getBytes() {
		ArrayList<Byte> output = new ArrayList<>(Config.PAGE_SIZE);
		
		int i = 2;
		output.add(getPageType().getByteCode());
		output.add(getNumOfCells());
		for (byte b : shortToBytes(getStartOfCellPointers())) {
			output.add(b);
			i++;
		}
		
		for (byte b : intToBytes(this.nextPagePointer)) {
			output.add(b);
			i++;
		}
		output.addAll(i, getDataCellOffsetsBytes());
		i += 2*getNumOfCells();
		
		
		for(DataCell c: getDataCells()) {
			ArrayList<Byte> bytes = (ArrayList<Byte>)c.getBytes();
			for(int j = output.size() - 1; j > output.size() - 1 - bytes.size(); j--) {
				output.add(j, bytes.get(j - output.size() + 1));
				if(j <= i) {
					throw new IllegalStateException("Error: Offset Array and Data Cells have met and page is full");
				}
			}
		}
		return output;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) { return true; }
		
		// If other object is null return false
		if (!Optional.ofNullable(o).isPresent()) {
			return false;
		}
		
		// If object is not TableInteriorPage return false
		if (!(o instanceof TableLeafPage)) {
			return false;
		}
		
		// Since not null and TableInteriorPage
		// Cast to TableInteriorPage
		TableLeafPage that = (TableLeafPage) o;
		
		return nextPagePointer == that.nextPagePointer && this.getPageNumber() == that.getPageNumber() &&
				this.getPageType() == that.getPageType() && this.getNumOfCells() == that.getNumOfCells() &&
				this.getDataCells().equals(that.getDataCells());
	}
	
	@Override
	public int hashCode() {
		DataCell[] array = new DataCell[getDataCells().size()];
		getDataCells().toArray(array);
		return Arrays.hashCode(array) + getNumOfCells() * getPageTypeCode() * getPageNumber();
	}
}

