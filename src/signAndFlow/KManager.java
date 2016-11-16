package signAndFlow;

import java.util.HashMap;

import processing.core.*;
import KinectPV2.*;
import signAndFlow.data.KinectData;

public class KManager implements Runnable
{		
	public KinectPV2 kinect; 
	public HashMap<Integer, KinectData> playerData = new HashMap<Integer, KinectData>();
	
	public PVector skeletonTranslate;
	public float skeletonScale;
	
	public boolean pointCloudEnabled = false;
	public boolean skeletonEnabled = false;
	
	public KManager(KinectPV2 k, PVector trans, float scale)
	{
		kinect = k;
		skeletonTranslate = trans;	
		skeletonScale = scale;
	}
	
	@Override
	public void run() 
	{
		Skeleton[] kinectSkeletonData = kinect.getSkeleton3d();
	
		for(int i = 0; i < kinectSkeletonData.length; i++) 
		{
			Skeleton sk = kinectSkeletonData[i];
			if(playerData.containsKey(sk.hashCode()))
			{
				if(sk.isTracked())
					playerData.get(sk.hashCode()).setData(sk.getJoints(), skeletonTranslate, skeletonScale);
				else 
					playerData.remove(sk.hashCode());
			}
			else
			{
				if(sk.isTracked())
					playerData.put(sk.hashCode(), new KinectData(sk.getJoints(), skeletonTranslate, skeletonScale));	
			}
		}		
	}
	
//	//use different color for each skeleton tracked
//	private int getIndexColor(int index) {
//	  int col = pApp.color(255);
//	  if (index == 0)
//	    col = pApp.color(255, 0, 0);
//	  if (index == 1)
//	    col = pApp.color(0, 255, 0);
//	  if (index == 2)
//	    col = pApp.color(0, 0, 255);
//	  if (index == 3)
//	    col = pApp.color(255, 255, 0);
//	  if (index == 4)
//	    col =pApp. color(0, 255, 255);
//	  if (index == 5)
//	    col = pApp.color(255, 0, 255);
//
//	  return col;
//	}
//	
//	private void drawBody(KJoint[] joints) {
//		  drawBone(joints, KinectPV2.JointType_Head, KinectPV2.JointType_Neck);
//		  drawBone(joints, KinectPV2.JointType_Neck, KinectPV2.JointType_SpineShoulder);
//		  drawBone(joints, KinectPV2.JointType_SpineShoulder, KinectPV2.JointType_SpineMid);
//
//		  drawBone(joints, KinectPV2.JointType_SpineMid, KinectPV2.JointType_SpineBase);
//		  drawBone(joints, KinectPV2.JointType_SpineShoulder, KinectPV2.JointType_ShoulderRight);
//		  drawBone(joints, KinectPV2.JointType_SpineShoulder, KinectPV2.JointType_ShoulderLeft);
//		  drawBone(joints, KinectPV2.JointType_SpineBase, KinectPV2.JointType_HipRight);
//		  drawBone(joints, KinectPV2.JointType_SpineBase, KinectPV2.JointType_HipLeft);
//
//		  // Right Arm    
//		  drawBone(joints, KinectPV2.JointType_ShoulderRight, KinectPV2.JointType_ElbowRight);
//		  drawBone(joints, KinectPV2.JointType_ElbowRight, KinectPV2.JointType_WristRight);
//		  drawBone(joints, KinectPV2.JointType_WristRight, KinectPV2.JointType_HandRight);
//		  drawBone(joints, KinectPV2.JointType_HandRight, KinectPV2.JointType_HandTipRight);
//		  drawBone(joints, KinectPV2.JointType_WristRight, KinectPV2.JointType_ThumbRight);
//
//		  // Left Arm
//		  drawBone(joints, KinectPV2.JointType_ShoulderLeft, KinectPV2.JointType_ElbowLeft);
//		  drawBone(joints, KinectPV2.JointType_ElbowLeft, KinectPV2.JointType_WristLeft);
//		  drawBone(joints, KinectPV2.JointType_WristLeft, KinectPV2.JointType_HandLeft);
//		  drawBone(joints, KinectPV2.JointType_HandLeft, KinectPV2.JointType_HandTipLeft);
//		  drawBone(joints, KinectPV2.JointType_WristLeft, KinectPV2.JointType_ThumbLeft);
//
//		  // Right Leg
//		  drawBone(joints, KinectPV2.JointType_HipRight, KinectPV2.JointType_KneeRight);
//		  drawBone(joints, KinectPV2.JointType_KneeRight, KinectPV2.JointType_AnkleRight);
//		  drawBone(joints, KinectPV2.JointType_AnkleRight, KinectPV2.JointType_FootRight);
//
//		  // Left Leg
//		  drawBone(joints, KinectPV2.JointType_HipLeft, KinectPV2.JointType_KneeLeft);
//		  drawBone(joints, KinectPV2.JointType_KneeLeft, KinectPV2.JointType_AnkleLeft);
//		  drawBone(joints, KinectPV2.JointType_AnkleLeft, KinectPV2.JointType_FootLeft);
//
//		  drawJoint(joints, KinectPV2.JointType_HandTipLeft);
//		  drawJoint(joints, KinectPV2.JointType_HandTipRight);
//		  drawJoint(joints, KinectPV2.JointType_FootLeft);
//		  drawJoint(joints, KinectPV2.JointType_FootRight);
//
//		  drawJoint(joints, KinectPV2.JointType_ThumbLeft);
//		  drawJoint(joints, KinectPV2.JointType_ThumbRight);
//
//		  drawJoint(joints, KinectPV2.JointType_Head);
//		}
//
//		private void drawJoint(KJoint[] joints, int jointType) {
//		  pApp.strokeWeight(2.0f + joints[jointType].getZ()*8);
//		  pApp.point(joints[jointType].getX(), joints[jointType].getY(), joints[jointType].getZ());
//		}
//
//		private void drawBone(KJoint[] joints, int jointType1, int jointType2) {
//			pApp.strokeWeight(2.0f + joints[jointType1].getZ()*8);
//			pApp.point(joints[jointType2].getX(), joints[jointType2].getY(), joints[jointType2].getZ());
//		}
//
//		private void drawHandState(PVector handLoc, int handState) {
//		  handState(handState);
//		  pApp.pushMatrix();
//		  pApp.translate(handLoc.x, handLoc.y, handLoc.z);
//		  pApp.ellipse(0, 0, 60, 60);
//		  pApp.popMatrix();
//		}
//
//		private void handState(int handState) {
//		  switch(handState) {
//		  case KinectPV2.HandState_Open:
//			  pApp.fill(0, 255, 0);
//		    break;
//		  case KinectPV2.HandState_Closed:
//			  pApp.fill(255, 0, 0);
//		    break;
//		  case KinectPV2.HandState_Lasso:
//			  pApp.fill(0, 0, 255);
//		    break;
//		  case KinectPV2.HandState_NotTracked:
//			  pApp.fill(100, 100, 100);
//		    break;
//		  }
//		}
}
