
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
	
	protected static List<String> testImages = Arrays.asList(
			"Photos/testPage1.jpg",
			"Photos/testPage2.jpg", 
			"Photos/testPage3.jpg", 
			"Photos/testPage4.jpg", 
			"Photos/testPage5.jpg"
	);
	
	public static void main(String[] args){
		//testInMemorySsdeep();
		
		
		try{
			List<String> hashes = new ArrayList<>();
			
			int i = 0;
			for(String filePath : testImages){
				ImageRecognizer ir = new ImageRecognizer(filePath);
				
				Mat infoPart = ir.getInfoPart();
				Imgcodecs.imwrite("9.infoPart.bmp", infoPart);
				Mat codePart = ir.getCodePart();
				Imgcodecs.imwrite("10.codePart.bmp", codePart);
				
	
				ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
				hasher.test();
				//String hash = hasher.getImageFuzzyHash();
				//hashes.add(hash);
				//System.out.println(hash);
				
				Mat rected = hasher.getRectedImage();
				Imgcodecs.imwrite("11." + (i++) + ".rect.png", rected);
				
				/*				
				BufferedImage img = ImageIO.read(new File("9.codePart.bmp"));
				LuminanceSource source = new BufferedImageLuminanceSource(img);
				BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
				Reader reader = new MultiFormatReader();
				Result result = reader.decode(bitmap);
				System.out.println(result.getText());
				*/
			}
			/*
			ssdeep hasher = new ssdeep();
			for(int first = 0; first < hashes.size() - 1; ++first){
				for(int second = first + 1; second < hashes.size(); ++second){
					SpamSumSignature firstSignature = new SpamSumSignature(hashes.get(first));
					SpamSumSignature secondSignature = new SpamSumSignature(hashes.get(second));
					
					int score = hasher.Compare(firstSignature, secondSignature);
					System.out.println(Integer.toString(first) + " - " + Integer.toString(second) + " -> " + Integer.toString(score));
				}
			}
			*/
		}
		catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("done.");
		
		
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

}