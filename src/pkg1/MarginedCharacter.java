package pkg1;

import org.opencv.core.Rect;

public class MarginedCharacter implements Comparable<MarginedCharacter>{
	private final static int strBlockPrecision = 1000;//по 3 цифры на позицию и на id
	private final double position;//расстояние от начала строки
	private final CharacterId id;
	private final Rect rect;
	
	public MarginedCharacter(final double position, final CharacterId id, final Rect rect) throws Exception{
		assert position >= 0;
		
		this.position = position;
		this.id = id;
		this.rect = rect;
	}
	
	public MarginedCharacter(final double position, final CharacterId id) throws Exception{
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
		return (int)Math.round(this.position * strBlockPrecision);
	}
	
	public String getPosition_s(){
		return Integer.toString(this.getIntegerPosition());
	}
	
	public String getId_s(){
		return this.id.toString();
	}

	public String toString(final int blockSize) throws Exception{
		assert blockSize > 0;
		
		
		String id_s = this.id.toString(blockSize);

		int intPos = this.getIntegerPosition();
		if(intPos == 0){
			intPos = 1;//нельзя вставлять нулевое значение
		}
		String position_s = String.format("%0" + blockSize + "d", intPos);
		if(position_s.length() != blockSize){
			throw new Exception("Incorrect spaceBefore_s length");
		}
		return id_s + position_s;
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
	
	public boolean equals(final MarginedCharacter o){
		return this.id.equals(o.id);
	}
	
	public int compareTo(final MarginedCharacter o){
		return this.id.compareTo(o.id);
	}
	
}




