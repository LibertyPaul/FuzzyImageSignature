package hash;


public class CharMatch{
	private final MarginedCharacter key;
	private final MarginedCharacter value;
	
	public CharMatch(final MarginedCharacter key, final MarginedCharacter value){
		this.key = key;
		this.value = value;
	}
	
	public MarginedCharacter getKey(){
		return this.key;
	}
	
	public MarginedCharacter getValue(){
		return this.value;
	}
}
