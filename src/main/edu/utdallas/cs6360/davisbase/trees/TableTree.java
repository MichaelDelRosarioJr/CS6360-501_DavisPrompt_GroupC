package edu.utdallas.cs6360.davisbase.trees;

import edu.utdallas.cs6360.davisbase.DatabaseType;
import edu.utdallas.cs6360.davisbase.utils.FileHandler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utdallas.cs6360.davisbase.Config.*;

/**
 * B+Tree Class
 *
 * The root tree is always held in main memory so user operations never have to do a disk-read on the root before we can
 * access the rest of the tree.<br>
 *
 * All pages instantiated into memory have been entirely read from the file and reconstructed in main memory.<br>
 *
 * The below algorithms do the best to acomplish all of their goals in "one-pass"
 * TODO: Add counting of reads/writes to keep me honest,
 *  -creating new root: 1 reads and potentially 1 write if it does not exist.
 *
 * B+-Tree Algorithms and formulas were taken from the text book used in<i>CS 4349 Advanced Algorithm Design and
 * Analysis</i>. <i>Introduction to Algorithms 3rd Edition</i> a book so synonymous with CS Algorithms curriculum that
 * it is often referred to as the CLRS Algorithms book.
 * <br>
 *
 * - Notes that need to be formatted better
 *	   -All except root: All pages have degree t>=2, i.e. all pages but root must hold t - 1 DataCells
 *		   -min: t-1 data cells
 *		   -max: 2t - 1 data cells
 *	   -Root: For empty tree root can have 0 keys, for nonempty root must have at least 1 key
 *	   -Internal Nodes:
 *	   -full when the number of keys is (2t-1) keys
 *
 *	    -Maybe function to get height from number of nodes?
 *	   	h <= logt((n+1)/2) => n >= 2t^(h) - 1
 *
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class TableTree {
	/**
	 * The operation mode for the RandomAccessFile
	 */
	private static final String READ_WRITE_MODE = "rw";
	private static final int ZERO = 0;
	
	
	/**
	 * A logger that logs things for logging purposes
	 */
	private static final Logger LOGGER = Logger.getLogger(TableTree.class.getName());
	
	/**
	 * The name of the database this tree supports
	 */
	private String databaseName;
	/**
	 * The fully qualified filename for the file.
	 * TODO: Might not need to store
	 */
	private String fileName;
	
	/**
	 * The RandomAccessFile for the database. Saved so we do not have the make writes immediately to file, they can wait
	 * until they are absolutely necessary
	 */
	private RandomAccessFile tableFile;
	
	/**
	 * An enumarator that can take on the values of USER to represent a database created by the user or CATALOG to
	 * represent a database created by the software to help manage the database
	 */
	private DatabaseType databaseType;
	
	/**
	 * 	Holds metadata about the tree's configuration that all must be easily transferred between pages as we traverse
	 * 	into the tree(#columns/types/etc) and calculates information about B-Trees such as as tree's degree degree.
	 */
	private TableConfig treeConfig;
	
	/**
	 * The logical root page of the tree which is also the first physical page in the file
	 */
	private Page root;
	
	/**
	 * A placeholder used for splitting a node
	 */
	private Page newLeftChild;
	
	/**
	 * Can used in conjunction with `largestPageNumber` to determine if a new page should be inserted at the end or if a
	 * better location should be searched within the file.<br>
	 *
	 * This can also be used to determine if the file can be shrunk to reclaim empty pages
	 */
	private int numOfPages;
	
	//private Page lastPage;
	
	/**
	 * To keep track of the largest rowId until IndexTables are implemented
	 * TODO: Replace with call to index tables maybe?
	 */
	private int rowIdCounter;
	
	// TODO implement
	private int numLeafPages;
	private int numInteriorPages;
	
	/**
	 * Default constructor that sets fileName and databaseName to null and the DB type to USER
	 */
	public TableTree() {
		this.fileName = null;
		this.databaseName = null;
		this.databaseType = DatabaseType.USER;
	}
	
	/**
	 * Constructor that accepts the table name as an argument. This constructor assumes the
	 * table is a USER table and not a system catalog<br>
	 *
	 * The filename is created and it checks if the file already exists. If it does not exist
	 * the root page/node is created which is also a leaf node.
	 * @param databaseName the name of the user table to access
	 */
	public TableTree(String databaseName, ArrayList<DataType> colTypes) {
		this.databaseName = databaseName;
		this.databaseType = DatabaseType.USER;
		this.fileName = FileHandler.getTableFileName(this.databaseName, this.databaseType);
		this.treeConfig = new TableConfig(colTypes);
		openTreeFile();
		
	}
	
	/**
	 * Constructor that accepts the table name and the database type as arguments. <br>
	 *
	 * To be able to do this insert in "one-pass" without having to re-traverse the tree or pass back information to the
	 * beginning of the traversal we will split any full nodes that cross our path on the way down. This way if we
	 * proceed to the next page and it needs to be split before we can insert we can be sure that the previous page
	 * will not be full because we would have just split it before visiting this page.
	 *
	 * This constructor should be used when attemping to access a system catalog table
	 * @param databaseName the name of the table to access
	 * @param type DatabaseType.CATALOG or DatabaseType.USER
	 */
	public TableTree(String databaseName, DatabaseType type) {
		this.databaseName = databaseName;
		this.databaseType = type;
		this.fileName = FileHandler.getTableFileName(this.databaseName, this.databaseType);
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *   Insert Methods & helpers
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * 1-of-3 methods that make up the recursive B+Tree traversal insert algorithm.
	 * This is insert method that other classes will call when the users or a system table need to make an insertion
	 * into the file. This one implicitly calls the root page as our starting page.<br>
	 *
	 * The datatypes and values are accepted to abstract away the the creation of the underlying DataCell classes that
	 * hold the DataRecords
	 * @param colTypes A List of byte type codes to represent the various data types that will be stored in this file
	 * @param colValues the actual values of each column for this new row.
	 *                  TODO:
	 * @return the 4-byte of the newly inserted record, -1 if the insert failed
	 */
	public void insert(ArrayList<DataType> colTypes, ArrayList<String> colValues){
		LOGGER.log(Level.INFO, "Entering insert(colTypes, colValues)");
		if (Optional.ofNullable(this.root).isPresent()) {
			throw new IllegalStateException("Tree can't have null root");
		}
		
		// Check if valid insert
		if (validInsert(colTypes, colValues)) {
			LOGGER.log(Level.INFO, "Valid insert");
			// Create new LeafCell so less to pass around
			int newRowId = getRowIdCounter();
			insert(new TableLeafCell(newRowId, new DataRecord(colTypes, colValues)));
			
		}
		// TODO: Count insertions, add in Config class
		LOGGER.log(Level.INFO, "Exiting insert(colTypes, colValues)");
	}
	
	/**
	 * 1-of-3 methods that make up the recursive B+Tree traversal insert algorithm.
	 * This is insert method that other classes will call when the users or a system table need to make an insertion
	 * into the file. This one implicitly calls the root page as our starting page.<br>
	 *
	 * The DataRecord is accepted with already initialized column types and values
	 * @param dataRecord the DataRecord to insert into the Table
	 *                  TODO:
	 * @return the 4-byte of the newly inserted record, -1 if the insert failed
	 */
	public void insert(DataRecord dataRecord) {
		LOGGER.log(Level.INFO, "Entering insert(DataRecord)");
		if (!Optional.ofNullable(this.root).isPresent()) {
			throw new IllegalStateException("Tree can't have null root");
		}
		
		if (validInsert(dataRecord)) {
			LOGGER.log(Level.INFO, "Valid insert");
			int newRowId = getRowIdCounter();
			insert(new TableLeafCell(newRowId, dataRecord));
		}
		LOGGER.log(Level.INFO, "Exiting insert(DataRecord)");
	}
	
	/**
	 * 2-of-3 methods that make up the recursive B+Tree traversal insert algorithm.
	 * This method accepts a TableLeafCell created by either insert(DataRecord) or insert(DataType[], String[]). This
	 * method first checks if the root is full and if it is then it is split. Before splitting though a new root must
	 * first be created and a pointer to the old root inserted before moving on to split it.<br>
	 *
	 * Afterwards we call insertNonFull on the new root and continue trying to insert, else we call insertNonFull on the
	 * old root.
	 * @param newRecord a TableLeafCell with it's rowId that was created by one of the first insert methods.
	 */
	private void insert(TableLeafCell newRecord) {
		LOGGER.log(Level.INFO, "Entering insert(TableLeafCell)");
		// Check if root is is full and needs splitting
		if (this.root.isFull(this.treeConfig)) {
			// Save old root for the split function
			this.newLeftChild = this.root;
			
			// Get the next free page number for its new position in the file
			this.newLeftChild.setPageNumber(getNewPageNumber());
			
			// Create new root and save it since there will be no way to save it later
			TableInteriorPage tmpNewPage = new TableInteriorPage(PageType.TABLE_INTERIOR_ROOT, ZERO, this.treeConfig);
			
			// Create a new data cell to store in the new root page that points to the left child
			TableInteriorCell newRootCell = new TableInteriorCell(this.newLeftChild.getMaxRowId(), this.newLeftChild.getPageNumber());
			
			tmpNewPage.addDataCell(newRootCell);
			
			this.root = tmpNewPage;
			
			// Run split root
			splitChildPage(tmpNewPage);
			insertNonFull(tmpNewPage, newRecord);
		} else {
			insertNonFull(this.root, newRecord);
		}
		LOGGER.log(Level.INFO, "Exiting insert(TableLeafCell)");
	}
	
	/**
	 * 3-of-3 methods for inserting a new record into a table. This is called from method 2-of-3 and assumes that the
	 * the current page is non-full, but one of the children we may recurse into might be full.<br>
	 *
	 * If this is a leaf page then assuming this is not a duplicate entry we simply insert it, increase the
	 * rowIdCounter, and write the new page to file.<br>
	 *
	 * Else this is not a leaf cell and we must continue traversing down the tree. After finding the correct child to
	 * traverse into it checks if the child is full and starts a split operation. All the work is handled by the split
	 * method this time as it was only for splitting the root was there extra work. After splitting the method decides
	 * which of the pages is the correct one after the split and then is traverses into it accordingly.
	 * @param currentPage the page following the page that called this method
	 * @param tableLeafCell the new DataRecord to insert wrapped in a tableLeafCell with it's rowId
	 */
	private void insertNonFull(Page currentPage, TableLeafCell tableLeafCell) {
		LOGGER.log(Level.INFO, "Entering insertNonFull()");
		int rowId = tableLeafCell.getRowId();
		if(currentPage.isLeaf()) {
			// In Leaf, double check there isn't a duplicate and if there throw an exception, otherwise insert
			TableLeafPage leaf = (TableLeafPage)currentPage;
			
			if(leaf.contains(tableLeafCell)) {
				throw new IllegalStateException("Error, there is a duplicate entry");
			}
			currentPage.addDataCell(tableLeafCell);
			incrementRowIdCounter();
			currentPage.writePage(tableFile);
		}
		if(currentPage.isInterior()) {
			TableInteriorPage currentInteriorPage = (TableInteriorPage)currentPage;
			// Get the next page number to traverse to and load it from the file
			int nextPageNum = currentInteriorPage.getNextPage(rowId);
			Page nextPage = getPage(nextPageNum);
			int nextPagePtr;
			if (nextPage.isFull(treeConfig)) {
				this.newLeftChild = nextPage;
				splitChildPage((TableInteriorPage) currentPage);
				
				if (rowId >= nextPage.getMaxRowId()) {
					nextPagePtr = ((TableInteriorPage) currentPage).getNextPagePointer();
				} else {
					nextPagePtr = ((TableInteriorCell)currentPage.getFirst()).getLeftChildPointer();
				}
				this.newLeftChild = null;
				nextPage = getPage(nextPagePtr);
			}
			insertNonFull(nextPage, tableLeafCell);
		}
		LOGGER.log(Level.INFO, "Exiting insertNonFull()");
	}
	
	/**
	 * Checks to see if the given insert of colTypes and colValues are valid. Input validation should be done at the
	 * front end
	 * @param colTypes array of bytes that represent each column's data types
	 * @param colValues the actual values that will be stored in each column.
	 * @return true if this is a valid insert false otherwise
	 */
	boolean validInsert(ArrayList<DataType> colTypes, ArrayList<String> colValues) {
		if(!DataType.sameColTypes(colTypes, treeConfig.getColTypes())) {
			throw new IllegalArgumentException("Given column data types do not match this tree's column data types");
		}
		
		if(colTypes.size() != colValues.size()) {
			throw new IllegalArgumentException("The column type arrays and the column values array have different " +
					"sizes");
		}
		return true;
	}
	
	/**
	 * Checks to see if the given insert of this DataRecord is valid for this table. Input validation should be done at
	 * the front end
	 * @param dataRecord a DataRecord to insert ito the table
	 * @return true if this is a valid insert false otherwise
	 */
	boolean validInsert(DataRecord dataRecord) {
		if(!DataType.sameColTypes(dataRecord.getColumnDataTypes(), treeConfig.getColTypes())) {
			throw new IllegalArgumentException("Given column data types do not match this tree's column data types");
		}
		return true;
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 * Split/Merge Methods & helpers
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * This method when given a parent page will split one of it's children. The child to be split is assumed to have
	 * already been saved in newLeftChild.<br>
	 *
	 * The page is split on the middle/median entry when the cells are sorted by rowId. The median entry will go to the
	 * newRightChild, but it's rowId will be used to update the rowId of the pointer to newLeftChild. <br>
	 *     Before the split the pointer to the newLeftChild was the minRowId of the page immediately to the left, that
	 *     pointer will be updated to the median rowId as that will be the minRowId of the newRightChild. <br>
	 *
	 * If the newRightChild is not the far right child then a new pointer to the newRightChild will be placed in the
	 * parent with the minRowId of the page immediately to the right. If it is the far right child then the rightPointer
	 * of the parent page will be updated to point to the newRightChild and the medianRowId(now the minRowId of
	 * newRightChild) will be used to insert a new pointer in the parent to the newLeftChild. <br>
	 *
	 * After the new pages and their pointers are created the all values >= to the medianRowId will be moved over to the
	 * newRightChild. Afterwards the pages will be written to disk and the pointer to the newLeftChild is cleared to be
	 * safe.
	 * @param parentPage the parent page of the page to be split
	 */
	private void splitChildPage(TableInteriorPage parentPage) {
		LOGGER.log(Level.INFO, "Entering splitChildPage()");
		// Assuming a parent Interior Page
		DataCell dataCell;
		Page rightChild = null;
		int medianRowId = this.newLeftChild.getMedianRowId();
		
		// If leaf Page, create a new leaf page
		if (this.newLeftChild.isLeaf()) {
			rightChild = newRightLeafPage(parentPage);
			TableLeafPage leftChild = (TableLeafPage)this.newLeftChild;
			moveCellsToRight(leftChild, rightChild);
		}
		if (this.newLeftChild.isInterior()) {
			rightChild = newRightInteriorPage(parentPage);
			TableInteriorPage leftChild = (TableInteriorPage) this.newLeftChild;
			moveCellsToRight(leftChild, rightChild);
		}
		
		this.newLeftChild.writePage(tableFile);
		this.newLeftChild = null;
		rightChild.writePage(tableFile);
		parentPage.writePage(tableFile);
		
		LOGGER.log(Level.INFO, "Exiting splitChildPage()");
	}
	
	/**
	 * Moved all DataCells with rowId >= medianRowId over to the newRightChild of a split.
	 * @param leftChild the original page that is being split
	 * @param rightChild the new page we need to move around half of the leftChild's entries to
	 */
	private void moveCellsToRight(Page leftChild, Page rightChild) {
		int medianRowId = leftChild.getMedianRowId();
		ArrayList<DataCell> moveList = new ArrayList<>();
		for(DataCell cell : leftChild.getDataCells()) {
			if (cell.getRowId() >= medianRowId) {
				// Move to right child,  Remove from left
				moveList.add(leftChild.getDataCellFromRowId(cell.getRowId()));
				// If == media row ID then remove as it is now a key in the new parent
			}
		}
		// Check if it was the root and adjust the PageType
		if (leftChild.isRoot()) {
			if (leftChild.isLeaf()) {
				leftChild.setPageType(PageType.TABLE_LEAF_PAGE);
			} else {
				leftChild.setPageType(PageType.TABLE_INTERIOR_PAGE);
			}
		}
		// Move the cells over
		leftChild.removeList(moveList);
		rightChild.addList(moveList);
	}
	
	/**
	 * When splitting a page this method is called when creating a new leaf page.
	 *
	 * The nextPagePointer of the original leaf is moved over to the rightChild so as to continue the linked list of
	 * leaves. The newRightChild's pageNumber is added as the leftChilds nextPagePointer.<br>
	 *
	 *
	 * Before the split the pointer to the newLeftChild was the minRowId of the page immediately to the left, that
	 * pointer will be updated to the median rowId as that will be the minRowId of the newRightChild. <br>
	 *
	 * If the newRightChild is not the far right child then a new pointer to the newRightChild will be placed in the
	 * parent with the minRowId of the page immediately to the right. If it is the far right child then the rightPointer
	 * of the parent page will be updated to point to the newRightChild and the medianRowId(now the minRowId of
	 * newRightChild) will be used to insert a new pointer in the parent to the newLeftChild. <br>
	 *
	 *
	 * @param splitParent the page parenting the split
	 * @return new TableLeafPage with all pointers correctly set, just absent of DataCells
	 */
	private TableLeafPage newRightLeafPage(TableInteriorPage splitParent) {
		LOGGER.log(Level.INFO, "Entering newRightLeafPage()");
		// Cast to TableLeafPage so we can access the subclass' getters/setters
		TableLeafPage tmpLeafPage = (TableLeafPage) this.newLeftChild;
		// Create new right child leaf page and store leftChild's rightPointer as the rightChild's new right pointer
		TableLeafPage rightChild = new TableLeafPage(PageType.TABLE_LEAF_PAGE, getNewPageNumber(), tmpLeafPage.getNextPagePointer(), treeConfig);
		
		// Switch the left child's rightPointer to the right child's new page number
		tmpLeafPage.setNextPagePointer(rightChild.getPageNumber());
		
		// Add pointer of new tree to old parent
		int medianRowId = this.newLeftChild.getMedianRowId();
		int maxIdInLeft = this.newLeftChild.getMaxRowId();
		int minIdInLeft = this.newLeftChild.getMinRowId();
		int maxIdInParent = splitParent.getMaxRowId();
		
		if(splitParent.isRoot() && splitParent.getNumOfCells() == ONE && splitParent.getMaxRowId() ==
				newLeftChild.getMaxRowId()) {
			// This is the first split set the rightChild as the nextPagePointer and update the leftChildPointer to
			// the median rowId
			splitParent.getDataCells().get(ZERO).setRowId(medianRowId);
			splitParent.setNextPagePointer(rightChild.getPageNumber());
		} else if (maxIdInParent == minIdInLeft) {
			// Far Right Page
			// Insert new Data Cell to point to left child as there is no pointer to this cell in the parent
			TableInteriorCell newLeftPointer = new TableInteriorCell(medianRowId, newLeftChild.getPageNumber());
			splitParent.addDataCell(newLeftPointer);
			// Update rightPagePointer of Parent
			splitParent.setNextPagePointer(rightChild.getPageNumber());
		} else {
			// Update leftChild's pointer in parent to median value
			TableInteriorCell parentLeftPointer =
					(TableInteriorCell) splitParent.getDataCellFromRowId(maxIdInLeft);
			parentLeftPointer.setRowId(medianRowId);
			
			// Go to next leaf page in LinkedList chain on bottom row of the page
			TableLeafPage nextLeafPage = (TableLeafPage)getPage(rightChild.getNextPagePointer());
			// Get smallest value from it, this is the rowId for the new TableInteriorCell to put in the parent
			TableInteriorCell newRightPointer = new TableInteriorCell(nextLeafPage.getMinRowId(), rightChild.getPageNumber());
			splitParent.addDataCell(newRightPointer);
		}
		LOGGER.log(Level.INFO, "Exiting newRightLeafPage()");
		return rightChild;
	}
	
	/**
	 * When splitting a page this method is called when creating a new leaf page.
	 *
	 * The nextPagePointer of the original leaf is moved over to the rightChild<br>
	 *
	 
	 * Before the split the pointer to the newLeftChild was the minRowId of the page immediately to the left, that
	 * pointer will be updated to the median rowId as that will be the minRowId of the newRightChild. <br>
	 *
	 * If the newRightChild is not the far right child then a new pointer to the newRightChild will be placed in the
	 * parent with the minRowId of the page immediately to the right. If it is the far right child then the rightPointer
	 * of the parent page will be updated to point to the newRightChild and the medianRowId(now the minRowId of
	 * newRightChild) will be used to insert a new pointer in the parent to the newLeftChild. <br>
	 *
	 *
	 * @param splitParent the page parenting the split
	 * @return new TableInteriorPage with all pointers correctly set, just absent of DataCells
	 */
	private TableInteriorPage newRightInteriorPage(TableInteriorPage splitParent) {
		LOGGER.log(Level.INFO, "Entering newRightInteriorPage()");
		
		// Cast to TableInteriorPage so we can access the subclass' getters/setters
		TableInteriorPage newInteriorLeftChild = (TableInteriorPage) this.newLeftChild;
		
		// Create new right child interior page and store leftChild's rightPointer as the rightChild's new right pointer
		TableInteriorPage newInteriorRightChild = new TableInteriorPage(PageType.TABLE_INTERIOR_PAGE, getNewPageNumber(),
				newInteriorLeftChild.getNextPagePointer(), treeConfig);
		
		
		int medianRowId = newInteriorLeftChild.getMedianRowId();
		int maxIdInLeft = newInteriorLeftChild.getMaxRowId();
		int minIdinLeft = newInteriorLeftChild.getMinRowId();
		int maxIdInParent = splitParent.getMaxRowId();
		
		// if leftChild.pageNum = parent.pageNum, then before the split
		// this was a far-right page.
		if(splitParent.isRoot() && splitParent.getNumOfCells() == ONE && splitParent.getMaxRowId() == newLeftChild.getMaxRowId()) {
			// This is the first split set the rightChild as the nextPagePointer and update the leftChildPointer to
			// the median rowId
			splitParent.getDataCells().get(ZERO).setRowId(medianRowId);
			splitParent.setNextPagePointer(newInteriorRightChild.getPageNumber());
		} else if(minIdinLeft >= maxIdInParent) {
			// Page is part of the alt-right
			splitParent.setNextPagePointer(newInteriorRightChild.getPageNumber());
			
			// Add new pointer to left child in parent and store it's median rowId as the key
			TableInteriorCell newLeftPointerInParent = new TableInteriorCell(medianRowId,
					newInteriorLeftChild.getPageNumber());
			
			splitParent.addDataCell(newLeftPointerInParent);
		} else {
			
			// Get smallest pointer from next bucket over(to the right) which will be the rowId value for the pointer of
			// the new right child. Must get this before updating the leftPointer to the Median ID
			int rightChildPointerKey = splitParent.getCellRightNeighbor(maxIdInLeft).getRowId();
			
			// Update leftChild's pinter in the partent to median value
			TableInteriorCell pointerToLeft = splitParent.getDataCellFromPagePointer(newInteriorLeftChild.getPageNumber());
			pointerToLeft.setRowId(medianRowId);
			
			// Create a new cell to stuff into the parent for the right child
			TableInteriorCell newRightPointer = new TableInteriorCell(rightChildPointerKey,
					newInteriorRightChild.getPageNumber());
			splitParent.addDataCell(newRightPointer);
		}
		return newInteriorRightChild;
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *           File I/O
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * Checks to see if the Tree file exits if it does it sets the root page field of this tree instance if it does
	 * not exist it calls `createTreeFile` to create a new tree file.
	 */
	private void openTreeFile() {
		LOGGER.log(Level.INFO, "Entering openTreeFile()");
		if (!FileHandler.doesTableExist(this.fileName)) {
			LOGGER.log(Level.INFO, "File DNE, must create new table file");
			// Need to create new file
			LOGGER.log(Level.INFO, "Creating new table file for: {0}", this.databaseName);
			createTreeFile();
		} else {
			try {
				LOGGER.log(Level.INFO, "Entering table exists, need to read from file");
				this.tableFile = new RandomAccessFile(this.fileName, READ_WRITE_MODE);
				LOGGER.log(Level.INFO, "Table {0} exists", this.databaseName);
				getRootPage();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
		}
		LOGGER.log(Level.INFO, "Exiting openTreeFile()");
	}
	
	/**
	 * Method to create a tree file and insert a new root leaf page if the tree does not exist
	 */
	private void createTreeFile() {
		LOGGER.log(Level.INFO, "Entering createTreeFile()");
		// Create file and set length equal to PAGE_SIZE
		FileHandler.createTableFile(this.fileName);
		LOGGER.log(Level.INFO, "New table file for {0} created", this.databaseName);
		try {
			this.tableFile = new RandomAccessFile(this.fileName, READ_WRITE_MODE);
			this.root = createNewRootLeaf();
			LOGGER.log(Level.INFO, "Exiting createTreeFile()");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
	}
	
	/**
	 * Used whenever the file is first created to insert a new root page that also happens to be a leaf because it's a
	 * new file.
	 * TODO: Move to Abstract Tree class
	 * @return the new root leaf page just created so it can be saved with for later use
	 */
	private TableLeafPage createNewRootLeaf() {
		LOGGER.log(Level.INFO, "Entering createNewRootLeaf()");
		TableLeafPage tableLeafPage = new TableLeafPage(PageType.TABLE_LEAF_ROOT, ZERO, -ONE, treeConfig);
		tableLeafPage.writePage(this.tableFile);
		LOGGER.log(Level.INFO, "New Root Leaf for: {0}: ", this.databaseName);
		numOfPages++;
		LOGGER.log(Level.INFO, "Exiting createNewRootLeaf()");
		return tableLeafPage;
	}
	
	/**
	 * Method that retrieves a specific page from the file depending on the page number and the PAGE_SIZE value set
	 * in the Config class.
	 *
	 * TODO: add in Index pages
	 * @see edu.utdallas.cs6360.davisbase.Config
	 * @param pageNumber a 4-byte integer representing the physical position of the desired page within the file
	 *                   relative to the beginning of the file
	 * @return depending on the type code stored in file a
	 * TableInteriorPage/TableLeafPage/IndexInteriorPage/IndexLeafPage
	 */
	private Page getPage(int pageNumber) {
		try {
			byte[] pageBytes = new byte[PAGE_SIZE];
			this.tableFile.seek(PAGE_SIZE * pageNumber);
			this.tableFile.read(pageBytes);
			
			// Return a different subclass depending on the PageType value
			return PageType.getEnum(pageBytes[ZERO]) == PageType.TABLE_LEAF_PAGE ?
					new TableLeafPage(pageBytes, pageNumber, treeConfig) :
					new TableInteriorPage(pageBytes, pageNumber, treeConfig);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
		return null;
	}
	
	/**
	 * Expand the file if it is not enough room to insert a new page
	 */
	private void expandFile() {
		try {
			this.tableFile.setLength(this.tableFile.length() + PAGE_SIZE);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
	}
	
	/**
	 * Shrink the file if a page was removed at the end and reclaim space.
	 */
	private void shrinkFile() {
		try {
			this.tableFile.setLength(this.tableFile.length() - PAGE_SIZE);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
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
	 * Retrieves the root page from the beginning of the file and stores it for later use by the tree. It does this
	 * by passing the root page number of 0 to `getPage()`
	 */
	private void getRootPage() {
		// Prepare array and file to read in the root page data
		this.root = getPage(ROOT_PAGE_NUMBER);
	}
	
	/**
	 * TODO: Link to Metadata tables
	 */
	public int getRowIdCounter() {
		LOGGER.log(Level.INFO, "Returning rowIdCounter value: {0}", rowIdCounter);
		return rowIdCounter;
	}
	
	/**
	 * Used to increment the rowIdCounter when a new entry is added
	 */
	private void incrementRowIdCounter() {
		this.rowIdCounter++;
		LOGGER.log(Level.INFO, "Incremented rowIdCounter new value: {0}", rowIdCounter);
	}
	
	/**
	 * This can probably be removed because rowIds should always be unique and never reassigned.
	 * TODO: Take out
	 */
	private void decrementRowIdCounter() {
		this.rowIdCounter--;
		LOGGER.log(Level.INFO, "Decremented rowIdCounter new value: {0}", rowIdCounter);
	}
	
	/**
	 * Getter for property 'fileName'.
	 *
	 * @return Value for property 'fileName'.
	 */
	String getFileName() {
		return fileName;
	}
	
	/**
	 * Retrieve the number of used pages
	 * @return the number of used pages in the file
	 */
	private int getNumOfPages() {
		return this.numOfPages;
	}
	
	/**
	 * Increase the number of used pages
	 */
	private void incrementPages() {
		this.numOfPages++;
	}
	
	/**
	 * Decrease the number of used pages
	 */
	private void decrementPages() {
		this.numOfPages--;
	}
	
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 * Methods that Calculate Things
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * This method searches pages sequentially until it finds a page header of all null values
	 * meaning we have found a free page. If a page is not found a new one is added at the end of the file
	 *
	 * TODO: Implement some search algorithm to find an empty space of 000s/NULLs to insert a new page
	 * 		 Something like binary search?
	 */
	private int getNewPageNumber() {
		try {
			int maxPagesInFile = (int)(tableFile.length() / PAGE_SIZE);
			if (maxPagesInFile == numOfPages || maxPagesInFile < this.numOfPages) {
				int newPageNum = this.numOfPages;
				incrementPages();
				return newPageNum;
			}
			// Must find empty page in file
			for(int i = ZERO; i < this.numOfPages; i++) {
				this.tableFile.seek(i * PAGE_SIZE);
				byte[] pageTypeCode = new byte[ONE];
				this.tableFile.read(pageTypeCode);
				if (pageTypeCode[ZERO] == ZERO) {
					incrementPages();
					return i;
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
		return -ONE;
	}
	
	/**
	 * This recursive method simply traverses down the far right branch of this tree and it's children and counts the
	 * number of along levels on the way. This public one is starts at the root, if it is a leaf it returns 0. If it is
	 * not then the private version of this method is called on the nextPagePointer for the root.
	 * @return an integer representing the number of levels of the tree with 0 being the root level.
	 */
	int getCurrentHeight() {
		if (this.root.isLeaf()) {
			return ZERO;
		}
		TableInteriorPage interiorRoot = (TableInteriorPage)this.root;
		return getCurrentHeight(getPage(interiorRoot.getNextPagePointer()));
	}
	
	/**
	 * This private method continues the traversal down the right side of the tree. If it is a leaf (the base case) then
	 * it returns 1, otherwise it recurses down the right tree again and returns that tree's level + 1
	 * @param page a page from the far right branch of the tree.
	 * @return if a leaf 1, else getCurrentHeight(rightChild) + 1
	 */
	private int getCurrentHeight(Page page) {
		if (page.isLeaf()) {
			return ONE;
		}
		TableInteriorPage interiorPage = (TableInteriorPage)page;
		return getCurrentHeight(getPage(interiorPage.getNextPagePointer())) + ONE;
	}
}