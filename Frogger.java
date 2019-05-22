import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.*;
import java.util.Random;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Frogger extends JPanel implements KeyListener{
    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;
    public static final int FPS = 30;
    World world;
    public static  DrawImage imageFinder;
    Color background;

    class Runner implements Runnable{
	public void run(){
	    while(true){
		world.updateState();
		repaint();
		try{
		    Thread.sleep(1000/FPS);
		}
		catch(InterruptedException e){}
	    }
	    
	}
	
    }
    
    public void keyPressed(KeyEvent e) {
	commands(e);
    }
    
    public void addNotify() {
	super.addNotify();
	requestFocus();
    }
    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    public void commands(KeyEvent e){
	char c=e.getKeyChar();
	if(c=='w'||c=='a'||c=='s'||c=='d')
	    userObject.move(c,world);
	/*	if(c=='p')
	    this=new Frogger(); */
	if(c=='l'){			
	    System.out.println("Thanks for playing!");			
	    System.exit(0);
	}
    }//commands method executes specific commands depending on key pressed on keyboard
    
    public Frogger(){
	world = new World(WIDTH, HEIGHT,16,16,this);
	addKeyListener(this);
	this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
	Thread mainThread = new Thread(new Runner());
	mainThread.start();
    }
    //makes new world object,assigns it a keylistener, makes a thread, starts the thread 
    
    public static void main(String[] args){
	JFrame frame = new JFrame("Frogger+");
	imageFinder=new DrawImage();
	frame.add(imageFinder);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	Frogger mainInstance = new Frogger();
	frame.setContentPane(mainInstance);
	frame.pack();
	frame.setVisible(true);
    }
    @Override
	public void paintComponent(Graphics g) {
	super.paintComponent(g);
	if(world.defeated)
	{
	    g.setColor(Color.black);
	    g.fillRect(0,0,WIDTH,HEIGHT);
	    g.setColor(Color.RED);
	    g.drawString("You dun' messed up son, press 'l' to leave, L in tow",(WIDTH / 2) - 22,HEIGHT / 2);
	}//draws frame when player loses
	else{
	    if(world.youWin){
		g.setColor(Color.BLUE);
		g.fillRect(0,0,WIDTH,HEIGHT);
		g.setColor(Color.GREEN);
		g.drawString("AAAAAYYYYYY YOU WON GANG GANG GANG",WIDTH/2,HEIGHT/2);
	    }// draws frame when player wins
	    else{
		Terrain.build(g);
		world.drawLogs(g);
		world.drawObstacles(g);
		userObject.draw(g);
	    }
	}
    }
} 

class World{
    static int height;
    static int  width;
    int numObstacles;
    int numLogs;
    Obstacle[] obstacles;
    environmentalObjects[] logs;
    userObject player;
    Pair[] position;
    Pair[] positionlogs;
    userObject frog;
    Frogger gameRunning;
    boolean initialized;
    boolean defeated;
    boolean youWin;
    public World(int initWidth, int initHeight,int numObstacle, int numLog, Frogger game){
	initialized = false;
	gameRunning = game;
	numObstacles = numObstacle;
	numLogs = numLog;
	position = new Pair[numObstacles];
	positionlogs = new Pair[numLogs];
	obstacles = new Obstacle[numObstacle];
	logs = new environmentalObjects[numLogs];
	frog = new userObject(this);
	width=initWidth;
	height=initHeight;	

	for (int i=0; i<numObstacle/4;i++){
	    position[i] =new Pair(0, height*10/12);   
	}
	for (int i = numObstacle/4; i < numObstacle/2; i++){
	    position[i] =new Pair(0, height*7/12);
	}
	for (int i = numObstacle/2; i < numObstacle*3/4; i++){
	    position[i] =new Pair(width-32, height*4/12);	
	}
	for (int i = numObstacle*3/4; i<numObstacle; i++){
	    position[i] =new Pair (width-32, height/12);
	}
	for (int i= 0; i<numLogs/4;i++){
	    positionlogs[i] =new Pair (0, 0);   
	}
	for (int i = numLogs/4; i < numLogs/2; i++){
	    positionlogs[i] =new Pair (0, 3*height/12);
	}
	for (int i = numLogs/2; i < numLogs*3/4; i++){
	    positionlogs[i] =new Pair (width-32, 6*height/12);	
	}
	for (int i = numLogs*3/4; i<numLogs; i++){
	    positionlogs[i] =new Pair (width-32, 9*height/12);
	}
	for(int i=0; i < numObstacle;i++){
	    if (i < numObstacle / 2)
		obstacles[i] = new Obstacle(position[i], 1, this);
	    if (i >= numObstacle/2)
		obstacles[i] = new Obstacle(position[i], 0, this);
	}
	
	for(int i=0; i < numLogs; i++){
	    if (i < numLogs / 2)
		logs[i]=new environmentalObjects(positionlogs[i], 1, this);
	    if (i >= numLogs/2)
		logs[i] = new environmentalObjects(positionlogs[i], 0, this);

	}
    }// for loops to make log and car objects and assign each of them positions  
    
    public void initialState(){
	userObject.position.x = width / 2;
	userObject.position.y = height - 32;
	for(int i=0;i<numObstacles;i++){
	    obstacles[i].position = position[i];
	}
	for(int i=0;i<numLogs;i++){
	    logs[i].position = positionlogs[i];
	}
	initialized=true;
    }//method to set userObject, cars and logs to their initial states. 

    
    public int contactLog(){
	for (int i = 0; i < numLogs; i++){ 
	    if(userObject.position.x <= (logs[i].position.x + logs[i].size.x - (userObject.size.x)/2) &&
	       userObject.position.x + (userObject.size.x / 2) >= logs[i].position.x  &&
	       userObject.position.y + (userObject.size.y / 2) >= logs[i].position.y + (userObject.size.y)/2&&
	       userObject.position.y <= (logs[i].position.y + logs[i].size.y - 25))
		return i;
	}
	return -1;
	
    }///This method determines whether or not the frog is at least half way on a log and returns the number of the log that the frog is on, and if it isn't on one, then it returns -1 
    public boolean hit(){
	for (int i = 0; i < numLogs; i++){ 
	    if(userObject.position.x <= (obstacles[i].position.x + obstacles[i].size.x-20) &&
	       userObject.position.x + (userObject.size.x / 2) >= obstacles[i].position.x &&
	       userObject.position.y + (userObject.size.y / 2) - 10  >= obstacles[i].position.y &&
	       userObject.position.y <= (obstacles[i].position.y + obstacles[i].size.y-45))
		return true;
	}
	return false;
	
    }// to check whether the frog has been hit by a car
	
    public void updateState(){
	if(!initialized)
	    initialState();
	else{
	    int theLog = contactLog();
	    if(theLog != -1)
		userObject.position.x = userObject.position.x+logs[theLog].speed;
	    for (int i = 0; i < numObstacles; i++){	
		obstacles[i].update(this);
		logs[i].update(this);
	    }
	    userObject.stillAlive(this);
	}
	if(userObject.position.y<=0)
	    youWin=true;
    }//updates the states of userObject,logs and obstacles.
    
    public void drawObstacles(Graphics g){
	for(int i=0;i<numObstacles;i++){
	    obstacles[i].draw(g,this);
	}	
    }
    
    public void drawLogs(Graphics g){
	for (int i = 0; i < numLogs; i ++){
	    logs[i].draw(g, this);
	}
    }
    
}

class DrawImage extends JPanel{
    static Image picture;
    static Toolkit kit;
    
    public DrawImage(){
	kit=Toolkit.getDefaultToolkit();
	picture=null;
    }
    
    public static void changePicture(String pictureName){
	try{
	    picture=ImageIO.read(new File(pictureName+".png"));
	}
	catch(Exception e){
	    System.out.println("Fishsticks! "+e+"\t"+pictureName);
	}
    }
}// to read image files 

class userObject{ //referring to the frog
    private static int lives;
    static Pair position;
    static boolean alive;
    static String facing;
    static Pair size;
    
    public userObject(World w){
	size=new Pair(32,32);
	facing="Up";
	alive=true;
	lives=3;
	position=new Pair(w.width/2,w.height-32);
	DrawImage.changePicture("Frog_"+facing+"_Sprite_0");	
    }
    
    public static void draw(Graphics g){
	DrawImage.changePicture("Frog_"+facing+"_Sprite_0"); // selects upwards facing picture initially
	g.drawImage(DrawImage.picture, position.x,position.y,size.x,size.y,null);
    }
    
    public static void move(char direction, World w){
	w.gameRunning.getGraphics().drawImage(DrawImage.picture,position.x,position.y,size.x,size.y,null);
	// moves frog according to w a s d keyboard inputs
	if(direction=='w'){
	    facing="Up";
	    DrawMove(w);	
	}
	if(direction=='a'){
	    facing="Left";
	    DrawMove(w);
	}
	if(direction=='s'){
	    facing="Down";
	    DrawMove(w);
	}
	if(direction=='d'){
	    facing="Right";
	    DrawMove(w);
	}	
	DrawImage.changePicture("Frog_"+facing+"_Sprite_0");
	w.gameRunning.getGraphics().drawImage(DrawImage.picture,position.x,position.y,size.x,size.y,null);
	stillAlive(w);
    }
    
    
    public static void DrawMove(World w){
	DrawImage.changePicture("Frog_"+facing+"_Sprite_1");
	w.gameRunning.getGraphics().drawImage(DrawImage.picture,position.x,position.y,size.x,size.y,null);
	try{Thread.sleep(3);}catch(InterruptedException e){}
	if(facing.equals("Left") || facing.equals("Right")){ 
	    for(int i=0;i<2;i++){
		if(i<1)
		    DrawImage.changePicture("Frog_"+facing+"_Sprite_2");
		if(i>=1)
		    DrawImage.changePicture("Frog_"+facing+"_Sprite_3"); //selects either right or left facing frog
		if(facing.equals("Right"))
		    position.x = position.x + 8;
		if(facing.equals("Left"))
		    position.x = position.x - 8; // moves frog eight units in desired direction
		w.gameRunning.getGraphics().drawImage(DrawImage.picture,position.x,position.y,size.x,size.y,null);
	    }
	}
	if(facing.equals("Up") || facing.equals("Down")){
	    for(int i=0;i<2;i++){
		if(i<1)
		    DrawImage.changePicture("Frog_"+facing+"_Sprite_2");
		if(i>=1)
		    DrawImage.changePicture("Frog_"+facing+"_Sprite_3"); // selects either up or down facing frog
		if(facing.equals("Up"))
		    position.y = position.y - 8;
		if(facing.equals("Down"))
		    position.y = position.y + 8; // moves frog eight units in desired direction
		w.gameRunning.getGraphics().drawImage(DrawImage.picture,position.x,position.y,size.x,size.y,null);
	    }
	}
    }
    
    public static void stillAlive(World w){
	
	
	if (((position.y >= 0  && (position.y + size.y) <= w.height/12) || 
	     (position.y >= w.height*3/12 && (position.y + size.y) <= w.height*4/12) || 
	     (position.y >= w.height*6/12 && (position.y + size.y) <= w.height*7/12) ||
             (position.y >= w.height*9/12 && (position.y + size.y) <= w.height*10/12)
	     ) && w.contactLog()==-1 && alive ) {
	    alive=false;
	    respawn(w); // if frog is in the water, die then resurrect
	    w.gameRunning.getGraphics().drawImage(DrawImage.picture,position.x,position.y,size.x,size.y,null);
	}
	if(w.hit()){
	    alive=false; // if car frog gets hit by car, it dies
	    respawn(w); // resurrects
	}

	if(!alive&&lives>0){
	    respawn(w); // resurrects frog to starting position
	    draw(w.gameRunning.getGraphics());
	} 
	if((!alive && lives <= 0) || lives < 0){ w.defeated=true;} // lose game
	
    }
    public static void respawn(World w){
	position.x = w.width/2;
	position.y = w.height-32;
	facing="Up";
	alive=true;
	lives=lives-1; // takes away one of three lives, puts frog back at start
	draw(w.gameRunning.getGraphics());
    }
}

class Pair{
    //All other classes sould use the get methods so these are private
    public int x;
    public int y;
    
    public Pair(int initX, int initY){
	x = initX;
	y = initY;
    }

    public boolean equals(Pair compare){
	boolean equal=false;
	if(x == compare.x && y == compare.y){
	    equal = true;
	}
	return equal;
    }
}

class Obstacle{
    int speed;
    Pair size;
    Pair position;
    Random rand;
    String facing;
    String color;
    
    public Obstacle(Pair p, int leftorright, World w){	
	rand = new Random();
	/*This if else determines the direction the car moves in and how fast*/
	if (leftorright == 1){
	    facing = "Right";
	    speed = 1 + rand.nextInt(5);
	}
	else {
	    facing = "Left";
	    speed = -1 - rand.nextInt(5);
	}
	/*this if else decides the color*/
	if(rand.nextInt(2) == 1)
	    color = "Blue";
	else
	    color = "Red";
	size = new Pair(64, 64);
	position = p;
	DrawImage.changePicture("Car_"+facing+"_Sprite_"+color);
    }
    
    public void update(World w){
    /*updates position of frog */
	position.x = position.x + speed;
	/*these if statements set the logs back to their original positions after they reach the opposite side*/
	if (speed < 0)
	    if (position.x <= 0)
		position.x = w.width;
	
	if (speed > 0)
	    if (position.x >= w.width)
		position.x = 0;
    }
    
    public void draw(Graphics g, World w){
    	// draws the car
	g.setColor(w.gameRunning.background);
	DrawImage.changePicture("Car_"+facing+"_Sprite_"+color);
	g.drawImage(DrawImage.picture, position.x, position.y, size.x, size.y, null);
    }	
}

class environmentalObjects{
    int speed;
    Pair size;
    int type;
    Pair position;
    Random rand;
    
    public environmentalObjects(Pair p, int leftorright,World w){
	rand = new Random();	
	/* this if else  sets the speed/direction of logs*/
	if (leftorright == 1){
	    speed = 1 + rand.nextInt(5);
	}
	else {
	    speed = -1 - rand.nextInt(5);
	}
	
	/* determines size and position of logs */
	size = new Pair(64, 64);
	position = p;
	DrawImage.changePicture("Log_Sprite");
    }
    
    
    public void update(World w){
    	//updates position of logs, resets them to their original position once they reach the opposite side of the screen
	position.x = position.x + speed;
	if (speed < 0)
	    if (position.x <= 0)
		position.x = w.width;
	
	if (speed > 0)
	    if (position.x >= w.width)
		position.x = 0;
    }
    
    public void draw(Graphics g, World w){
    	// draws log
	g.setColor(w.gameRunning.background);	
	DrawImage.changePicture("Log_Sprite");
	g.drawImage(DrawImage.picture, position.x, position.y, size.x, size.y, null);
	
    }
    
}


class Terrain{ 
    public static void build(Graphics g){
    
    // draws rows on screen, grass, roads, and rivers in that order
	for (int i = 0; i < 12; i++){
	    if (i % 3 == 2)
		g.setColor(Color.GREEN);
	    if (i % 3 == 1) 	
		g.setColor(Color.GRAY);
	    if(i % 3 == 0)
		g.setColor(Color.BLUE);	
	    
	    g.fillRect(0,i * World.height / 12, World.width, World.height / 12);
	    
	}
    }	
}
