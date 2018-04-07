package net.fe.editor;

import static java.util.Collections.emptyList;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearDepth;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;

import chu.engine.Game;
import chu.engine.anim.Renderer;
import net.fe.FEResources;

public class LevelEditor extends Game {
	
	private LevelEditorStage currentStage;
	private VoidToBooleanFunction handler;

	@Override
	public void init(int width, int height, String name) {
		super.init(width, height, name);
		currentStage = new LevelEditorStage(3, 20, "levels", "seven?");
	}
	
	@Override
	public void loop() {
		while(true) {
			final long time = System.nanoTime();
			glClear(GL_COLOR_BUFFER_BIT |
					GL_DEPTH_BUFFER_BIT |
					GL_STENCIL_BUFFER_BIT);
			glClearDepth(1.0f);
			getInput();
			glPushMatrix();
			{
				currentStage.beginStep(emptyList());
				currentStage.onStep();
				currentStage.processAddStack();
				currentStage.processRemoveStack();
				Renderer.getCamera().lookThrough();
				currentStage.render();
				currentStage.endStep();
			}
			glPopMatrix();
			Display.update();
			Display.sync(FEResources.getTargetFPS());
			timeDelta = System.nanoTime()-time;
			if(Display.isCloseRequested() && (handler == null || handler.eval()))
				break;
		}
		AL.destroy();
		Display.destroy();
	}
	
	public LevelEditorStage getStage() {
		return currentStage;
	}
	
	public void setCloseRequestedListener(VoidToBooleanFunction handler) {
		this.handler = handler;
	}

	public static interface VoidToBooleanFunction {
		public boolean eval();
	}
}
