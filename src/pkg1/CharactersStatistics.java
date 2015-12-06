package pkg1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CharactersStatistics{
	protected List<SimilarCharacterGroup> characterGroups;
	
	public CharactersStatistics(){
		this.characterGroups = new ArrayList<>();
	}
	
	protected int getCount(){
		int result = 0;
		for(SimilarCharacterGroup characterFrequency : this.characterGroups){
			result += characterFrequency.getCount();
		}
		return result;
	}
	
	public void add(CharacterMatrix characterMatrix){
		final double similarityBound = 0.13;
	
		int maxSimilarityPos = 0;
		double minDifference = similarityBound + 42;//заведомо несовпадающее значение
		for(int i = 0; i < this.characterGroups.size(); ++i){
			//double currentDifference = this.characterGroups.get(i).fullComparsion(characterMatrix);
			double currentDifference = this.characterGroups.get(i).multipleRandomComparsion(characterMatrix, 5);
			if(currentDifference < minDifference){
				minDifference = currentDifference;
				maxSimilarityPos = i;
			}
		}
		
		if(minDifference < similarityBound){
			//если похожи, то добавляем в корзину
			this.characterGroups.get(maxSimilarityPos).addCharacter(characterMatrix);
		}
		else{
			//иначе создаем новую корзину
			SimilarCharacterGroup similarGroup = new SimilarCharacterGroup(characterMatrix);
			this.characterGroups.add(similarGroup);
		}
	}
	
	public List<SimilarCharacterGroup> getFrequency(){
		return new ArrayList<SimilarCharacterGroup>(this.characterGroups);
	}
	
	@Override
	public String toString(){
		long count = this.getCount();
		List<SimilarCharacterGroup> sorted = new ArrayList<>(this.characterGroups);
		Collections.sort(sorted, Collections.reverseOrder());

		String result = "" + count + " " + sorted.size() + " ";
		for(SimilarCharacterGroup characterGroup : sorted){
			result += characterGroup.getCount() + " ";
		}
		
		return result;
	}
	
	public void dump(File path) throws IOException{
		for(int i = 0; i < this.characterGroups.size(); ++i){
			this.characterGroups.get(i).dumpCharacters(new File(path.getAbsolutePath() + "/" + i + "."));
		}
	}
	
	protected List<Integer> getFrequencies(){
		List<Integer> res = new ArrayList<>(this.characterGroups.size());
		final int count = this.getCount();
		for(final SimilarCharacterGroup characterGroup : this.characterGroups){
			res.add(characterGroup.getCount());
		}
		return res;
	}
}




