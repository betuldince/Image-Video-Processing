import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public class ImageDisplayMode2 {

    JFrame frame;
    JLabel lbIm1;
    BufferedImage imgOne;

    int width = 512;
    int height = 512;
    static int[] freqMap = new int[256];
    static int totalRed = 0;
	static List<Buckets> bucketList;

    public static void readImageRGBVal(int width, int height, String imgPath) {
        try {
            int frameLength = width * height * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    int rVal = (pix >> 16) & 0xFF;
                    freqMap[rVal]++;
                    ind++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

 

    private static int midPoint(int start, int end) {
		int totalPixel =  numPixelInRange(start, end);
		if(totalPixel>1){
			totalPixel = totalPixel / 2;
			int counter = 0;
			int mid = start;
			for (int i = start; i < end; i++) {
				counter += freqMap[i];
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

	private static int numPixelInRange(int s, int e){
		int retVal = 0;
		if(s==e){
			return freqMap[s];
		}
		for(int i=s;i<=e;i++){
			retVal += freqMap[i];
		}
		return retVal;
	}

    private static void createTwoBuckets(List<Buckets> bucketList, Buckets bucket) {
        int numPix = bucket.getNumPixels();
        int midP = midPoint(bucket.getStart(), bucket.getEnd());
	 
		 
		if((midP == bucket.getStart() || midP == bucket.getEnd()) && bucket.getNumPixels()!= 0 ){
			bucketList.remove(bucket);
			Buckets bucket1 = new Buckets(midP, midP,freqMap[midP],freqMap );
			bucketList.add(bucket1);
			 

		}else {
			Buckets bucket1 = new Buckets(bucket.getStart(), midP, numPixelInRange(bucket.getStart(),midP),freqMap);
			Buckets bucket2 = new Buckets(midP, bucket.getEnd(),numPixelInRange(midP,bucket.getEnd()),freqMap);
	 
			bucketList.remove(bucket);
			bucketList.add(bucket1);
			bucketList.add(bucket2);

		}


    }
	private static int findFirstBucket(List<Buckets> buckets){
		 
		
		for(int i = 0 ;i<buckets.size();i++){
			if (buckets.get(i).getStart() != buckets.get(i).getEnd()) {
				
				return i;
			}
		}
		return -1;
	}
 
	

    public static void main(String[] args) {
        ImageDisplayMode2 ren = new ImageDisplayMode2();
        int numBuckets = 256;

        readImageRGBVal(ren.width, ren.height, args[0]);
        for (int i = 0; i < freqMap.length; i++) {
            totalRed += freqMap[i];
        }
		bucketList = new ArrayList<>();
        Buckets initialBucket = new Buckets(0, 255, totalRed,freqMap);
        bucketList.add(initialBucket);

		 
		 
		while (bucketList.size() <numBuckets) {
			int ind = 0;
			Collections.sort(bucketList, Collections.reverseOrder());

            Buckets mostPixelsBucket = bucketList.get(ind);
			if(mostPixelsBucket.getStart() != mostPixelsBucket.getEnd()){
				createTwoBuckets(bucketList, mostPixelsBucket);
				 
			}else {
				Collections.sort(bucketList, Collections.reverseOrder());
				int index = findFirstBucket(bucketList);
				Buckets mostPixelsBucket1 = bucketList.get(index);
				createTwoBuckets(bucketList, mostPixelsBucket1);
			} 
			
		}
		Collections.sort(bucketList, Comparator.comparingInt(Buckets::getStart));

		int i = 0;		
		for (Buckets bucket : bucketList) {
			System.out.println(i+": "+ bucket);
			i++;
		}

		/* 
		int pixel = 229;

		for (Buckets bucket : bucketList) {
			int avgV = bucket.returnAvg(pixel);
			if(avgV !=-1){
				pixel = avgV;
			}
			 
		}
		System.out.println("Pixel "+ pixel);
		
		 */
    }
}


class Buckets implements Comparable<Buckets> {
    private int start;
    private int end;
    private int numOfPixels;
	private int[] freqMap;
	private int avg;
	 
    public Buckets(int start, int end, int numOfPixels, int [] freqMap) {
        this.start = start;
        this.end = end;
        this.numOfPixels = numOfPixels;
		this.freqMap = freqMap;
		avgOfBucket();
    }
	 
	public void avgOfBucket(){
		int total = 0;
		int totalFreq = 0;
		int end2 = Math.min(end+1, 255) ;
		for(int i = start ; i<=end2;i++){
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
