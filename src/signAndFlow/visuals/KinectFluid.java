package signAndFlow.visuals;

import java.util.Collection;

import oscP5.*;
import signAndFlow.PManager;
import signAndFlow.data.KinectData;
import KinectPV2.KinectPV2;

public class KinectFluid 
{		
	public void draw()
	{
		Collection<KinectData> playerData = PManager.kMan.playerData.values();
		
		int i = 0;
		for(KinectData kd : playerData)
		{
			OscMessage kinect_joints = new OscMessage("/kinect_joints");
			kinect_joints.add(i);				
							
			// hands
			kinect_joints.add(kd.rightHandValid);
			kinect_joints.add(kd.leftHandValid);
			
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_HandRight].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_HandRight].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_HandRight].z);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_HandLeft].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_HandLeft].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_HandLeft].z);

			// elbowRight			
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_ElbowRight].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_ElbowRight].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_ElbowRight].z);

			// elbowLeft
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_ElbowLeft].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_ElbowLeft].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_ElbowLeft].z);

			// head			
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_Head].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_Head].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_Head].z);
			
			// shoulder				
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_SpineShoulder].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_SpineShoulder].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_SpineShoulder].z);
			
			// torso				
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_SpineMid].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_SpineMid].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_SpineMid].z);
			
			// hips			
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_SpineBase].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_SpineBase].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_SpineBase].z);

			// kneeRight
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_KneeRight].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_KneeRight].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_KneeRight].z);
			
			// kneeLeft
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_KneeLeft].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_KneeLeft].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_KneeLeft].z);

			// footRight
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_FootRight].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_FootRight].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_FootRight].z);
			
			// footLeft
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_FootLeft].x);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_FootLeft].y);
			kinect_joints.add(kd.jointLocations[KinectPV2.JointType_FootLeft].z);

			PManager.osc.send(kinect_joints, PManager.localAddress);			
			
//			System.out.println("******Hand message sent*********");

			i++;
			
			}
		}			
	}	
