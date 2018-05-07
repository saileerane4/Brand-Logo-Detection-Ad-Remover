import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class VideoPlayer extends JPanel implements ActionListener {

	
	private static final long serialVersionUID = -8426080774234368297L;
	
	//Width and Height of frame
	public static final int WIDTH = 480;
	public static final int HEIGHT = 270;
	
	//Timer to allow video to update
	Timer timer;

	//AWT and Swing objects to help display video
	BufferedImage img;
	InputStream videoStream;
	JLabel frame;

	//Swing components to control video
	JButton play, pause, stop;

	//Audio info for generating audio playback
	File audio;
	AudioInputStream audioStream;
	AudioFormat format;
	DataLine.Info info;
	Clip clip;
	
	//Filenames of video and audio
	String videopath;
	String audiopath;
	
	//Debug for watching logo recognition video
	boolean debug = false;

	//Current video frame
	int curFrame;

	//Framerate of video
	public static final int FRAMERATE = 30;

	public VideoPlayer(String videoPath, String audioPath) {
		//Get period from framerate to nearest int. 30 fps ~= 33 ms per frame
		int period = 1000 / FRAMERATE;

		//Remember filenames
		videopath = videoPath;
		audiopath = audioPath;
		
		//Timer updates every half frame so video will never be desynced by more than a half frame
		//Excluding off by one issues on my part that may or may not be present
		timer = new Timer(period/2, this);
		
		//Set up components to read video
		img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		try {
			videoStream = new FileInputStream(videopath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		//Swing Components to display video and controls
		frame = new JLabel(new ImageIcon(img));
		play = new JButton("Play");
		pause = new JButton("Pause");
		stop = new JButton("Stop");
		
		//add layout components for better display
		GridBagConstraints layout = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		
		layout.gridx = 0; layout.gridy = 0;
		layout.gridwidth = 3;
		this.add(frame, layout);
		
		layout.fill = GridBagConstraints.HORIZONTAL;
		layout.gridx = 0;
		layout.gridy = 1;
		layout.gridwidth = 1;
		layout.weightx = 1;
		this.add(play, layout);

		layout.gridx = 1;
		this.add(pause, layout);
		
		layout.gridx = 2;
		this.add(stop, layout);

		//ActionListeners to add functionality to the buttons
		play.addActionListener(this);
		pause.addActionListener(this);
		stop.addActionListener(this);

		//Set up components to play audio
		audio = new File(audiopath);
		try {
			audioStream = AudioSystem.getAudioInputStream(audio);
			format = audioStream.getFormat();
			info = new DataLine.Info(Clip.class, format);
			clip = (Clip)AudioSystem.getLine(info);
			clip.open(audioStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Start timer
		timer.start();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		//If pause button is pressed. Pause if playing.
		if (arg0.getSource() == pause) {
			//stream.skip((long)(10 * format.getFrameSize() * format.getFrameRate()));
			//clip.setFramePosition(clip.getFramePosition() + (int)(10 * format.getFrameRate()));
			if (clip.isActive()) {
				clip.stop();
				//System.out.println(curFrame);
			}
		}
		//If play button is pressed. Play if paused/stopped
		else if (arg0.getSource() == play) {
			if (!clip.isActive()) {
				clip.start();
				//System.out.println("play");
			}
		}
		//If stop button is pressed
		else if (arg0.getSource() == stop) {
			//Reset audio
			clip.stop();
			clip.close();
			
			try {
				audioStream = AudioSystem.getAudioInputStream(audio);
				format = audioStream.getFormat();
				info = new DataLine.Info(Clip.class, format);
				clip = (Clip)AudioSystem.getLine(info);
				clip.open(audioStream);
				
				//Reset video
				curFrame = 0;
				videoStream.close();
				videoStream = new FileInputStream(videopath);
				
				//Black out screen
				for(int y = 0; y < HEIGHT; y++){
					for(int x = 0; x < WIDTH; x++){
						img.setRGB(x,y,0);
					}
				}
				repaint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		else {
			
			if (!clip.isActive()) {
				return;
			}
			try {
				//Generate byte array to hold frame
				byte[] bytes = new byte[WIDTH*HEIGHT*3];

				//Gets the video frame the audio is on
				int audioFrame = (int)(clip.getFramePosition() / format.getFrameRate() * FRAMERATE);

				//if audio is behind video, wait for audio to catch up
				if (audioFrame < curFrame) {
					return;
				}
				//If audio is ahead of video, keep going through frames until video catches up
				while (audioFrame > curFrame) {
					int offset = 0;
					int numRead = 0;
					while (offset < bytes.length && (numRead=videoStream.read(bytes, offset, bytes.length-offset)) >= 0) {
						offset += numRead;
					}

					//Update frame
					//logo detection
					int ind = 0;
					for(int y = 0; y < HEIGHT; y++){
						for(int x = 0; x < WIDTH; x++){
							int r = bytes[ind] & 0xff;
							int g = bytes[ind+HEIGHT*WIDTH] & 0xff;
							int b = bytes[ind+HEIGHT*WIDTH*2] & 0xff; 
							
							if (debug) {
								//Only look at center half
								if (x < WIDTH / 4 || x > 3 * WIDTH / 4) {
									g = 0;
									b = 0;
									r = 0;
								}
								
								float hsv[] = new float[3];
								Color.RGBtoHSB(r, g, b, hsv);
								//High Brightness/Saturation
								if (hsv[1] > 0.8 || hsv[2] > 0.8) {
									float h = hsv[0] * 240;
									//Green
									if (h > 70 && h < 120 && hsv[1] > .7) {
										g = 255;
										r = 0;
										b = 70;
									}
									//Yellow
									else if (h > 30 && h < 50) {
										r = 255;
										g = 255;
										b = 0;
									}
									//Blue
									else if (h >= 120 && h < 165 && hsv[1] > .7) {
										r = 0;
										g = 30;
										b = 200;
									}
									//Red
									else if ((h > 220 || h < 10) && hsv[1] > .7) {
										r = 255;
										g = 0;
										b = 20;
									}
									//White
									else if (hsv[1] < .1) {
										r = 255;
										g = 255;
										b = 255;
									}
									//Everything Else
									else {
										r = 0;
										g = 0;
										b = 0;
									}
								}
								//Not enough saturation/brightness
								//So leave as white or as black
								else if (g > 200 && b > 200 && r > 200) {
									r = 255;
									g = 255;
									b = 255;
								}
								else {
									g = 0;
									b = 0;
									r = 0;
								}
							}
							int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
							img.setRGB(x,y,pix);

							++ind;
						}
					}
					++curFrame;
				}
				//Actually paint the frame
				repaint();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
