package pkg1;

public class MarginedCharacter{
	private final int position;//расстояние от начала строки
	private final CharacterId id;
	
	public MarginedCharacter(final int position, final CharacterId id) throws Exception{
		assert position >= 0;
		if(position == 0){
			this.position = 1;
		}
		else{
			this.position = position;
		}
		
		if(Integer.toString(this.position).length() > 3){
			throw new Exception("Too long spaceBefore");
		}
		
		this.id = id;
	}
	
	public MarginedCharacter(final MarginedCharacter character){
		this.position = character.position;
		this.id = character.id;
	}
	
	public CharacterId getId(){
		return this.id;
	}
	
	public double getPosition(){
		return (double)this.position / 1000;
	}
	
	/*
	public int getRelativePosition(){
		return Integer.toString(this.position).length();
	}
	*/
	public String toString(final int blockSize) throws Exception{
		assert blockSize > 0;
		
		String id_s = this.id.toString(blockSize);
		String spaceBefore_s = String.format("%0" + blockSize + "d", this.position);
		if(spaceBefore_s.length() != blockSize){
			throw new Exception("Incorrect spaceBefore_s length");
		}
		return id_s + spaceBefore_s;
	}
	
	public static MarginedCharacter fromString(String string) throws Exception{
		assert string.length() % 2 == 0;
		
		int blockSize = string.length() / 2;
		
		String id_s = string.substring(0, blockSize).replaceFirst("^0*(?!$)", "");
		String spaceBefore_s = string.substring(blockSize, blockSize * 2).replaceFirst("^0*(?!$)", "");
		
		CharacterId id = CharacterId.fromString(id_s);
		int spaceBefore = Integer.parseUnsignedInt(spaceBefore_s);
		
		return new MarginedCharacter(spaceBefore, id);
	}
	
}




