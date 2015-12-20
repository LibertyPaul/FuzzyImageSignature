package pkg1;

public final class CharacterId implements Comparable<CharacterId>{
	private static int next_id = 100;//id != 0
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
	
	public int compareTo(CharacterId o){
		return this.id - o.id;
	}
	
	public int getIdLength(){
		return Integer.toString(this.id).length();
	}
	
	public String toString(){
		return Integer.toString(this.id);
	}
	
	public String toString(final int blockSize){
		String id_s = String.format("%0" + blockSize + "d", this.id);
		assert id_s.length() == blockSize;
		
		return id_s;
	}
	
	public static CharacterId fromString(final String string){
		String id_s = string.replaceFirst("^0*(?!$)", "");//remove leading zeroes
		int id = Integer.parseInt(id_s);
		return new CharacterId(id);
	}

}



