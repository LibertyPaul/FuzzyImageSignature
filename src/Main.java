
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

import pkg1.*;
import ssdeep.InMemorySsdeep;
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
	
	public static void mainTest() throws Exception{
		List<String> testImages = Arrays.asList(
				//"testPhotos/1.jpg",
				//"testPhotos/2.jpg",
				"testPhotos/3.jpg",
				"testPhotos/4.jpg",
				"testPhotos/5.jpg",
				"testPhotos/6.jpg"
		);
		
		List<ImageFyzzyHashSum> hashes = new ArrayList<>();
		
		for(String fName : testImages){
			ImageRecognizer ir = new ImageRecognizer(fName);
			
			Mat infoPart = null;
			Mat codePart = null;
			try{
				infoPart = ir.getInfoPart();
				Imgcodecs.imwrite("9.infoPart.bmp", infoPart);
				codePart = ir.getCodePart();
				Imgcodecs.imwrite("10.codePart.bmp", codePart);
			}
			catch(Exception ex){
				System.err.println(ex.getMessage());
				continue;
			}
			
			ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
			hasher.test();
			break;
			/*
			ImageFyzzyHashSum hash = hasher.getImageFuzzyHash();
			hashes.add(hash);
			System.out.println(hash.toString());
			*/
		}
		
		for(int first = 0; first < hashes.size() - 1; ++first){
			for(int second = first + 1; second < hashes.size(); ++second){
				System.out.println(first + " <--> " + second + " : " + hashes.get(first).calcDifference(hashes.get(second)));
			}
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
	
	public static void main(String[] args){
		long startTime = System.currentTimeMillis();
		
		try{
			Main.mainTest();
		}
		catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long stopTime = System.currentTimeMillis();
		double timeMiliSec = stopTime - startTime;
		System.out.println("Time elapsed: " + timeMiliSec / 1000);
	}

}