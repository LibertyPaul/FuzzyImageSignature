package pkg1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EuclidianDistance{	
	private static List<Double> arrayToList(final double[] vec){
		List<Double> result = new ArrayList<>(vec.length);
		for(final double value : vec){
			result.add(value);
		}
		return result;
	}

	public static double calc(final double[] vec1, final double[] vec2){
		List<Double> list1 = EuclidianDistance.arrayToList(vec1);
		List<Double> list2 = EuclidianDistance.arrayToList(vec2);
		
		return EuclidianDistance.calc(list1, list2);
	}
	
	protected static <T extends Number> List<Double> calcDiff(final List<T> vec1, final List<T> vec2){
		assert vec1.size() == vec2.size();
		List<Double> diff = new ArrayList<>(vec1.size());//разность векторов
		for(int i = 0; i < vec1.size(); ++i){
			Double currentDiff = Math.abs(vec1.get(i).doubleValue() - vec2.get(i).doubleValue());
			diff.add(currentDiff);
		}
		return diff;
	}
	
	public static <T extends Number> double calc(final List<T> vec1, final List<T> vec2){
		return EuclidianDistance.calc(EuclidianDistance.calcDiff(vec1, vec2));
	}
	
	protected static <T extends Number> double calc(List<T> vec){
		//считает дистанцию вектора diff и нулевого вектора
		double result = 0;
		for(final T value : vec){
			result += Math.pow(value.doubleValue(), 2);
		}
		result = Math.pow(result, 0.5) / Math.pow(vec.size(), 0.5);
		
		return result;
	}
	
	
	public static void test(){
		double[] arr1 = {0., 0.5, 10., 4., 23., 0., 1., 1., 3.};
		double[] arr2 = {17., 10., 0., 2., 2., 3., 44., 4., 7.};
		
		List<Double> list1 = Arrays.asList(0., 0.5, 10., 4., 23., 0., 1., 1., 3.);
		List<Double> list2 = Arrays.asList(17., 10., 0., 2., 2., 3., 44., 4., 7.);
		
		assert EuclidianDistance.arrayToList(arr1).equals(list1);
		assert EuclidianDistance.arrayToList(arr2).equals(list2);
		
		List<Double> diff1 = EuclidianDistance.calcDiff(list1, list2);
		List<Double> diff2 = EuclidianDistance.calcDiff(list2, list1);
		
		assert diff1.equals(diff2);
		
		List<Double> correctDiff = Arrays.asList(17., 9.5, 10., 2., 21., 3., 43., 3., 4.);
		assert diff1.equals(correctDiff);
		
	}
}













