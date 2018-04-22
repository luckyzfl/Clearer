package com.example.feilongzuo.test;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by FeilongZuo on 18/4/21.
 */

public class PictureHandle2 {
    public static Bitmap inipic;
    public static Bitmap anspic;
    public static void handle(Bitmap iipic){
        inipic=iipic;
        Mat src = new Mat(inipic.getHeight(), inipic.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(inipic, src);

                            /*
                                自定义核
                                0   -1`  0
                                -1  5   -1
                                0   -1  0
                              */
        Mat kernel = new Mat(3, 3, CvType.CV_16SC1);
        kernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);
        // 对图像和自定义核做卷积
        Imgproc.filter2D(src, src, src.depth(), kernel);

        // Mat转Bitmap
        anspic = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, anspic);
    }
}
