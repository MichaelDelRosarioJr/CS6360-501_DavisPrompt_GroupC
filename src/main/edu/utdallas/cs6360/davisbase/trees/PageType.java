package edu.utdallas.cs6360.davisbase.trees;

/**
 * An enum class to take care of some of the PageType logic
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public enum PageType {
	INDEX_LEAF_ROOT((byte)0x00),
	INDEX_INTERIOR_ROOT((byte)0x01),
	INDEX_INTERIOR_PAGE ((byte)0x02),
	INDEX_LEAF_PAGE ((byte)0x0A),
	
	TABLE_LEAF_ROOT((byte) 0x03),
	TABLE_INTERIOR_ROOT ((byte) 0x4),
	TABLE_INTERIOR_PAGE ((byte)0x05),
	TABLE_LEAF_PAGE ((byte)0x0D);
	
	private final byte typeCode;
	
	/**
	 * Constructor that sets the type code for the page type
	 * @param code the code representing a page type
	 */
	PageType(byte code) {
		this.typeCode = code;
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
}
