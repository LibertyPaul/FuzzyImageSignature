package hash;

import org.opencv.core.Rect;

public class MarginedCharacter implements Comparable<MarginedCharacter>{
	private final static int strBlockPrecision = 1000;//по 3 цифры на позицию и на id
	private final double position;//расстояние от начала строки
	private final CharacterId id;
	private final Rect rect;
	
	public MarginedCharacter(final double position, final CharacterId id, final Rect rect){
		assert position >= 0 && position <= 1.0;
		
		this.position = position;
		this.id = id;
		this.rect = rect;
	}
	
	public MarginedCharacter(final double position, final CharacterId id){
		this(position, id, null);
	}
	
	public MarginedCharacter(final MarginedCharacter character){
		this.position = character.position;
		this.id = character.id;
		this.rect = character.rect;
	}
	
	public CharacterId getId(){
		return this.id;
	}
	
	public double getPosition(){
		return this.position;
	}
	
	public Rect getRect(){
		return this.rect;
	}
	
	private int getIntegerPosition(){
		return (int)Math.round(this.position * MarginedCharacter.strBlockPrecision);
	}
	
	public String getPosition_s(){
		return Integer.toString(this.getIntegerPosition());
	}
	
	public String getId_s(){
		return this.id.toString();
	}

	public String toString(final int blockSize){
		assert blockSize > 0;
		
		String id_s = this.id.toString(blockSize);
		String position_s = String.format("%0" + blockSize + "d", this.getIntegerPosition());
		assert position_s.length() == blockSize;

		return id_s + position_s;
	}
	
	public static MarginedCharacter fromString(final int blockSize, final String character_s){
		assert character_s.length()  == blockSize * 2;
		
		String id_s = character_s.substring(0, blockSize);
		CharacterId id = CharacterId.fromString(id_s);
		
		String spaceBefore_s = character_s.substring(blockSize);
		int spaceBefore_i = Integer.parseUnsignedInt(spaceBefore_s);
		double spaceBefore = (double)spaceBefore_i / MarginedCharacter.strBlockPrecision;
		
		return new MarginedCharacter(spaceBefore, id);
	}
	
	public boolean equals(final MarginedCharacter o){
		return this.id.equals(o.id);
	}
	
	public int compareTo(final MarginedCharacter o){
		return this.id.compareTo(o.id);
	}
	
}




