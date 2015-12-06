package pkg1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class ImageFyzzyHashSum{
	protected List<Integer> repeats;//количество повторов каждого уникального символа
	
	public ImageFyzzyHashSum(List<Integer> repeats){
		this.repeats = new ArrayList<Integer>(repeats);
		Collections.sort(this.repeats, Collections.reverseOrder());
	}
	
	protected int getUniqueCount(){
		return this.repeats.size();
	}
	
	protected long getSum(){
		long sum = 0;
		for(final Integer value : this.repeats){
			sum += value;
		}
		return sum;
	}
	
	protected List<Double> getFrequences(){
		long sum = this.getSum();
		List<Double> result = new ArrayList<>(this.getUniqueCount());
		for(final Integer value : this.repeats){
			double frequency = (double)value / sum;
			result.add(frequency);
		}
		return result;
	}

	public double calcDifference(ImageFyzzyHashSum o){
		//returns double [0.0, 1.0]
		List<Double> vec1 = this.getFrequences();
		List<Double> vec2 = o.getFrequences();
		
		//2 цикла: один не сработает, другой добавит в конец меньшего массива нули
		for(int i = vec2.size(); i < vec1.size(); ++i){
			vec2.add(0.0);
		}
		for(int i = vec1.size(); i < vec2.size(); ++i){
			vec1.add(0.0);
		}
		
		final double minValue = 0;
		final double maxValue = 1;
		final double rangeSize = maxValue - minValue;
		
		return EuclidianDistance.calc(vec1, vec2) / rangeSize;
	}
	
	//TODO: compress hash
	@Override
	public String toString(){
		//формат строки: [c1 c2 c3 ... cN]
		String result = "";
		String delimiter = " "; 
		
		for(final Integer value : this.repeats){
			result += value + delimiter;
		}
		
		return result;
	}
	
	public static ImageFyzzyHashSum fromString(String str){
		String[] tokens = str.split(" ");
		List<Integer> repeats = new ArrayList<>(tokens.length);
		
		for(final String token : tokens){
			Integer current = null;
			try{
				current = Integer.parseUnsignedInt(token);
			}
			catch(NumberFormatException ex){
				System.err.println("Integer.parseUnsignedInt error");
			}
			repeats.add(current);
		}
		
		return new ImageFyzzyHashSum(repeats);
	}
}





