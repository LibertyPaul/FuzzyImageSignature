package pkg1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EuclidianDistance{
	/*
	public static double calc(double[] vec1, double[] vec2){
		assert vec1.length == vec2.length;
		
		double distanceMaxPossibleValue = vec1.length * Math.pow(vec1.length, 0.5);
		
		double result = 0;
		for(int i = 0; i < vec1.length; ++i){
			double sub = vec1[i] - vec2[i];
			double squareSub = Math.pow(sub, 2);
			result += squareSub;
		}
		result = Math.pow(result, 0.5);
		return result / distanceMaxPossibleValue;
	}
	*/
	
	private static List<Double> arrayToList(double[] vec){
		List<Double> result = new ArrayList<>(vec.length);
		for(final double value : vec){
			result.add(value);
		}
		return result;
	}

	public static double calc(double[] vec1, double[] vec2){
		List<Double> list1 = EuclidianDistance.arrayToList(vec1);
		List<Double> list2 = EuclidianDistance.arrayToList(vec2);
		
		return EuclidianDistance.calc(list1, list2);
	}
	
	
	public static <T extends Number> double calc(List<T> vec1, List<T> vec2){
		assert vec1.size() == vec2.size();
		
		double result = 0;
		for(int i = 0; i < vec1.size(); ++i){
			try{
				double sub = vec1.get(i).doubleValue() - vec2.get(i).doubleValue();
				double squareSub = Math.pow(sub, 2);
				result += squareSub;
			}catch(IndexOutOfBoundsException ex){
				System.err.println("here!");
			}
		}
		result = Math.pow(result, 0.5) / Math.pow(vec1.size(), 0.5);
		return result;
	}
}
