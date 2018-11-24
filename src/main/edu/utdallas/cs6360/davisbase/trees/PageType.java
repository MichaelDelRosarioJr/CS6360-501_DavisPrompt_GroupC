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
	
	PageType(byte code) {
		this.typeCode = code;
	}
	
	public byte getTypeCode() {
		return this.typeCode;
	}
	
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
	 * Used by B-Tree to determine root page type based on it's file byte code.<br>
	 * It is determined to be a root page by it being the first location within the file.
	 * @param typeCode a type code from in the DavisBase specifications
	 * @return the appropriate root type
	 */
	public static PageType getRootType(byte typeCode) {
		switch (typeCode) {
			case (byte)0x0D:
				return TABLE_LEAF_ROOT;
			case (byte)0x05:
				return TABLE_INTERIOR_ROOT;
			case (byte)0x0A:
				return INDEX_LEAF_ROOT;
			case (byte) 0x02:
				return INDEX_INTERIOR_ROOT;
			default:
				throw new IllegalStateException("Invalid type code:" + typeCode + " from table/index file");
		}
	}
}
