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
		assert relativeWidth >= 0.0;
		
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
		
		String characterCount_s = String.format("%0" + groupSize + "d", characters.size());
		assert characterCount_s.length() == groupSize;
		result += characterCount_s;
		
		final int width_i = (int)Math.round(this.relativeWidth * 1000); 
		String lineSize_s = String.format("%0" + groupSize + "d", width_i);
		assert lineSize_s.length() == groupSize;
		result += lineSize_s;
		
		for(final MarginedCharacter character : this.characters){
			result += character.toString(groupSize);
		}
		return result;
	}
	
	public static MarginedLine fromString(final int blockSize, final String line_s){
		/*
		 * формат:
		 * <relativeSize><character0><character1>...<characterN> 
		 */
		
		assert line_s.length() % blockSize == 0;
		final String lineWidth_s = line_s.substring(0, blockSize);
		final double lineWidth = (double)Integer.parseUnsignedInt(lineWidth_s) / 1000;
		
		String characters_s = line_s.substring(blockSize);
		final int characterLength = blockSize * 2;
		assert characters_s.length() % characterLength == 0;
		
		final int characterCount = characters_s.length() / characterLength;
		List<MarginedCharacter> characters = new ArrayList<>(characterCount);
		
		for(int i = 0; i < characterCount; ++i){
			final String current_s = characters_s.substring(i * characterLength, (i + 1) * characterLength);
			final MarginedCharacter current = MarginedCharacter.fromString(blockSize, current_s);
			characters.add(current);
		}
		
		return new MarginedLine(characters, lineWidth);
	}
	
	public boolean equals(final MarginedLine line){
		if(this.characters.size() != line.characters.size()){
			return false;
		}
		
		for(int i = 0; i < this.characters.size(); ++i){
			if(this.characters.get(i).equals(line.getCharacters().get(i)) == false){
				return false;
			}
		}
		
		return true;
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






