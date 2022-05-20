import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;

class vector{
	int codebook;
	double value[][];
	
	public vector(int codebook, double value[][]) {
		this.codebook = codebook;
		this.value = value;
	}
}

public class Main {
	
	static int count = 0;
	
	static HashMap<Integer,double[][]> codebook=new HashMap<Integer,double[][]>();
	
	static ArrayList<vector> imageVectors = new ArrayList<vector>();
	
	public static int[][] toInteger(double mat[][], int s, int l) {
		int newMat[][] = new int[s][l];
		for(int i=0; i<s;i++)
		{
			for(int k=0; k<l;k++)
			{
				newMat[i][k] = (int) Math.round(mat[i][k]);
			}	
		}
		
		return newMat;
	}
	
	public static int[][] readImage(String filePath) {
        File file = new File(filePath);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] pixels = new int[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int p = image.getRGB(x, y);
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                pixels[y][x] = r;

                p = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, p);
            }

        }
        return pixels;
    }
	
	
	public static double MSE(double imgVec[][], double bookVec[][], int m, int n) {
		double mse, result=0;
		for(int i=0; i<m;i++)
		{
			for(int k=0; k<n;k++)
			{
				mse=Math.abs(imgVec[i][k]-bookVec[i][k]);
				mse*=mse;
				result+=mse;
			}	
		}
		result/=(m*n);
		return result;
	}
	
	public static void assignVectors(ArrayList<double[][]> vectors, int m, int n) {
		imageVectors.clear();
		for(int v=0; v<vectors.size(); v++) {
			double imgVec[][] = vectors.get(v);
			double minMse=0;
			double minMseVec[][] = new double[m][n];
			for(int f=count; f>count-codebook.size(); f--) {
				double bookVec[][] = codebook.get(f);
				double mse = MSE( imgVec,  bookVec,  m,  n);
				if(f==count || mse<minMse) {
					minMse=mse;
					minMseVec = bookVec;
				}
			}
			int temp;
			for ( int key : codebook.keySet() ) {
				if(codebook.get(key)==minMseVec) {
					temp = key;
					vector vec = new vector(temp,imgVec);
					imageVectors.add(vec);
					break;
				}
			}
		}
	}
	
	public static void updateCodebook(int m, int n) {
		int size = codebook.size();
		int countTemp = count;
		count=0;
		codebook.clear();			
				
		for(int f=countTemp; f>countTemp-size; f--) {
			ArrayList<double[][]> vec = new ArrayList<double[][]> ();
			for(int i=0;i<imageVectors.size();i++) {
				if(imageVectors.get(i).codebook==f) 
				{
					//imageVectors.get(i).codebook=count+1;
					vec.add(imageVectors.get(i).value);
				}
			}
			double avg[][] = new double[m][n];
			avg = average(vec, m, n);

		}
	}
	
	public static void split(int m, int n) {
		int N = codebook.size();
		
		for(int f=1; f<=N; f++) {
			double vector[][] = codebook.get(f);
			codebook.remove(f);
			double temp1[][]= new double[m][n];
			double temp2[][]= new double[m][n];
			for(int i=0; i<m;i++) {
				for(int k=0; k<n;k++) {
					temp1[i][k] = Math.floor(vector[i][k]) ;
					temp2[i][k] = Math.ceil(vector[i][k]) ;
				}
			}
			codebook.put(++count, temp1);
			codebook.put(++count, temp2);
		}
	}
	
	
	
	static ArrayList<double[][]> toVectors(int mat[][],int s, int l, int m, int n){
		ArrayList<double[][]> vectors = new ArrayList<double[][]>();
		for(int i=0; i<s;i+=m){
			for(int f=0; f<l;f+=n){
				double temp[][]= new double[m][n];
				for(int k=i; k<m+i;k++){
					for(int v=f; v<n+f;v++){					
						temp[k-i][v-f] = mat[k][v];
					}
				}
				vectors.add(temp);
			}
		}
		return vectors;
	}
	
	static double[][] decompress(int s, int l, int m, int n){
		int counter=0;
		double compMat[][]= new double[s][l];
		for(int i=0; i<s;i+=m){
			for(int f=0; f<l;f+=n){
				double temp[][]= codebook.get(imageVectors.get(counter).codebook);
				for(int k=i; k<m+i;k++){
					for(int v=f; v<n+f;v++){					
						compMat[k][v] = temp[k-i][v-f];
					}
				}
				counter++;
			}
			
		}
		
		
		
		
		return compMat;
	}
	
	static ArrayList<String> compress(int s, int l, int m, int n){
		ArrayList<String> compressed = new ArrayList<String>();
			for(int f=0; f<imageVectors.size();f++){
				int index = imageVectors.get(f).codebook;
				compressed.add(Integer.toBinaryString(index-1));
			}
		
		return compressed;
	}
	
	
	static double[][] average(ArrayList<double[][]> vectors, int m, int n) {
		
		double avg[][]= new double[m][n];
		
		for(int i=0; i<m;i++)
			for(int k=0; k<n;k++)
				avg[i][k] =0;		

		for(int f=0;f<vectors.size(); f++) {
			double temp[][] = new double[m][n];
			temp = vectors.get(f);
			for(int i=0; i<m;i++)
			{
				for(int k=0; k<n;k++)
				{
					avg[i][k]+=temp[i][k];	
				}
			}
		}
		
		int vectorN = vectors.size();
		
		for(int i=0; i<m;i++)
		{
			for(int k=0; k<n;k++)
				avg[i][k]/=vectorN;		
		}
		codebook.put(++count, avg);
		
		return avg;
		
	}
	
	
	public static void main(String[] args) throws IOException {
		
		Scanner s = new Scanner(System.in);
		
		int mat[][]= readImage("img.jpeg");
		
		
		File file= new File("img.jpeg");
	    BufferedImage img = ImageIO.read(file);
	    int imgCols = img.getHeight();
	    int imgRows = img.getWidth();
		
		
		
		System.out.println("Enter vector size separated by space");
		int m =s.nextInt();
		int n =s.nextInt();
				
		System.out.println("Enter code book size");
		int codebookSize = s.nextInt();
		
		
		double avg[][] = new double[m][n];
		ArrayList<double[][]> vectors = toVectors(mat, imgRows, imgCols, m , n);
		avg = average(vectors, m, n);	
		
		
		
//		System.out.println((imgCols*imgRows)/(m*n));
//		System.out.println(vectors.size());
		
//		for(int h=0; h<vectors.size(); h++) {
//		double vector[][] = vectors.get(h);
//		for(int i=0; i<m;i++)
//		{
//			for(int k=0; k<n;k++)
//			{
//				System.out.println(vector[i][k]);		
//			}
//		}
//		System.out.println("\n");
//		}
		
		
		
		while(codebook.size()!=codebookSize) {
			
			split(m,n);	
				
			assignVectors( vectors,  m,  n);
			
			updateCodebook( m, n);
			
		}
		
		assignVectors( vectors,  m,  n);
		
		//ArrayList<double[][]> vectorss =toVectors(toInteger(decompress(imgRows, imgCols, m, n), imgRows, imgCols), imgRows,  imgCols,  m,  n);
		
		
		WriteImage(toInteger(decompress(imgRows, imgCols, m, n), imgRows, imgCols), imgRows,  imgCols, System.getProperty("user.dir")+"\\result.jpeg");
		

		
	}
		
		
		

	

	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static BufferedImage getBufferedImage(int[][] imagePixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            int s;
            if(y == 199)
                s = 13;
            for (int x = 0; x < width; x++) {
                int value = -1 << 24;
                value = 0xff000000 | (imagePixels[y][x] << 16) | (imagePixels[y][x] << 8) | (imagePixels[y][x]);
                image.setRGB(x, y, value);
            }
        }
        return image;
    }

    public static void WriteImage(int[][] imagePixels, int width, int height, String outPath) {
        BufferedImage image = getBufferedImage(imagePixels, width, height);
        File ImageFile = new File(outPath);
        try {
            ImageIO.write(image, "jpg", ImageFile);
        } catch (IOException e) {
            System.out.println("an error occurred");;
        }
    }

}
	
	
	
	
	
	
	
	
	
		

	
	



