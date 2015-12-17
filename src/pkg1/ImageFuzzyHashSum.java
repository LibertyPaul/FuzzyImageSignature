package pkg1;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;


public class ImageFuzzyHashSum{
	private List<MarginedLine> marginedLines;
	private static final int groupSizeLength = 3;
	
	public ImageFuzzyHashSum(List<MarginedLine> marginedLines){
		this.marginedLines = marginedLines;
	}
	
	@Override
	public String toString(){
		/*
		 * Формат строки : X[[GM]*SP]+
		 * X - размер группы. X.length = groupSizeLength
		 * G - значение id. Если G.length < X, то добавляются нули слева. G.length <= X
		 * M - относительнае позиция в строке
		 * S - сепаратор. S = 0 * X раз
		 * P - размер строки * 1000 / размер страницы
		 * 
		 * P.S. тут должны быть только десятичные числа,
		 * т.к. QR код оптимизирует их хранение, в сравнении с обычными символами
		 *  
		 */
		
		int groupSize = 0;//кол-во десятичных разрядов для самого большого числа. (к остальным допишутся нули)
		for(final MarginedLine line : marginedLines){
			for(final MarginedCharacter id : line.getCharacters()){
				groupSize = Math.max(groupSize, id.getId_s().length());
				groupSize = Math.max(groupSize, id.getPosition_s().length());
			}
		}
		assert Integer.toString(groupSize).length() <= ImageFuzzyHashSum.groupSizeLength;
		
		String result = String.format("%0" + ImageFuzzyHashSum.groupSizeLength + "d", groupSize);

		for(final MarginedLine line : marginedLines){
			result += line.toString(groupSize);
		}
		
		return result;
	}
	
	public static ImageFuzzyHashSum fromString(String simpleString) throws Exception{
		String groupSize_s = simpleString.substring(0, ImageFuzzyHashSum.groupSizeLength);
		int groupSize = Integer.parseUnsignedInt(groupSize_s);
		if((simpleString.length() - ImageFuzzyHashSum.groupSizeLength) % groupSize != 0){
			throw new Exception("Incorrect hash string");
		}
		int blockSize = groupSize * 2;
		
		List<MarginedLine> marginedLines = new ArrayList<>();
		List<MarginedCharacter> marginedCharacters = new ArrayList<>();
		
		//group - число записанное фиксированным количеством знаков
		//block - несколько групп подряд
		
		int groupCount = (simpleString.length() - ImageFuzzyHashSum.groupSizeLength) / groupSize;
		if(groupCount % 2 != 0){
			throw new Exception("Incorrect hash string");
		}
		int blockCount = groupCount / 2;
		
		for(int i = 0; i < blockCount; ++i){
			int startIndex = ImageFuzzyHashSum.groupSizeLength + i * blockSize;
			int endIndex = startIndex + blockSize;
			String firstGroup_s = simpleString.substring(startIndex, startIndex + groupSize);
			String secondGroup_s = simpleString.substring(startIndex + groupSize, endIndex);
			
			if(firstGroup_s.replaceFirst("^0*(?!$)", "").length() == 0){
				int position = Integer.parseUnsignedInt(secondGroup_s);
				marginedLines.add(new MarginedLine(marginedCharacters, position));
				marginedCharacters = new ArrayList<>();
			}
			else{
				marginedCharacters.add(MarginedCharacter.fromString(firstGroup_s + secondGroup_s));
			}
		}
		
		return new ImageFuzzyHashSum(marginedLines);
	}
	
	private static Map<MarginedCharacter, List<MarginedCharacter>> mergeMaps(Map<MarginedCharacter, List<MarginedCharacter>> map1, Map<MarginedCharacter, List<MarginedCharacter>> map2){
		Map<MarginedCharacter, List<MarginedCharacter>> result = new TreeMap<>(map1);
		
		for(final Entry<MarginedCharacter, List<MarginedCharacter>> entry : map2.entrySet()){
			List<MarginedCharacter> current = result.get(entry.getKey());
			if(current == null){
				result.put(entry.getKey(), entry.getValue());
			}
			else{
				List<MarginedCharacter> both = new ArrayList<>();
				both.addAll(current);
				both.addAll(entry.getValue());
				result.put(entry.getKey(), both);
			}
		}
		
		return result;
	}
	
	private static Map<MarginedCharacter, MarginedCharacter> getCharMatch(final Map<MarginedCharacter, List<MarginedCharacter>> charMap){
		Set<Entry<MarginedCharacter, List<MarginedCharacter>>> mapEntrySet = charMap.entrySet();
		Map<MarginedCharacter, MarginedCharacter> result = new TreeMap<>();
		
		for(final Entry<MarginedCharacter, List<MarginedCharacter>> entry : mapEntrySet){
			MarginedCharacter id = entry.getKey();
			List<MarginedCharacter> candidates = entry.getValue();
			
			TreeMap<MarginedCharacter, Integer> candidatesMap = new TreeMap<>();
			for(final MarginedCharacter candidate : candidates){
				if(candidatesMap.containsKey(candidate)){
					Integer currentCount = candidatesMap.get(candidate);
					candidatesMap.put(candidate, currentCount + 1);
				}
				else{
					candidatesMap.put(candidate, 1);
				}
			}
			
			MarginedCharacter mostFrequent = null;
			Integer maxScore = 0;
			for(final Entry<MarginedCharacter, Integer> candidateScore : candidatesMap.entrySet()){
				Integer score = candidateScore.getValue();
				if(score > maxScore){
					maxScore = score;
					mostFrequent = candidateScore.getKey();
				}
			}
			
			result.put(id, mostFrequent);
		}
		
		return result;
	}
	
	public HashCompareResult compare(ImageFuzzyHashSum o) throws Exception{
		List<MarginedLine> list1 = this.marginedLines;
		List<MarginedLine> list2 = o.marginedLines;
		
		final int lineSizeDifferenceBound = 15;//на какое кол-во символов могут отличаться строки
		for(int i1 = 0, i2 = 0; i1 < list1.size() - 1 && i2 < list2.size() - 1; ++i1, ++i2){
			final int line1Size = list1.get(i1).getCharacters().size();
			final int line2Size = list2.get(i2).getCharacters().size();
			if(Math.abs(line1Size - line2Size) > lineSizeDifferenceBound){
				//строки слишком разного размера
				//пробуем сначала сдвинуть первую а потом вторую, оцениваем результаты и применяем наиболее удачный вариант
				
				final int line12Size = list1.get(i1 + 1).getCharacters().size();
				final int topShiftDifference = Math.abs(line2Size - line12Size);
				
				final int line22Size = list1.get(i2 + 1).getCharacters().size();
				final int bottomShiftDifference = Math.abs(line1Size - line22Size);
				
				if(topShiftDifference > bottomShiftDifference){
					list2.remove(i2);
					System.out.printf("Line %d was removed from line2\n", i2);
				}
				else{
					list1.remove(i1);
					System.out.printf("Line %d was removed from line1\n", i1);
				}
				
				--i1;
				--i2;
			}
		}
		//если все-таки не получилось выравнять
		//берем и тупо отрезаем лишние строки
		while(list1.size() > list2.size()){
			list1.remove(list1.size() - 1);
		}

		while(list2.size() > list1.size()){
			list2.remove(list2.size() - 1);
		}

		Map<MarginedCharacter, List<MarginedCharacter>> idMatching = new TreeMap<>();
		for(int i = 0; i < list1.size(); ++i){
			MarginedLine line1 = list1.get(i);
			MarginedLine line2 = list2.get(i);
			
			idMatching = ImageFuzzyHashSum.mergeMaps(idMatching, line1.matchLines(line2));
		}
		
		
		Map<MarginedCharacter, MarginedCharacter> matches = ImageFuzzyHashSum.getCharMatch(idMatching);
		
		int errors = 0;
		int currentStreak = 0;
		int longestStreak = 0;
		
		List<MarginedCharacter> incorrect = new ArrayList<>();
		
		for(int i = 0; i < list1.size(); ++i){
			List<CharMatch> match = list1.get(i).matchByPosition(list2.get(i));
			for(final CharMatch charMatch : match){
				MarginedCharacter key = charMatch.getKey();
				assert matches.containsKey(key);
				
				MarginedCharacter trueValue = matches.get(key);
				MarginedCharacter checkValue = charMatch.getValue();
				
				if(trueValue.equals(checkValue)){
					if(currentStreak > longestStreak){
						longestStreak = currentStreak;
						currentStreak = 0;
					}
				}
				else{
					++currentStreak;
					++errors;
					incorrect.add(checkValue);
				}
			}
		}
		
		HashCompareResult result = new HashCompareResult(errors, longestStreak, incorrect);
		return result;
	}
	
	public void verbose(){
		for(final MarginedLine line : this.marginedLines){
			System.out.print(line.getCharacters().size() + " ");
		}
		System.out.println();
	}
	
	public static void test(){
		String hash = "000212310102030410001200131405567600121212";
		ImageFuzzyHashSum sum = null;
		try{
			sum = ImageFuzzyHashSum.fromString(hash);
		}
		catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assert hash.equals(sum.toString());
	}
	
}





