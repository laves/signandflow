package signAndFlow.visuals;
import KinectPV2.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GL4;
import javax.media.opengl.GLES2;
import javax.media.opengl.GLES3;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.TextureIO;

import processing.core.*;
import processing.opengl.*;
import signAndFlow.KManager;
import signAndFlow.PManager;
import signAndFlow.audio.AudioManager;
import signAndFlow.data.KinectData;
import signAndFlow.data.Utils;
import signAndFlow.visuals.Star.StarState;

public class KinectStarField
{
	private ArrayList<Star> stars;

	private float a = 0;
	private int zval = 50;
	private float pointCloudScale = 260f;
	private float pointCloudTransX, pointCloudTransY;
	private float audioDampening = 5f;
	
	private int numStars = 0;
	private boolean drawSkeleton = false;
	private boolean ending = false;
	private boolean forceShow = false;
	private float fadeToWhite = 0;
	private float supernovaCoeff = 100;
	private StarState starState = StarState.Gravity;


	private PShader starShader;
	
	private PGL pgl;
	private IntBuffer vBuffID;
	private IntBuffer cBuffID;
	private IntBuffer sBuffID;
//	private IntBuffer texID;
//	private int texLoc;

	public FloatBuffer kinectPointBuffer;
	public FloatBuffer colourBuffer;
	public FloatBuffer sizeBuffer;
	
	public KinectStarField()
	{
		stars = new ArrayList<Star>();

		pointCloudTransX = (PManager.width/2);
		pointCloudTransY = (PManager.height/2)+50;
		
		starShader = PManager.pApp.loadShader(getClass().getResource("/signAndFlow/res/pointDraw.frag.glsl").getPath(), getClass().getResource("/signAndFlow/res/pointDraw.vert.glsl").getPath());
		
		numStars = kinectPointCloudInit();
		initVBO(numStars);
	}


	/*
	 *  Waits for Kinect to give realistic point cloud data
	 *  (there's usually a 2-3 second wait)
	 */
	private int kinectPointCloudInit()
	{
		ArrayList<Float> validPts = new ArrayList<Float>();
		int validPointCount = 0;

		FloatBuffer pcb = PManager.kMan.kinect.getPointCloudDepthPos();	
		for(int buffIdx = 0; buffIdx < pcb.capacity(); buffIdx+=3)
		{
			  float x = pcb.get(buffIdx);
			  float y = pcb.get(buffIdx + 1);
			  float z = pcb.get(buffIdx + 2);
			  
			  if(x != Float.NEGATIVE_INFINITY && 
				 y != Float.NEGATIVE_INFINITY && 
				 z != Float.NEGATIVE_INFINITY)
			  {					  
				  validPointCount++;			
				  validPts.add(x);
				  validPts.add(y);
				  validPts.add(z);
			  }
		}
		
		float[] pts = new float[validPointCount*3];
		float[] colours = new float[validPointCount*4];
		float[] sizes = new float[validPointCount];
		for(int i=0; i<validPointCount; i++)
		{
			int ptIdx = i*3;
			pts[ptIdx] = validPts.get(ptIdx);
			pts[ptIdx+1] = validPts.get(ptIdx+1);
			pts[ptIdx+2] = validPts.get(ptIdx+2);
			
		  	float rZ = zval + PManager.pApp.random(-100,0);			
			float rX = PManager.pApp.random(0, PManager.width);
			float rY = PManager.pApp.random(0, PManager.height);			
			
			int fftBandIdx = i % PManager.aMan.getFFTAvgSpecSize();
			Star newStar = new Star(this, PManager.aMan.getFFTPackets()[fftBandIdx], 10, i);
			newStar.location = new PVector(rX, rY, rZ);
			newStar.homeLocation = newStar.location;
			newStar.pointCloudLocation = new PVector(pts[i], pts[i+1], pts[i+2]);
			
			if(PManager.pApp.random(1f) > 0.5f)
				newStar.isPointCloudStar = true;
			else
				newStar.isPointCloudStar = false;	
			
			sizes[i] = newStar.mass;
			
			int cIdx = i*4;
			colours[cIdx] = newStar.homeColour.x;
			colours[cIdx+1] = newStar.homeColour.y;
			colours[cIdx+2] = newStar.homeColour.z;
			colours[cIdx+3] = newStar.lumVel;
			
			stars.add(newStar);				
		}
		kinectPointBuffer = Utils.allocateDirectFloatBuffer(pts.length);
		kinectPointBuffer.put(pts);
		kinectPointBuffer.rewind();
		
		colourBuffer = Utils.allocateDirectFloatBuffer(colours.length);
		colourBuffer.put(colours);
		colourBuffer.rewind();
		
		sizeBuffer = Utils.allocateDirectFloatBuffer(sizes.length);
		sizeBuffer.put(sizes);
		sizeBuffer.rewind();
		
		return validPointCount;
	}
	
	private void initVBO(int nvert)
	{		
		pgl = PManager.getPGL();
		
		vBuffID = Utils.allocateDirectIntBuffer(1);
		pgl.genBuffers(1, vBuffID);
		pgl.bindBuffer(PGL.ARRAY_BUFFER, vBuffID.get(0));
		pgl.bufferData(PGL.ARRAY_BUFFER, nvert * 3 * Utils.SIZEOF_FLOAT, kinectPointBuffer, PGL.STATIC_DRAW);
		
		cBuffID = Utils.allocateDirectIntBuffer(1);
		pgl.genBuffers(1, cBuffID);
		pgl.bindBuffer(PGL.ARRAY_BUFFER, cBuffID.get(0));
		pgl.bufferData(PGL.ARRAY_BUFFER, nvert * 4 * Utils.SIZEOF_FLOAT, colourBuffer, PGL.STATIC_DRAW);
		
		sBuffID = Utils.allocateDirectIntBuffer(1);
		pgl.genBuffers(1, sBuffID);
		pgl.bindBuffer(PGL.ARRAY_BUFFER, sBuffID.get(0));
		pgl.bufferData(PGL.ARRAY_BUFFER, nvert * Utils.SIZEOF_FLOAT, sizeBuffer, PGL.STATIC_DRAW);
		
//		texID = IntBuffer.allocate(1);
//		pgl.genTextures(1, texID);
//		pgl.bindTexture(PGL.TEXTURE_2D, 1);
//		pgl.texParameteri(PGL.TEXTURE_2D, PGL.TEXTURE_MIN_FILTER, PGL.LINEAR);
//		pgl.texParameteri(PGL.TEXTURE_2D, PGL.TEXTURE_MAG_FILTER, PGL.LINEAR);
//		pgl.texImage2D(PGL.TEXTURE_2D, 0, PGL.RGBA, starTexture.width, starTexture.height, 0, PGL.RGBA, PGL.UNSIGNED_BYTE, IntBuffer.wrap(starTexture.pixels).rewind());
//		pgl.bindTexture(PGL.TEXTURE_2D, 0);
	}

	public void draw() 
	{		
		Collection<KinectData> kd = PManager.kMan.playerData.values();
		
		if(starState.equals(StarState.Point_Cloud) || starState.equals(StarState.Supernova))
			fetchPointCloudData(kd);			
		else
		{			
			for(Star s: stars)
				s.update(kd, starState, audioDampening, forceShow);
		}
//		fetchPointCloudData(kd);			
		renderGLPoints();
	}
	
	private void fetchPointCloudData(Collection<KinectData> kd)
	{
		  FloatBuffer pcb = PManager.kMan.kinect.getPointCloudDepthPos();	
		  
		  int validPointCount = 0;
		  for(int i = 0; i < pcb.capacity()/3; i++)
		  {			
			  int buffIdx = i*3;
			  float x = pcb.get(buffIdx);
			  float y = pcb.get(buffIdx + 1);
			  float z = pcb.get(buffIdx + 2);
			  
			  if( x != Float.NEGATIVE_INFINITY && 
				  y != Float.NEGATIVE_INFINITY && 
				  z != Float.NEGATIVE_INFINITY)
			  {
				  if(validPointCount < numStars)
				  {
					  Star s = stars.get(validPointCount);				 
				  
					  buffIdx = (validPointCount*3);
					  s.pointCloudLocation.set(x, y, z);
					  s.update(kd, starState, audioDampening, forceShow);	  
				  }
				  validPointCount++;
			  }
		  }
		  
		  PManager.pApp.translate(pointCloudTransX, pointCloudTransY, zval);
		  PManager.pApp.scale(pointCloudScale, -1*pointCloudScale, pointCloudScale);
		  PManager.pApp.rotate(a, 0.0f, 1.0f, 0.0f);		
		  
		  if(starState.equals(StarState.Supernova)){
			  if(supernovaCoeff>1.1f)
				  supernovaCoeff-=0.4f;
		  }
	}
	
	private void renderGLPoints()
	{	
		  pgl = (PJOGL)PManager.getPGL();		  
		  starShader.bind();

		  int vertexLocation = pgl.getAttribLocation(starShader.glProgram, "vertex");
		  pgl.enableVertexAttribArray(vertexLocation);
		  pgl.bindBuffer(PGL.ARRAY_BUFFER, vBuffID.get(0));
		  pgl.bufferData(PGL.ARRAY_BUFFER,  kinectPointBuffer.capacity() * Utils.SIZEOF_FLOAT, kinectPointBuffer, PGL.STATIC_DRAW);
		  pgl.vertexAttribPointer(vertexLocation, 3, PGL.FLOAT, false, 0, 0);
		  
		  int colourLocation = pgl.getAttribLocation(starShader.glProgram, "color");
		  pgl.enableVertexAttribArray(colourLocation);
		  pgl.bindBuffer(PGL.ARRAY_BUFFER, cBuffID.get(0));
		  pgl.bufferData(PGL.ARRAY_BUFFER, colourBuffer.capacity() * Utils.SIZEOF_FLOAT, colourBuffer, PGL.STATIC_DRAW);
		  pgl.vertexAttribPointer(colourLocation, 4, PGL.FLOAT, false, 0, 0);
		  
		  int sizeLocation = pgl.getAttribLocation(starShader.glProgram, "size");
		  pgl.enableVertexAttribArray(sizeLocation);
		  pgl.bindBuffer(PGL.ARRAY_BUFFER, sBuffID.get(0));
		  pgl.bufferData(PGL.ARRAY_BUFFER, sizeBuffer.capacity() * Utils.SIZEOF_FLOAT, sizeBuffer, PGL.STATIC_DRAW);
		  pgl.vertexAttribPointer(sizeLocation, 1, PGL.FLOAT, false, 0, 0);
		  
		  pgl.enable(PGL.POINT_SMOOTH);
		  pgl.disable(PGL.DEPTH_TEST);			 
		  pgl.enable(PGL.BLEND);
		  pgl.blendFunc(PGL.SRC_ALPHA, PGL.ONE);
		  
		  // Draw
		  if(starState.equals(StarState.Supernova)){
			  pgl.drawArrays(PGL.LINES, 0, (int)(kinectPointBuffer.capacity()/supernovaCoeff));
		  }
		  else
			  pgl.drawArrays(PGL.POINTS, 0, kinectPointBuffer.capacity());
		 
		  // Unbind
		  starShader.unbind();
		  
		  pgl.disable(PGL.BLEND);		  
		  pgl.bindBuffer(PGL.ARRAY_BUFFER, 0);	
		  pgl.disableVertexAttribArray(vertexLocation);
		  pgl.disableVertexAttribArray(colourLocation);
		  

//		  FloatBuffer sizes = FloatBuffer.wrap(new float[2]);
//		  gl2.glGetFloatv(GL2.GL_ALIASED_POINT_SIZE_RANGE, sizes);
//		  float quadratic[] =  { 1.0f, 0.0f, 0.01f };
//		  gl2.glPointParameterfv( GL2.GL_POINT_DISTANCE_ATTENUATION, quadratic , 1);
//		  gl2.glPointParameterf( GL2.GL_POINT_FADE_THRESHOLD_SIZE, 60.0f);
		    
//		  gl2.glPointParameterf(GL2.GL_POINT_SIZE_MIN, sizes.get(0));
//		  gl2.glPointParameterf(GL2.GL_POINT_SIZE_MAX, sizes.get(1));
//		  gl2.glTexEnvf(GL2.GL_POINT_SPRITE, GL2.GL_COORD_REPLACE, GL2.GL_TRUE);

	}

//	@Override
//	public void keyPressed() 
//	{
//		if (pApp.key == '-') {
//			if(starState.equals(StarState.Point_Cloud) || starState.equals(StarState.Supernova)){
//				pointCloudScale -= 5;
//				System.out.println("Scale Value: " + pointCloudScale);
//			}
//			else{
//				skeletonScale-=1;
//				System.out.println("SkeletonScale: "+ skeletonScale);
//			}
//				
//		} else if (pApp.key == '=') {
//			if(starState.equals(StarState.Point_Cloud)|| starState.equals(StarState.Supernova)){				
//				pointCloudScale += 5;
//				System.out.println("Scale Value: " + pointCloudScale);
//			}
//			else{
//				skeletonScale+=1;
//				System.out.println("SkeletonScale: "+ skeletonScale);
//			}
//		} else if (pApp.key == ',') {
//			audioDampening -= 0.1;
//			System.out.println("Audio Dampening: " + audioDampening);
//		} else if (pApp.key == '.') {
//			audioDampening += 0.1;
//			System.out.println("Audio Dampening: " + audioDampening);
//		} else if (pApp.key == 'u') {
//			minD += 0.1;
//			System.out.println("Min Depth: " + minD);
//		} else if (pApp.key == 'j') {
//			minD -= 0.1;
//			System.out.println("Min Depth: " + minD);
//		} else if (pApp.key == 'i') {
//			maxD += 0.1;
//			System.out.println("Max Depth: " + maxD);
//		} else if (pApp.key == 'k') {
//			maxD -= 0.1;
//			System.out.println("Max Depth: " + maxD);
//		}		
//		else if(pApp.key == 'f')
//			forceShow = !forceShow;
//		else if (pApp.key == 's')
//			drawSkeleton = !drawSkeleton;
//		else if (pApp.key == '1')
//			starState = StarState.No_Move;
//		else if (pApp.key == '2')
//			starState = StarState.Spring_React;	
//		else if (pApp.key == '3')
//			starState = StarState.Gravity;	
//		else if (pApp.key == '4')
//			starState = StarState.Point_Cloud;
//		else if (pApp.key == '6')
//		{
//			starState = StarState.Supernova;
//			supernovaCoeff = 100;
//		}
//
//		else if (pApp.key == ' ')
//			ending = true;
//
//		if (pApp.key == PApplet.CODED) 
//		{
//			if(starState.equals(StarState.Point_Cloud) || starState.equals(StarState.Supernova)){
//			    if (pApp.keyCode == PApplet.UP){
//			    	pointCloudTransY+=1;
//			    	System.out.println("Point Cloud Translation Y: "+ pointCloudTransY);
//			    }
//			    else if (pApp.keyCode == PApplet.RIGHT){
//			    	pointCloudTransX+=1;
//			    	System.out.println("Point Cloud Translation X: "+ pointCloudTransX);
//			    }
//			    else if (pApp.keyCode == PApplet.DOWN){
//			    	pointCloudTransY-=1;
//			    	System.out.println("Point Cloud Translation Y: "+ pointCloudTransY);
//			    }
//			    else if (pApp.keyCode == PApplet.LEFT){
//			    	pointCloudTransX-=1;
//			    	System.out.println("Point Cloud Translation X: "+ pointCloudTransX);
//			    }
//		    }
//			else
//			{
//				if (pApp.keyCode == PApplet.UP){
//			    	skeletonTransY+=1;
//			    	System.out.println("Skeleton Translation Y: "+ skeletonTransY);
//			    }
//			    else if (pApp.keyCode == PApplet.RIGHT){
//			    	skeletonTransX+=1;
//			    	System.out.println("Skeleton Translation X: "+ skeletonTransX);
//			    }
//			    else if (pApp.keyCode == PApplet.DOWN){
//			    	skeletonTransY-=1;
//			    	System.out.println("Skeleton Translation Y: "+ skeletonTransY);
//			    }
//			    else if (pApp.keyCode == PApplet.LEFT){
//			    	skeletonTransX-=1;
//			    	System.out.println("Skeleton Translation X: "+ skeletonTransX);
//			    }				
//			}
//		}
//	}	

}

