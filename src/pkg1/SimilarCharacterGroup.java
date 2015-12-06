package pkg1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimilarCharacterGroup implements Comparable<SimilarCharacterGroup>{
	protected List<DetectedCharacter> characterList;
	
	private SimilarCharacterGroup(){
		this.characterList = new ArrayList<>();
	}
	
	public SimilarCharacterGroup(DetectedCharacter character){
		this();
		this.characterList.add(character);
	}
	
	protected static double comapreWith(final DetectedCharacter character, final List<DetectedCharacter> characters){
		List<Double> values = new ArrayList<>();
		for(final DetectedCharacter currentCharacter : characters){
			final double current = currentCharacter.getMatrix().compare(character.getMatrix());
			values.add(current);
		}
		return EuclidianDistance.calc(values);
	}
	
	public double fullComparsion(DetectedCharacter characterMatrix){		
		return SimilarCharacterGroup.comapreWith(characterMatrix, this.characterList);
	}
	
	public double multipleRandomComparsion(DetectedCharacter character, int count){
		assert count >= 0;
		if(this.characterList.size() <= count){
			return this.fullComparsion(character);
		}
		
		Collections.shuffle(this.characterList);
		List<DetectedCharacter> subList = this.characterList.subList(0, count);
		
		return SimilarCharacterGroup.comapreWith(character, subList);
	}
	
	public void addCharacter(DetectedCharacter character){
		this.characterList.add(character);
	}
	
	public int getCount(){
		return this.characterList.size();
	}

	public List<DetectedCharacter> getCharacters(){
		return this.characterList;
	}
	
	
	
	
	@Override
	public int compareTo(SimilarCharacterGroup o){
		return this.characterList.size() - o.characterList.size();
	}
	
	public void dumpCharacter(File path){
		assert this.characterList.size() > 0;
		
		final DetectedCharacter cm = this.characterList.get(0);
		int currentId = System.identityHashCode(cm);
		cm.getMatrix().dump(new File(path.getAbsolutePath() + "." + currentId + ".bmp"));
	}
	
	public void dumpCharacters(File path){
		for(final DetectedCharacter cm : this.characterList){
			int currentId = System.identityHashCode(cm);
			cm.getMatrix().dump(new File(path.getAbsolutePath() + "." + currentId + ".bmp"));
		}
	}
}
