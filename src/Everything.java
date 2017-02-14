import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;

//this class name will probably be changed later
public class Everything extends Canvas implements KeyListener {
	
	//some stuff java likes to have
	private static final long serialVersionUID = 1L;
	//width and height of the render area, the actual frame will be twice this size
	static final int WIDTH = 600, HEIGHT = 480;
	//camera position in x,y,z
	private Vector3 camPos;
	//camera postition in x,y,z (roll,pitch,yaw)
	//currently only support for yaw rotation
	private Vector3 camRot;
	//field of view of the camera
	private double fieldOfView;
	//the data for the screen image
	private int[] pixels;
	//zbuffer
	private double[] zBuffer;
	//the actual BufferedImage object being used as a screen
	private BufferedImage screen;
	//frame count from last second
	private int frames;
	//current time in milliseconds
	private long curTime;
	//time since the last second has elapsed
	private long culmTime;
	
	public static void main(String args[]) {
		//JFrame setup
		JFrame frame = new JFrame("Everything");
		
		//uses + 6 and + 28 because those are the offsets of the frame decorations on my machine, could probably be made more elegant
		//uses * 2 because the screen image gets streched to make seeing dots easier
		frame.setSize(WIDTH * 2 + 6, HEIGHT * 2 + 28);
		
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		Everything e = new Everything();
		frame.add(e);
		frame.setLayout(null);
		e.setBounds(0, 0, WIDTH * 2, HEIGHT * 2);
		frame.setVisible(true);
		frame.repaint();
		frame.setIgnoreRepaint(true);
		e.mainLoop();
	}
	
	public void mainLoop() {
		int fps = 0;
		screen = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		//this array of ints is tied to the screen data, so drawing the image will draw the data represented by this array
		pixels = ((DataBufferInt) screen.getRaster().getDataBuffer()).getData();
		zBuffer = new double[WIDTH * HEIGHT];
		//initialize the camera at the center and with no rotation
		fieldOfView = Math.toRadians(90);
		camPos = new Vector3(WIDTH / 2, HEIGHT / 2);
		camRot = new Vector3(0f, Math.PI / 2, 0f);
		
		//use implemented KeyListener methods from this class
		addKeyListener(this);
		//requests for the Canvas to be taking input from devices
		requestFocus();
		
		curTime = System.currentTimeMillis();
		
		//actual main logic loop
		while(true) {
			BufferStrategy bs = getBufferStrategy();
			if(bs == null) {
				createBufferStrategy(3);
				continue;
			}
			Graphics2D g = (Graphics2D) bs.getDrawGraphics();
			render(g);
			update();
			bs.show();
			fps += 1;
			long nowTime = System.currentTimeMillis();
			culmTime += nowTime - curTime;
			if(culmTime > 1000) {
				culmTime -= 1000;
				frames = fps;
				fps = 0;
			}
			curTime = nowTime;
		}
	}
	
	//any non-rendering and non-input logic should go here, currently nothing
	public void update() {

	}
	
	//fills the pixels array with black
	private void clearScreen() {
		Arrays.fill(pixels, 0xff000000);
		Arrays.fill(zBuffer, -1);
	}
	
	//stores the x and y coords in x and y of the vector3, and 1 or -1 in z if the result is on or off screen
	private Vector3 pointToScreen(Vector3 point)
	{
		double dx = Math.cos(fieldOfView / 2.0) / Math.sin(fieldOfView / 2.0);
		double xT = point.getX() - camPos.getX();
		xT /= WIDTH / 2;
		double yT = point.getY() - camPos.getY();
		yT /= HEIGHT / 2;
		yT *= (float) HEIGHT / (float) WIDTH;
		double zT = point.getZ() - camPos.getZ();
		boolean neg = zT < 0;
		double thetaX = Math.atan2(zT, xT);
		double thetaY = Math.atan2(yT, zT);
		thetaX -= camRot.getZ();
		thetaY += camRot.getY();
		//System.out.println(Math.toDegrees(theta));
		double distX = Math.sqrt(xT * xT + zT * zT);
		double distY = Math.sqrt(yT * yT + zT * zT);
		double fullDist = Math.sqrt(xT * xT + yT * yT + zT * zT);
		zT = fullDist * Math.sin(thetaX) * Math.sin(thetaY);
		xT = distX * Math.cos(thetaX);
		//System.out.printf("%f - (%f, %f, %f)", xT / zT, point.getX(), point.getY(), point.getZ());
		yT = distY * Math.cos(thetaY);
		xT *= WIDTH / 2;
		yT *= HEIGHT / 2;
		if(neg) {
			zT = -zT;
		}
		/*//culling
		if(zT < 0) {
			return;
		}*/
		boolean flip = zT < 0;
		if(flip)
			zT = -zT;
		double xCord = (dx * (xT / zT)) + camPos.getX() + (WIDTH / 2 - camPos.getX());
		//xCord += xCord * Math.cos(rot.getZ()) - xCord;
		double yCord = (dx * (yT / zT)) + camPos.getY();
		//System.out.printf("%f, %f : %f, %f\n", xCord, yCord, (dx * (xT / (zT))), (dx * (yT / (zT))));
		//System.out.printf("%.2f : %.2f\n", xCord, yCord);
		return new Vector3(xCord, yCord, flip ? -zT : zT);
	}
	
	//render a single point, using the pixels array as the screen
	private void renderPoint(Vector3 point, int color) {
		Vector3 screenPoint = pointToScreen(point);
		//culling
		if(screenPoint.getZ() < 0)
		{
			return;
		}
		double xCord = screenPoint.getX();
		double yCord = screenPoint.getY();

		if(xCord >= 0 && xCord < WIDTH && yCord >= 0 && yCord < HEIGHT) {
			int pos = (int) yCord * WIDTH + (int) xCord;
			if(zBuffer[pos] > screenPoint.getZ() || zBuffer[pos] == -1)
			{
				pixels[pos] = color;
				zBuffer[pos] = screenPoint.getZ();
			}

		}
	}
	
	private void renderLine(Vector3 p1, Vector3 p2, int color)
	{
		Vector3 sP1 = pointToScreen(p1);
		Vector3 sP2 = pointToScreen(p2);
		//lazy culling for now
		if(sP1.getZ() < 0 || sP2.getZ() < 0)
			return;
		double xS = sP1.getX();
		double xE = sP2.getX();
		double yS =	sP1.getY();
		double yE = sP2.getY();
		//System.out.printf("%f, %f, %f, %f\n", xS, xE, yS, yE);
		//more lazy culling
		if(Math.min(xS, xE) < 0 || Math.max(xS, xE) >= WIDTH || Math.min(yS, yE) < 0 || Math.max(yS, yE) >= HEIGHT) {
			return;
		}
		boolean flipY = yE < yS;
		boolean flipX = xE < xS;
		if(xS - xE == 0)
		{
			for(double i = 0; i < Math.abs(yE - yS); i++)
			{
				int pos = (int) (yS + (flipY ? -i : i)) * WIDTH + (int) (xS);
				if(zBuffer[pos] >  sP1.getZ() || zBuffer[pos] == -1)
				{
					pixels[pos] = color;
					zBuffer[pos] = sP1.getZ();
				}
			}
			return;
		}
		double slope = Math.abs(((yE - yS) / (xE - xS)));
		if (slope > 1)
		{
			for(double i = 0; i < Math.abs(yE - yS); i++)
			{
				int pos = (int) (yS + (flipY ? -i : i)) * WIDTH + (int) (xS + (flipX ? -(i/slope) : (i/slope)));
				if(zBuffer[pos] >  sP1.getZ() || zBuffer[pos] == -1)
				{
					pixels[pos] = color;
					zBuffer[pos] = sP1.getZ();
				}
			}
		}
		else
		{
			for(double i = 0; i < Math.abs(xE - xS); i++)
			{
				int pos = (int) (yS + (flipY ? slope * i : slope * -i)) * WIDTH + (int) (xS + (flipX ? -i : i));
				if(zBuffer[pos] >  sP1.getZ() || zBuffer[pos] == -1)
				{
					pixels[pos] = color;
					zBuffer[pos] = sP1.getZ();
				}
			}
		}
		
	}
	
	private void renderThings() {
		for(double i = -4.5; i < 5; i+=0.5) {
			Random rand = new Random((long) i);
			int color = 0xff000000 | (rand.nextInt(256) << 16) | (rand.nextInt(256) << 8) | rand.nextInt(256);
			//renderPoint(new Vector3(200, 200, i));
			//renderPoint(new Vector3(200, 200 + 50, i));
			renderLine(new Vector3(200, 200, i), new Vector3(200, 200 + 50, i), color);
			renderLine(new Vector3(400, 200, i), new Vector3(400, 200 + 50, i), color);
			renderLine(new Vector3(200, 200, i), new Vector3(400, 200, i), color);
			renderLine(new Vector3(200, 200 + 50, i), new Vector3(400, 200 + 50, i), color);
			//renderPoint(new Vector3(400, 200, i));
			//renderPoint(new Vector3(400, 200 + 50, i));
		}
		renderPoint(new Vector3(WIDTH / 2, HEIGHT / 2, 2), 0xffff0000);
		renderPoint(new Vector3(WIDTH / 2, HEIGHT / 2, -2), 0xffff0000);

	}
	
	public void render(Graphics2D g) {
		clearScreen();
		renderThings();
		g.drawImage(screen, 0, 0, WIDTH * 2, HEIGHT * 2, null);
		g.setColor(Color.YELLOW);
		//draw some debug information on the bottom of the screen
		g.drawString(String.format("X: %.2f Y: %.2f Z: %.2f FOV: %.2f", camPos.getX(), camPos.getY(), camPos.getZ(), Math.toDegrees(fieldOfView)), 5, HEIGHT * 2 - 18);
		g.drawString(String.format("Yaw: %.2f Pitch: %.2f FPS: %d", Math.toDegrees(camRot.getZ()), Math.toDegrees(camRot.getY()), frames), 5, HEIGHT * 2 - 5);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//resets the camera location and rotation
		if(e.getKeyCode() == KeyEvent.VK_R) {
			camPos = new Vector3(WIDTH / 2, HEIGHT / 2, 0);
			camRot = new Vector3(0, Math.PI / 2, 0);
		}
		
		//handle input movement left-right, up-down, and in-out
		if(e.getKeyCode() == KeyEvent.VK_A) {
			camPos.move(new Vector3(-3, 0, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {
			camPos.move(new Vector3(3, 0, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_E) {
			camPos.move(new Vector3(0, -3, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_Q) {
			camPos.move(new Vector3(0, 3, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_W) {
			camPos.move(new Vector3(0, 0, 0.03));
		}
		if(e.getKeyCode() == KeyEvent.VK_S) {
			camPos.move(new Vector3(0, 0, -0.03));
		}
		
		//change the field of view
		if(e.getKeyCode() == KeyEvent.VK_Z) {
			fieldOfView += 0.02;
			if(fieldOfView > Math.PI) {
				fieldOfView = Math.PI;
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_X) {
			fieldOfView -= 0.02;
			if(fieldOfView < 0) {
				fieldOfView = 0;
			}
		}
		
		//handle input for rotation, will be moved to mouse input at some point
		if(e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET){
			camRot.move(new Vector3(0, 0, -Math.PI / 180));
			if(camRot.getZ() < 0) {
				camRot.move(new Vector3(0, 0, Math.PI * 2));
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET){
			camRot.move(new Vector3(0, 0, Math.PI / 180));
			if(camRot.getZ() > Math.PI * 2) {
				camRot.move(new Vector3(0, 0, -Math.PI * 2));
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_MINUS) {
			camRot.move(new Vector3(0, Math.PI / 180, 0));
			if(camRot.getY() > Math.PI){
				camRot.setY(Math.PI);
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_EQUALS) {
			camRot.move(new Vector3(0, -Math.PI / 180, 0));
			if(camRot.getY() < 0){
				camRot.setY(0);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

}
