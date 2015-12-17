package pkg1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MarginedLine{
	private final List<MarginedCharacter> characters;
	private final double relativeWidth;
	
	public MarginedLine(final List<MarginedCharacter> characters, final double relativeWidth){
		assert characters != null;
		assert relativeWidth > 0;
		
		this.characters = characters;
		this.relativeWidth = relativeWidth;
	}
	
	public List<MarginedCharacter> getCharacters(){
		return this.characters;
	}
	
	public double getRelativeWidth(){
		return this.relativeWidth;
	}
	
	public String toString(final int groupSize){
		String result = "";
		for(final MarginedCharacter character : this.characters){
			try{
				result += character.toString(groupSize);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		result += String.format("%0" + groupSize + "d", 0) + String.format("%0" + groupSize + "d", (int)this.getRelativeWidth());
		return result;
	}
	
	private MarginedCharacter getClosest(final double position){
		int minPos = 0;
		double minDistance = Math.abs(this.characters.get(0).getPosition() - position);
		for(int i = 1; i < this.characters.size(); ++i){
			final double currentDistance = Math.abs(this.characters.get(i).getPosition() - position);
			if(currentDistance < minDistance){
				minDistance = currentDistance;
				minPos = i;
			}
		}
		
		return this.characters.get(minPos);
	}
	
	public List<CharMatch> matchByPosition(final MarginedLine line){
		List<CharMatch> result = new ArrayList<>(this.characters.size());
		for(final MarginedCharacter ch1 : line.characters){
			MarginedCharacter ch2 = this.getClosest(ch1.getPosition());
			CharMatch currentMatch = new CharMatch(ch1, ch2);
			result.add(currentMatch);
		}
		return result;
	}
	
	public Map<MarginedCharacter, List<MarginedCharacter>> matchLines(final MarginedLine o){
		Map<MarginedCharacter, List<MarginedCharacter>> result = new TreeMap<>();
		
		List<CharMatch> matches = this.matchByPosition(o);
		for(final CharMatch match : matches){
			if(result.containsKey(match.getKey())){
				List<MarginedCharacter> value = result.get(match.getKey());
				value.add(match.getValue());
			}
			else{
				List<MarginedCharacter> value = new ArrayList<>(1);
				value.add(match.getValue());
				result.put(match.getKey(), value);
			}
		}
		
		return result;
	}
	
}






