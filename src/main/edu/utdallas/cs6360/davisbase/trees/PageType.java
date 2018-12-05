package edu.utdallas.cs6360.davisbase.trees;

/**
 * An enum class to take care of some of the PageType logic
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public enum PageType {
	INDEX_LEAF_ROOT((byte)0x00, "Index Leaf Root"),
	INDEX_INTERIOR_ROOT((byte)0x01, "Index Interior Root"),
	INDEX_INTERIOR_PAGE ((byte)0x02, "Index Interior Page"),
	INDEX_LEAF_PAGE ((byte)0x0A, "Index Leaf Page"),
	
	TABLE_LEAF_ROOT((byte) 0x03, "Table Leaf Root"),
	TABLE_INTERIOR_ROOT ((byte) 0x4, "Table Interior Root"),
	TABLE_INTERIOR_PAGE ((byte)0x05, "Table Interior Page"),
	TABLE_LEAF_PAGE ((byte)0x0D, "Table Leaf Page");
	
	private final byte typeCode;
	private final String typeName;
	
	/**
	 * Constructor that sets the type code for the page type
	 * @param code the code representing a page type
	 * @param name the name of the type of Page for the toString() method
	 */
	PageType(byte code, String name) {
		this.typeCode = code;
		this.typeName = name;
	}
	
	/**
	 * Returns the typeCode for the page type
	 * @return the type code for the page type
	 */
	public byte getTypeCode() {
		return this.typeCode;
	}
	
	/**
	 * Returns a PageType when given a code
	 * @param code byte representing a PageType
	 * @return the PageType associated with the code
	 */
	public static PageType getEnum(byte code) {
		for(PageType value : values()) {
			if(value.getTypeCode() == code) { return value; }
		}
		throw new IllegalArgumentException();
	}
	
	/**
	 * Get the appropriate byte code for writing page headers to files
	 * @return byte codes specified by professor Davis
	 */
	public byte getByteCode() {
		if(this.typeCode == TABLE_LEAF_ROOT.getTypeCode()) { return TABLE_LEAF_PAGE.getTypeCode(); }
		if(this.typeCode == TABLE_INTERIOR_ROOT.getTypeCode()) { return TABLE_INTERIOR_PAGE.getTypeCode(); }
		
		if(this.typeCode == INDEX_LEAF_ROOT.getTypeCode()) { return INDEX_LEAF_PAGE.getTypeCode(); }
		if(this.typeCode == INDEX_INTERIOR_ROOT.getTypeCode()) { return INDEX_INTERIOR_PAGE.getTypeCode(); }
		
		return this.typeCode;
	}
	
	/**
	 * toString() method for logging purposes
	 * @return the name of the page type
	 */
	public String toString() {
		return this.typeName;
	}
}