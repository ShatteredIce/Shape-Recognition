//i changed it!!
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Main {
	
	final int colorThreshold = 5;
	final int neighborMinThreshold = 6;
	final int neighborMaxThreshold = 25;
	
	public Main(){
		BufferedImage test = loadImage("./res/raw/triangle4.png");
		
		System.out.print("hello!");
		BufferedImage shape = highlightShape(findEdges(convertGrayscale(test)), test);
		ArrayList<ArrayList> mine = findEndpoints(findEdges(convertGrayscale(test)));
		for (int i = 0; i < mine.size(); i ++){
			ArrayList temp = mine.get(i);
			for (int j = 0; j < 2; j ++) {
				for (int k = 0; k < 2; k ++) {
					shape.setRGB((int)temp.get(0), (int)temp.get(1), (new Color(255,0,0)).getRGB());
				}
			}
		}
		storeImage(shape, "./res/processed/squarebox_out.png");
		System.out.print("done!");
		 
		//storeImage(findEdges(convertGrayscale(test)), "./res/output.png");
	}

	public static void main(String[] args) {
		new Main();
	}
	
	private BufferedImage loadImage(String path) {
		File in = new File(path);
		BufferedImage im;
		try {
			im = ImageIO.read(in);
			return im;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	private void storeImage(BufferedImage image, String path) {
		File out = new File(path);
		try {
			ImageIO.write(image, "png", out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private BufferedImage convertGrayscale(BufferedImage test){
		//pure image is the one that is greyscaled 
		int picWidth = test.getWidth();
    	int picHeight = test.getHeight();
    	BufferedImage pureImage = new BufferedImage(picWidth, picHeight, test.getType()); ; //i think that this is a problem in the way the pointers work
    	for (int counterX = 0; counterX < (int)(picWidth ) ; counterX += 1){
    		for (int counterY = 0; counterY < (int)(picHeight ) ; counterY += 1){
    			int originalColor;
        		originalColor = test.getRGB(counterX, counterY);
        		Color myColor = new Color(originalColor);
        		int Red = myColor.getRed();
        		int Blue = myColor.getBlue();
        		int Green = myColor.getGreen();
        		
        		Color myNewColor = null;
        		int Grey = (Red + Blue + Green) / 3;
            	myNewColor = new Color (Grey, Grey, Grey);
            	int newRGB = myNewColor.getRGB();
        		pureImage.setRGB(counterX, counterY, newRGB);
    		}
    	}
    	return pureImage;
	}
	
	private BufferedImage blurImage(BufferedImage img){
		int width = img.getWidth();
		int height = img.getHeight();
		BufferedImage convertedImage = new BufferedImage(width, height, img.getType()); 
		for(int xpos = 1; xpos < width - 1; xpos++){
			for(int ypos = 1; ypos < height - 1; ypos++){
				double red = 0;
				double blue = 0;
				double green = 0;
				for(int i = xpos - 1; i <= xpos + 1; i++){
					for(int j = ypos - 1; j <= ypos + 1; j++){
						Color currentColor = new Color(img.getRGB(i, j));
						red += (1d/9d) * currentColor.getRed();
						blue += (1d/9d) * currentColor.getBlue();
						green += (1d/9d) * currentColor.getGreen();
						
					}
				}
				Color newColor = new Color((int) red, (int) green, (int) blue);
				convertedImage.setRGB(xpos, ypos, newColor.getRGB());
				
			}
		}
		return convertedImage;
	}
	
	private BufferedImage findEdges(BufferedImage img){
		int width = img.getWidth();
		int height = img.getHeight();
		BufferedImage convertedImage = new BufferedImage(width, height, img.getType()); 
		for(int xpos = 2; xpos < width - 2; xpos++){
			for(int ypos = 2; ypos < height - 2; ypos++){
				int numSimilar = 0;
				for(int i = xpos - 2; i <= xpos + 2; i++){
					for(int j = ypos - 2; j <= ypos + 2; j++){
						Color pixel1 = new Color(img.getRGB(xpos, ypos));
						Color pixel2 = new Color(img.getRGB(i, j));
						if(checkSimilarity(pixel1, pixel2)){
							numSimilar++;
						}
					}
				}
				if(numSimilar > neighborMinThreshold && numSimilar < neighborMaxThreshold){
					convertedImage.setRGB(xpos, ypos, Color.RED.getRGB());
				}
				else{
					convertedImage.setRGB(xpos, ypos, Color.WHITE.getRGB());
				}
				
			}
		}
		return convertedImage;
	}
	
	private boolean checkSimilarity(Color pixel1, Color pixel2){
		int red1 = pixel1.getRed();
		int red2 = pixel2.getRed();
		if(Math.abs(red1 - red2) > colorThreshold){
			return true;
		}
		else{
			return false;
		}
	}
	
	private BufferedImage highlightShape(BufferedImage blackLines, BufferedImage real){
		BufferedImage highlight = real;
		int picWidth = highlight.getWidth();
		int picHeight = highlight.getHeight();
		Color red = Color.RED;
		Color trace = Color.CYAN;
		for (int counterX = 0; counterX < (picWidth) ; counterX += 1){
    		for (int counterY = 0; counterY < (picHeight) ; counterY += 1){
    			int originalColor;
        		originalColor = blackLines.getRGB(counterX, counterY);
        		Color myColor = new Color(originalColor);
        		if (myColor.equals(red)){
        			highlight.setRGB(counterX, counterY, trace.getRGB());
        		}
    		}
		}
		return highlight;
		
	}
	private Double[][] processShape(ArrayList<ArrayList<ArrayList<Integer>>> inArray) {
		int len = inArray.size();
		int i = 0;
		Double[][] outArray = new Double[len][3];
		/// Identify: ratio of max-width:max-height, number of points, std from average angle
		for (ArrayList<ArrayList<Integer>> shape : inArray) {
			double minX = (double)shape.get(0).get(0), minY = (double)shape.get(0).get(1), maxX = (double)shape.get(0).get(1), maxY = (double)shape.get(0).get(1);
			Double[] angles = new Double[shape.size()];
			double sum = 0;
			int j = 0;
			for (ArrayList<Integer> points : shape) {
				double x = (double)points.get(0);
				double y = (double)points.get(1);
				double x1 = (double)shape.get(j+1).get(0);
				double x2 = (double)shape.get(j-1).get(0);
				double y1 = (double)shape.get(j+1).get(1);
				double y2 = (double)shape.get(j-1).get(1);
				minX = x < minX? x : minX;
				minY = y < minY? y : minY;
				maxX = x > maxX? x : maxX;
				maxY = y > maxY? y : maxY;
				angles[j] = Math.PI - (Math.atan(Math.abs(y2-y)/Math.abs(x2-x))+Math.atan(Math.abs(y1-y)/Math.abs(x1-x)));
				sum += angles[j];
				j ++;
			}
			double avg = sum / angles.length;
			double sqDiffSum = 0;
			for (double angle: angles) {
				sqDiffSum += (avg - angle) * (avg - angle);
			}
			double r  = (maxX - minX)/(maxY - minY);
			double sqDiffMean = sqDiffSum / angles.length; 
			double std = Math.sqrt(sqDiffMean);
			outArray[i][0] = r;
			outArray[i][1] = (double)shape.size();
			outArray[i][2] = std;
			i ++;
		}
		return outArray;		
	}
	
	private ArrayList findEndpoints(BufferedImage blackLines){
		//passing him an array list of shapes (arraylists) of points (arraylists)
		ArrayList<ArrayList> allPoints = findAllPointsOnShapes(blackLines);
		ArrayList<ArrayList> allEnds = new ArrayList<ArrayList>();
		if (allPoints.size() == 0){
			return null;
		}else{
			for (int i = 0; i < allPoints.size(); i += 1){ //for each point on shape
				ArrayList<Integer> coordinates = allPoints.get(i); //get the point
				int x = coordinates.get(0);
				int y = coordinates.get(1);
				
				//check right and three downs, but not if on edge
				if (y != blackLines.getHeight()){ //not at the bottom of the screen
					if (x != 0){ //if not all the way left, left down
						ArrayList<Integer> newEndPointLD =  tryToFindEndHelp(coordinates, 11, blackLines, 1);
						allEnds.add(newEndPointLD);
					}
					if (x != blackLines.getWidth()){ //if not all the way right
						//go right down
						ArrayList<Integer> newEndPointRD =  tryToFindEndHelp(coordinates, 13, blackLines, 1);
						allEnds.add(newEndPointRD);
					}
					//go down
					ArrayList<Integer> newEndPointD = tryToFindEndHelp(coordinates, 12, blackLines, 1);
					allEnds.add(newEndPointD);
				}
				if (x != blackLines.getWidth()){
					//go right
					ArrayList<Integer> newEndPointR = tryToFindEndHelp(coordinates, 3, blackLines, 1);
					allEnds.add(newEndPointR);
				}
			}
			//filter through all ends, this should be ALL... these could be NULL but really really shouldnt be
			for (int j = 0; j < allEnds.size(); j +=1){
				if (allEnds.get(j) == null){
					allEnds.remove(j);
					j -= 1;
				}else{ 
					boolean alreadyThere = false;
					for (int k = 0; k < j; k +=1){
						//if (allEnds.get(j).get(0).equals(allEnds.get(k).get(0)) && allEnds.get(j).get(1).equals( allEnds.get(k).get(1))){
						int jO = (int)allEnds.get(j).get(0);
						int kO = (int)allEnds.get(k).get(0);
						int kL = (int)allEnds.get(k).get(1);
						int jL = (int)allEnds.get(j).get(1);
						if (Math.abs(jO - kO) <= 5){
							if (Math.abs(jL - kL) <= 5 ){
								alreadyThere = true;
								break;
							}
						}		
					}
					if (alreadyThere){
						allEnds.remove(j);
						j -= 1;
					}
					
				}
			}
			allEnds.add(allPoints.get(0));
			//allEnds.add(allPoints.get(allPoints.size() -1 ));
			return allEnds;
		}
		
	}
	
	private ArrayList<Integer> tryToFindEndHelp(ArrayList<Integer> currentCoord, int dir, BufferedImage blackLines, int time){ //this is recursive
		//ends if hits the edge of screen OR can't find colored pixel
		//LEFTDOWN = 11
		//DOWN = 12
		//RIGHTDOWN = 13
		//RIGHT = 3
		int x = currentCoord.get(0);
		int y = currentCoord.get(1);
		Color red = Color.RED;
		ArrayList<Integer> possible = new ArrayList<Integer>();
		ArrayList<Integer> returnME;
		if (dir == 11){ //left down
			if (x != 0 && y != blackLines.getHeight()){
				// "explore next pixel"
				int originalColor;
        		originalColor = blackLines.getRGB(x  - 1, y + 1);
        		Color myColor = new Color(originalColor);
        		possible.add(x-1);
    			possible.add(y+1);
        		if (myColor.equals(red)){
        			returnME = tryToFindEndHelp(possible, dir, blackLines, time += 1);
        		}else{
        			//it's an end point!!
        			if (time < 10){
        				return null;
        			}
        			return currentCoord;
        		}
			}else{
				return currentCoord;
			}
			
		}else if (dir == 12){ //DOWN
			if (y != blackLines.getHeight()){
				// "explore next pixel"
				int originalColor;
        		originalColor = blackLines.getRGB(x, y + 1);
        		Color myColor = new Color(originalColor);
        		possible.add(x);
    			possible.add(y+1);
        		if (myColor.equals(red)){
        			returnME = tryToFindEndHelp(possible, dir, blackLines, time += 1);
        		}else{
        			//it's an end point!!
        			if (time < 10){
        				return null;
        			}
        			return currentCoord;
        		}
			}else{
				return currentCoord;
			}
		}else if (dir == 13){ //right down
			if (x != blackLines.getWidth() && y != blackLines.getHeight()){
				// "explore next pixel"
				int originalColor;
        		originalColor = blackLines.getRGB(x  + 1, y + 1);
        		Color myColor = new Color(originalColor);
        		possible.add(x + 1);
    			possible.add(y+1);
        		if (myColor.equals(red)){
        			returnME = tryToFindEndHelp(possible, dir, blackLines, time += 1);
        		}else{
        			if (time < 10){
        				return null;
        			}
        			//it's an end point!!
        			return currentCoord;
        		}
			}else{
				return currentCoord;
			}
		}else if (dir == 3){ //right
			if (x != blackLines.getWidth()){
				// "explore next pixel"
				int originalColor;
        		originalColor = blackLines.getRGB(x  + 1, y);
        		Color myColor = new Color(originalColor);
        		possible.add(x + 1);
    			possible.add(y);
        		if (myColor.equals(red)){
        			returnME = tryToFindEndHelp(possible, dir, blackLines, time += 1);
        		}else{
        			if (time < 10){
        				return null;
        			}
        			//it's an end point!!
        			return currentCoord;
        		}
			}else{
				return currentCoord;
			}
		}else{
			System.out.println("ya goofed!!");
			return null;
		}
		return returnME; 
	}
	
	private ArrayList findAllPointsOnShapes(BufferedImage blackLines){
		
		int picWidth = blackLines.getWidth();
		int picHeight = blackLines.getHeight();
		Color red = Color.RED;
		ArrayList<ArrayList> listAll = new ArrayList<ArrayList>();
		for (int counterX = 0; counterX < (picWidth) ; counterX += 1){
    		for (int counterY = 0; counterY < (picHeight) ; counterY += 1){
    			int originalColor;
    			originalColor = blackLines.getRGB(counterX, counterY);
        		Color myColor = new Color(originalColor);
        		if (myColor.equals(red)){
        			//this means this "point" is on a side!!
        			ArrayList<Integer> temporary = new ArrayList<Integer>();
        			temporary.add(counterX);
        			temporary.add(counterY);
        			listAll.add(temporary);
        		}
    		}
		}
		return listAll;
	}
		
}
