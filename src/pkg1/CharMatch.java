package pkg1;

public class CharMatch{
	private final CharacterId key;
	private final CharacterId value;
	
	public CharMatch(final CharacterId key, final CharacterId value){
		this.key = key;
		this.value = value;
	}
	
	public CharacterId getKey(){
		return this.key;
	}
	
	public CharacterId getValue(){
		return this.value;
	}
}
