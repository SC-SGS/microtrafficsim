package microtrafficsim.core.vis.context.state;

import com.jogamp.opengl.GL;


/**
 * OpenGL blend-mode state.
 *
 * @author Maximilian Luz
 */
public class BlendMode {

    private boolean enabled;
    private boolean invalidated;

    private int eqRGB;
    private int eqA;

    private int sFactorRGB;
    private int sFactorA;

    private int dFactorRGB;
    private int dFactorA;


    /**
     * Constructs a new {@code BlendMode} state.
     */
    public BlendMode() {
        this.enabled = false;

        // default values for factors and equations seem unspecified
        eqRGB = -1;
        eqA   = -1;

        sFactorRGB = -1;
        sFactorA   = -1;

        dFactorRGB = -1;
        dFactorA   = -1;
    }


    /**
     * Enables the OpenGL blend-mode.
     *
     * @param gl the {@code GL}-Object of the OpenGL context.
     */
    public void enable(GL gl) {
        if (enabled) return;

        gl.glEnable(GL.GL_BLEND);
        enabled = true;
    }

    /**
     * Disables the OpenGL blend-mode.
     *
     * @param gl the {@code GL}-Object of the OpenGL context.
     */
    public void disable(GL gl) {
        if (!enabled) return;

        gl.glDisable(GL.GL_BLEND);
        enabled = false;
    }

    /**
     * Checks if the blend-mode is enabled.
     *
     * @return {@code true} if the blend-mode is enabled, {@code false} otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }


    /**
     * Set the blend factors (a.k.a. OpenGL blend function).
     *
     * @param gl      the {@code GL}-Object of the OpenGL context.
     * @param sFactor the source factor.
     * @param dFactor the destination factor.
     */
    public void setFactors(GL gl, int sFactor, int dFactor) {
        setFactors(gl, sFactor, dFactor, sFactor, dFactor);
    }

    /**
     * Set the blend factors (a.k.a. OpenGL blend function).
     *
     * @param gl         the {@code GL}-Object of the OpenGL context.
     * @param sFactorRGB the RGB source factor.
     * @param dFactorRGB the RGB destination factor.
     * @param sFactorA   the alpha source factor.
     * @param dFactorA   the alpha destination factor.
     */
    public void setFactors(GL gl, int sFactorRGB, int dFactorRGB, int sFactorA, int dFactorA) {
        if (this.sFactorRGB == sFactorRGB && this.sFactorA == dFactorRGB
                && this.dFactorRGB == sFactorA && this.dFactorA == dFactorA
                && !this.invalidated) {
            return;
        }

        gl.glBlendFuncSeparate(sFactorRGB, dFactorRGB, sFactorA, dFactorA);

        this.sFactorRGB  = sFactorRGB;
        this.sFactorA    = sFactorA;
        this.dFactorRGB  = dFactorRGB;
        this.dFactorA    = dFactorA;
        this.invalidated = false;
    }

    /**
     * Returns the RGB source factor of the blend equation.
     *
     * @return the RGB source factor of the blend equation.
     */
    public int getSrcFactorRGB() {
        return sFactorRGB;
    }

    /**
     * Returns the alpha source factor of the blend equation.
     *
     * @return the alpha source factor of the blend equation.
     */
    public int getSrcFactorAlpha() {
        return sFactorA;
    }

    /**
     * Returns the RGB destination factor of the blend equation.
     *
     * @return the RGB destination factor of the blend equation.
     */
    public int getDstFactorRGB() {
        return dFactorRGB;
    }

    /**
     * Returns the alpha destination factor of the blend equation.
     *
     * @return the alpha destination factor of the blend equation.
     */
    public int getDstFactorAlpha() {
        return dFactorA;
    }


    /**
     * Set the blend-equation.
     *
     * @param gl the {@code GL}-Object of the OpenGL context.
     * @param eq the new blend-equation.
     */
    public void setEquation(GL gl, int eq) {
        setEquation(gl, eq, eq);
    }

    /**
     * Set the blend-equation.
     *
     * @param gl    the {@code GL}-Object of the OpenGL context.
     * @param eqRGB the new RGB blend-equation.
     * @param eqA   the new alpha blend-equation.
     */
    public void setEquation(GL gl, int eqRGB, int eqA) {
        if (this.eqRGB == eqRGB && this.eqA == eqA && !this.invalidated) return;

        gl.glBlendEquationSeparate(eqRGB, eqA);

        this.eqRGB       = eqRGB;
        this.eqA         = eqA;
        this.invalidated = false;
    }

    /**
     * Returns the RGB blend equation.
     *
     * @return the RGB blend equation.
     */
    public int getEquationRGB() {
        return eqRGB;
    }

    /**
     * Returns the alpha blend equation.
     *
     * @return the alpha blend equation.
     */
    public int getEquationAlpha() {
        return eqA;
    }


    /**
     * Invalidate the cache used in this state.
     */
    public void invalidate() {
        this.invalidated = true;
    }
}
