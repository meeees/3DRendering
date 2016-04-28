import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import javax.swing.JFrame;


public class Everything extends Canvas {
	
	private static final long serialVersionUID = 1L;
	static final int WIDTH = 600, HEIGHT = 480;
	private int[] pixels;
	private BufferedImage screen;
	Vector3 firstTest;
	private double fieldOfView;
	
	public static void main(String args[]) {
		JFrame frame = new JFrame("Everything");
		frame.setSize(WIDTH + 6, HEIGHT + 28);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		Everything e = new Everything();
		frame.add(e);
		frame.setLayout(null);
		e.setBounds(0, 0, WIDTH, HEIGHT);
		frame.setVisible(true);
		frame.repaint();
		frame.setIgnoreRepaint(true);
		e.doThings();
	}
	
	public void doThings() {
		screen = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		pixels = ((DataBufferInt) screen.getRaster().getDataBuffer()).getData();
		firstTest = new Vector3(20f, 20f, 1f);
		fieldOfView = Math.toRadians(60);
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
		}
	}
	
	public void update() {
		firstTest.setX(250f);
		firstTest.setY(300f);
		firstTest.setZ(1f);
		fieldOfView = Math.toRadians(60);
	}
	
	private void clearScreen() {
		Arrays.fill(pixels, 0xff000000);
	}
	
	private void renderThings() {
		
		double dx = Math.cos(fieldOfView / 2.0) / Math.sin(fieldOfView / 2.0);
		System.out.printf("%.5f : ", dx);
		double xCord = (dx * (firstTest.getX() - WIDTH / 2)) / (firstTest.getZ()) + WIDTH / 2;
		double yCord = (dx * (firstTest.getY() - HEIGHT / 2)) / (firstTest.getZ()) + HEIGHT / 2;
		System.out.printf("%.2f : %.2f\n", xCord, yCord);
		if(xCord >= 0 && xCord < WIDTH && yCord >= 0 && yCord < HEIGHT) {
			
			pixels[(int) yCord * WIDTH + (int) xCord] = 0xffff0000;
		}
	}
	
	public void render(Graphics2D g) {
		clearScreen();
		renderThings();
		g.drawImage(screen, 0, 0, null);
	}

}
