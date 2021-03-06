package microtrafficsim.core.vis.opengl;

import com.jogamp.opengl.GL4;
import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * OpenGL data-type wrapper.
 *
 * @author Maximilian Luz
 */
public class DataType {
    public final int typeId;
    public final int size;

    /**
     * Constructs a new {@code DataType} instance for the given type-id and size.
     *
     * @param typeId the OpenGL id of the type.
     * @param size   the size of the type, with greater than 1 indicating an array.
     */
    public DataType(int typeId, int size) {
        this.typeId = typeId;
        this.size   = size;
    }

    /**
     * Returns the type-id as string.
     *
     * @param id the type-id for which the string should be returned.
     * @return the name of the type associated with the given id.
     */
    public static String typeIdToString(int id) {
        switch (id) {
        case GL4.GL_FLOAT:                                     return "GL_FLOAT";
        case GL4.GL_FLOAT_VEC2:                                return "GL_FLOAT_VEC2";
        case GL4.GL_FLOAT_VEC3:                                return "GL_FLOAT_VEC3";
        case GL4.GL_FLOAT_VEC4:                                return "GL_FLOAT_VEC4";
        case GL4.GL_FLOAT_MAT2:                                return "GL_FLOAT_MAT2";
        case GL4.GL_FLOAT_MAT3:                                return "GL_FLOAT_MAT3";
        case GL4.GL_FLOAT_MAT4:                                return "GL_FLOAT_MAT4";
        case GL4.GL_FLOAT_MAT2x3:                              return "GL_FLOAT_MAT2x3";
        case GL4.GL_FLOAT_MAT2x4:                              return "GL_FLOAT_MAT2x4";
        case GL4.GL_FLOAT_MAT3x2:                              return "GL_FLOAT_MAT3x2";
        case GL4.GL_FLOAT_MAT3x4:                              return "GL_FLOAT_MAT3x4";
        case GL4.GL_FLOAT_MAT4x2:                              return "GL_FLOAT_MAT4x2";
        case GL4.GL_FLOAT_MAT4x3:                              return "GL_FLOAT_MAT4x3";

        case GL4.GL_DOUBLE:                                    return "GL_DOUBLE";
        case GL4.GL_DOUBLE_VEC2:                               return "GL_DOUBLE_VEC2";
        case GL4.GL_DOUBLE_VEC3:                               return "GL_DOUBLE_VEC3";
        case GL4.GL_DOUBLE_VEC4:                               return "GL_DOUBLE_VEC4";
        case GL4.GL_DOUBLE_MAT2:                               return "GL_DOUBLE_MAT2";
        case GL4.GL_DOUBLE_MAT3:                               return "GL_DOUBLE_MAT3";
        case GL4.GL_DOUBLE_MAT4:                               return "GL_DOUBLE_MAT4";
        case GL4.GL_DOUBLE_MAT2x3:                             return "GL_DOUBLE_MAT2x3";
        case GL4.GL_DOUBLE_MAT2x4:                             return "GL_DOUBLE_MAT2x4";
        case GL4.GL_DOUBLE_MAT3x2:                             return "GL_DOUBLE_MAT3x2";
        case GL4.GL_DOUBLE_MAT3x4:                             return "GL_DOUBLE_MAT3x4";
        case GL4.GL_DOUBLE_MAT4x2:                             return "GL_DOUBLE_MAT4x2";
        case GL4.GL_DOUBLE_MAT4x3:                             return "GL_DOUBLE_MAT4x3";

        case GL4.GL_INT:                                       return "GL_INT";
        case GL4.GL_INT_VEC2:                                  return "GL_INT_VEC2";
        case GL4.GL_INT_VEC3:                                  return "GL_INT_VEC3";
        case GL4.GL_INT_VEC4:                                  return "GL_INT_VEC4";

        case GL4.GL_UNSIGNED_INT:                              return "GL_UNSIGNED_INT";
        case GL4.GL_UNSIGNED_INT_VEC2:                         return "GL_UNSIGNED_INT_VEC2";
        case GL4.GL_UNSIGNED_INT_VEC3:                         return "GL_UNSIGNED_INT_VEC3";
        case GL4.GL_UNSIGNED_INT_VEC4:                         return "GL_UNSIGNED_INT_VEC4";

        case GL4.GL_BOOL:                                      return "GL_BOOL";
        case GL4.GL_BOOL_VEC2:                                 return "GL_BOOL_VEC2";
        case GL4.GL_BOOL_VEC3:                                 return "GL_BOOL_VEC3";
        case GL4.GL_BOOL_VEC4:                                 return "GL_BOOL_VEC4";

        case GL4.GL_SAMPLER_1D:                                return "GL_SAMPLER_1D";
        case GL4.GL_SAMPLER_2D:                                return "GL_SAMPLER_2D";
        case GL4.GL_SAMPLER_3D:                                return "GL_SAMPLER_3D";
        case GL4.GL_SAMPLER_CUBE:                              return "GL_SAMPLER_CUBE";
        case GL4.GL_SAMPLER_CUBE_SHADOW:                       return "GL_SAMPLER_CUBE_SHADOW";
        case GL4.GL_SAMPLER_1D_SHADOW:                         return "GL_SAMPLER_1D_SHADOW";
        case GL4.GL_SAMPLER_2D_SHADOW:                         return "GL_SAMPLER_2D_SHADOW";
        case GL4.GL_SAMPLER_1D_ARRAY:                          return "GL_SAMPLER_1D_ARRAY";
        case GL4.GL_SAMPLER_2D_ARRAY:                          return "GL_SAMPLER_2D_ARRAY";
        case GL4.GL_SAMPLER_1D_ARRAY_SHADOW:                   return "GL_SAMPLER_1D_ARRAY_SHADOW";
        case GL4.GL_SAMPLER_2D_ARRAY_SHADOW:                   return "GL_SAMPLER_2D_ARRAY_SHADOW";
        case GL4.GL_SAMPLER_2D_MULTISAMPLE:                    return "GL_SAMPLER_2D_MULTISAMPLE";
        case GL4.GL_SAMPLER_2D_MULTISAMPLE_ARRAY:              return "GL_SAMPLER_2D_MULTISAMPLE_ARRAY";
        case GL4.GL_SAMPLER_BUFFER:                            return "GL_SAMPLER_BUFFER";
        case GL4.GL_SAMPLER_2D_RECT:                           return "GL_SAMPLER_2D_RECT";
        case GL4.GL_SAMPLER_2D_RECT_SHADOW:                    return "GL_SAMPLER_2D_RECT_SHADOW";

        case GL4.GL_INT_SAMPLER_1D:                            return "GL_INT_SAMPLER_1D";
        case GL4.GL_INT_SAMPLER_2D:                            return "GL_INT_SAMPLER_2D";
        case GL4.GL_INT_SAMPLER_3D:                            return "GL_INT_SAMPLER_3D";
        case GL4.GL_INT_SAMPLER_CUBE:                          return "GL_INT_SAMPLER_CUBE";
        case GL4.GL_INT_SAMPLER_1D_ARRAY:                      return "GL_INT_SAMPLER_1D_ARRAY";
        case GL4.GL_INT_SAMPLER_2D_ARRAY:                      return "GL_INT_SAMPLER_2D_ARRAY";
        case GL4.GL_INT_SAMPLER_2D_MULTISAMPLE:                return "GL_INT_SAMPLER_2D_MULTISAMPLE";
        case GL4.GL_INT_SAMPLER_2D_MULTISAMPLE_ARRAY:          return "GL_INT_SAMPLER_2D_MULTISAMPLE_ARRAY";
        case GL4.GL_INT_SAMPLER_BUFFER:                        return "GL_INT_SAMPLER_BUFFER";
        case GL4.GL_INT_SAMPLER_2D_RECT:                       return "GL_INT_SAMPLER_2D_RECT";

        case GL4.GL_UNSIGNED_INT_SAMPLER_1D:                   return "GL_UNSIGNED_INT_SAMPLER_1D";
        case GL4.GL_UNSIGNED_INT_SAMPLER_2D:                   return "GL_UNSIGNED_INT_SAMPLER_2D";
        case GL4.GL_UNSIGNED_INT_SAMPLER_3D:                   return "GL_UNSIGNED_INT_SAMPLER_3D";
        case GL4.GL_UNSIGNED_INT_SAMPLER_CUBE:                 return "GL_UNSIGNED_INT_SAMPLER_CUBE";
        case GL4.GL_UNSIGNED_INT_SAMPLER_1D_ARRAY:             return "GL_UNSIGNED_INT_SAMPLER_1D_ARRAY";
        case GL4.GL_UNSIGNED_INT_SAMPLER_2D_ARRAY:             return "GL_UNSIGNED_INT_SAMPLER_2D_ARRAY";
        case GL4.GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE:       return "GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE";
        case GL4.GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY: return "GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY";
        case GL4.GL_UNSIGNED_INT_SAMPLER_BUFFER:               return "GL_UNSIGNED_INT_SAMPLER_BUFFER";
        case GL4.GL_UNSIGNED_INT_SAMPLER_2D_RECT:              return "GL_UNSIGNED_INT_SAMPLER_2D_RECT";

        case GL4.GL_IMAGE_1D:                                  return "GL_IMAGE_1D";
        case GL4.GL_IMAGE_2D:                                  return "GL_IMAGE_2D";
        case GL4.GL_IMAGE_3D:                                  return "GL_IMAGE_3D";
        case GL4.GL_IMAGE_2D_RECT:                             return "GL_IMAGE_2D_RECT";
        case GL4.GL_IMAGE_CUBE:                                return "GL_IMAGE_CUBE";
        case GL4.GL_IMAGE_BUFFER:                              return "GL_IMAGE_BUFFER";
        case GL4.GL_IMAGE_1D_ARRAY:                            return "GL_IMAGE_1D_ARRAY";
        case GL4.GL_IMAGE_2D_ARRAY:                            return "GL_IMAGE_2D_ARRAY";
        case GL4.GL_IMAGE_2D_MULTISAMPLE:                      return "GL_IMAGE_2D_MULTISAMPLE";
        case GL4.GL_IMAGE_2D_MULTISAMPLE_ARRAY:                return "GL_IMAGE_2D_MULTISAMPLE_ARRAY";

        case GL4.GL_INT_IMAGE_1D:                              return "GL_INT_IMAGE_1D";
        case GL4.GL_INT_IMAGE_2D:                              return "GL_INT_IMAGE_2D";
        case GL4.GL_INT_IMAGE_3D:                              return "GL_INT_IMAGE_3D";
        case GL4.GL_INT_IMAGE_2D_RECT:                         return "GL_INT_IMAGE_2D_RECT";
        case GL4.GL_INT_IMAGE_CUBE:                            return "GL_INT_IMAGE_CUBE";
        case GL4.GL_INT_IMAGE_BUFFER:                          return "GL_INT_IMAGE_BUFFER";
        case GL4.GL_INT_IMAGE_1D_ARRAY:                        return "GL_INT_IMAGE_1D_ARRAY";
        case GL4.GL_INT_IMAGE_2D_ARRAY:                        return "GL_INT_IMAGE_2D_ARRAY";
        case GL4.GL_INT_IMAGE_2D_MULTISAMPLE:                  return "GL_INT_IMAGE_2D_MULTISAMPLE";
        case GL4.GL_INT_IMAGE_2D_MULTISAMPLE_ARRAY:            return "GL_INT_IMAGE_2D_MULTISAMPLE_ARRAY";

        case GL4.GL_UNSIGNED_INT_IMAGE_1D:                     return "GL_UNSINGED_INT_IMAGE_1D";
        case GL4.GL_UNSIGNED_INT_IMAGE_2D:                     return "GL_UNSINGED_INT_IMAGE_2D";
        case GL4.GL_UNSIGNED_INT_IMAGE_3D:                     return "GL_UNSINGED_INT_IMAGE_3D";
        case GL4.GL_UNSIGNED_INT_IMAGE_2D_RECT:                return "GL_UNSINGED_INT_IMAGE_2D_RECT";
        case GL4.GL_UNSIGNED_INT_IMAGE_CUBE:                   return "GL_UNSINGED_INT_IMAGE_CUBE";
        case GL4.GL_UNSIGNED_INT_IMAGE_BUFFER:                 return "GL_UNSINGED_INT_IMAGE_BUFFER";
        case GL4.GL_UNSIGNED_INT_IMAGE_1D_ARRAY:               return "GL_UNSINGED_INT_IMAGE_1D_ARRAY";
        case GL4.GL_UNSIGNED_INT_IMAGE_2D_ARRAY:               return "GL_UNSINGED_INT_IMAGE_2D_ARRAY";
        case GL4.GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE:         return "GL_UNSINGED_INT_IMAGE_2D_MULTISAMPLE";
        case GL4.GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY:   return "GL_UNSINGED_INT_IMAGE_2D_MULTISAMPLE_ARRAY";

        default:                                               return "UNKNOWN";
        }
    }

    /**
     * Tests if this is a single-precision floating-point type (includes vectors and matrices).
     *
     * @return {@code true} if this is a single-precision floating-point type.
     */
    public boolean isSinglePrecisionFloat() {
        switch (typeId) {
        case GL4.GL_FLOAT:        return true;
        case GL4.GL_FLOAT_VEC2:   return true;
        case GL4.GL_FLOAT_VEC3:   return true;
        case GL4.GL_FLOAT_VEC4:   return true;
        case GL4.GL_FLOAT_MAT2:   return true;
        case GL4.GL_FLOAT_MAT3:   return true;
        case GL4.GL_FLOAT_MAT4:   return true;
        case GL4.GL_FLOAT_MAT2x3: return true;
        case GL4.GL_FLOAT_MAT2x4: return true;
        case GL4.GL_FLOAT_MAT3x2: return true;
        case GL4.GL_FLOAT_MAT3x4: return true;
        case GL4.GL_FLOAT_MAT4x2: return true;
        case GL4.GL_FLOAT_MAT4x3: return true;

        default:                  return false;
        }
    }

    /**
     * Tests if this is a double-precision floating-point type (includes vectors and matrices).
     *
     * @return {@code true} if this is a double-precision floating-point type.
     */
    public boolean isDoublePrecisionFloat() {
        switch (typeId) {
        case GL4.GL_DOUBLE:        return true;
        case GL4.GL_DOUBLE_VEC2:   return true;
        case GL4.GL_DOUBLE_VEC3:   return true;
        case GL4.GL_DOUBLE_VEC4:   return true;
        case GL4.GL_DOUBLE_MAT2:   return true;
        case GL4.GL_DOUBLE_MAT3:   return true;
        case GL4.GL_DOUBLE_MAT4:   return true;
        case GL4.GL_DOUBLE_MAT2x3: return true;
        case GL4.GL_DOUBLE_MAT2x4: return true;
        case GL4.GL_DOUBLE_MAT3x2: return true;
        case GL4.GL_DOUBLE_MAT3x4: return true;
        case GL4.GL_DOUBLE_MAT4x2: return true;
        case GL4.GL_DOUBLE_MAT4x3: return true;

        default:                   return false;
        }
    }

    /**
     * Tests if this is a signed integer type (includes vectors).
     *
     * @return {@code true} if this is a signed integer type.
     */
    public boolean isSignedInteger() {
        switch (typeId) {
        case GL4.GL_INT:      return true;
        case GL4.GL_INT_VEC2: return true;
        case GL4.GL_INT_VEC3: return true;
        case GL4.GL_INT_VEC4: return true;

        default:              return false;
        }
    }

    /**
     * Tests if this is a unsigned integer type (includes vectors).
     *
     * @return {@code true} if this is a unsigned integer type.
     */
    public boolean isUnsignedInteger() {
        switch (typeId) {
        case GL4.GL_UNSIGNED_INT:      return true;
        case GL4.GL_UNSIGNED_INT_VEC2: return true;
        case GL4.GL_UNSIGNED_INT_VEC3: return true;
        case GL4.GL_UNSIGNED_INT_VEC4: return true;

        default:                       return false;
        }
    }

    /**
     * Tests if this is a boolean type (includes vectors).
     *
     * @return {@code true} if this is a unsigned integer type.
     */
    public boolean isBoolean() {
        switch (typeId) {
        case GL4.GL_BOOL:      return true;
        case GL4.GL_BOOL_VEC2: return true;
        case GL4.GL_BOOL_VEC3: return true;
        case GL4.GL_BOOL_VEC4: return true;

        default:               return false;
        }
    }

    /**
     * Tests if this is a floating-point sampler type.
     *
     * @return {@code true} if this is a floating-point sampler type.
     */
    public boolean isFloatSampler() {
        switch (typeId) {
        case GL4.GL_SAMPLER_1D:                   return true;
        case GL4.GL_SAMPLER_2D:                   return true;
        case GL4.GL_SAMPLER_3D:                   return true;
        case GL4.GL_SAMPLER_CUBE:                 return true;
        case GL4.GL_SAMPLER_CUBE_SHADOW:          return true;
        case GL4.GL_SAMPLER_1D_SHADOW:            return true;
        case GL4.GL_SAMPLER_2D_SHADOW:            return true;
        case GL4.GL_SAMPLER_1D_ARRAY:             return true;
        case GL4.GL_SAMPLER_2D_ARRAY:             return true;
        case GL4.GL_SAMPLER_1D_ARRAY_SHADOW:      return true;
        case GL4.GL_SAMPLER_2D_ARRAY_SHADOW:      return true;
        case GL4.GL_SAMPLER_2D_MULTISAMPLE:       return true;
        case GL4.GL_SAMPLER_2D_MULTISAMPLE_ARRAY: return true;
        case GL4.GL_SAMPLER_BUFFER:               return true;
        case GL4.GL_SAMPLER_2D_RECT:              return true;
        case GL4.GL_SAMPLER_2D_RECT_SHADOW:       return true;

        default:                                  return false;
        }
    }

    /**
     * Tests if this is a signed integer sampler type.
     *
     * @return {@code true} if this is a signed integer sampler type.
     */
    public boolean isSignedIntegerSampler() {
        switch (typeId) {
        case GL4.GL_INT_SAMPLER_1D:                   return true;
        case GL4.GL_INT_SAMPLER_2D:                   return true;
        case GL4.GL_INT_SAMPLER_3D:                   return true;
        case GL4.GL_INT_SAMPLER_CUBE:                 return true;
        case GL4.GL_INT_SAMPLER_1D_ARRAY:             return true;
        case GL4.GL_INT_SAMPLER_2D_ARRAY:             return true;
        case GL4.GL_INT_SAMPLER_2D_MULTISAMPLE:       return true;
        case GL4.GL_INT_SAMPLER_2D_MULTISAMPLE_ARRAY: return true;
        case GL4.GL_INT_SAMPLER_BUFFER:               return true;
        case GL4.GL_INT_SAMPLER_2D_RECT:              return true;

        default:                                      return false;
        }
    }

    /**
     * Tests if this is a unsigned integer sampler type.
     *
     * @return {@code true} if this is a unsigned integer sampler type.
     */
    public boolean isUnsignedIntegerSampler() {
        switch (typeId) {
        case GL4.GL_UNSIGNED_INT_SAMPLER_1D:                   return true;
        case GL4.GL_UNSIGNED_INT_SAMPLER_2D:                   return true;
        case GL4.GL_UNSIGNED_INT_SAMPLER_3D:                   return true;
        case GL4.GL_UNSIGNED_INT_SAMPLER_CUBE:                 return true;
        case GL4.GL_UNSIGNED_INT_SAMPLER_1D_ARRAY:             return true;
        case GL4.GL_UNSIGNED_INT_SAMPLER_2D_ARRAY:             return true;
        case GL4.GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE:       return true;
        case GL4.GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY: return true;
        case GL4.GL_UNSIGNED_INT_SAMPLER_BUFFER:               return true;
        case GL4.GL_UNSIGNED_INT_SAMPLER_2D_RECT:              return true;

        default:                                               return false;
        }
    }

    /**
     * Tests if this is a (single-precision) floating-point image type.
     *
     * @return {@code true} if this is a (single-precision) floating-point image type.
     */
    public boolean isFloatImage() {
        switch (typeId) {
        case GL4.GL_IMAGE_1D:                   return true;
        case GL4.GL_IMAGE_2D:                   return true;
        case GL4.GL_IMAGE_3D:                   return true;
        case GL4.GL_IMAGE_2D_RECT:              return true;
        case GL4.GL_IMAGE_CUBE:                 return true;
        case GL4.GL_IMAGE_BUFFER:               return true;
        case GL4.GL_IMAGE_1D_ARRAY:             return true;
        case GL4.GL_IMAGE_2D_ARRAY:             return true;
        case GL4.GL_IMAGE_2D_MULTISAMPLE:       return true;
        case GL4.GL_IMAGE_2D_MULTISAMPLE_ARRAY: return true;

        default:                                return false;
        }
    }

    /**
     * Tests if this is a signed integer image type.
     *
     * @return {@code true} if this is a signed integer image type.
     */
    public boolean isSignedIntegerImage() {
        switch (typeId) {
        case GL4.GL_INT_IMAGE_1D:                   return true;
        case GL4.GL_INT_IMAGE_2D:                   return true;
        case GL4.GL_INT_IMAGE_3D:                   return true;
        case GL4.GL_INT_IMAGE_2D_RECT:              return true;
        case GL4.GL_INT_IMAGE_CUBE:                 return true;
        case GL4.GL_INT_IMAGE_BUFFER:               return true;
        case GL4.GL_INT_IMAGE_1D_ARRAY:             return true;
        case GL4.GL_INT_IMAGE_2D_ARRAY:             return true;
        case GL4.GL_INT_IMAGE_2D_MULTISAMPLE:       return true;
        case GL4.GL_INT_IMAGE_2D_MULTISAMPLE_ARRAY: return true;

        default: return false;
        }
    }

    /**
     * Tests if this is a unsigned integer image type.
     *
     * @return {@code true} if this is a unsigned integer image type.
     */
    public boolean isUnsignedIntegerImage() {
        switch (typeId) {
        case GL4.GL_UNSIGNED_INT_IMAGE_1D:                   return true;
        case GL4.GL_UNSIGNED_INT_IMAGE_2D:                   return true;
        case GL4.GL_UNSIGNED_INT_IMAGE_3D:                   return true;
        case GL4.GL_UNSIGNED_INT_IMAGE_2D_RECT:              return true;
        case GL4.GL_UNSIGNED_INT_IMAGE_CUBE:                 return true;
        case GL4.GL_UNSIGNED_INT_IMAGE_BUFFER:               return true;
        case GL4.GL_UNSIGNED_INT_IMAGE_1D_ARRAY:             return true;
        case GL4.GL_UNSIGNED_INT_IMAGE_2D_ARRAY:             return true;
        case GL4.GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE:       return true;
        case GL4.GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY: return true;

        default:                                             return false;
        }
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(typeId).add(size).getHash();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DataType)) return false;

        DataType other = (DataType) obj;
        return this.typeId == other.typeId && this.size == other.size;
    }

    @Override
    public String toString() {
        return this.getClass() + " {" + getTypeString() + "}";
    }

    /**
     * Return a {@code String} describing this type.
     *
     * @return the description of this type, consisting of type-name and size/length.
     */
    public String getTypeString() {
        return typeIdToString(typeId) + "[" + size + "]";
    }
}
