package microtrafficsim.core.vis.opengl;

import com.jogamp.opengl.GL2;


public class DataTypes {
    private DataTypes() {}

    public static final DataType BYTE   = new DataType(GL2.GL_BYTE, 1);
    public static final DataType BYTE_2 = new DataType(GL2.GL_BYTE, 2);
    public static final DataType BYTE_3 = new DataType(GL2.GL_BYTE, 3);
    public static final DataType BYTE_4 = new DataType(GL2.GL_BYTE, 4);

    public static final DataType UNSIGNED_BYTE   = new DataType(GL2.GL_UNSIGNED_BYTE, 1);
    public static final DataType UNSIGNED_BYTE_2 = new DataType(GL2.GL_UNSIGNED_BYTE, 2);
    public static final DataType UNSIGNED_BYTE_3 = new DataType(GL2.GL_UNSIGNED_BYTE, 3);
    public static final DataType UNSIGNED_BYTE_4 = new DataType(GL2.GL_UNSIGNED_BYTE, 4);

    public static final DataType INT   = new DataType(GL2.GL_INT, 1);
    public static final DataType INT_2 = new DataType(GL2.GL_INT, 2);
    public static final DataType INT_3 = new DataType(GL2.GL_INT, 3);
    public static final DataType INT_4 = new DataType(GL2.GL_INT, 4);

    public static final DataType FLOAT   = new DataType(GL2.GL_FLOAT, 1);
    public static final DataType FLOAT_2 = new DataType(GL2.GL_FLOAT, 2);
    public static final DataType FLOAT_3 = new DataType(GL2.GL_FLOAT, 3);
    public static final DataType FLOAT_4 = new DataType(GL2.GL_FLOAT, 4);

    public static final DataType INT_VEC2 = new DataType(GL2.GL_INT_VEC2, 1);
    public static final DataType INT_VEC3 = new DataType(GL2.GL_INT_VEC3, 1);
    public static final DataType INT_VEC4 = new DataType(GL2.GL_INT_VEC4, 1);

    public static final DataType FLOAT_VEC2 = new DataType(GL2.GL_FLOAT_VEC2, 1);
    public static final DataType FLOAT_VEC3 = new DataType(GL2.GL_FLOAT_VEC3, 1);
    public static final DataType FLOAT_VEC4 = new DataType(GL2.GL_FLOAT_VEC4, 1);

    public static final DataType FLOAT_MAT2 = new DataType(GL2.GL_FLOAT_MAT2, 1);
    public static final DataType FLOAT_MAT3 = new DataType(GL2.GL_FLOAT_MAT3, 1);
    public static final DataType FLOAT_MAT4 = new DataType(GL2.GL_FLOAT_MAT4, 1);

    public static final DataType SAMPLER_2D = new DataType(GL2.GL_SAMPLER_2D, 1);
}
