
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import pkg1.CharacterMatrix;
import pkg1.ImageFuzzyHash;
import pkg1.ImageRecognizer;
import ssdeep.InMemorySsdeep;
import ssdeep.SpamSumSignature;
import ssdeep.ssdeep;

/*
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
*/
public class Main{
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	
	
	
	
	public static void mainTest(){
		List<String> testImages = Arrays.asList(
				"Photos/testPage1.jpg",
				"Photos/testPage2.jpg", 
				"Photos/testPage3.jpg", 
				"Photos/testPage4.jpg", 
				"Photos/testPage5.jpg", 
				"Photos/testPage6.jpg", 
				"Photos/testPage7.jpg", 
				"Photos/testPage8.jpg"
		);
		//testInMemorySsdeep();
		try{
			ImageRecognizer ir = new ImageRecognizer("Photos/testPage8.jpg");
			
			Mat infoPart = ir.getInfoPart();
			Imgcodecs.imwrite("9.infoPart.bmp", infoPart);
			Mat codePart = ir.getCodePart();
			Imgcodecs.imwrite("10.codePart.bmp", codePart);
			

			ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
			String hash = hasher.getImageFuzzyHash();
			System.out.println(hash);
		}
		catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("done.");
	}
	
	public static void testCharacterMatrix(){
		Mat m1 = Imgcodecs.imread("allChars/384.bmp");
		CharacterMatrix cm1 = new CharacterMatrix(m1);

		Mat m2 = Imgcodecs.imread("allChars/385.bmp");
		CharacterMatrix cm2 = new CharacterMatrix(m2);
		
		System.out.println(cm1.compare(cm2));
	}
	
	public static void testInMemorySsdeep(){
		String testFileName = "Photos/testPage1.jpg";
		
		ssdeep hasher = new ssdeep();
		InMemorySsdeep inMemoryHasher = new InMemorySsdeep();
		
		String hashFromFile = null;
		try{
			hashFromFile = hasher.fuzzy_hash_file(new File(testFileName));
		}
		catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String hashFromMemory = null;
		
		Path path = Paths.get(testFileName);
		try{
			hashFromMemory = inMemoryHasher.fuzzy_hash_array(Files.readAllBytes(path));
		}
		catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(hashFromFile.compareTo(hashFromMemory) == 0){
			System.out.println("testInMemorySsdeep [ OK ]");
		}
		else{
			System.out.println("testInMemorySsdeep [ ERROR ]");
			System.out.println("Hash from file:    " + hashFromFile);
			System.out.println("Hash from memory:  " + hashFromMemory);
		}
	}
	
	public static void main(String[] args){
		long startTime = System.currentTimeMillis();
		
		//Main.testCharacterMatrix();
		Main.mainTest();
		
		long stopTime = System.currentTimeMillis();
		double timeMiliSec = stopTime - startTime;
		System.out.println("Time elapsed: " + timeMiliSec / 1000);
	}

}