package pkg1;

import java.io.IOException;

import ssdeep.InMemorySsdeep;
import ssdeep.SpamSumSignature;
import ssdeep.ssdeep;

public class DataHash implements Comparable<DataHash>{
	private byte[] data;
	private String hash;
	private static final int similarityTreshold = 66;
	private static InMemorySsdeep sharedInMemoryHasher = null;
	
	public DataHash(byte[] data) throws NullPointerException{
		if(data == null)
			throw new NullPointerException("data pointer should not be null");
		this.data = data;
	}
	
	public DataHash(DataHash dataHash){
		this.data = dataHash.data;
		this.hash = dataHash.hash;
	}
	
	public DataHash clone(){
		return new DataHash(this);
	}
	
	protected static InMemorySsdeep getInMemorySsdeep(){
		if(DataHash.sharedInMemoryHasher == null){
			DataHash.sharedInMemoryHasher = new InMemorySsdeep();
		}
		return DataHash.sharedInMemoryHasher;
	}
	
	public byte[] getData(){
		return this.data.clone();
	}
	
	private String calcHash(){
		String hash = DataHash.getInMemorySsdeep().fuzzy_hash_array(this.data);
		return hash;
	}
	
	public String getHash(){
		if(this.hash == null)
			this.hash = this.calcHash();
		
		return this.hash;
	}
	
	@Override
	public int compareTo(DataHash dataHash){
		if(this == dataHash){
			return 0;
		}
		
		int strCompareResult = this.getHash().compareTo(dataHash.getHash());
		if(strCompareResult == 0){
			return 0;
		}
		
		SpamSumSignature lhs = new SpamSumSignature(this.getHash());
		SpamSumSignature rhs = new SpamSumSignature(dataHash.getHash());
		
		int score = 0;
		try{
			score = DataHash.getInMemorySsdeep().Compare(lhs, rhs);
		}
		catch(ArrayIndexOutOfBoundsException ex){
			System.err.println("Compare exception ArrayIndexOutOfBoundsException");
			return strCompareResult;
		}
		if(score > DataHash.similarityTreshold){
			return 0;
		}
		else{
			return strCompareResult;
		}
	}
}






