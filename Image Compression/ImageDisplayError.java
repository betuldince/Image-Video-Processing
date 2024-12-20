import java.io.*;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Comparator;
import java.util.Iterator;




public class ImageDisplayError {

	static int mode = 0;
	static double bucket=0;
	static int numBucket=0;
	static int error1=0;
	static int error2=0;
 
	int width = 512;
	int height = 512;
	static int [] freqMapR = new int[256];
	static int [] freqMapG = new int[256];
	static int [] freqMapB = new int[256];


	static List<Buckets> bucketListR;
	static List<Buckets> bucketListR2;
	static List<Buckets> bucketListG;
	static List<Buckets> bucketListG2;
	static List<Buckets> bucketListB;
	static List<Buckets> bucketListB2;

	static List <Buckets> bucketList;


    private void readImageRGB(int width, int height, String imgPath) {
        try {
            int frameLength = width * height * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;

			if(mode==2){
				for(int y = 0; y < height; y++)
				{
					for(int x = 0; x < width; x++)
					{
						byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 
	
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						int rVal = (pix >> 16) & 0xFF;
						int gVal = (pix >> 8) & 0xFF;
						int bVal = (pix) & 0xFF;
						freqMapR[rVal]++;
						freqMapG[gVal]++;
						freqMapB[bVal]++;
	 
					 
						ind++;
					}
				}
				setRangesR();
				setRangesG();
				setRangesB();

				ind = 0;

			}else{
				SetRangeUniform();
			}

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    int newPix = 0;
                    if (mode == 1) {
						
                        newPix = UniformQuantization(pix);
                    }  else if(mode == 2){
						newPix = NonUniformQuantization(pix);
					}

                    ind++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public int UniformQuantization(int pix){
		 
		int rVal = (pix >> 16) & 0xFF;
		int gVal = (pix >> 8) & 0xFF;
		int bVal = (pix) & 0xFF;

		int newRVal = 0;
		int newGVal = 0;
		int newBVal = 0;

		for(int i = 0;i< bucketList.size();i++){
			if (rVal >= bucketList.get(i).getStart() && rVal <= bucketList.get(i).getEnd()) {
				newRVal = bucketList.get(i).getMiddle();
 
			}
		}

		for(int i = 0;i< bucketList.size();i++){
			if (gVal >= bucketList.get(i).getStart() && gVal <= bucketList.get(i).getEnd()) {
				newGVal = bucketList.get(i).getMiddle();
			 
			}
		}
		for(int i = 0;i< bucketList.size();i++){
			if (bVal >= bucketList.get(i).getStart() && bVal <= bucketList.get(i).getEnd()) {
				newBVal = bucketList.get(i).getMiddle();
			 
			}
		}

		int errorR = Math.abs(rVal-newRVal) ;
		int errorG = Math.abs(gVal-newGVal);
		int errorB = Math.abs(bVal-newBVal);
		int sumError = errorR + errorG + errorB;
		error1 += (sumError);
		int newPix = 0xff000000 | ((newRVal & 0xff) << 16) | ((newGVal & 0xff) << 8) | (newBVal & 0xff);
		return newPix;

	}


	public void SetRangeUniform(){
	    bucketList = new ArrayList<>();


		double division  =256.0/ numBucket;
		double divisionRemaining = division - Math.floor(division);

		if(divisionRemaining<0.1 ){
			int bucketSize = (int)Math.floor(256.0/numBucket);	
			int remainingVal = 256 - (bucketSize * numBucket);
			 
			for(int i = 0;i<numBucket;i++){
				int start = i * bucketSize;
				int end = start+ bucketSize;
				if(i == numBucket - 1){
					end += remainingVal;
				}
				Buckets newB = new Buckets( Math.min(start, 255), Math.min(end, 255));
				bucketList.add(newB);
			}
			 
		}else{
			int bucketSize = (int)Math.ceil(256.0/numBucket);
			int remainingVal = 256 - (bucketSize * numBucket);

			for(int i = 0;i<numBucket;i++){
				int start = i * bucketSize;
				int end = start+ bucketSize;
				if(i == numBucket - 1){
					end += remainingVal;
				}
				Buckets newB = new Buckets( Math.min(start, 255), Math.min(end, 255));
				bucketList.add(newB);
			}

			int counter = 0;
			Iterator<Buckets> iterator = bucketList.iterator();
			
			while (iterator.hasNext()) {
				Buckets bucket = iterator.next();
				
				if (bucket.getStart() == 255 && bucket.getEnd() == 255 ) {
					counter ++;				 
					iterator.remove();
				}
			}	 
			for(int i = 0;i<counter;i++){
				createTwoBuckets();
			}
			 
		}
	}

	public void createTwoBuckets(){

		int index = randomBucketToDivide(bucketList);
		if(index != -1){
			Buckets bucket = bucketList.get(index);
			int start = bucket.getStart();
			int end = bucket.getEnd();
			int mid = bucket.getMiddle();
			Buckets new1 = new Buckets(start, mid);
			Buckets new2 = new Buckets(mid+1, end);

			bucketList.remove(bucket);
			bucketList.add(new1);
			bucketList.add(new2);
		}
	}

	public static int randomBucketToDivide(List<Buckets> buckets) {
        Random random = new Random();

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < buckets.size(); i++) {
            if (buckets.get(i).getStart() != buckets.get(i).getEnd()) {
                validIndices.add(i);
            }
        }

        if (!validIndices.isEmpty()) {
            return validIndices.get(random.nextInt(validIndices.size()));
        } else {
            return -1;  
        }
    }

  

	public int NonUniformQuantization(int pix){

		int rVal = (pix >> 16) & 0xFF;
		int gVal = (pix >> 8) & 0xFF;
		int bVal = (pix) & 0xFF;

		int newR = newValR(rVal);
		int newG = newValG(gVal);
		int newB = newValB(bVal);

		int errorR = Math.abs(rVal-newR) ;
		int errorG = Math.abs(gVal-newG);
		int errorB = Math.abs(bVal-newB);
 
		int sumError = errorR + errorG + errorB;
		error2 += (sumError);

		int newPix = 0xff000000 | ((newR & 0xff) << 16) | ((newG & 0xff) << 8) | (newB & 0xff);

		return newPix;
	}

	int newValR(int rVal){
		int pixel = rVal;
		for (Buckets bucket : bucketListR) {
			 
			int avgV = bucket.returnAvg(pixel);
			if(avgV !=-1){
				pixel = avgV;
			}
			 
		}
		return pixel;

	}
	int newValG(int gVal){
		int pixel = gVal;
		for (Buckets bucket : bucketListG) {
			int avgV = bucket.returnAvg(pixel);
			if(avgV !=-1){
				pixel = avgV;
			}
			 
		}
		return pixel;

	}
	int newValB(int bVal){
		int pixel = bVal;
		for (Buckets bucket : bucketListB) {
			int avgV = bucket.returnAvg(pixel);
			if(avgV !=-1){
				pixel = avgV;
			}
			 
		}
		return pixel;

	}



	private static int midPointG(int start, int end) {
		int totalPixel =  numPixelInRangeG(start, end);
		if(totalPixel>1){
			totalPixel = totalPixel / 2;
			int counter = 0;
			int mid = start;
			for (int i = start; i < end; i++) {
				counter += freqMapG[i];
				mid++;
				if (counter >= totalPixel) {
					break;
				}
			}
			 
			return mid-1;
		}else{
			return (start+end)/2;
		}
    }




	private static int numPixelInRangeG(int s, int e){
		int retVal = 0;
		if(s==e){
			return freqMapG[s];
		}
		for(int i=s;i<=e;i++){
			retVal += freqMapG[i];
		}
		return retVal;
	}
	private static int numPixelInRangeB(int s, int e){
		int retVal = 0;
		if(s==e){
			return freqMapB[s];
		}
		for(int i=s;i<=e;i++){
			retVal += freqMapB[i];
		}
		return retVal;
	}


	private static int numPixelInRangeR(int s, int e){
		int retVal = 0;
		if(s==e){
			return freqMapR[s];
		}
		for(int i=s;i<=e;i++){
			retVal += freqMapR[i];
		}
		return retVal;
	}

	private static void createTwoBucketsG(List<Buckets> bucketList, Buckets bucket) {
        int numPix = bucket.getNumPixels();
        int midP = midPointG(bucket.getStart(), bucket.getEnd());

		Buckets bucket1 = new Buckets(bucket.getStart(), midP, numPixelInRangeG(bucket.getStart(),midP),freqMapG);
		Buckets bucket2 = new Buckets(midP+1, bucket.getEnd(),numPixelInRangeG(midP+1,bucket.getEnd()),freqMapG);
		
		bucketList.remove(bucket);
		bucketList.add(bucket1);
		bucketList.add(bucket2);

    }



	private static void createTwoBucketsB(List<Buckets> bucketList, Buckets bucket) {
        int numPix = bucket.getNumPixels();
        int midP = midPointB(bucket.getStart(), bucket.getEnd());

		Buckets bucket1 = new Buckets(bucket.getStart(), midP, numPixelInRangeB(bucket.getStart(),midP),freqMapB);
		Buckets bucket2 = new Buckets(midP+1, bucket.getEnd(),numPixelInRangeB(midP+1,bucket.getEnd()),freqMapB);
		
		bucketList.remove(bucket);
		bucketList.add(bucket1);
		bucketList.add(bucket2);

    }
	private static int midPointB(int start, int end) {
		int totalPixel =  numPixelInRangeB(start, end);
		if(totalPixel>1){
			totalPixel = totalPixel / 2;
			int counter = 0;
			int mid = start;
			for (int i = start; i < end; i++) {
				counter += freqMapB[i];
				mid++;
				if (counter >= totalPixel) {
					break;
				}
			}
			 
			return mid-1;
		}else{
			return (start+end)/2;
		}
    }

	

 
	private static void setRangesR(){
		bucketListR = new ArrayList<>();
		bucketListR2 = new ArrayList<>();
		int totalRed = 512*512;
		Buckets initialBucket = new Buckets(0, 255, totalRed,freqMapR);
		bucketListR.add(initialBucket);
		bucketListR2.add(initialBucket);
 
		while (bucketListR.size()+bucketListR2.size() <=numBucket  ) {
			
			Collections.sort(bucketListR, Collections.reverseOrder());
			 
            Buckets mostPixelsBucket = bucketListR.get(0);
			
			if(mostPixelsBucket.getStart() != mostPixelsBucket.getEnd()){
				
				createTwoBucketsR(bucketListR, mostPixelsBucket);
				 
			}else{
				bucketListR.remove(mostPixelsBucket);
				bucketListR2.add(mostPixelsBucket);
			}

		}
		bucketListR2.remove(initialBucket);
		bucketListR.addAll(bucketListR2);
	}
	private static int midPointR(int start, int end) {
		int totalPixel =  numPixelInRangeR(start, end);
		if(totalPixel>1){
			totalPixel = totalPixel / 2;
			int counter = 0;
			int mid = start;
			for (int i = start; i < end; i++) {
				counter += freqMapR[i];
				mid++;
				if (counter >= totalPixel) {
					break;
				}
			}
			 
			return mid-1;
		}else{
			return (start+end)/2;
		}

    }
	private static void createTwoBucketsR(List<Buckets> bucketList, Buckets bucket) {
        int numPix = bucket.getNumPixels();
        int midP = midPointR(bucket.getStart(), bucket.getEnd());

		Buckets bucket1 = new Buckets(bucket.getStart(), midP, numPixelInRangeR(bucket.getStart(),midP),freqMapR);
		Buckets bucket2 = new Buckets(midP+1, bucket.getEnd(),numPixelInRangeR(midP+1,bucket.getEnd()),freqMapR);
		
		bucketList.remove(bucket);
		bucketList.add(bucket1);
		bucketList.add(bucket2);

    }

	private static void setRangesG(){
		bucketListG = new ArrayList<>();
		bucketListG2 = new ArrayList<>();
		int totalRed = 512*512;
		Buckets initialBucket = new Buckets(0, 255, totalRed,freqMapG);
		bucketListG.add(initialBucket);
		bucketListG2.add(initialBucket);
 
		while (bucketListG.size()+bucketListG2.size() <=numBucket  ) {
			
			Collections.sort(bucketListG, Collections.reverseOrder());
			 
            Buckets mostPixelsBucket = bucketListG.get(0);
			
			if(mostPixelsBucket.getStart() != mostPixelsBucket.getEnd()){
				
				createTwoBucketsG(bucketListG, mostPixelsBucket);
				 
			}else{
				bucketListG.remove(mostPixelsBucket);
				bucketListG2.add(mostPixelsBucket);
			}

		}
		bucketListG2.remove(initialBucket);
		bucketListG.addAll(bucketListG2);
	}
	private static void setRangesB(){
		bucketListB = new ArrayList<>();
		bucketListB2 = new ArrayList<>();
		int totalRed = 512*512;
		Buckets initialBucket = new Buckets(0, 255, totalRed,freqMapB);
		bucketListB.add(initialBucket);
		bucketListB2.add(initialBucket);
 
		while (bucketListB.size()+bucketListB2.size() <=numBucket ) {
			
			Collections.sort(bucketListB, Collections.reverseOrder());
			 
            Buckets mostPixelsBucket = bucketListB.get(0);
			
			if(mostPixelsBucket.getStart() != mostPixelsBucket.getEnd()){
				
				createTwoBucketsB(bucketListB, mostPixelsBucket);
				 
			}else{
				bucketListB.remove(mostPixelsBucket);
				bucketListB2.add(mostPixelsBucket);
			}

		}
		bucketListB2.remove(initialBucket);
		bucketListB.addAll(bucketListB2);
	}

	 

 

    public void processImage(String[] args) {
        
        readImageRGB(width, height, args[0]);
    }

    public static void main(String[] args) {
        ImageDisplayError ren = new ImageDisplayError();
        mode = Integer.parseInt(args[1]);

        for (int i = 2; i <= 255; i++) {
            numBucket = i;

			if (mode==1) {
				error1 = 0; 
				ren.processImage(args);
				System.out.print(error1+ " ");
 
			}else{

				error2 = 0;  
				ren.processImage(args);
				System.out.print(error2+" "  );
 
			}
 
        }
		 

		
}
}


class Buckets implements Comparable<Buckets> {
    private int start;
    private int end;
    private int numOfPixels;
	private int[] freqMap;
	private int avg;
	private int mid;
	 
    public Buckets(int start, int end, int numOfPixels, int [] freqMap) {
        this.start = start;
        this.end = end;
        this.numOfPixels = numOfPixels;
		this.freqMap = freqMap;
		avgOfBucket();
    }

	public Buckets(int start, int end) {
        this.start = start;
        this.end = end;
		 
    }

	public void avgOfBucket(){
		int total = 0;
		int totalFreq = 0;
		int end2 = Math.min(end+1, 255) ;
		for(int i = start ; i<=end;i++){
			total += i*freqMap[i];
			totalFreq += freqMap[i];
		}
		if(totalFreq>0){
			avg =  total/totalFreq;
		}else{
			avg = start; // ASK THIS!
		}
		
	}
	public int returnAvg(int pixVal){
		if(pixVal>= start && pixVal <= end){
			return avg;
		}
		return -1;
	}

	public int getAvg(){
		return avg;
	}

	public int getMiddle(){
		return (start+end)/2;
	}

 
    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getNumPixels() {
        return numOfPixels;
    }

    public void setStart(int startN) {
        this.start = startN;
    }

    public void setEnd(int endN) {
        this.end = endN;
    }

    public void setNumPixels(int numOfPixelsN) {
        this.numOfPixels = numOfPixelsN;
    }

    @Override
    public int compareTo(Buckets other) {
         
        return Integer.compare(this.numOfPixels, other.numOfPixels);
    }

    @Override
    public String toString() {
        return "[" + start + "-" + end + "-" + numOfPixels + "-"+ avg+ "]";
    }
}
