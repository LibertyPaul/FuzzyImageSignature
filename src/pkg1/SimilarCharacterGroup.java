package pkg1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimilarCharacterGroup implements Comparable<SimilarCharacterGroup>{
	protected List<CharacterMatrix> characterList;
	private static Random rg = null;
	
	private SimilarCharacterGroup(){
		this.characterList = new ArrayList<>();
	}
	
	public SimilarCharacterGroup(CharacterMatrix characterMatrix){
		this();
		this.characterList.add(characterMatrix);
	}
	
	protected static Random getRandomGenerator(){
		if(SimilarCharacterGroup.rg == null){
			SimilarCharacterGroup.rg = new Random();
		}
		return SimilarCharacterGroup.rg;
	}
	
	protected CharacterMatrix getRandomCharacter(){
		int index = SimilarCharacterGroup.getRandomGenerator().nextInt(this.characterList.size());
		return characterList.get(index);
	}
	
	public double fullComparsion(CharacterMatrix characterMatrix){
		double min = Double.POSITIVE_INFINITY;
		for(CharacterMatrix cm : this.characterList){
			double current = cm.compare(characterMatrix);
			if(min >= current){
				min = current;
			}
		}
		return min;
	}
	
	public double randomComparsion(CharacterMatrix characterMatrix){
		CharacterMatrix random = this.getRandomCharacter();
		return random.compare(characterMatrix);
	}
	
	public double multipleRandomComparsion(CharacterMatrix characterMatrix, int count){
		assert count >= 0;
		if(this.characterList.size() <= count){
			return this.fullComparsion(characterMatrix);
		}
		
		double min = Double.POSITIVE_INFINITY;
		for(int i = 0; i < count; ++i){
			double current = this.randomComparsion(characterMatrix);
			if(min >= current){
				min = current;
			}
		}
		return min;
	}
	
	public void addCharacter(CharacterMatrix characterMatrix){
		this.characterList.add(characterMatrix);
	}
	
	public int getCount(){
		return this.characterList.size();
	}

	@Override
	public int compareTo(SimilarCharacterGroup o){
		return this.characterList.size() - o.characterList.size();
	}
	
	public void dumpCharacters(File path) throws IOException{
		for(int i = 0; i < this.characterList.size(); ++i){
			this.characterList.get(i).dump(new File(path.getAbsolutePath() + "/" + i + ".bmp"));
		}
		
	}
}
