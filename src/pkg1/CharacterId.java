package pkg1;

public final class CharacterId{
	private static int next_id;
	private final int id;
	
	private CharacterId(int id){
		this.id = id;
	}

	public CharacterId(){
		this(next_id++);
	}
	
	public CharacterId(CharacterId characterId){
		this(characterId.id);
	}
	
	public CharacterId clone(){
		return new CharacterId(this.id);
	}
	
	public boolean equals(final CharacterId o){
		return this.id == o.id;
	}
	
	public String toString(){
		return Integer.toString(this.id);
	}
}
