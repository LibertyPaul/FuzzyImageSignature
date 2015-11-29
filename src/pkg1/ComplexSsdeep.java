package pkg1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pkg1.DataHash;
import ssdeep.InMemorySsdeep;

public class ComplexSsdeep{
	protected List<DataHash> dataHashes;
	protected String complexHash;
	
	public ComplexSsdeep(){
		this.dataHashes = new ArrayList<>();
		this.complexHash = null;
	}
	
	public void add(byte[] data){
		DataHash newData = new DataHash(data);
		this.dataHashes.add(newData);
		this.complexHash = null;
	}
	
	private String calcComplexHash() throws IOException{
		//TODO: придумать алгоритм
		return "";
	}
	
	public String getComplexHash() throws IOException{
		if(this.complexHash == null)
			this.complexHash = this.calcComplexHash();
		
		return this.complexHash;
	}
	
}
