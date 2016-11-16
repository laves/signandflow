package signAndFlow;

import java.util.concurrent.locks.ReentrantLock;

import netP5.NetAddress;
import oscP5.OscP5;
import processing.core.*;
import processing.opengl.PJOGL;
import signAndFlow.audio.AudioManager;

public class PManager {
	
	public static AudioManager aMan = null;
	public static KManager kMan = null;
	public static PApplet pApp = null;
	public static OscP5 osc = null;
	public static NetAddress localAddress = null;
	private static PJOGL pgl = null;
	private static ReentrantLock pglLock = new ReentrantLock();
	private static boolean pglActive = false;
	
	public static int width;
	public static int height;
	
	public static void setApplet(PApplet p)
	{
		pApp = p;
		width = p.width;
		height = p.height;
	}
	
	public static PJOGL getPGL()
	{
		pglLock.lock();
		if(pglActive)
		{
			pglLock.unlock();
			return pgl;
		}
		else
		{
			pgl = (PJOGL)pApp.beginPGL();
			pglActive = true;
			pglLock.unlock();			
			return pgl;
		}
	}
	
	public static void endPGL()
	{
		pglLock.lock();
		if(pglActive)
		{

			pglActive = false;
			pApp.endPGL();
			pgl = null;

		}
		pglLock.unlock();
	}
}
