package signAndFlow.visuals;

import java.util.*;

import KinectPV2.*;
import processing.core.*;
import signAndFlow.PManager;
import signAndFlow.audio.FFTPacket;
import signAndFlow.data.KinectData;

public class Star {

	  public PVector location;
	  public PVector homeLocation;
	  public PVector pointCloudLocation;
	  public PVector velocity;
	  public PVector acceleration;
	  public PVector force;
	  
	  public PVector homeColour;
	  public PVector colourVel;
	  public float homeLum;
	  public float lumVel;
	  public float mass;
	  public int vIdx;
	  public int cIdx;
	  public int historySize;
	  
	  // for spring effect
	  private float springConstant;
	  private float springDampening;
	  private float gravDampening;
	  
	  public FFTPacket fftObj;
	  public boolean isPointCloudStar = true; 
	  
	  private KinectStarField starField;
	  
	  
	  public enum StarState{
		  No_Move,
		  Spring_React,
		  Gravity,
		  Point_Cloud,
		  Supernova
	  }
	  
	  public Star(KinectStarField ksf, FFTPacket f, int hs, int idx) 
	  {
		  	PApplet pApp = PManager.pApp;
		    
		  	starField = ksf;
		    vIdx = idx*3;
		    cIdx = idx*4;
		    fftObj = f;
		    historySize = hs;
		    
		    location = new PVector(0, 0, 0);
		    velocity = new PVector(0, 0, 0);
		    acceleration = new PVector(0, 0, 0);
		    force = new PVector(0, 0, 0);		    		    		    
		    springConstant = pApp.random(0.1f, 0.2f);
		    springDampening = pApp.random(0.93f, 0.99f);
		    gravDampening = pApp.random(0.93f, 0.96f);
		    mass = pApp.random(0.1f, 2f);
		    
		    homeLum = pApp.random(1);
		    float vLow = pApp.random(0.2f);
		    float vHigh = pApp.random(1);
		    switch(new Random().nextInt(3))
		    {		    
		    	// red star
			    case 0: homeColour = new PVector(vHigh, vLow, vLow);
			    	break;
			    	
			    // blue star
			    case 1: homeColour = new PVector(vLow, vLow, vHigh);
			    	break;
			    	
			    // yellow star
			    case 2: homeColour = new PVector(vHigh, vHigh, vLow);
			    	break;
		    }		   
	  }

	  public void update(Collection <KinectData> hands, StarState state, float audioDampening, boolean forceShow) 
	  {
		  // determine audio effect
		  float fftAvg = fftObj.getFFTAverageOverN(historySize);		  		  
		  float colourFactor; 
		  if(forceShow)
			  colourFactor = 1;
		  else
			  colourFactor = fftAvg/audioDampening;
		  
		  // set colour
		  PVector tmpColour = new PVector();
		  if(state.equals(StarState.Supernova))
		  {
			  PApplet pApp = PManager.pApp;
			  tmpColour = new PVector(pApp.random(1), pApp.random(1), pApp.random(1));
		  }
		  else
		  {
			  tmpColour.set(homeColour);
			  tmpColour.mult(colourFactor);
		  }
		  
		  // set luminosity
		  float tmpLuminosity = homeLum;
		  tmpLuminosity  *= colourFactor;

		  
		  if(state.equals(StarState.Point_Cloud)|| state.equals(StarState.Supernova) || isPointCloudStar)
		  {
			  location.set(pointCloudLocation);
			  
			  starField.kinectPointBuffer.put(vIdx, location.x);
			  starField.kinectPointBuffer.put(vIdx+1, location.y);
			  starField.kinectPointBuffer.put(vIdx+2, location.z);
			  
			  starField.colourBuffer.put(cIdx, tmpColour.x);
			  starField.colourBuffer.put(cIdx+1, tmpColour.y);
			  starField.colourBuffer.put(cIdx+2, tmpColour.z);
			  starField.colourBuffer.put(cIdx+3, tmpLuminosity);
			  return;
		  }
		  
		  if(state.equals(StarState.Spring_React))
		  {
			  force.mult(0);			  
			  
			  boolean reaching = false;
			  for(KinectData h: hands)
			  { 			 
				    PVector tmpForce;
					if(h.rightHandValid && PVector.dist(h.jointLocations[KinectPV2.JointType_HandRight], homeLocation) < 50)
					{
				    	tmpForce = PVector.sub(location, h.jointLocations[KinectPV2.JointType_HandRight]);
				    	tmpForce.mult(-springConstant);
				    	force.add(tmpForce);
				    	reaching = true;
			    	}	    
					if(h.leftHandValid && PVector.dist(h.jointLocations[KinectPV2.JointType_HandLeft], homeLocation) < 50)
					{
				    	tmpForce = PVector.sub(location, h.jointLocations[KinectPV2.JointType_HandLeft]);
				    	tmpForce.mult(-springConstant);
				    	force.add(tmpForce);
				    	reaching = true;
			    	}	    
			  }
			  if(!reaching)
			  {
				  force = PVector.sub(location, homeLocation);
				  force.mult(-springConstant);		    
			  }			  
			  acceleration.set(PVector.div(force, mass));  
			  velocity.add(acceleration);
			  velocity.mult(springDampening);

		  }
		  else if(state.equals(StarState.Gravity))
		  {
			  force.mult(0);			  
			  for(KinectData h: hands)
			  { 			    
			    	force.add(h.handAttract(this));		    				    
			  }
			  acceleration.set(PVector.div(force, mass));  
			  velocity.add(acceleration);
			  velocity.mult(gravDampening);
		  }
		  else
		  { 
			  velocity.add(acceleration);
		  }
		  location.add(velocity);  

		  
		  if(location.x < 0)
			  location.x = PManager.width;
		  else if(location.x > PManager.width)
			  location.x = 0;
		  
		  if(location.y < 0)
			  location.y = PManager.height;
		  else if(location.y > PManager.height)
			  location.y = 0;
		  
		  if(location.z > 1000)
			  location.z = -1000;
		  else if(location.z < -1000)
			  location.z = 1000;
		  
		  starField.kinectPointBuffer.put(vIdx, location.x);
		  starField.kinectPointBuffer.put(vIdx+1, location.y);
		  starField.kinectPointBuffer.put(vIdx+2, location.z);
		  
		  starField.colourBuffer.put(cIdx, tmpColour.x);
		  starField.colourBuffer.put(cIdx+1, tmpColour.y);
		  starField.colourBuffer.put(cIdx+2, tmpColour.z);
		  starField.colourBuffer.put(cIdx+3, tmpLuminosity);
	  }
	  
}
