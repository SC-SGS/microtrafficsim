package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL;


public class BlendMode {
	
	private boolean enabled;
	private boolean invalidated;
	
	private int eqRGB;
	private int eqA;
	
	private int sFactorRGB;
	private int sFactorA;
	
	private int dFactorRGB;
	private int dFactorA;
	
	
	public BlendMode() {
		this.enabled = false;
		
		// default values for factors and equations seem unspecified
		eqRGB = -1;
		eqA = -1;
		
		sFactorRGB = -1;
		sFactorA = -1;
		
		dFactorRGB = -1;
		dFactorA = -1;
	}

	
	public void enable(GL gl) {
		if (enabled) return;
		
		gl.glEnable(GL.GL_BLEND);
		enabled = true;
	}
	
	public void disable(GL gl) {
		if (!enabled) return;
		
		gl.glDisable(GL.GL_BLEND);
		enabled = false;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	
	public void setFactors(GL gl, int sFactor, int dFactor) {
		setFactors(gl, sFactor, dFactor, sFactor, dFactor);
	}
	
	public void setFactors(GL gl, int sFactorRGB, int dFactorRGB, int sFactorA, int dFactorA) {
		if (this.sFactorRGB == sFactorRGB && this.sFactorA == dFactorRGB
				&& this.dFactorRGB == sFactorA && this.dFactorA == dFactorA
				&& !this.invalidated)
			return;
		
		gl.glBlendFuncSeparate(sFactorRGB, dFactorRGB, sFactorA, dFactorA);
		
		this.sFactorRGB = sFactorRGB;
		this.sFactorA = sFactorA;
		this.dFactorRGB = dFactorRGB;
		this.dFactorA = dFactorA;
		this.invalidated = false;
	}
	
	public int getSrcFactorRGB() {
		return sFactorRGB;
	}
	
	public int getSrcFactorAlpha() {
		return sFactorA;
	}
	
	public int getDstFactorRGB() {
		return dFactorRGB;
	}
	
	public int getDstFactorAlpha() {
		return dFactorA;
	}
	
	
	public void setEquation(GL gl, int eq) {
		setEquation(gl, eq, eq);
	}
	
	public void setEquation(GL gl, int eqRGB, int eqA) {
		if (this.eqRGB == eqRGB && this.eqA == eqA && !this.invalidated)
			return;
		
		gl.glBlendEquationSeparate(eqRGB, eqA);
		
		this.eqRGB = eqRGB;
		this.eqA = eqA;
		this.invalidated = false;
	}
	
	public int getEquationRGB() {
		return eqRGB;
	}
	
	public int getEquationAlpha() {
		return eqA;
	}


	public void invalidate() {
		this.invalidated = true;
	}
}
