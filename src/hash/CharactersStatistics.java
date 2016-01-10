package hash;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

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
	
	public void add(DetectedCharacter characterMatrix){
		if(this.characterGroups.size() == 0){
			SimilarCharacterGroup similarGroup = new SimilarCharacterGroup(characterMatrix);
			this.characterGroups.add(similarGroup);
		}
		else{
			final double similarityBound = 0.224;
		
			int maxSimilarityPos = 0;
			double minScore = this.characterGroups.get(0).fullComparsion(characterMatrix);
			for(int i = 1; i < this.characterGroups.size(); ++i){
				double currentScore = this.characterGroups.get(i).fullComparsion(characterMatrix);
				
				if(currentScore < minScore){
					minScore = currentScore;
					maxSimilarityPos = i;
				}
			}
			
			if(minScore < similarityBound){
				//если похожи, то добавляем в корзину
				this.characterGroups.get(maxSimilarityPos).addCharacter(characterMatrix);
			}
			else{
				//иначе создаем новую корзину
				SimilarCharacterGroup similarGroup = new SimilarCharacterGroup(characterMatrix);
				this.characterGroups.add(similarGroup);
			}
		}
	}
	
	public List<SimilarCharacterGroup> getFrequency(){
		return new ArrayList<SimilarCharacterGroup>(this.characterGroups);
	}
	
	protected List<Integer> getFrequencies(){
		List<Integer> res = new ArrayList<>(this.characterGroups.size());
		for(final SimilarCharacterGroup characterGroup : this.characterGroups){
			res.add(characterGroup.getCount());
		}
		return res;
	}
	
	public LinePage createLinePage(){
		//TODO: перед вызовом этой функции нужно сливать группы одинаковых символов
		LinePage linePage = new LinePage();
		for(final SimilarCharacterGroup scg : this.characterGroups){
			final CharacterId groupId = new CharacterId();
			for(final DetectedCharacter character : scg.getCharacters()){
				NumberedCharacter current = new NumberedCharacter(character.getRect(), groupId);
				linePage.addCharacter(current);
			}
		}
		return linePage;
	}
	

	
	
	
	@Override
	public String toString(){
		long count = this.getCount();
		List<SimilarCharacterGroup> sorted = new ArrayList<>(this.characterGroups);
		Collections.sort(sorted, Collections.reverseOrder());

		String result = "" + count + " " + sorted.size() + "\n";
		for(SimilarCharacterGroup characterGroup : sorted){
			result += (double)characterGroup.getCount() / count + "\n";
		}
		
		return result;
	}
	
	public void dump(File path){
		for(final SimilarCharacterGroup scg : this.characterGroups){
			int id = System.identityHashCode(scg);
			scg.dumpCharacters(new File(path.getAbsolutePath() + "/" + id + "."));
		}
	}
	
	public void dumpUnique(File path){
		for(final SimilarCharacterGroup scg : this.characterGroups){
			int id = System.identityHashCode(scg);
			scg.dumpCharacter(new File(path.getAbsolutePath() + "/" + id + "."));
		}
	}
	
	public Mat dumpGroupsToImage(final Mat srcImage){
		final Mat srcCopy = srcImage.clone();
		final Random rg = new Random();
		for(final SimilarCharacterGroup group : this.characterGroups){
			final Scalar groupColor = new Scalar(rg.nextInt(256), rg.nextInt(256), rg.nextInt(256));
			group.dumpToImage(srcCopy, groupColor);
		}
		return srcCopy;
	}
}




