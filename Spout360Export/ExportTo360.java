/* Utility class for rendering the 6 faces of a cube map and then to an equirectangular texture
Based on https://github.com/tracerstar/processing-360-video
*/

import java.lang.reflect.Method;
import java.nio.IntBuffer;

import processing.core.PApplet;
import processing.core.PShape;
import processing.opengl.PGL;
import processing.opengl.PShader;
import processing.core.PGraphics;

public class ExportTo360 {
	
	PShader cubemapShader;
	PShape myRect;

	IntBuffer fbo;
	IntBuffer rbo;
	IntBuffer envMapTextureID;

	int envMapSize = 1024; //width & height used for the cubemap texture
	float zClippingPlane = 2000.0f;
	Method  onDrawSceneMethod;
	PApplet p5;
	
	public ExportTo360(PApplet pApplet){
		this.p5 = pApplet;
		onDrawSceneMethod = getMethodRef( p5, "drawScene");
	}
	
	public void initCubeMap() {
		//change the domeSphere shape to a rectangle as we're not projecting on a dome
		myRect = p5.createShape(PApplet.RECT, -p5.width/2, -p5.height/2, p5.width, p5.height);
		myRect.setStroke(false);

		int txtMapSize = envMapSize;
		if (p5.g.pixelDensity == 2) {
			txtMapSize = envMapSize * 2;
		}

		PGL pgl = p5.beginPGL();

		envMapTextureID = IntBuffer.allocate(1);
		pgl.genTextures(1, envMapTextureID);
		pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, envMapTextureID.get(0));
		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_S, PGL.CLAMP_TO_EDGE);
		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_T, PGL.CLAMP_TO_EDGE);
		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_WRAP_R, PGL.CLAMP_TO_EDGE);
		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_MIN_FILTER, PGL.LINEAR);
		pgl.texParameteri(PGL.TEXTURE_CUBE_MAP, PGL.TEXTURE_MAG_FILTER, PGL.LINEAR);
		for (int i = PGL.TEXTURE_CUBE_MAP_POSITIVE_X; i < PGL.TEXTURE_CUBE_MAP_POSITIVE_X + 6; i++) {
			pgl.texImage2D(i, 0, PGL.RGBA8, txtMapSize, txtMapSize, 0, PGL.RGBA, PGL.UNSIGNED_BYTE, null);
		}

		// Init fbo, rbo
		fbo = IntBuffer.allocate(1);
		rbo = IntBuffer.allocate(1);
		pgl.genFramebuffers(1, fbo);
		pgl.bindFramebuffer(PGL.FRAMEBUFFER, fbo.get(0));
		pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, PGL.TEXTURE_CUBE_MAP_POSITIVE_X, envMapTextureID.get(0), 0);

		pgl.genRenderbuffers(1, rbo);
		pgl.bindRenderbuffer(PGL.RENDERBUFFER, rbo.get(0));
		pgl.renderbufferStorage(PGL.RENDERBUFFER, PGL.DEPTH_COMPONENT24, txtMapSize, txtMapSize);

		// Attach depth buffer to FBO
		pgl.framebufferRenderbuffer(PGL.FRAMEBUFFER, PGL.DEPTH_ATTACHMENT, PGL.RENDERBUFFER, rbo.get(0));    

		p5.endPGL();

		// Load cubemap shader.
		cubemapShader = p5.loadShader("data/equirectangular.glsl");
		cubemapShader.set("cubemap", 1);

	}

	public void drawCubeMap(PGraphics pg) {
		PGL pgl = p5.beginPGL();
		pgl.activeTexture(PGL.TEXTURE1);
		pgl.enable(PGL.TEXTURE_CUBE_MAP);  
		pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, envMapTextureID.get(0));     
		regenerateEnvMap(pgl);
		p5.endPGL();

		drawDomeMaster(pg);

		pgl.bindTexture(PGL.TEXTURE_CUBE_MAP, 0);
	}

public void drawDomeMaster(PGraphics pg) {
    pg.beginDraw();
    pg.camera();
    pg.ortho(pg.width/2, -pg.width/2, -pg.height/2, pg.height/2);

    pg.resetMatrix();
    pg.shader(cubemapShader);
    pg.shape(myRect);
    pg.resetShader();
    pg.endDraw();
  }

	// Called to regenerate the envmap
	void regenerateEnvMap(PGL pgl) {    
		// bind fbo
		pgl.bindFramebuffer(PGL.FRAMEBUFFER, fbo.get(0));

		// generate 6 views from origin(0, 0, 0)
		pgl.viewport(0, 0, envMapSize, envMapSize);    
		p5.perspective(90.0f * PApplet.DEG_TO_RAD, 1.0f, 1.0f, zClippingPlane);

		//note the <= to generate 6 faces, not 5 as per DomeProjection example
		for (int face = PGL.TEXTURE_CUBE_MAP_POSITIVE_X; face <= 
				PGL.TEXTURE_CUBE_MAP_NEGATIVE_Z; face++) {

			p5.resetMatrix();

			if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_X) {
				p5.camera(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
			} else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_X) {
				p5.camera(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
			} else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Y) {
				p5.camera(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f);  
			} else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Y) {
				p5.camera(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f);
			} else if (face == PGL.TEXTURE_CUBE_MAP_POSITIVE_Z) {
				p5.camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
			} else if (face == PGL.TEXTURE_CUBE_MAP_NEGATIVE_Z) {
				p5.camera(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);
			}

			p5.rotateY(PApplet.HALF_PI); //sets forward facing to center of screen for video output
//			translate(-zClippingPlane, -zClippingPlane, 0);//defaults coords to processing style with 0,0 in top left of visible space

			pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, face, envMapTextureID.get(0), 0);

			try {
				onDrawSceneMethod.invoke( p5);
			} 
			catch (Exception e) {
				PApplet.println("No draw method found");
			}
			//p5.drawScene(); // Draw objects in the scene
			p5.flush(); // Make sure that the geometry in the scene is pushed to the GPU    
			p5.noLights();  // Disabling lights to avoid adding many times
			pgl.framebufferTexture2D(PGL.FRAMEBUFFER, PGL.COLOR_ATTACHMENT0, face, 0, 0);
		}
	}
	
	private Method getMethodRef(Object obj, String methodName) {
		Method ret = null;
		try {
			ret = obj.getClass().getMethod(methodName);
		}
		catch (Exception e) {
			PApplet.println("No " + methodName + " method found");
		}
		return ret;
	}
}