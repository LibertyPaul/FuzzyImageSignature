package pkg1;

import java.util.List;


public class ImageFuzzyHashSum{
	private List<CharacterId> idList;
	private String hash;
	
	public ImageFuzzyHashSum(List<CharacterId> idList){
		this.idList = idList;
		this.hash = null;
	}
	
	@Override
	public final String toString(){
		int maxSize = 0;//кол-во десятичных разрядов для самого большого числа. (к остальным допишутся нули)
		for(final CharacterId id : this.idList){
			maxSize = Math.max(maxSize, id.toString().length());
		}

		String result = maxSize + ":";
		
		for(final CharacterId id : this.idList){
			String current = id.toString();
			while(current.length() < maxSize){
				current = 0 + current;
			}
			result += current;
		}
		
		return result;
	}
	/*
	public static ImageFuzzyHashSum fromString(String hash){
		
	}
	*/
}





