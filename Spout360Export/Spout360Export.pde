/* Processing sketch demostrating how to send via Spout a 360 render
 Based on https://github.com/tracerstar/processing-360-video */
 
import java.nio.IntBuffer;

import spout.*;

  int numBoxes = 50;
  
  // Utility class for rendering to 360
  ExportTo360 exporter;
  //Spout object for sharing output texture
  Spout spout;
  // This is the buffer that will be sent using Spout
  PGraphics pg;

  public void settings() {
    size(2048, 1024, "processing.opengl.PGraphics3D");
    //for testing, you might want to work at a smaller resolution, but for export, the above is preferred
    //size(1024, 512, "processing.opengl.PGraphics3D");
  }


  public void setup() {
    background(0);

    pg = createGraphics(width, height, P3D);
    
    exporter= new ExportTo360(this);
    exporter.initCubeMap();
   
    spout = new Spout(this);
    spout.createSender("Spout360");
  }


  public void draw() {
    strokeWeight(0);
    noStroke();
    
    pushMatrix();
    exporter.drawCubeMap(pg);
    popMatrix();

    image(pg,0,0, width, height);
    surface.setTitle("FPS: " + (int) frameRate);
    
    // send it via spout. For some reason, sharing the default buffer either through sendTexture() or sendTexture(this.g) kills the frame rate 
    spout.sendTexture(pg);
  }


  /*
    Put your shapes/objects and lights here to be drawn to the screen
   */
  public void drawScene() {  
    background(40);
    pointLight(100, 0, 0,  0, 0, 20);              // under red light
    pointLight(51, 153, 153,  0, -50, 150);        // over teal light
    pointLight(124, 124, 124,  0, 0, 00);          // mid light gray light

    // draw spinning boxes around point (0,0,0)
    float radius = 100f;
    fill(250);
    for(int i=0;i < numBoxes;i++){
      float r = radius + radius/2 * sin(i * PI/numBoxes); // different radius for each box
      // animate movement using spherical coordinates 
      float theta = frameCount * 0.01f + i * 100f;
      float phi = frameCount * 0.01f + i * 550f;
      float x = r * sin(theta) * cos(phi);
      float y = r * sin(theta) * sin(phi);
      float z = r * cos(theta);
      
      pushMatrix();
      translate(x, y, z);
      rotateY(frameCount * 0.02 + i * PI/10);
      rotateZ(frameCount * 0.02 + i * PI/10);
      box(20);
      popMatrix();
    }
  }

 