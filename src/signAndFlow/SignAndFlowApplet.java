package signAndFlow;

import oscP5.OscP5;
import netP5.NetAddress;
import KinectPV2.KinectPV2;
import processing.core.*;
import signAndFlow.data.*;
import signAndFlow.visuals.*;
import signAndFlow.audio.AudioManager;

public class SignAndFlowApplet extends PApplet
{
	private AudioManager audioManager;
	private KinectPV2 kinect;
	private KManager kinectDataManager;
	
	private KinectStarField kinectStarField;
	private KinectFluid kinectFluid;
//	private KinectSprites kinectSprites;
	
	private Section section = Section.Flow;
	private Section nextSection = null;
	
	// Kinect params
	private float MIN_THRESH = -5f;
	private float MAX_THRESH = 20f;
	private PVector SKELETON_DATA_TRANSLATION = new PVector(0, 0);
	private float SKELETON_DATA_SCALE = 500f;
	
	// crossfade params
	private boolean CROSSFADE = false;
	private float ALPHA_MASK = 0;
	
	// make sure this matches fluid app address!!!
	private static final NetAddress fluid_address = new NetAddress("127.0.0.1", 5000);
	
	public static void main(String args[]) 
	{
		PApplet.main(new String[] { "--present", SignAndFlowApplet.class.getName() });
	}
	
	public void setup()
	{
		// Processing setup
		size(displayWidth, displayHeight, OPENGL);
		noCursor();
		
		SKELETON_DATA_TRANSLATION.add(new PVector(width/2f, height/2f));
		
		// set singleton
		PManager.setApplet(this);
		
		// audio setup & run
		audioManager = new AudioManager(this, 40);
		PManager.aMan = audioManager;
		new Thread(audioManager).start();
		
		// kinect init
		kinect = new KinectPV2(this);
		kinect.enableSkeleton(true);
	
		kinect.enableSkeleton3dMap(true);
		kinect.enablePointCloud(true);
		kinect.activateRawDepth(true);
		
		kinect.setLowThresholdPC(MIN_THRESH);
		kinect.setHighThresholdPC(MAX_THRESH);
		kinect.init();
		
		// kinect data manager setup & run
		kinectDataManager = new KManager(kinect, SKELETON_DATA_TRANSLATION, SKELETON_DATA_SCALE);
		PManager.kMan = kinectDataManager;
		new Thread(kinectDataManager).start();

		// OSC stuff
		PManager.osc = new OscP5(this, 8001);
		PManager.localAddress = fluid_address;
		
		// visuals
		kinectStarField = new KinectStarField();
		kinectFluid = new KinectFluid();
	}
	
	public void draw()
	{
		background(0);
		
		switch(section)
		{
			case StarField:
				kinectStarField.draw();
				break;
			case Flow:
				kinectFluid.draw();
				break;
			case Flocking:
				drawFlocking();
				break;
		}
		
		if(CROSSFADE)
		{
			crossfade();
		}
		
		
		new Thread(audioManager).start();
		new Thread(kinectDataManager).start();
	}
	
	
	
	private void drawFlocking(){
		
	}
	
	private void crossfade()
	{
		background(0, 0, 0, ALPHA_MASK);
		
		if(nextSection != null)
			ALPHA_MASK += 0.1f;
		else
			ALPHA_MASK -= 0.1f;
		
		if(ALPHA_MASK == 255f){
			section = nextSection;
			nextSection = null;
		}
		else if(ALPHA_MASK == 0)
			CROSSFADE = false;
	}
	
	public void keyPressed()
	{
		// scale
		if (this.key == '=') {
			SKELETON_DATA_SCALE+=1;
			System.out.println("Skeleton Scale: "+ SKELETON_DATA_SCALE);
		}
		else if(key == '-'){
			SKELETON_DATA_SCALE-=1;
			System.out.println("Skeleton Scale: "+ SKELETON_DATA_SCALE);
		}
			
		
		// sections
	    else if (key == '1') {
			nextSection = Section.StarField;
			CROSSFADE = true;
		}
		else if(key == '2'){
			nextSection  = Section.Flow;	
			CROSSFADE = true;
		}
		else if(key == '3'){
			nextSection = Section.Flocking;
			CROSSFADE = true;
		}		
		
		// translation
		if (keyCode == PApplet.UP){
			SKELETON_DATA_TRANSLATION.sub(0, 1, 0);
	    	System.out.println("Skeleton Translation Y: "+ SKELETON_DATA_TRANSLATION);
	    }
	    else if (keyCode == PApplet.RIGHT){
			SKELETON_DATA_TRANSLATION.add(1, 0, 0);
	    	System.out.println("Skeleton Translation X: "+ SKELETON_DATA_TRANSLATION);
	    }
	    else if (keyCode == PApplet.DOWN){
			SKELETON_DATA_TRANSLATION.add(0, 1, 0);
	    	System.out.println("Skeleton Translation Y: "+ SKELETON_DATA_TRANSLATION);
	    }
	    else if (keyCode == PApplet.LEFT){
			SKELETON_DATA_TRANSLATION.sub(1, 0, 0);
	    	System.out.println("Skeleton Translation X: "+ SKELETON_DATA_TRANSLATION);
	    }
	}
}
