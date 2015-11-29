package pkg1;

import java.io.IOException;

import ssdeep.InMemorySsdeep;

public class DataHash{
	private byte[] data;
	private String hash;
	
	public DataHash(byte[] data) throws NullPointerException{
		if(data == null)
			throw new NullPointerException("data pointer should not be null");
		this.data = data;
	}
	
	public DataHash(DataHash dataHash){
		this.data = dataHash.data;
		this.hash = dataHash.hash;
	}
	
	public byte[] getData(){
		return this.data.clone();
	}
	
	private String calcHash() throws IOException{
		InMemorySsdeep hasher = new InMemorySsdeep();
		String hash = hasher.fuzzy_hash_array(this.data);
		return hash;
	}
	
	public String getHash() throws IOException{
		if(this.hash == null)
			this.hash = this.calcHash();
		
		return this.hash;
	}
}
