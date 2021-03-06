package ass2.spec;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * COMMENT: Comment Game
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener {

	private Terrain myTerrain;
	private TextureData data;
	private Camera camera;
	private Avatar teapot;
	private Light light;
	private Rain rain;
	
	public Game(Terrain terrain) {
		super("Assignment 2");
		myTerrain = terrain;

		float x = 0;
		float z = 0;
		float offSet[] = terrain.getOffset();
		float y = (float) myTerrain.altitude(x + offSet[1], z + offSet[0]);

		teapot = new Avatar(0, myTerrain.altitude(offSet[1], offSet[0]), 0,
				myTerrain);
		camera = new Camera(teapot, myTerrain);
		light = new Light(myTerrain);
		float[] sunpos = new float[]{myTerrain.getSunlight()[0],myTerrain.getSunlight()[1],myTerrain.getSunlight()[2],0};
		light.setLightPos(sunpos);
		
		rain = new Rain(camera,myTerrain);
	}

	/**
	 * Run the game.
	 *
	 */
	public void run() {
		GLProfile glp = GLProfile.getDefault();
		File file = new File("grass.jpg");
		try {
			data = TextureIO.newTextureData(glp, file, false, "jpg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GLCapabilities caps = new GLCapabilities(glp);
		GLJPanel panel = new GLJPanel();
		panel.addGLEventListener(this);
		panel.addKeyListener(this);

		// Add an animator to call 'display' at 60fps
		FPSAnimator animator = new FPSAnimator(60);
		animator.add(panel);
		animator.start();

		getContentPane().add(panel);
		setSize(800, 600);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * Load a level file and display it.
	 * 
	 * @param args
	 *            - The first argument is a level file in JSON format
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Terrain terrain = LevelIO.load(new File(args[0]));
		Game game = new Game(terrain);
		game.run();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		//gl.glClearColor(1,1,1,1);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		// gluLookAt must be called under modelview matrix

		camera.setCamera(gl);
		light.setUpLight(gl);
		light.draw(gl);
		int lightMode = 0;
		if(teapot.onTorch())
			lightMode = 1;
		
		if (camera.getMode() != Camera.cameraMode.FIRST)
			teapot.draw(gl);
		
		myTerrain.draw(gl, data,lightMode);
		rain.display(gl);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glDeleteBuffers(1, myTerrain.getBufferIds(), 0);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glEnable(GL2.GL_LIGHTING);

		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_POLYGON_OFFSET_POINT);
		gl.glEnable(GL2.GL_POLYGON_OFFSET_LINE);
		gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
        myTerrain.init(gl);
        gl.glLoadIdentity();
	 
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		camera.setAspect(1.0f * width / height);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			if(camera.getMode() == Camera.cameraMode.FREE)
				camera.vmove(0.1);
			else
				teapot.vmove(0.1);
				
			break;
		case KeyEvent.VK_DOWN:
			if(camera.getMode() == Camera.cameraMode.FREE)
				camera.vmove(-0.1);
			else
				teapot.vmove(-0.1);
			break;
		case KeyEvent.VK_LEFT:
			if(camera.getMode() == Camera.cameraMode.FREE)
				camera.rotate(-0.1);
			else
				teapot.rotate(-0.1);
			break;
		case KeyEvent.VK_RIGHT:
			if(camera.getMode() == Camera.cameraMode.FREE)
				camera.rotate(0.1);
			else
				teapot.rotate(0.1);
			break;
		case KeyEvent.VK_Q:
			if(camera.getMode() == Camera.cameraMode.FREE)
				camera.look(0.1);
			else
				teapot.look(0.1);
			break;
		case KeyEvent.VK_E:
			if(camera.getMode() == Camera.cameraMode.FREE)
				camera.look(-0.1);
			else
				teapot.look(-0.1);
			break;
		case KeyEvent.VK_T:
			teapot.toggleTorch();
			light.hideSun();
			break;
		case KeyEvent.VK_V:
			camera.changeMode();
			break;
		case KeyEvent.VK_R:
			rain.rainChange();
			break;
		case KeyEvent.VK_M:
			if(!teapot.onTorch())
				light.toggleSun();
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}
