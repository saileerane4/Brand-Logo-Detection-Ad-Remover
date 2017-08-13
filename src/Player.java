import java.io.File;

import javax.swing.JFrame;


public class Player extends JFrame {

	/**
	 * Serial ID to make Eclipse happy
	 */
	private static final long serialVersionUID = -1483249650507308646L;
	//Default file paths
	public static String videopath = "dataset/Videos/data_test1.rgb";
	public static String audiopath = "dataset/Videos/data_test1.wav";
	
	/**
	 * Creates a JFrame to hold the player
	 * @param videoPath The path to the video data
	 * @param audioPath The path to the audio data
	 */
	public Player(String videoPath, String audioPath) {
		//Create new JFrame with name of filename minus extension
		super((new File(videoPath).getName().split("\\."))[0]);
		
		//Add the Video player to this frame and pack
		VideoPlayer p = new VideoPlayer(videoPath, audioPath);
		this.add(p);
		this.pack();
		
		//I don't get why setVisible isn't default but whatever
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		//Use command line arguments
		if (args.length >= 2) {
			Player player = new Player(args[0], args[1]);
			//I call pack here to make Eclipse stop complaining mostly
			player.pack();
		}
		//Use default paths
		else {
			Player player = new Player(videopath, audiopath);
			player.pack();
		}
	}

}
