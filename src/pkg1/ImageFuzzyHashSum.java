package pkg1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;


public class ImageFuzzyHashSum{
	private List<MarginedLine> marginedLines;
	private static final int groupSizeLength = 4;
	
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
				groupSize = Math.max(groupSize, id.getId().getIdLength());
				groupSize = Math.max(groupSize, id.getSpaceLength());
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
	
	private static boolean isHole(int space1, int space2){
		final int factor = 5;
		return (
			(double)space1 / space2 > factor ||
			(double)space2 / space1 > factor
		);
	}
	
	private static Map<CharacterId, List<CharacterId>> mergeMaps(Map<CharacterId, List<CharacterId>> map1, Map<CharacterId, List<CharacterId>> map2){
		Map<CharacterId, List<CharacterId>> result = new TreeMap<>(map1);
		
		for(final Entry<CharacterId, List<CharacterId>> entry : map2.entrySet()){
			List<CharacterId> current = result.get(entry.getKey());
			if(current == null){
				result.put(entry.getKey(), entry.getValue());
			}
			else{
				List<CharacterId> both = new ArrayList<>();
				both.addAll(current);
				both.addAll(entry.getValue());
				result.put(entry.getKey(), both);
			}
		}
		
		return result;
	}
	
	private static List<CharMatch> getCharMatch(final Map<CharacterId, List<CharacterId>> charMap){
		Set<Entry<CharacterId, List<CharacterId>>> mapEntrySet = charMap.entrySet();
		List<CharMatch> result = new ArrayList<>(mapEntrySet.size());
		
		for(final Entry<CharacterId, List<CharacterId>> entry : mapEntrySet){
			CharacterId id = entry.getKey();
			List<CharacterId> candidates = entry.getValue();
			
			TreeMap<CharacterId, Integer> candidatesMap = new TreeMap<>();
			for(final CharacterId candidate : candidates){
				if(candidatesMap.containsKey(candidate)){
					Integer currentCount = candidatesMap.get(candidate);
					candidatesMap.put(candidate, currentCount + 1);
				}
				else{
					candidatesMap.put(candidate, 1);
				}
			}
			
			CharacterId mostFrequent = null;
			Integer maxScore = 0;
			for(final Entry<CharacterId, Integer> candidateScore : candidatesMap.entrySet()){
				Integer score = candidateScore.getValue();
				if(score > maxScore){
					maxScore = score;
					mostFrequent = candidateScore.getKey();
				}
			}
			
			CharMatch match = new CharMatch(id, mostFrequent);
			result.add(match);
		}
		
		return result;
	}
	
	public double compare(ImageFuzzyHashSum o) throws Exception{
		List<MarginedLine> list1 = this.marginedLines;
		List<MarginedLine> list2 = o.marginedLines;
		
		if(list1.size() != list2.size()){
			throw new Exception("list1.size() != list2.size()");
		}

		Map<CharacterId, List<CharacterId>> idMatching = new TreeMap<>();
		for(int line = 0; line < list1.size(); ++line){
			MarginedLine line1 = list1.get(line);
			MarginedLine line2 = list2.get(line);
			
			idMatching = ImageFuzzyHashSum.mergeMaps(idMatching, line1.matchLines(line2));
		}
		
		
		List<CharMatch> charMatch = ImageFuzzyHashSum.getCharMatch(idMatching);
		
		return 0; 
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





