package com.dming.testgif;

import android.content.Context;
import android.opengl.GLES20;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ShaderHelper {

    /**
     * 编译顶点着色器
     *
     * @param shaderCode 编译代码
     * @return 着色器对象ID
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * 编译片段着色器
     *
     * @param shaderCode 编译代码
     * @return 着色器对象ID
     */
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * 编译片段着色器
     *
     * @param type       着色器类型
     * @param shaderCode 编译代码
     * @return 着色器对象ID
     */
    private static int compileShader(int type, String shaderCode) {
        // 1.创建一个新的着色器对象
        final int shaderObjectId = GLES20.glCreateShader(type);

        // 2.获取创建状态
        if (shaderObjectId == 0) {
            // 在OpenGL中，都是通过整型值去作为OpenGL对象的引用。之后进行操作的时候都是将这个整型值传回给OpenGL进行操作。
            // 返回值0代表着创建对象失败。
            DLog.e("Could not create new shader.");
            return 0;
        }

        // 3.将着色器代码上传到着色器对象中
        GLES20.glShaderSource(shaderObjectId, shaderCode);

        // 4.编译着色器对象
        GLES20.glCompileShader(shaderObjectId);

        // 5.获取编译状态：OpenGL将想要获取的值放入长度为1的数组的首位
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // 打印编译的着色器信息
//        DLog.i( "Results of compiling source:" + "\n" + shaderCode + "\n:"
//                + GLES20.glGetShaderInfoLog(shaderObjectId));

        // 6.验证编译状态
        if (compileStatus[0] == 0) {
            // 如果编译失败，则删除创建的着色器对象
            GLES20.glDeleteShader(shaderObjectId);
            DLog.e("Compilation of shader failed.");
            // 7.返回着色器对象：失败，为0
            return 0;
        }

        // 7.返回着色器对象：成功，非0
        return shaderObjectId;
    }

    /**
     * 创建OpenGL程序：通过链接顶点着色器、片段着色器
     *
     * @param vertexShaderId   顶点着色器ID
     * @param fragmentShaderId 片段着色器ID
     * @return OpenGL程序ID
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {

        // 1.创建一个OpenGL程序对象
        final int programObjectId = GLES20.glCreateProgram();

        // 2.获取创建状态
        if (programObjectId == 0) {
            DLog.e("Could not create new program");
            return 0;
        }

        // 3.将顶点着色器依附到OpenGL程序对象
        GLES20.glAttachShader(programObjectId, vertexShaderId);
        // 3.将片段着色器依附到OpenGL程序对象
        GLES20.glAttachShader(programObjectId, fragmentShaderId);

        // 4.将两个着色器链接到OpenGL程序对象
        GLES20.glLinkProgram(programObjectId);

        // 5.获取链接状态：OpenGL将想要获取的值放入长度为1的数组的首位
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);

        // 打印链接信息
//            DLog.i("Results of linking program:\n"
//                    + GLES20.glGetProgramInfoLog(programObjectId));

        // 6.验证链接状态
        if (linkStatus[0] == 0) {
            // 链接失败则删除程序对象
            GLES20.glDeleteProgram(programObjectId);
            DLog.e("Linking of program failed.");
            // 7.返回程序对象：失败，为0
            return 0;
        }
        GLES20.glDeleteShader(vertexShaderId);
        GLES20.glDeleteShader(fragmentShaderId);
        // 7.返回程序对象：成功，非0
        return programObjectId;
    }

    /**
     * 验证OpenGL程序对象状态
     *
     * @param programObjectId OpenGL程序ID
     * @return 是否可用
     */
    private static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);

        final int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        DLog.i("Results of validating program: " + validateStatus[0]
                + "\nLog:" + GLES20.glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    public static String readResource(Context context, int id) {
        InputStream inputStream = context.getResources().openRawResource(id);
        StringBuilder out = new StringBuilder();
        byte[] b = new byte[4096];
        try {
            for (int n; (n = inputStream.read(b)) != -1; ) {
                out.append(new String(b, 0, n));
            }
        } catch (IOException io) {
        }
        return out.toString();
    }

    /**
     * 创建OpenGL程序对象
     *
     * @param vertexShaderRes   顶点着色器代码
     * @param fragmentShaderRes 片段着色器代码
     */
    public static int loadProgram(Context context, int vertexShaderRes, int fragmentShaderRes) {
        final int vertexShaderId = ShaderHelper.compileVertexShader(readResource(context, vertexShaderRes));
        final int fragmentShaderId = ShaderHelper.compileFragmentShader(readResource(context, fragmentShaderRes));
        return ShaderHelper.linkProgram(vertexShaderId, fragmentShaderId);
    }

    public static FloatBuffer arrayToFloatBuffer(float[] fArray) {
        FloatBuffer fBuffer = ByteBuffer.allocateDirect(fArray.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fArray);
        fBuffer.position(0);
        return fBuffer;
    }

    public static ShortBuffer arrayToShortBuffer(short[] sArray) {
        ShortBuffer sBuffer = ByteBuffer.allocateDirect(sArray.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(sArray);
        sBuffer.position(0);
        return sBuffer;
    }

}
