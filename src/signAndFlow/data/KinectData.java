package signAndFlow.data;

import KinectPV2.*;
import processing.core.*;
import signAndFlow.visuals.Star;

public class KinectData 
{
	public PVector[] jointLocations;
	
	public Integer rightHandState;
	public Integer leftHandState;
	public boolean rightHandValid = false;
	public boolean leftHandValid = false;
	
	public static final float M = 100000f;	
	private static final float G = 0.4f;	
	
	public KinectData(KJoint[] playerData, PVector trans, float scale)
	{
		jointLocations = new PVector[playerData.length];
		for(int i = 0; i < playerData.length; i++)
			jointLocations[i] = new PVector(0, 0, 0);
		
		setData(playerData, trans, scale);
	}
	
	public void setData(KJoint[] playerData, PVector trans, float scale)
	{
		for(int i = 0; i<playerData.length; i++)
		{
			jointLocations[i].set(playerData[i].getX()* scale *-1, playerData[i].getY()* scale * -1, playerData[i].getZ());
			jointLocations[i].add(trans);			
		}
		
		rightHandState = playerData[KinectPV2.JointType_HandRight].getState();
		rightHandValid = (!rightHandState.equals(KinectPV2.HandState_Unknown) && !rightHandState.equals(KinectPV2.HandState_NotTracked));
		leftHandState = playerData[KinectPV2.JointType_HandLeft].getState();
		leftHandValid = (!leftHandState.equals(KinectPV2.HandState_Unknown) && !leftHandState.equals(KinectPV2.HandState_NotTracked));
	}
	
	public PVector handAttract(Star s)
	{	
		PVector force = new PVector(0, 0, 0);
		if(rightHandValid)
    	{
			PVector tmpF = PVector.sub(jointLocations[KinectPV2.JointType_HandRight], s.location); // Calculate direction of force
		    float d = tmpF.mag();                               // Distance between objects
		    d = PApplet.constrain(d, 1.0f, 10000.0f);         // Limiting the distance to eliminate "extreme" results for very close or very far objects
		    float strength = (G * M * s.mass) / (d * d);      // Calculate gravitional force magnitude
		    tmpF.setMag(strength);   						// Get force vector --> magnitude * direction
		    force.add(tmpF);
    	}
		if(leftHandValid)
    	{
			PVector tmpF = PVector.sub(jointLocations[KinectPV2.JointType_HandLeft], s.location); // Calculate direction of force
		    float d = tmpF.mag();                               // Distance between objects
		    d = PApplet.constrain(d, 1.0f, 10000.0f);         // Limiting the distance to eliminate "extreme" results for very close or very far objects
		    float strength = (G * M * s.mass) / (d * d);      // Calculate gravitional force magnitude
		    tmpF.setMag(strength);   // Get force vector --> magnitude * direction
		    force.add(tmpF);
    	}
	    return force;
	}
}
