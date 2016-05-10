package microtrafficsim.math;

import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * A row-major 4x4 float Matrix.
 * 
 * @author Maximilian Luz
 */
public class Mat4f {
	private float[] raw = new float[16];
	
	
	private Mat4f() {}
	
	public Mat4f(float a11, float a12, float a13, float a14,
			float a21, float a22, float a23, float a24,
			float a31, float a32, float a33, float a34,
			float a41, float a42, float a43, float a44) {
		
		raw[0] = a11;
		raw[1] = a12;
		raw[2] = a13;
		raw[3] = a14;
		
		raw[4] = a21;
		raw[5] = a22;
		raw[6] = a23;
		raw[7] = a24;
		
		raw[8] = a31;
		raw[9] = a32;
		raw[10] = a33;
		raw[11] = a34;
		
		raw[12] = a41;
		raw[13] = a42;
		raw[14] = a43;
		raw[15] = a44;
	}

	public Mat4f(float[] matrix) {
		System.arraycopy(matrix, 0, this.raw, 0, 16);
	}

	public Mat4f(Mat4f matrix) {
		this(matrix.raw);
	}
	
	
	public Mat4f set(Mat4f other) {
		System.arraycopy(other.raw, 0, this.raw, 0, 16);
		return this;
	}
	
	public Mat4f set(float[] raw) {
		System.arraycopy(raw, 0, this.raw, 0, 16);
		return this;
	}
	
	public float[] getRaw() {
		return raw;
	}
	
	
	// TODO: test!!
	public float det3() {
		return raw[0] * (raw[5] * raw[10] - raw[6] * raw[9])
				- raw[1] * (raw[4] * raw[10] - raw[6] * raw[8])
				+ raw[2] * (raw[4] * raw[9] - raw[5] * raw[8]);
	}
	
	// TODO: test!!
	public float det() {
		// 2x2 determinants enumerated from left to right
		float d1 = raw[8] * raw[13] - raw[ 9] * raw[12];
		float d2 = raw[8] * raw[14] - raw[10] * raw[12];
		float d3 = raw[8] * raw[15] - raw[11] * raw[12];
		float d4 = raw[9] * raw[14] - raw[10] * raw[13];
		float d5 = raw[9] * raw[15] - raw[11] * raw[13];
		float d6 = raw[10] * raw[15] - raw[11] * raw[14];
		
		return raw[0] * (raw[5] * d6 - raw[6] * d5 + raw[7] * d4)
				- raw[1] * (raw[4] * d6 - raw[6] * d3 + raw[7] * d2)
				+ raw[2] * (raw[4] * d5 - raw[5] * d3 + raw[7] * d1)
				- raw[3] * (raw[4] * d4 - raw[5] * d2 + raw[6] * d1);
	}

	public boolean isAffine() {
		return raw[12] == 0.f && raw[13] == 0.f && raw[14] == 0.f && raw[15] == 1.f;
	}
	

	public Mat4f makeIdentity() {
		System.arraycopy(Mat4f.identity, 0, this.raw, 0, 16);
		return this;
	}

	public Mat4f makeOrtho(float left, float right, float top, float bottom,
			float near, float far) {
		
		this.raw[0] = 2.0f / (right - left);
		this.raw[1] = 0;
		this.raw[2] = 0;
		this.raw[3] = -(right + left) / (right - left);

		this.raw[4] = 0;
		this.raw[5] = 2.0f / (top - bottom);
		this.raw[6] = 0;
		this.raw[7] = -(top + bottom) / (top - bottom);

		this.raw[8] = 0;
		this.raw[9] = 0;
		this.raw[10] = -2.0f / (far - near);
		this.raw[11] = -(far + near) / (far - near);

		this.raw[12] = 0;
		this.raw[13] = 0;
		this.raw[14] = 0;
		this.raw[15] = 1;

		return this;
	}
	
	public Mat4f makePerspective(float fovy, float aspectRatio, float zNear, float zFar) {
		float f = (float) (1/Math.tan(Math.toRadians(fovy/2)));
		
		this.raw[1] = 0;
		this.raw[2] = 0;
		this.raw[3] = 0;
		this.raw[4] = 0;
		this.raw[6] = 0;
		this.raw[7] = 0;
		this.raw[8] = 0;
		this.raw[9] = 0;
		this.raw[12] = 0;
		this.raw[13] = 0;
		this.raw[15] = 0;
		
		this.raw[0] = f/aspectRatio;
		this.raw[5] = f;
		this.raw[10] = (zFar+zNear)/(zNear-zFar);
		this.raw[11] = (2*zNear*zFar)/(zNear-zFar);
		this.raw[14] = -1;
		
		return this;
	}

	public Mat4f makeLookAt(Vec3f eye, Vec3f center, Vec3f up) {
		return makeLookInDirection(eye, Vec3f.sub(center, eye), up);
	}

	public Mat4f makeLookInDirection(Vec3f eye, Vec3f direction, Vec3f up) {
		float abs = (float) Math.sqrt(direction.x*direction.x + direction.y*direction.y + direction.z*direction.z);
		float fwdX = direction.x / abs;
		float fwdY = direction.y / abs;
		float fwdZ = direction.z / abs;

		float sideX = up.z*fwdY - up.y*fwdZ;
		float sideY = up.x*fwdZ - up.z*fwdX;
		float sideZ = up.y*fwdX - up.x*fwdY;

		abs = (float) Math.sqrt(sideX*sideX + sideY*sideY + sideZ*sideZ);
		sideX /= abs;
		sideY /= abs;
		sideZ /= abs;

		float upX = sideY*fwdZ - sideZ*fwdY;
		float upY = sideZ*fwdX - sideX*fwdZ;
		float upZ = sideX*fwdY - sideY*fwdX;

		this.raw[0] = sideX;
		this.raw[1] = sideY;
		this.raw[2] = sideZ;
		this.raw[3] = 0;
		
		this.raw[4] = upX;
		this.raw[5] = upY;
		this.raw[6] = upZ;
		this.raw[7] = 0;
		
		this.raw[8] = -fwdX;
		this.raw[9] = -fwdY;
		this.raw[10] = -fwdZ;
		this.raw[11] = 0;
		
		this.raw[12] = 0;
		this.raw[13] = 0;
		this.raw[14] = 0;
		this.raw[15] = 1;

		return this.translate(-eye.x, -eye.y, -eye.z);
	}
	
	
	public Mat4f add(Mat4f other) {
		raw[0] += other.raw[0];
		raw[1] += other.raw[1];
		raw[2] += other.raw[2];
		raw[3] += other.raw[3];
		
		raw[4] += other.raw[4];
		raw[5] += other.raw[5];
		raw[6] += other.raw[6];
		raw[7] += other.raw[7];
		
		raw[8] += other.raw[8];
		raw[9] += other.raw[9];
		raw[10] += other.raw[10];
		raw[11] += other.raw[11];
		
		raw[12] += other.raw[12];
		raw[13] += other.raw[13];
		raw[14] += other.raw[14];
		raw[15] += other.raw[15];
		
		return this;
	} 
	
	public Mat4f sub(Mat4f other) {
		raw[0] -= other.raw[0];
		raw[1] -= other.raw[1];
		raw[2] -= other.raw[2];
		raw[3] -= other.raw[3];
		
		raw[4] -= other.raw[4];
		raw[5] -= other.raw[5];
		raw[6] -= other.raw[6];
		raw[7] -= other.raw[7];
		
		raw[8] -= other.raw[8];
		raw[9] -= other.raw[9];
		raw[10] -= other.raw[10];
		raw[11] -= other.raw[11];
		
		raw[12] -= other.raw[12];
		raw[13] -= other.raw[13];
		raw[14] -= other.raw[14];
		raw[15] -= other.raw[15];
		
		return this;
	}
	
	public Mat4f mul(Mat4f other) {
		this.raw = new float[] {
				this.raw[0]*other.raw[0] + this.raw[1]*other.raw[4] + this.raw[2]*other.raw[8] + this.raw[3]*other.raw[12],
				this.raw[0]*other.raw[1] + this.raw[1]*other.raw[5] + this.raw[2]*other.raw[9] + this.raw[3]*other.raw[13],
				this.raw[0]*other.raw[2] + this.raw[1]*other.raw[6] + this.raw[2]*other.raw[10] + this.raw[3]*other.raw[14],
				this.raw[0]*other.raw[3] + this.raw[1]*other.raw[7] + this.raw[2]*other.raw[11] + this.raw[3]*other.raw[15],
				this.raw[4]*other.raw[0] + this.raw[5]*other.raw[4] + this.raw[6]*other.raw[8] + this.raw[7]*other.raw[12],
				this.raw[4]*other.raw[1] + this.raw[5]*other.raw[5] + this.raw[6]*other.raw[9] + this.raw[7]*other.raw[13],
				this.raw[4]*other.raw[2] + this.raw[5]*other.raw[6] + this.raw[6]*other.raw[10] + this.raw[7]*other.raw[14],
				this.raw[4]*other.raw[3] + this.raw[5]*other.raw[7] + this.raw[6]*other.raw[11] + this.raw[7]*other.raw[15],
				this.raw[8]*other.raw[0] + this.raw[9]*other.raw[4] + this.raw[10]*other.raw[8] + this.raw[11]*other.raw[12],
				this.raw[8]*other.raw[1] + this.raw[9]*other.raw[5] + this.raw[10]*other.raw[9] + this.raw[11]*other.raw[13],
				this.raw[8]*other.raw[2] + this.raw[9]*other.raw[6] + this.raw[10]*other.raw[10] + this.raw[11]*other.raw[14],
				this.raw[8]*other.raw[3] + this.raw[9]*other.raw[7] + this.raw[10]*other.raw[11] + this.raw[11]*other.raw[15],
				this.raw[12]*other.raw[0] + this.raw[13]*other.raw[4] + this.raw[14]*other.raw[8] + this.raw[15]*other.raw[12],
				this.raw[12]*other.raw[1] + this.raw[13]*other.raw[5] + this.raw[14]*other.raw[9] + this.raw[15]*other.raw[13],
				this.raw[12]*other.raw[2] + this.raw[13]*other.raw[6] + this.raw[14]*other.raw[10] + this.raw[15]*other.raw[14],
				this.raw[12]*other.raw[3] + this.raw[13]*other.raw[7] + this.raw[14]*other.raw[11] + this.raw[15]*other.raw[15]
		};

		return this;
	}
	
	public Vec4f mul(Vec4f v) {
		return new Vec4f(
				this.raw[0] * v.x + this.raw[1] * v.y + this.raw[2] * v.z + this.raw[3] * v.z,
				this.raw[4] * v.x + this.raw[5] * v.y + this.raw[6] * v.z + this.raw[7] * v.z,
				this.raw[8] * v.x + this.raw[9] * v.y + this.raw[10] * v.z + this.raw[11] * v.z,
				this.raw[12] * v.x + this.raw[13] * v.y + this.raw[14] * v.z + this.raw[15] * v.z
		);
	}
	
	public Mat4f mul(float scalar) {
		raw[0] *= scalar;
		raw[1] *= scalar;
		raw[2] *= scalar;
		raw[3] *= scalar;
		raw[4] *= scalar;
		raw[5] *= scalar;
		raw[6] *= scalar;
		raw[7] *= scalar;
		raw[8] *= scalar;
		raw[9] *= scalar;
		raw[10] *= scalar;
		raw[11] *= scalar;
		raw[12] *= scalar;
		raw[13] *= scalar;
		raw[14] *= scalar;
		raw[15] *= scalar;
		
		return this;
	}
	
	// M * T
	public Mat4f translate(Vec3f vec) {
		this.raw[3] = this.raw[0]*vec.x + this.raw[1]*vec.y + this.raw[2]*vec.z + this.raw[3];
		this.raw[7] = this.raw[4]*vec.x + this.raw[5]*vec.y + this.raw[6]*vec.z + this.raw[7];
		this.raw[11] = this.raw[8]*vec.x + this.raw[9]*vec.y + this.raw[10]*vec.z + this.raw[11];
		this.raw[15] = this.raw[12]*vec.x + this.raw[13]*vec.y + this.raw[14]*vec.z + this.raw[15];
		
		return this;
	}
	
	// M * T
	public Mat4f translate(float x, float y, float z) {
		this.raw[3] = this.raw[0]*x + this.raw[1]*y + this.raw[2]*z + this.raw[3];
		this.raw[7] = this.raw[4]*x + this.raw[5]*y + this.raw[6]*z + this.raw[7];
		this.raw[11] = this.raw[8]*x + this.raw[9]*y + this.raw[10]*z + this.raw[11];
		this.raw[15] = this.raw[12]*x + this.raw[13]*y + this.raw[14]*z + this.raw[15];
		
		return this;
	}
	
	public Mat4f scale(Vec3f vec) {
		this.raw[0] *= vec.x;
		this.raw[1] *= vec.y;
		this.raw[2] *= vec.z;
		this.raw[4] *= vec.x;
		this.raw[5] *= vec.y;
		this.raw[6] *= vec.z;
		this.raw[8] *= vec.x;
		this.raw[9] *= vec.y;
		this.raw[10] *= vec.z;
		this.raw[12] *= vec.x;
		this.raw[13] *= vec.y;
		this.raw[14] *= vec.z;
		
		return this;
	}

	public Mat4f scale(float sx, float sy, float sz) {
		this.raw[0] *= sx;
		this.raw[1] *= sy;
		this.raw[2] *= sz;
		this.raw[4] *= sx;
		this.raw[5] *= sy;
		this.raw[6] *= sz;
		this.raw[8] *= sx;
		this.raw[9] *= sy;
		this.raw[10] *= sz;
		this.raw[12] *= sx;
		this.raw[13] *= sy;
		this.raw[14] *= sz;
		
		return this;
	}
	
	// M * R
	public Mat4f rotate(Vec3f axis, float angle) {
		float s = (float) Math.sin(angle);
		float c = (float) Math.cos(angle);
		float omc = 1.f - c;
		
		float xx = axis.x * axis.x;
		float xy = axis.x * axis.y;
		float xz = axis.x * axis.z;
		float yy = axis.y * axis.y;
		float yz = axis.y * axis.z;
		float zz = axis.z * axis.z;
		float xs = axis.x * s;
		float ys = axis.y * s;
		float zs = axis.z * s;
		
		float r0 = xx * omc + c;
		float r1 = xy * omc - zs;
		float r2 = xz * omc + ys;
		float r4 = xy * omc + zs;
		float r5 = yy * omc + c;
		float r6 = yz * omc - xs;
		float r8 = xz * omc - ys;
		float r9 = yz * omc + xs;
		float r10 = zz * omc + c;
		
		float[] tmp = {
				this.raw[0] * r0 + this.raw[1] * r4 + this.raw[2] * r8,
				this.raw[0] * r1 + this.raw[1] * r5 + this.raw[2] * r9,
				this.raw[0] * r2 + this.raw[1] * r6 + this.raw[2] * r10,
				this.raw[3],
				this.raw[4] * r0 + this.raw[5] * r4 + this.raw[6] * r8,
				this.raw[4] * r1 + this.raw[5] * r5 + this.raw[6] * r9,
				this.raw[4] * r2 + this.raw[5] * r6 + this.raw[6] * r10,
				this.raw[7],
				this.raw[8] * r0 + this.raw[9] * r4 + this.raw[10] * r8,
				this.raw[8] * r1 + this.raw[9] * r5 + this.raw[10] * r9,
				this.raw[8] * r2 + this.raw[9] * r6 + this.raw[10] * r10,
				this.raw[11],
				this.raw[12] * r0 + this.raw[13] * r4 + this.raw[14] * r8,
				this.raw[12] * r1 + this.raw[13] * r5 + this.raw[14] * r9,
				this.raw[12] * r2 + this.raw[13] * r6 + this.raw[14] * r10,
				this.raw[15]
		};
		
		this.raw = tmp;
		return this;
	}
	
	// M * R
	public Mat4f rotate(float x, float y, float z, float angle) {
		float s = (float) Math.sin(Math.toRadians(angle));
		float c = (float) Math.cos(Math.toRadians(angle));
		float omc = 1.f - c;
		
		float xx = x * x;
		float xy = x * y;
		float xz = x * z;
		float yy = y * y;
		float yz = y * z;
		float zz = z * z;
		float xs = x * s;
		float ys = y * s;
		float zs = z * s;
		
		float r0 = xx * omc + c;
		float r1 = xy * omc - zs;
		float r2 = xz * omc + ys;
		float r4 = xy * omc + zs;
		float r5 = yy * omc + c;
		float r6 = yz * omc - xs;
		float r8 = xz * omc - ys;
		float r9 = yz * omc + xs;
		float r10 = zz * omc + c;
		
		float[] tmp = {
				this.raw[0] * r0 + this.raw[1] * r4 + this.raw[2] * r8,
				this.raw[0] * r1 + this.raw[1] * r5 + this.raw[2] * r9,
				this.raw[0] * r2 + this.raw[1] * r6 + this.raw[2] * r10,
				this.raw[3],
				this.raw[4] * r0 + this.raw[5] * r4 + this.raw[6] * r8,
				this.raw[4] * r1 + this.raw[5] * r5 + this.raw[6] * r9,
				this.raw[4] * r2 + this.raw[5] * r6 + this.raw[6] * r10,
				this.raw[7],
				this.raw[8] * r0 + this.raw[9] * r4 + this.raw[10] * r8,
				this.raw[8] * r1 + this.raw[9] * r5 + this.raw[10] * r9,
				this.raw[8] * r2 + this.raw[9] * r6 + this.raw[10] * r10,
				this.raw[11],
				this.raw[12] * r0 + this.raw[13] * r4 + this.raw[14] * r8,
				this.raw[12] * r1 + this.raw[13] * r5 + this.raw[14] * r9,
				this.raw[12] * r2 + this.raw[13] * r6 + this.raw[14] * r10,
				this.raw[15]
		};
		
		this.raw = tmp;
		return this;
	}
	
	// M * R
	public Mat4f rotateX(float angle) {
		float s = (float) Math.sin(Math.toRadians(angle));
		float c = (float) Math.cos(Math.toRadians(angle));
		
		float[] tmp = {
				this.raw[0],
				this.raw[1] * c + this.raw[2] * s,
				-this.raw[1] * s + this.raw[2] * c,
				this.raw[3],
				this.raw[4],
				this.raw[5] * c + this.raw[6] * s,
				-this.raw[5] * s + this.raw[6] * c,
				this.raw[7],
				this.raw[8],
				this.raw[9] * c + this.raw[10] * s,
				-this.raw[9] * s + this.raw[10] * c,
				this.raw[11],
				this.raw[12],
				this.raw[13] * c + this.raw[14] * s,
				-this.raw[13] * s + this.raw[14] * c,
				this.raw[15]
		};
		
		this.raw = tmp;
		return this;
	}
	
	// M * R
	public Mat4f rotateY(float angle) {
		float s = (float) Math.sin(Math.toRadians(angle));
		float c = (float) Math.cos(Math.toRadians(angle));

		float[] tmp = {
				this.raw[0] * c - this.raw[2] * s,
				this.raw[1],
				this.raw[0] * s + this.raw[2] * c,
				this.raw[3],
				this.raw[4] * c - this.raw[6] * s,
				this.raw[5],
				this.raw[4] * s + this.raw[6] * c,
				this.raw[7],
				this.raw[8] * c - this.raw[10] * s,
				this.raw[9],
				this.raw[8] * s + this.raw[10] * c,
				this.raw[11],
				this.raw[12] * c - this.raw[14] * s,
				this.raw[13],
				this.raw[12] * s + this.raw[14] * c,
				this.raw[15]
		};
		
		this.raw = tmp;
		return this;
	}
	
	// M * R
	public Mat4f rotateZ(float angle) {
		float s = (float) Math.sin(Math.toRadians(angle));
		float c = (float) Math.cos(Math.toRadians(angle));
		
		float[] tmp = {
				this.raw[0] * c + this.raw[1] * s,
				-this.raw[0] * s + this.raw[1] * c,
				this.raw[2],
				this.raw[3],
				this.raw[4] * c + this.raw[5] * s,
				-this.raw[4] * s + this.raw[5] * c,
				this.raw[6],
				this.raw[7],
				this.raw[8] * c + this.raw[9] * s,
				-this.raw[8] * s + this.raw[9] * c,
				this.raw[10],
				this.raw[11],
				this.raw[12] * c + this.raw[13] * s,
				-this.raw[12] * s + this.raw[13] * c,
				this.raw[14],
				this.raw[15]
		};
		
		this.raw = tmp;
		return this;
	}
	
	
	public Mat4f transpose() {
		float swp;
		
		swp = this.raw[1];
		this.raw[1] = this.raw[4];
		this.raw[4] = swp;
		
		swp = this.raw[2];
		this.raw[2] = this.raw[8];
		this.raw[8] = swp;
		
		swp = this.raw[6];
		this.raw[6] = this.raw[9];
		this.raw[9] = swp;
		
		swp = this.raw[3];
		this.raw[3] = this.raw[12];
		this.raw[12] = swp;
		
		swp = this.raw[7];
		this.raw[7] = this.raw[13];
		this.raw[13] = swp;
		
		swp = this.raw[11];
		this.raw[11] = this.raw[14];
		this.raw[14] = swp;
		
		return this;
	}
	
	public Mat4f transpose3() {
		float swp;
		
		swp = this.raw[1];
		this.raw[1] = this.raw[4];
		this.raw[4] = swp;
		
		swp = this.raw[2];
		this.raw[2] = this.raw[8];
		this.raw[8] = swp;
		
		swp = this.raw[6];
		this.raw[6] = this.raw[9];
		this.raw[9] = swp;
		
		return this;
	}
	
	public Mat4f invert() {
		if (raw[12] == 0.f && raw[13] == 0.f && raw[14] == 0.f && raw[15] == 1.f)
			return invertAffine();
		else
			return invertGeneral();
	}
	
	public Mat4f invertAffine() {
		float dot1t = this.raw[0] * this.raw[3] + this.raw[4] * this.raw[7] + this.raw[8] * this.raw[11];
		float dot2t = this.raw[1] * this.raw[3] + this.raw[5] * this.raw[7] + this.raw[9] * this.raw[11];
		float dot3t = this.raw[2] * this.raw[3] + this.raw[6] * this.raw[7] + this.raw[10] * this.raw[11];
		
		float[] tmp = {
				this.raw[0], this.raw[4], this.raw[8], -dot1t,
				this.raw[1], this.raw[5], this.raw[9], -dot2t,
				this.raw[2], this.raw[6], this.raw[10], -dot3t,
				0.f, 0.f, 0.f, 1.f
		};
		
		this.raw = tmp;
		return this;
	}
	
	public Mat4f invertGeneral() {
		float d10d15 = this.raw[10] * this.raw[15] - this.raw[11] * this.raw[14];
		float d06d15 = this.raw[6]  * this.raw[15] - this.raw[7]  * this.raw[14];
		float d06d11 = this.raw[6]  * this.raw[11] - this.raw[7]  * this.raw[10];
	    float d02d15 = this.raw[2]  * this.raw[15] - this.raw[3]  * this.raw[14];
	    float d02d11 = this.raw[2]  * this.raw[11] - this.raw[3]  * this.raw[10];
	    float d02d07 = this.raw[2]  * this.raw[7]  - this.raw[3]  * this.raw[6];
	    float d09d15 = this.raw[9]  * this.raw[15] - this.raw[11] * this.raw[13];
	    float d05d15 = this.raw[5]  * this.raw[15] - this.raw[7]  * this.raw[13];
	    float d05d11 = this.raw[5]  * this.raw[11] - this.raw[7]  * this.raw[9];
	    float d01d15 = this.raw[1]  * this.raw[15] - this.raw[3]  * this.raw[13];
	    float d01d11 = this.raw[1]  * this.raw[11] - this.raw[3]  * this.raw[9];
	    float d01d07 = this.raw[1]  * this.raw[7]  - this.raw[3]  * this.raw[5];
	    float d09d14 = this.raw[9]  * this.raw[14] - this.raw[10] * this.raw[13];
	    float d05d14 = this.raw[5]  * this.raw[14] - this.raw[6]  * this.raw[13];
	    float d05d10 = this.raw[5]  * this.raw[10] - this.raw[6]  * this.raw[9];
	    float d01d14 = this.raw[1]  * this.raw[14] - this.raw[2]  * this.raw[13];
	    float d01d10 = this.raw[1]  * this.raw[10] - this.raw[2]  * this.raw[9];
	    float d01d06 = this.raw[1]  * this.raw[6]  - this.raw[2]  * this.raw[5];
	    
	    float[] tmp = {
	    		 this.raw[5] * d10d15 - this.raw[9] * d06d15 + this.raw[13] * d06d11,
	    		-this.raw[1] * d10d15 + this.raw[9] * d02d15 - this.raw[13] * d02d11,
	    		 this.raw[1] * d06d15 - this.raw[5] * d02d15 + this.raw[13] * d02d07,
	    		-this.raw[1] * d06d11 + this.raw[5] * d02d11 - this.raw[9]  * d02d07,
	    		-this.raw[4] * d10d15 + this.raw[8] * d06d15 - this.raw[12] * d06d11,
	    		 this.raw[0] * d10d15 - this.raw[8] * d02d15 + this.raw[12] * d02d11,
	    		-this.raw[0] * d06d15 + this.raw[4] * d02d15 - this.raw[12] * d02d07,
	    		 this.raw[0] * d06d11 - this.raw[4] * d02d11 + this.raw[8]  * d02d07,
	    		 this.raw[4] * d09d15 - this.raw[8] * d05d15 + this.raw[12] * d05d11,
	    		-this.raw[0] * d09d15 + this.raw[8] * d01d15 - this.raw[12] * d01d11,
	    		 this.raw[0] * d05d15 - this.raw[4] * d01d15 + this.raw[12] * d01d07,
	    		-this.raw[0] * d05d11 + this.raw[4] * d01d11 - this.raw[8]  * d01d07,
	    		-this.raw[4] * d09d14 + this.raw[8] * d05d14 - this.raw[12] * d05d10,
	    		 this.raw[0] * d09d14 - this.raw[8] * d01d14 + this.raw[12] * d01d10,
	    		-this.raw[0] * d05d14 + this.raw[4] * d01d14 - this.raw[12] * d01d06,
	    		 this.raw[0] * d05d10 - this.raw[4] * d01d10 + this.raw[8]  * d01d06
	    };

	    float det = this.raw[0] * tmp[0]
	    		+ this.raw[1] * tmp[4]
	    		+ this.raw[2] * tmp[8]
	    		+ this.raw[3] * tmp[12];	
	    
	    if (det == 0) return null;
	    
	    det = 1.0f / det;
	    for (int i = 0; i < 16; i++)
	    	tmp[i] *= det;
		
		return this;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Mat4f))
			return false;
		
		Mat4f other = (Mat4f) obj;
		
		return this.raw[0] == other.raw[0]
				&& this.raw[1] == other.raw[1]
				&& this.raw[2] == other.raw[2]
				&& this.raw[3] == other.raw[3]
				&& this.raw[4] == other.raw[4]
				&& this.raw[5] == other.raw[5]
				&& this.raw[6] == other.raw[6]
				&& this.raw[7] == other.raw[7]
				&& this.raw[8] == other.raw[8]
				&& this.raw[9] == other.raw[9]
				&& this.raw[10] == other.raw[10]
				&& this.raw[11] == other.raw[11]
				&& this.raw[12] == other.raw[12]
				&& this.raw[13] == other.raw[13]
				&& this.raw[14] == other.raw[14]
				&& this.raw[15] == other.raw[15];
	}
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(this.raw[0])
				.add(this.raw[1])
				.add(this.raw[2])
				.add(this.raw[3])
				.add(this.raw[4])
				.add(this.raw[5])
				.add(this.raw[6])
				.add(this.raw[7])
				.add(this.raw[8])
				.add(this.raw[9])
				.add(this.raw[10])
				.add(this.raw[11])
				.add(this.raw[12])
				.add(this.raw[13])
				.add(this.raw[14])
				.add(this.raw[15])
				.getHash();
	}
	
	
	private static final float[] identity = {
			1.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 1.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 1.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f,
	};
	
	public static Mat4f identity() {
		return new Mat4f(identity);
	}
	
	public static Mat4f ortho(float left, float right, float top, float bottom,
			float near, float far) {
		return new Mat4f().makeOrtho(left, right, top, bottom, near, far);
	}
	
	public static Mat4f perspecive(float fovy, float aspectRatio, float zNear, float zFar) {
		return new Mat4f().makePerspective(fovy, aspectRatio, zNear, zFar);
	}

	public static Mat4f lookAt(Vec3f eye, Vec3f center, Vec3f up) {
		return new Mat4f().makeLookAt(eye, center, up);
	}
	
	public static Mat4f lookInDirection(Vec3f eye, Vec3f direction, Vec3f up) {
		return new Mat4f().makeLookInDirection(eye, direction, up);
	}
	
	
	public static Mat4f add(Mat4f a, Mat4f b) {
		return new Mat4f(
				a.raw[0] + b.raw[0],
				a.raw[1] + b.raw[1],
				a.raw[2] + b.raw[2],
				a.raw[3] + b.raw[3],
				a.raw[4] + b.raw[4],
				a.raw[5] + b.raw[5],
				a.raw[6] + b.raw[6],
				a.raw[7] + b.raw[7],
				a.raw[8] + b.raw[8],
				a.raw[9] + b.raw[9],
				a.raw[10] + b.raw[10],
				a.raw[11] + b.raw[11],
				a.raw[12] + b.raw[12],
				a.raw[13] + b.raw[13],
				a.raw[14] + b.raw[14],
				a.raw[15] + b.raw[15]
		);
	}
	
	public static Mat4f sub(Mat4f a, Mat4f b) {
		return new Mat4f(
				a.raw[0] - b.raw[0],
				a.raw[1] - b.raw[1],
				a.raw[2] - b.raw[2],
				a.raw[3] - b.raw[3],
				a.raw[4] - b.raw[4],
				a.raw[5] - b.raw[5],
				a.raw[6] - b.raw[6],
				a.raw[7] - b.raw[7],
				a.raw[8] - b.raw[8],
				a.raw[9] - b.raw[9],
				a.raw[10] - b.raw[10],
				a.raw[11] - b.raw[11],
				a.raw[12] - b.raw[12],
				a.raw[13] - b.raw[13],
				a.raw[14] - b.raw[14],
				a.raw[15] - b.raw[15]
		);
	}

	public static Mat4f mul(Mat4f a, Mat4f b) {
		return new Mat4f(
				a.raw[0]*b.raw[0] + a.raw[1]*b.raw[4] + a.raw[2]*b.raw[8] + a.raw[3]*b.raw[12],
				a.raw[0]*b.raw[1] + a.raw[1]*b.raw[5] + a.raw[2]*b.raw[9] + a.raw[3]*b.raw[13],
				a.raw[0]*b.raw[2] + a.raw[1]*b.raw[6] + a.raw[2]*b.raw[10] + a.raw[3]*b.raw[14],
				a.raw[0]*b.raw[3] + a.raw[1]*b.raw[7] + a.raw[2]*b.raw[11] + a.raw[3]*b.raw[15],
				a.raw[4]*b.raw[0] + a.raw[5]*b.raw[4] + a.raw[6]*b.raw[8] + a.raw[7]*b.raw[12],
				a.raw[4]*b.raw[1] + a.raw[5]*b.raw[5] + a.raw[6]*b.raw[9] + a.raw[7]*b.raw[13],
				a.raw[4]*b.raw[2] + a.raw[5]*b.raw[6] + a.raw[6]*b.raw[10] + a.raw[7]*b.raw[14],
				a.raw[4]*b.raw[3] + a.raw[5]*b.raw[7] + a.raw[6]*b.raw[11] + a.raw[7]*b.raw[15],
				a.raw[8]*b.raw[0] + a.raw[9]*b.raw[4] + a.raw[10]*b.raw[8] + a.raw[11]*b.raw[12],
				a.raw[8]*b.raw[1] + a.raw[9]*b.raw[5] + a.raw[10]*b.raw[9] + a.raw[11]*b.raw[13],
				a.raw[8]*b.raw[2] + a.raw[9]*b.raw[6] + a.raw[10]*b.raw[10] + a.raw[11]*b.raw[14],
				a.raw[8]*b.raw[3] + a.raw[9]*b.raw[7] + a.raw[10]*b.raw[11] + a.raw[11]*b.raw[15],
				a.raw[12]*b.raw[0] + a.raw[13]*b.raw[4] + a.raw[14]*b.raw[8] + a.raw[15]*b.raw[12],
				a.raw[12]*b.raw[1] + a.raw[13]*b.raw[5] + a.raw[14]*b.raw[9] + a.raw[15]*b.raw[13],
				a.raw[12]*b.raw[2] + a.raw[13]*b.raw[6] + a.raw[14]*b.raw[10] + a.raw[15]*b.raw[14],
				a.raw[12]*b.raw[3] + a.raw[13]*b.raw[7] + a.raw[14]*b.raw[11] + a.raw[15]*b.raw[15]
		);
	}
	
	public static Vec4f mulInPlace(Mat4f m, Vec4f v) {
		float x = m.raw[0] * v.x + m.raw[1] * v.y + m.raw[2] * v.z + m.raw[3] * v.z;
		float y = m.raw[4] * v.x + m.raw[5] * v.y + m.raw[6] * v.z + m.raw[7] * v.z;
		float z = m.raw[8] * v.x + m.raw[9] * v.y + m.raw[10] * v.z + m.raw[11] * v.z;
		float w = m.raw[12] * v.x + m.raw[13] * v.y + m.raw[14] * v.z + m.raw[15] * v.z;
		
		v.x = x;
		v.y = y;
		v.z = z;
		v.w = w;
		
		return v;
	}
	
	public static Mat4f mul(Mat4f a, float scalar) {
		return new Mat4f(
				a.raw[0] * scalar,
				a.raw[1] * scalar,
				a.raw[2] * scalar,
				a.raw[3] * scalar,
				a.raw[4] * scalar,
				a.raw[5] * scalar,
				a.raw[6] * scalar,
				a.raw[7] * scalar,
				a.raw[8] * scalar,
				a.raw[9] * scalar,
				a.raw[10] * scalar,
				a.raw[11] * scalar,
				a.raw[12] * scalar,
				a.raw[13] * scalar,
				a.raw[14] * scalar,
				a.raw[15] * scalar
		);
	}
	
	
	// m * T
	public static Mat4f translate(Mat4f m, Vec3f v) {
		return new Mat4f(m).translate(v);
	}
	
	// m * T
	public static Mat4f translate(Mat4f m, float x, float y, float z) {
		return new Mat4f(m).translate(x, y, z);
	}
	
	public static Mat4f scale(Mat4f m, Vec3f v) {
		return new Mat4f(
				m.raw[0] * v.x,
				m.raw[1] * v.y,
				m.raw[2] * v.z,
				m.raw[3],
				m.raw[4] * v.x,
				m.raw[5] * v.y,
				m.raw[6] * v.z,
				m.raw[7],
				m.raw[8] * v.x,
				m.raw[9] * v.y,
				m.raw[10] * v.z,
				m.raw[11],
				m.raw[12] * v.x,
				m.raw[13] * v.y,
				m.raw[14] * v.z,
				m.raw[15]
		);
	}
	
	public static Mat4f scale(Mat4f m, float x, float y, float z) {
		return new Mat4f(
				m.raw[0] * x,
				m.raw[1] * y,
				m.raw[2] * z,
				m.raw[3],
				m.raw[4] * x,
				m.raw[5] * y,
				m.raw[6] * z,
				m.raw[7],
				m.raw[8] * x,
				m.raw[9] * y,
				m.raw[10] * z,
				m.raw[11],
				m.raw[12] * x,
				m.raw[13] * y,
				m.raw[14] * z,
				m.raw[15]
		);
	}

	// m * R
	public static Mat4f rotate(Mat4f m, Vec3f axis, float angle) {
		float s = (float) Math.sin(Math.toRadians(angle));
		float c = (float) Math.cos(Math.toRadians(angle));
		float omc = 1.f - c;
		
		float xx = axis.x * axis.x;
		float xy = axis.x * axis.y;
		float xz = axis.x * axis.z;
		float yy = axis.y * axis.y;
		float yz = axis.y * axis.z;
		float zz = axis.z * axis.z;
		float xs = axis.x * s;
		float ys = axis.y * s;
		float zs = axis.z * s;
		
		float r0 = xx * omc + c;
		float r1 = xy * omc - zs;
		float r2 = xz * omc + ys;
		float r4 = xy * omc + zs;
		float r5 = yy * omc + c;
		float r6 = yz * omc - xs;
		float r8 = xz * omc - ys;
		float r9 = yz * omc + xs;
		float r10 = zz * omc + c;
		
		return new Mat4f(
				m.raw[0] * r0 + m.raw[1] * r4 + m.raw[2] * r8,
				m.raw[0] * r1 + m.raw[1] * r5 + m.raw[2] * r9,
				m.raw[0] * r2 + m.raw[1] * r6 + m.raw[2] * r10,
				m.raw[3],
				m.raw[4] * r0 + m.raw[5] * r4 + m.raw[6] * r8,
				m.raw[4] * r1 + m.raw[5] * r5 + m.raw[6] * r9,
				m.raw[4] * r2 + m.raw[5] * r6 + m.raw[6] * r10,
				m.raw[7],
				m.raw[8] * r0 + m.raw[9] * r4 + m.raw[10] * r8,
				m.raw[8] * r1 + m.raw[9] * r5 + m.raw[10] * r9,
				m.raw[8] * r2 + m.raw[9] * r6 + m.raw[10] * r10,
				m.raw[11],
				m.raw[12] * r0 + m.raw[13] * r4 + m.raw[14] * r8,
				m.raw[12] * r1 + m.raw[13] * r5 + m.raw[14] * r9,
				m.raw[12] * r2 + m.raw[13] * r6 + m.raw[14] * r10,
				m.raw[15]
		);
	}

	// m * R
	public static Mat4f rotate(Mat4f m, float x, float y, float z, float angle) {
		float s = (float) Math.sin(Math.toRadians(angle));
		float c = (float) Math.cos(Math.toRadians(angle));
		float omc = 1.f - c;
		
		float xx = x * x;
		float xy = x * y;
		float xz = x * z;
		float yy = y * y;
		float yz = y * z;
		float zz = z * z;
		float xs = x * s;
		float ys = y * s;
		float zs = z * s;
		
		float r0 = xx * omc + c;
		float r1 = xy * omc - zs;
		float r2 = xz * omc + ys;
		float r4 = xy * omc + zs;
		float r5 = yy * omc + c;
		float r6 = yz * omc - xs;
		float r8 = xz * omc - ys;
		float r9 = yz * omc + xs;
		float r10 = zz * omc + c;
		
		return new Mat4f(
				m.raw[0] * r0 + m.raw[1] * r4 + m.raw[2] * r8,
				m.raw[0] * r1 + m.raw[1] * r5 + m.raw[2] * r9,
				m.raw[0] * r2 + m.raw[1] * r6 + m.raw[2] * r10,
				m.raw[3],
				m.raw[4] * r0 + m.raw[5] * r4 + m.raw[6] * r8,
				m.raw[4] * r1 + m.raw[5] * r5 + m.raw[6] * r9,
				m.raw[4] * r2 + m.raw[5] * r6 + m.raw[6] * r10,
				m.raw[7],
				m.raw[8] * r0 + m.raw[9] * r4 + m.raw[10] * r8,
				m.raw[8] * r1 + m.raw[9] * r5 + m.raw[10] * r9,
				m.raw[8] * r2 + m.raw[9] * r6 + m.raw[10] * r10,
				m.raw[11],
				m.raw[12] * r0 + m.raw[13] * r4 + m.raw[14] * r8,
				m.raw[12] * r1 + m.raw[13] * r5 + m.raw[14] * r9,
				m.raw[12] * r2 + m.raw[13] * r6 + m.raw[14] * r10,
				m.raw[15]
		);
	}

	// m * R
	public static Mat4f rotateX(Mat4f m, float angle) {
		float s = (float) Math.sin(Math.toRadians(angle));
		float c = (float) Math.cos(Math.toRadians(angle));
		
		return new Mat4f(
				m.raw[0],
				m.raw[1] * c + m.raw[2] * s,
				-m.raw[1] * s + m.raw[2] * c,
				m.raw[3],
				m.raw[4],
				m.raw[5] * c + m.raw[6] * s,
				-m.raw[5] * s + m.raw[6] * c,
				m.raw[7],
				m.raw[8],
				m.raw[9] * c + m.raw[10] * s,
				-m.raw[9] * s + m.raw[10] * c,
				m.raw[11],
				m.raw[12],
				m.raw[13] * c + m.raw[14] * s,
				-m.raw[13] * s + m.raw[14] * c,
				m.raw[15]
		);
	}
	
	// m * R
	public static Mat4f rotateY(Mat4f m, float angle) {
		float s = (float) Math.sin(Math.toRadians(angle));
		float c = (float) Math.cos(Math.toRadians(angle));
		
		return new Mat4f(
				m.raw[0] * c - m.raw[2] * s,
				m.raw[1],
				m.raw[0] * s + m.raw[2] * c,
				m.raw[3],
				m.raw[4] * c - m.raw[6] * s,
				m.raw[5],
				m.raw[4] * s + m.raw[6] * c,
				m.raw[7],
				m.raw[8] * c - m.raw[10] * s,
				m.raw[9],
				m.raw[8] * s + m.raw[10] * c,
				m.raw[11],
				m.raw[12] * c - m.raw[14] * s,
				m.raw[13],
				m.raw[12] * s + m.raw[14] * c,
				m.raw[15]
		);
	}
	
	// m * R
	public static Mat4f rotateZ(Mat4f m, float angle) {
		float s = (float) Math.sin(Math.toRadians(angle));
		float c = (float) Math.cos(Math.toRadians(angle));

		return new Mat4f(
				m.raw[0] * c + m.raw[1] * s,
				-m.raw[0] * s + m.raw[1] * c,
				m.raw[2],
				m.raw[3],
				m.raw[4] * c + m.raw[5] * s,
				-m.raw[4] * s + m.raw[5] * c,
				m.raw[6],
				m.raw[7],
				m.raw[8] * c + m.raw[9] * s,
				-m.raw[8] * s + m.raw[9] * c,
				m.raw[10],
				m.raw[11],
				m.raw[12] * c + m.raw[13] * s,
				-m.raw[12] * s + m.raw[13] * c,
				m.raw[14],
				m.raw[15]
		);
	}

	
	public static Mat4f transpose(Mat4f m) {
		return new Mat4f(
				m.raw[0], m.raw[4], m.raw[8], m.raw[12],
				m.raw[1], m.raw[5], m.raw[9], m.raw[13],
				m.raw[2], m.raw[6], m.raw[10], m.raw[14],
				m.raw[3], m.raw[7], m.raw[11], m.raw[15]
		);
	}

	public static Mat4f transpose3(Mat4f m) {
		return new Mat4f(
				m.raw[0], m.raw[4], m.raw[8], m.raw[3],
				m.raw[1], m.raw[5], m.raw[9], m.raw[7],
				m.raw[2], m.raw[6], m.raw[10], m.raw[11],
				m.raw[12], m.raw[13], m.raw[14], m.raw[15]
		);
	}

	public static Mat4f invert(Mat4f m) {
		if (m.raw[12] == 0.f && m.raw[13] == 0.f && m.raw[14] == 0.f && m.raw[15] == 1.f)
			return invertAffine(m);
		else
			return invertGeneral(m);
	}
	
	public static Mat4f invertAffine(Mat4f m) {
		float dot1t = m.raw[0] * m.raw[3] + m.raw[4] * m.raw[7] + m.raw[8] * m.raw[11];
		float dot2t = m.raw[1] * m.raw[3] + m.raw[5] * m.raw[7] + m.raw[9] * m.raw[11];
		float dot3t = m.raw[2] * m.raw[3] + m.raw[6] * m.raw[7] + m.raw[10] * m.raw[11];
		
		return new Mat4f(
				m.raw[0], m.raw[4], m.raw[8], -dot1t,
				m.raw[1], m.raw[5], m.raw[9], -dot2t,
				m.raw[2], m.raw[6], m.raw[10], -dot3t,
				0.f, 0.f, 0.f, 1.f
		);
	}
	
	public static Mat4f invertGeneral(Mat4f m) {
		Mat4f result = new Mat4f();
		
		float d10d15 = m.raw[10] * m.raw[15] - m.raw[11] * m.raw[14];
		float d06d15 = m.raw[6]  * m.raw[15] - m.raw[7]  * m.raw[14];
		float d06d11 = m.raw[6]  * m.raw[11] - m.raw[7]  * m.raw[10];
	    float d02d15 = m.raw[2]  * m.raw[15] - m.raw[3]  * m.raw[14];
	    float d02d11 = m.raw[2]  * m.raw[11] - m.raw[3]  * m.raw[10];
	    float d02d07 = m.raw[2]  * m.raw[7]  - m.raw[3]  * m.raw[6];
	    float d09d15 = m.raw[9]  * m.raw[15] - m.raw[11] * m.raw[13];
	    float d05d15 = m.raw[5]  * m.raw[15] - m.raw[7]  * m.raw[13];
	    float d05d11 = m.raw[5]  * m.raw[11] - m.raw[7]  * m.raw[9];
	    float d01d15 = m.raw[1]  * m.raw[15] - m.raw[3]  * m.raw[13];
	    float d01d11 = m.raw[1]  * m.raw[11] - m.raw[3]  * m.raw[9];
	    float d01d07 = m.raw[1]  * m.raw[7]  - m.raw[3]  * m.raw[5];
	    float d09d14 = m.raw[9]  * m.raw[14] - m.raw[10] * m.raw[13];
	    float d05d14 = m.raw[5]  * m.raw[14] - m.raw[6]  * m.raw[13];
	    float d05d10 = m.raw[5]  * m.raw[10] - m.raw[6]  * m.raw[9];
	    float d01d14 = m.raw[1]  * m.raw[14] - m.raw[2]  * m.raw[13];
	    float d01d10 = m.raw[1]  * m.raw[10] - m.raw[2]  * m.raw[9];
	    float d01d06 = m.raw[1]  * m.raw[6]  - m.raw[2]  * m.raw[5];
	    
	    
	    result.raw[0]  =  m.raw[5] * d10d15 - m.raw[9] * d06d15 + m.raw[13] * d06d11;
	    result.raw[1]  = -m.raw[1] * d10d15 + m.raw[9] * d02d15 - m.raw[13] * d02d11;
	    result.raw[2]  =  m.raw[1] * d06d15 - m.raw[5] * d02d15 + m.raw[13] * d02d07;
	    result.raw[3]  = -m.raw[1] * d06d11 + m.raw[5] * d02d11 - m.raw[9]  * d02d07;
	    result.raw[4]  = -m.raw[4] * d10d15 + m.raw[8] * d06d15 - m.raw[12] * d06d11;
	    result.raw[5]  =  m.raw[0] * d10d15 - m.raw[8] * d02d15 + m.raw[12] * d02d11;
	    result.raw[6]  = -m.raw[0] * d06d15 + m.raw[4] * d02d15 - m.raw[12] * d02d07;
	    result.raw[7]  =  m.raw[0] * d06d11 - m.raw[4] * d02d11 + m.raw[8]  * d02d07;
	    result.raw[8]  =  m.raw[4] * d09d15 - m.raw[8] * d05d15 + m.raw[12] * d05d11;
	    result.raw[9]  = -m.raw[0] * d09d15 + m.raw[8] * d01d15 - m.raw[12] * d01d11;
	    result.raw[10] =  m.raw[0] * d05d15 - m.raw[4] * d01d15 + m.raw[12] * d01d07;
	    result.raw[11] = -m.raw[0] * d05d11 + m.raw[4] * d01d11 - m.raw[8]  * d01d07;
	    result.raw[12] = -m.raw[4] * d09d14 + m.raw[8] * d05d14 - m.raw[12] * d05d10;
	    result.raw[13] =  m.raw[0] * d09d14 - m.raw[8] * d01d14 + m.raw[12] * d01d10;
	    result.raw[14] = -m.raw[0] * d05d14 + m.raw[4] * d01d14 - m.raw[12] * d01d06;
	    result.raw[15] =  m.raw[0] * d05d10 - m.raw[4] * d01d10 + m.raw[8]  * d01d06;

	    float det = m.raw[0] * result.raw[0]
	    		+ m.raw[1] * result.raw[4]
	    		+ m.raw[2] * result.raw[8]
	    		+ m.raw[3] * result.raw[12];	
	    
	    if (det == 0) return null;
	    
	    det = 1.0f / det;
	    for (int i = 0; i < 16; i++)
	    	result.raw[i] *= det;
		
		return m;
	}
}
