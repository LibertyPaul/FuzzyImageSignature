package pkg1;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import pkg1.DataHash;
import ssdeep.InMemorySsdeep;
import ssdeep.SpamSumSignature;
import ssdeep.ssdeep;

public class ComplexSsdeep{
	protected String complexHash;
	protected List<DataHash> dataHashes;
	
	public ComplexSsdeep(){
		this.complexHash = null;
		this.dataHashes = new ArrayList<>();
	}
	
	public void add(DataHash dataHash){
		if(dataHash == null){
			throw new NullPointerException("dataHash should not be null");
		}
		this.complexHash = null;
		this.dataHashes.add(dataHash.clone());
		
	}
	
	public void add(byte[] data){
		DataHash dataHash = new DataHash(data);
		this.add(dataHash);
	}
	
	public void addAll(List<DataHash> dataHashes){
		for(DataHash dataHash : dataHashes){
			this.add(dataHash);
		}
	}
	
	protected Map<DataHash, Integer> getCharacterMap(){
		Map<DataHash, Integer> characterMap = new TreeMap<>();
		
		for(DataHash dataHash : this.dataHashes){
			if(characterMap.containsKey(dataHash)){
				Integer count = characterMap.get(dataHash);
				characterMap.put(dataHash, count + 1);
			}
			else{
				characterMap.put(dataHash, 1);
			}
		}
		
		return characterMap;
	}
	
	/*
	private List<List<Integer>> getCharRelationsMatrix() throws IOException{
		int count = this.dataHashes.size();
		List<List<Integer>> matrix = new ArrayList<>(count);
		for(int row = 0; row < count; ++row){
			matrix.add(new ArrayList<Integer>(count));
			for(int col = 0; col < count; ++col){
				matrix.get(row).add(0);
			}
		}
		
		ssdeep hasher = new ssdeep();
		for(int first = 0; first < count - 1; ++first){
			matrix.get(first).set(first, 100);//хеш равен самому себе
			for(int second = first + 1; second < count; ++second){
				SpamSumSignature ss1 = new SpamSumSignature(this.dataHashes.get(first).getHash());
				SpamSumSignature ss2 = new SpamSumSignature(this.dataHashes.get(second).getHash());
				int score = hasher.Compare(ss1, ss2);
				matrix.get(first).set(second, score);
			}
		}
		return matrix;
	}
	*/
	private String calcComplexHash() throws IOException{
		/*
		List<List<Integer>> charRelationsMatrix = this.getCharRelationsMatrix();
		PrintWriter writer = new PrintWriter("matrix.txt", "UTF-8");
		for(List<Integer> row : charRelationsMatrix){
			for(Integer value : row){
				writer.printf("%1$4s", value);
			}
			writer.println();
			writer.println();
		}
		writer.close();
		*/
		//TODO: придумать алгоритм
		return "";
	}
	
	public String getComplexHash() throws IOException{
		if(this.complexHash == null)
			this.complexHash = this.calcComplexHash();
		
		return this.complexHash;
	}
	
	public void test(){
		Map<DataHash, Integer> characterMap = this.getCharacterMap();
		System.out.println("############ Next map: ############");
		for(Entry<DataHash, Integer> entry : characterMap.entrySet()){
			System.out.printf("%d ", entry.getValue());
		}
		System.out.println();
	}
	
}
