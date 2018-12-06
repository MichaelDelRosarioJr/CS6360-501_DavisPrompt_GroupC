package edu.utdallas.cs6360.davisbase.trees;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static edu.utdallas.cs6360.davisbase.utils.ByteHelpers.*;
import static edu.utdallas.cs6360.davisbase.Config.*;

/**
 * A Class to represent a data record from the Database<br>
 *
 * A DataRecord is created by supplying an array of bytes and an array of
 * Strings to the constructor. The Bytes are the codes representing the column
 * data types and the Strings are the columns values.<br>
 *
 * The getBytes() method can be used to export the DataRecord to a disk and a constructor
 * is provides that can recreate the DataRecord from it's byte representation.
 * @author Charles Krol
 * @author Matthew Villarreal
 * @author Michael Del Rosario
 * @author Mithil Vijay
 */
public class DataRecord {
	
	/**
	 * The DataTypes fir each column in this DataRecord
	 */
	private ArrayList<DataType> columnDataType;
	
	/**
	 * The actual values stored in the columns of this DataRecord
	 */
	private ArrayList<String> columnData;
	
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
	 * Default Constructor that sets the columnDataTye and columnData arrays to null
	 */
	DataRecord() {
		columnDataType = null;
		columnData = null;
	}
	
	/**
	 * A Constructor to be used to create new DataRecords from scripts or user input<br>
	 * This Constructor accepts a DataType array containing the column type DataTypes and
	 * a String array containing the column data.
	 * @param types a DataType array containing the column type
	 * @param data a String array containing the column data
	 */
	DataRecord(ArrayList<DataType> types, ArrayList<String> data) {
		this.columnData = data;
		this.columnDataType = types;
	}
	
	/**
	 * This Constructor is used when loading a DataRecord from disk.<br>
	 * It accepts an array of bytes that was prepared by the DataRecord's
	 * getBytes() class method at an earlier time. The array is parsed and the
	 * DataRecord is recreated from the raw bytes.<br>
	 *
	 * The byte array is wrapped in a ByteBuffer and the first byte is removed from
	 * the buffer and unsigned to get the number of columns is removed from the buffer
	 * and used to initialize the columnDataType and columnData arrays.<br>
	 *
	 * The column type codes are collected and stored in columnTypeCodes. Text type codes
	 * are stored with the length temporarily which is later accounted for when processing the
	 * data bytes.<br>
	 *
	 * The ByteBuffer's methods are used reconstruct the data from the bytes. When text data is
	 * encountered the length is determined from the type code and the correct TEXT_TYPE_CODE is
	 * stored in the columnDataType array.
	 *
	 * @param data an array of bytes representing a DataRecord created by the
	 *             getBytes() class method at an earlier time
	 */
	DataRecord(byte[] data) {
		// Wrap payload byte array from file in a ByteBuffer
		ByteBuffer dataBuffer = ByteBuffer.wrap(data);
		
		// Get the number of columns and create arrays
		
		int numColumns = byteToUnSignedInt(dataBuffer.get());
		this.columnDataType = new ArrayList<>();
		this.columnData = new ArrayList<>();
		
		ArrayList<Byte> originalCodes = new ArrayList<>();
		// Get column data types
		for(int i = ZERO; i < numColumns; i++) {
			originalCodes.add(dataBuffer.get());
			this.columnDataType.add(DataType.getEnum(originalCodes.get(i)));
			
		}
		
		// Pointer for column arrays
		// TODO change null values to load and store values to disk if not whitespace I think the NULL field means it can be NULL not that is actually is null
		int columnPointer = ZERO;
		while (dataBuffer.hasRemaining() && columnPointer < this.columnDataType.size()) {
			switch (this.columnDataType.get(columnPointer)) {
				case NULL1_TYPE_CODE:
					this.columnData.add("");
					columnPointer++;
					dataBuffer.get();
					break;
				case NULL2_TYPE_CODE:
					this.columnData.add("");
					columnPointer++;
					dataBuffer.getShort();
					break;
				case NULL4_TYPE_CODE:
					this.columnData.add("");
					columnPointer++;
					dataBuffer.getInt();
					break;
				case NULL8_TYPE_CODE:
					this.columnData.add("");
					columnPointer++;
					dataBuffer.getLong();
					break;
				case TINY_INT_TYPE_CODE:
					this.columnData.add(Byte.toString(dataBuffer.get()));
					columnPointer++;
					break;
				case SHORT_TYPE_CODE:
					this.columnData.add(Short.toString(dataBuffer.getShort()));
					columnPointer++;
					break;
				case INT_TYPE_CODE:
					this.columnData.add(Integer.toString(dataBuffer.getInt()));
					columnPointer++;
					break;
				case LONG_TYPE_CODE:
					this.columnData.add(Long.toString(dataBuffer.getLong()));
					columnPointer++;
					break;
				case REAL_TYPE_CODE:
					this.columnData.add(Float.toString(dataBuffer.getFloat()));
					columnPointer++;
					break;
				case DOUBLE_TYPE_CODE:
					this.columnData.add(Double.toString(dataBuffer.getDouble()));
					columnPointer++;
					break;
				case DATETIME_TYPE_CODE:
				case DATE_TYPE_CODE:
					this.columnData.add(Long.toUnsignedString(dataBuffer.getLong()));
					columnPointer++;
					break;
				case TEXT_TYPE_CODE:
					// Get the size of the text field from the type code byte representation
					int lengthOfText = DataType.getDataTypeSize(originalCodes.get(columnPointer));
					// Store the correct type code
					this.columnDataType.add(DataType.TEXT_TYPE_CODE);
					// Use the lengthOfText local variable to collect the text values
					byte[] textColData = new byte[lengthOfText];
					dataBuffer.get(textColData, ZERO, lengthOfText);
					this.columnData.add(new String(textColData, StandardCharsets.US_ASCII));
					columnPointer++;
					break;
				default:
					throw new IllegalStateException("Invalid Data Type Byte Code");
			}
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
	 * A getter method for a column's data type
	 * @param colId the id of the column type to retrieve
	 * @return the column's data type code
	 */
	DataType getColumnDataType(int colId) {
		return this.columnDataType.get(colId);
	}
	
	/**
	 * A setter method column data. If the column type is a null
	 * it's entry in columnDataType is updated to reflect it no longer
	 * being null
	 * @param colId the id of the column to update
	 * @param data the String containing the correct data
	 */
	void setColumnData(int colId, String data) {
		DataType colType = this.columnDataType.get(colId);
		
		if (colType == DataType.NULL1_TYPE_CODE) {
			this.columnDataType.set(colId, DataType.TINY_INT_TYPE_CODE);
		}
		
		if (colType == DataType.NULL2_TYPE_CODE) {
			this.columnDataType.set(colId, DataType.SHORT_TYPE_CODE);
		}
		
		if (colType == DataType.NULL4_TYPE_CODE) {
			this.columnDataType.set(colId, DataType.INT_TYPE_CODE);
		}
		
		if (colType == DataType.NULL8_TYPE_CODE) {
			this.columnDataType.set(colId, DataType.LONG_TYPE_CODE);
		}
		
		this.columnData.set(colId, data);
	}
	
	/**
	 * A method that sets a column to null. If the data type does
	 * not support NULL values nothing will happen. <br>
	 *
	 * It is assumed the NOT NULL constraint has already been checked
	 * before this method gets called
	 * @param colId the id of the column to set to null
	 */
	void setColumnNull(int colId) {
		DataType colType = this.columnDataType.get(colId);
		
		if (colType == DataType.TINY_INT_TYPE_CODE) {
			this.columnDataType.set(colId, DataType.NULL1_TYPE_CODE);
			this.columnData.set(colId, "");
		}
		
		if (colType == DataType.SHORT_TYPE_CODE) {
			this.columnDataType.set(colId, DataType.NULL2_TYPE_CODE);
			this.columnData.set(colId, "");
		}
		
		if (colType == DataType.INT_TYPE_CODE) {
			this.columnDataType.set(colId, DataType.NULL4_TYPE_CODE);
			this.columnData.set(colId, "");
		}
		
		if (colType == DataType.LONG_TYPE_CODE) {
			this.columnDataType.set(colId, DataType.NULL8_TYPE_CODE);
			this.columnData.set(colId, "");
		}
		
	}
	
	/**
	 * A method to return the byte array containing
	 * DataRecord formatted into the specified format
	 * @return a byte array containing the correctly formatted data
	 */
	ArrayList<Byte> getBytes() {
		ArrayList<Byte> output = new ArrayList<>();
		output.add((byte) this.columnData.size());
		
		
		output.addAll(getColumnCodes());
		// Get column data bytes and add to the end of the ArrayList
		output.addAll(getColumnDataBytes());
		
		return output;
	}
	
	/**
	 * Method to return the size of the page
	 * @return the number of data cells stored on the page
	 */
	short size() {
		ArrayList<Byte> dataRecordBytes = getBytes();
		return (short)dataRecordBytes.size();
	}
	
	/**
	 * A private helper method to help reduce the complexity of the getBytes() class method
	 * @return an ArrayList with the column data type codes
	 */
	private ArrayList<Byte> getColumnCodes() {
		ArrayList<Byte> output = new ArrayList<>();
		
		for (int i = ZERO; i < this.columnDataType.size(); i++) {
			if (this.columnDataType.get(i).getTypeCode() < DataType.TEXT_TYPE_CODE.getTypeCode()) {
				output.add(this.columnDataType.get(i).getTypeCode());
			} else {
				int textLength = this.columnData.get(i).getBytes(StandardCharsets.US_ASCII).length;
				output.add((byte) (this.columnDataType.get(i).getTypeCode() +(byte)textLength));
			}
		}
		return output;
	}
	
	/**
	 * A private helper method to help reduce the complexity of the getBytes() class method
	 * @return an ArrayList with the data bytes of our column values
	 */
	private ArrayList<Byte> getColumnDataBytes() {
		ArrayList<Byte> output = new ArrayList<>();
		
		for (int i = ZERO; i < this.columnData.size(); i++) {
			switch (this.columnDataType.get(i)) {
				case NULL1_TYPE_CODE:
					output.add((byte) ZERO);
					break;
				case NULL2_TYPE_CODE:
					for (int j = ZERO; j < Short.BYTES; j++) { output.add((byte) ZERO); }
					break;
				case NULL4_TYPE_CODE:
					for (int j = ZERO; j < Integer.BYTES; j++) { output.add((byte) ZERO); }
					break;
				case NULL8_TYPE_CODE:
					for (int j = ZERO; j < Long.BYTES; j++) { output.add((byte) ZERO); }
					break;
				case TINY_INT_TYPE_CODE:
					output.add(Byte.parseByte(this.columnData.get(i)));
					break;
				case SHORT_TYPE_CODE:
					byte[] shortVal
							= shortToBytes(Short.parseShort(this.columnData.get(i)));
					for (byte b: shortVal) {
						output.add(b);
					}
					break;
				case INT_TYPE_CODE:
					byte[] intVal
							= intToBytes(Integer.parseInt(this.columnData.get(i)));
					for (byte b: intVal) {
						output.add(b);
					}
					break;
				case LONG_TYPE_CODE:
					byte[] longVal
							= longToBytes(Long.parseLong(this.columnData.get(i)));
					for (byte b: longVal) {
						output.add(b);
					}
					break;
				case REAL_TYPE_CODE:
					byte[] floatVal
							= floatToByte(Float.parseFloat(this.columnData.get(i)));
					for (byte b: floatVal) {
						output.add(b);
					}
					break;
				case DOUBLE_TYPE_CODE:
					byte[] doubleVal
							= doubleToBytes(Double.parseDouble(this.columnData.get(i)));
					for (byte b: doubleVal) {
						output.add(b);
					}
					break;
				case DATETIME_TYPE_CODE:
					byte[] dateTimeLongVal
							= longToBytes(Long.parseUnsignedLong(this.columnData.get(i)));
					for (byte b: dateTimeLongVal) {
						output.add(b);
					}
					break;
				case DATE_TYPE_CODE:
					byte[] dateLongVal
							= longToBytes(Long.parseUnsignedLong(this.columnData.get(i)));
					for (byte b: dateLongVal) {
						output.add(b);
					}
					break;
				case TEXT_TYPE_CODE:
					byte[] textVal = this.columnData.get(i).getBytes(StandardCharsets.US_ASCII);
					for (byte b: textVal) {
						output.add(b);
					}
					break;
				default:
					throw new IllegalStateException("Illegal Data Type");
				
			}
		}
		return output;
	}
	
	/**
	 * Accepts a time in milliseconds from the epoch and returns
	 * a string in the format yyyy-MM-dd_HH:mm:ss
	 * @param ms time in milliseconds from the epoch
	 * @return a string in the format yyyy-MM-dd_HH:mm:ss
	 */
	static String getDateTimeTypeString(long ms) {
		DateFormat datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		Calendar dateTimeCal = Calendar.getInstance();
		dateTimeCal.setTimeInMillis(ms);
		return datetimeFormatter.format(dateTimeCal.getTime());
	}
	
	/**
	 * Accepts a time in milliseconds from the epoch and returns
	 * a string in the format yyyy-MM-dd
	 * @param ms time in milliseconds from the epoch
	 * @return a string in the format yyyy-MM-dd
	 */
	static String getDateTypeString(long ms) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar dateCal = Calendar.getInstance();
		dateCal.setTimeInMillis(ms);
		return dateFormat.format(dateCal.getTime());
		
	}
	
	/**
	 * A helper function used by the equals() method and the getBytes() method
	 * that returns the number of columns as a byte
	 * @return a byte representing the number of columns in the DataRecord
	 */
	byte getNumColumns() {
		return (byte) this.columnData.size();
	}
	
	/**
	 * A helper method used by the equals() method that returns the
	 * DataRecord's column type codes
	 * @return an array of bytes containing the DataRecord's coulmn type codes
	 */
	ArrayList<DataType> getColumnDataTypes() { return this.columnDataType; }
	
	/**
	 * A helper method used by the equals() method that returns the
	 * DataRecord's column data
	 * @return a String array containing the DataRecord's column data
	 */
	ArrayList<String> getColumnData() { return this.columnData; }
	
	/**
	 * Method that outputs the values in a DataRecord
	 * @return String representation of a DataRecord
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(String value : this.columnData) {
			builder.append(String.format("|%10s|", value));
		}
		return builder.toString();
	}
	
	/**
	 * *****************************
	 * *****************************
	 * *****************************
	 *     Overridden Methods
	 * *****************************
	 * *****************************
	 * *****************************
	 */
	
	/**
	 * Used to determine if the given object matches this
	 * DataRecord object. <br>
	 *
	 * 1. Check if the given object is null and if it is returns false<br>
	 * 2. Check if the given object's class matches the DataRecord class,
	 * if it doesn't it returns false<br>
	 * 3. Cast the object to a DataRecord since it is not null and a DataRecord<br>
	 * 4. If they have a different number of columns return false<br>
	 * 5. If their column type codes do not match return false<br>
	 * 6. If the contents of their columns do not match then return false<br>
	 * 7. Everything matches, return true
	 *
	 * @param o an object to compare to an instance of a DataRecord
	 * @return True if the object matches this DataRecord, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		
		if (o == this) { return true; }
		
		// If other object is null return false
		if (!Optional.ofNullable(o).isPresent()) {
			return false;
		}
		
		// If object is not DataRecord return false
		if (!(o instanceof DataRecord)) {
			return false;
		}
		
		// Since not null and DataRecord
		// Cast to DataRecord
		final DataRecord other = (DataRecord) o;
		
		// If different number of columns return false
		if (this.getNumColumns() != other.getNumColumns()) {
			return false;
		}
		
		// If they column types don't match return false
		ArrayList<DataType> otherDataTyes = other.getColumnDataTypes();
		for (int i = ZERO; i < this.getNumColumns(); i++) {
			if (this.columnDataType.get(i) != otherDataTyes.get(i)) {
				return false;
			}
		}
		
		// Since same number of columns and types match check
		// If column contents match, else return false
		ArrayList<String> otherColumnData = other.getColumnData();
		for(int i = ZERO; i < this.getNumColumns(); i++) {
			if (!this.columnData.get(i).equals(otherColumnData.get(i))) {
				return false;
			}
		}
		// Same object, return true
		return true;
	}
	
	/**
	 * Override Object.hashCode() because Intellij is yelling at me to do it
	 * @return object's hash code
	 */
	@Override
	public int hashCode() {
		int result = columnDataType.hashCode();
		result = 31 * result + columnData.hashCode();
		return result;
	}
}