/*
 * Copyright (c) 2017 52inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ftinc.canvasscript;


import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.os.Build;
import android.os.LocaleList;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.ftinc.canvasscript.params.ArcParams;
import com.ftinc.canvasscript.params.BitmapParams;
import com.ftinc.canvasscript.params.CanvasParams;
import com.ftinc.canvasscript.params.CircleParams;
import com.ftinc.canvasscript.params.ColorParams;
import com.ftinc.canvasscript.params.ConcatParams;
import com.ftinc.canvasscript.params.LineParams;
import com.ftinc.canvasscript.params.OvalParams;
import com.ftinc.canvasscript.params.PaintParams;
import com.ftinc.canvasscript.params.PathParams;
import com.ftinc.canvasscript.params.PictureParams;
import com.ftinc.canvasscript.params.PointParams;
import com.ftinc.canvasscript.params.RectParams;
import com.ftinc.canvasscript.params.RestoreParams;
import com.ftinc.canvasscript.params.RotateParams;
import com.ftinc.canvasscript.params.RoundRectParams;
import com.ftinc.canvasscript.params.SaveLayerParams;
import com.ftinc.canvasscript.params.SaveParams;
import com.ftinc.canvasscript.params.ScaleParams;
import com.ftinc.canvasscript.params.SkewParams;
import com.ftinc.canvasscript.params.TranslateParams;
import com.ftinc.canvasscript.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;


/**
 * <p>
 *     A utility class that provides a chainable set of APIs for working with Android's {@link Canvas}
 *     for custom drawing. You can use it to draw to an existing canvas (i.e. if your working with custom views) or
 *     for generating/modifying {@link Bitmap}s
 * </p>
 *
 * @author Drew Heavner
 * @since 2017
 */
public final class CanvasScript {

    private static final String TAG = "CanvasScript";
    private static final int DEFAULT_PAINT_FLAGS = Paint.ANTI_ALIAS_FLAG;
    private static final int DEFAULT_QUEUE_SIZE = 10;
    private static final int MAX_ALPHA = 255;

    static {
        Log.setEnabled(BuildConfig.DEBUG);
    }

    @NonNull private final Canvas rootCanvas;
    @Nullable private final Bitmap bitmap;
    @Nullable private Paint currentPaint;
    private final Deque<CanvasParams> parameters;
    private int currentSaveCount = CanvasParams.NO_SAVE;


    private CanvasScript(@NonNull Canvas canvas) {
        parameters = new ArrayDeque<>(DEFAULT_QUEUE_SIZE);
        rootCanvas = canvas;
        bitmap = null;
    }


    private CanvasScript(@NonNull Bitmap bitmap) {
        this.bitmap = bitmap;
        parameters = new ArrayDeque<>(DEFAULT_QUEUE_SIZE);
        rootCanvas = new Canvas(this.bitmap);
    }


    /**
     * Create a new instance with a new bitmap defined by the following parameters
     * @param width the width of the new bitmap to draw to
     * @param height the height of the new bitmap to draw to
     * @return self for chaining
     */
    public static CanvasScript create(int width, int height) {
        return create(width, height, Bitmap.Config.ARGB_8888);
    }


    /**
     * Create a new instance with a new bitmap defined by the following parameters
     * @param width the width of the new bitmap to draw to
     * @param height the height of the new bitmap to draw to
     * @param config the bitmap configuration
     * @return self for chaining
     * @see Canvas
     */
    public static CanvasScript create(int width, int height, @NonNull Bitmap.Config config) {
        return new CanvasScript(Bitmap.createBitmap(width, height, config));
    }


    /**
     * Create a new instance with a bitmap instance that will be the root of the
     * {@link Canvas} created
     * @param bitmap the base drawing bitmap
     * @return self for chaining
     * @throws IllegalArgumentException if bitmap is not mutable or is recycled
     * @see Canvas
     */
    public static CanvasScript create(@NonNull Bitmap bitmap) {
        if (!bitmap.isMutable() || bitmap.isRecycled()) {
            throw new IllegalArgumentException("Bitmap must be mutable and unrecycled");
        }
        return new CanvasScript(bitmap);
    }


    /**
     * Wrap an existing canvas to direct all the script calls to when calling {@link #draw()}
     * @param canvas the canvas to wrap and direct all drawing calls to
     * @return self for chaining
     */
    public static CanvasScript wrap(Canvas canvas) {
        return new CanvasScript(canvas);
    }


    /*   Paint Methods   */


    public CanvasScript paint(@Nullable Paint paint) {
        currentPaint = paint;
        return this;
    }


    public CanvasScript color(@ColorInt int color) {
        createPaintIfNull();
        currentPaint.setColor(color);
        return this;
    }


    public CanvasScript colorFilter(@Nullable ColorFilter filter) {
        createPaintIfNull();
        currentPaint.setColorFilter(filter);
        return this;
    }


    public CanvasScript argb(int a, int r, int g, int b) {
        createPaintIfNull();
        currentPaint.setARGB(a, r, g, b);
        return this;
    }


    public CanvasScript alpha(@IntRange(from = 0, to = MAX_ALPHA) int alpha) {
        createPaintIfNull();
        currentPaint.setAlpha(alpha);
        return this;
    }


    public CanvasScript alpha(@FloatRange(from = 0f, to = 1f) float alpha) {
        createPaintIfNull();
        currentPaint.setAlpha((int) (MAX_ALPHA * alpha));
        return this;
    }


    public CanvasScript flags(int flags) {
        createPaintIfNull();
        currentPaint.setFlags(flags);
        return this;
    }


    public CanvasScript addFlags(int flags) {
        createPaintIfNull();
        currentPaint.setFlags(currentPaint.getFlags() | flags);
        return this;
    }


    public CanvasScript style(Paint.Style style) {
        createPaintIfNull();
        currentPaint.setStyle(style);
        return this;
    }


    public CanvasScript strokeWidth(float width) {
        createPaintIfNull();
        currentPaint.setStrokeWidth(width);
        return this;
    }


    public CanvasScript strokeMiter(float miter) {
        createPaintIfNull();
        currentPaint.setStrokeMiter(miter);
        return this;
    }


    public CanvasScript strokeCap(Paint.Cap cap) {
        createPaintIfNull();
        currentPaint.setStrokeCap(cap);
        return this;
    }


    public CanvasScript strokeJoin(Paint.Join join) {
        createPaintIfNull();
        currentPaint.setStrokeJoin(join);
        return this;
    }


    public CanvasScript shader(Shader shader) {
        createPaintIfNull();
        currentPaint.setShader(shader);
        return this;
    }


    public CanvasScript xfermode(Xfermode xfermode) {
        createPaintIfNull();
        currentPaint.setXfermode(xfermode);
        return this;
    }


    public CanvasScript porterDuffXfer(PorterDuff.Mode mode) {
        createPaintIfNull();
        currentPaint.setXfermode(new PorterDuffXfermode(mode));
        return this;
    }


    public CanvasScript pathEffect(PathEffect pathEffect) {
        createPaintIfNull();
        currentPaint.setPathEffect(pathEffect);
        return this;
    }


    public CanvasScript maskFilter(MaskFilter maskFilter) {
        createPaintIfNull();
        currentPaint.setMaskFilter(maskFilter);
        return this;
    }


    public CanvasScript typeface(Typeface typeface) {
        createPaintIfNull();
        currentPaint.setTypeface(typeface);
        return this;
    }


    public CanvasScript shadow(float radius, float dx, float dy, @ColorInt int shadowColor) {
        createPaintIfNull();
        currentPaint.setShadowLayer(radius, dx, dy, shadowColor);
        return this;
    }


    public CanvasScript clearShadow() {
        if (currentPaint != null) {
            currentPaint.clearShadowLayer();
        }
        return this;
    }


    public CanvasScript textAlign(Paint.Align align) {
        createPaintIfNull();
        currentPaint.setTextAlign(align);
        return this;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public CanvasScript textLocale(@NonNull Locale locale) {
        createPaintIfNull();
        currentPaint.setTextLocale(locale);
        return this;
    }


    @TargetApi(Build.VERSION_CODES.N)
    public CanvasScript textLocales(@NonNull @Size(min = 1) LocaleList locales) {
        createPaintIfNull();
        currentPaint.setTextLocales(locales);
        return this;
    }


    public CanvasScript textSize(float textSize) {
        createPaintIfNull();
        currentPaint.setTextSize(textSize);
        return this;
    }


    public CanvasScript textScaleX(float scaleX) {
        createPaintIfNull();
        currentPaint.setTextScaleX(scaleX);
        return this;
    }


    public CanvasScript textSkewX(float skewX) {
        createPaintIfNull();
        currentPaint.setTextSkewX(skewX);
        return this;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CanvasScript letterSpacing(float letterSpacing) {
        createPaintIfNull();
        currentPaint.setLetterSpacing(letterSpacing);
        return this;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CanvasScript fontFeatureSettings(String settings) {
        createPaintIfNull();
        currentPaint.setFontFeatureSettings(settings);
        return this;
    }


    /*   Canvas Manipulation Methods   */


    /**
     * Add a custom canvas script parameter to the stack for custom operators
     * @param customParameter the custom canvas parameter
     * @return self for chaining
     */
    public CanvasScript custom(@NonNull CanvasParams customParameter) {
        parameters.add(customParameter);
        return this;
    }


    /**
     * Add another Canvas script build parameters to this script
     * @param script the other canvas script to concat onto this one
     * @return self for chaining
     */
    public CanvasScript script(CanvasScript script) {
        parameters.addAll(script.parameters);
        return this;
    }


    /**
     * Add another script as a child offset by dx, dy
     * @param dx the x-offset of the script
     * @param dy the y-offset of the script
     * @param script the script to concatenate
     * @return self for chaining
     */
    public CanvasScript script(float dx, float dy, CanvasScript script) {
        parameters.add(new SaveParams());
        parameters.add(new TranslateParams(dx, dy));
        parameters.addAll(script.parameters);
        parameters.add(new RestoreParams());
        return this;
    }


    /**
     * Add a circle to the render stack
     * @param cx The center x coordinate
     * @param cy The center y coordinate
     * @param radius The radius of the circle
     * @return self for chaining
     * @see Canvas#drawCircle(float, float, float, Paint)
     */
    public CanvasScript circle(float cx, float cy, float radius) {
        checkNonNullPaint();
        parameters.add(new CircleParams(cx, cy, radius, getPaintCopy()));
        return this;
    }


    /**
     * Add a circle to the render stack with a custom {@link Paint} parameter
     * @param cx The center x coordinate
     * @param cy The center y coordinate
     * @param radius The radius of the circle
     * @param paint custom paint to use when rendering
     * @return self for chaining
     * @see Canvas#drawCircle(float, float, float, Paint)
     */
    public CanvasScript circle(float cx, float cy, float radius, @NonNull Paint paint) {
        parameters.add(new CircleParams(cx, cy, radius, paint));
        return this;
    }


    /**
     * Add a line to the render stack
     * @param sx The start-x coordinate
     * @param sy The start-y coordinate
     * @param ex The end-x coordinate
     * @param ey The end-y coordinate
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawLine(float, float, float, float, Paint)
     */
    public CanvasScript line(float sx, float sy, float ex, float ey) {
        checkNonNullPaint();
        parameters.add(new LineParams(sx, sy, ex, ey, getPaintCopy()));
        return this;
    }


    /**
     * Add a set of lines to the render stack
     * @param pts The array, in multiples of 4, of points that represent the line [x0, y0, x1, y1, ..]
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawLines(float[], Paint)
     */
    public CanvasScript lines(@Size(multiple = LineParams.SIZE) @NonNull float[] pts) {
        checkNonNullPaint();
        parameters.add(new LineParams(pts, getPaintCopy()));
        return this;
    }


    /**
     * Add a set of lines to the render stack
     * @param pts The array, in multiples of 4, of points that represent the line [x0, y0, x1, y1, ..]
     * @param paint custom paint for rendering
     * @return self for chaining
     * @see Canvas#drawLines(float[], Paint)
     */
    public CanvasScript lines(@Size(multiple = LineParams.SIZE) @NonNull float[] pts, @NonNull Paint paint) {
        parameters.add(new LineParams(pts, paint));
        return this;
    }


    /**
     * Add a set of lines to the render stack
     * @param pts The array, in multiples of 4, of points that represent the line [x0, y0, x1, y1, ..]
     * @param offset   Number of values in the array to skip before drawing.
     * @param count    The number of values in the array to process, after
     *                 skipping "offset" of them.
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawLines(float[], Paint)
     */
    public CanvasScript lines(@Size(multiple = LineParams.SIZE) @NonNull float[] pts, int offset, int count) {
        checkNonNullPaint();
        parameters.add(new LineParams(pts, offset, count, getPaintCopy()));
        return this;
    }


    /**
     * Add a set of lines to the render stack
     * @param pts The array, in multiples of 4, of points that represent the line [x0, y0, x1, y1, ..]
     * @param offset Number of values in the array to skip before drawing.
     * @param count The number of values in the array to process, after
     *              skipping "offset" of them.
     * @param paint The custom paint to draw the lines with
     * @return self for chaining
     * @see Canvas#drawLines(float[], Paint)
     */
    public CanvasScript lines(@Size(multiple = LineParams.SIZE) @NonNull float[] pts, int offset, int count,
                              @NonNull Paint paint) {
        parameters.add(new LineParams(pts, offset, count, paint));
        return this;
    }


    /**
     * Add an oval to the render stack
     * @param left The left of the bounds to draw in
     * @param top The top of the bounds to draw in
     * @param right The right of the bounds to draw in
     * @param bottom The bottom of the bounds to draw in
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawOval(float, float, float, float, Paint)
     */
    public CanvasScript oval(float left, float top, float right, float bottom) {
        checkNonNullPaint();
        parameters.add(new OvalParams(left, right, top, bottom, getPaintCopy()));
        return this;
    }


    /**
     * Add an oval to the render stack
     * @param left The left of the bounds to draw in
     * @param top The top of the bounds to draw in
     * @param right The right of the bounds to draw in
     * @param bottom The bottom of the bounds to draw in
     * @param paint The custom paint to draw with
     * @return self for chaining
     * @see Canvas#drawOval(float, float, float, float, Paint)
     */
    public CanvasScript oval(float left, float top, float right, float bottom, @NonNull Paint paint) {
        parameters.add(new OvalParams(left, right, top, bottom, paint));
        return this;
    }


    /**
     * Add an oval to the render stack
     * @param bounds The bounds of the oval to draw in
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawOval(RectF, Paint)
     */
    public CanvasScript oval(@NonNull RectF bounds) {
        checkNonNullPaint();
        parameters.add(new OvalParams(bounds, getPaintCopy()));
        return this;
    }


    /**
     * Add an oval to the render stack
     * @param bounds The bounds of the oval to draw in
     * @param paint The custom paint to draw with
     * @return self for chaining
     * @see Canvas#drawOval(RectF, Paint)
     */
    public CanvasScript oval(@NonNull RectF bounds, @NonNull Paint paint) {
        parameters.add(new OvalParams(bounds, paint));
        return this;
    }


    /**
     * Add a rect to the render stack
     * @param left The left of the rect bounds
     * @param top The top of the rect bounds
     * @param right The right of the rect bounds
     * @param bottom The bottom of the rect bounds
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawRect(float, float, float, float, Paint)
     */
    public CanvasScript rect(float left, float top, float right, float bottom) {
        checkNonNullPaint();
        parameters.add(new RectParams(left, top, right, bottom, getPaintCopy()));
        return this;
    }


    /**
     * Add a rect to the render stack
     * @param rect The bounds of the rectangle
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawRect(Rect, Paint)
     */
    public CanvasScript rect(@NonNull Rect rect) {
        checkNonNullPaint();
        parameters.add(new RectParams(rect, getPaintCopy()));
        return this;
    }


    /**
     * Add a rect to the render stack
     * @param rect The bounds of the rectangle
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawRect(RectF, Paint)
     */
    public CanvasScript rect(@NonNull RectF rect) {
        checkNonNullPaint();
        parameters.add(new RectParams(rect, getPaintCopy()));
        return this;
    }


    /**
     * Add a rect to the render stack
     * @param left The left of the rect bounds
     * @param top The top of the rect bounds
     * @param right The right of the rect bounds
     * @param bottom The bottom of the rect bounds
     * @param paint The paint to draw with
     * @return self for chaining
     * @see Canvas#drawRect(float, float, float, float, Paint)
     */
    public CanvasScript rect(float left, float top, float right, float bottom, @NonNull Paint paint) {
        parameters.add(new RectParams(left, top, right, bottom, paint));
        return this;
    }


    /**
     * Add a rect to the render stack
     * @param rect The bounds of the rectangle
     * @param paint The paint to draw with
     * @return self for chaining
     * @see Canvas#drawRect(Rect, Paint)
     */
    public CanvasScript rect(@NonNull Rect rect, @NonNull Paint paint) {
        parameters.add(new RectParams(rect, paint));
        return this;
    }


    /**
     * Add a rect to the render stack
     * @param rect The bounds of the rectangle
     * @param paint The paint to draw with
     * @return self for chaining
     * @see Canvas#drawRect(RectF, Paint)
     */
    public CanvasScript rect(@NonNull RectF rect, @NonNull Paint paint) {
        parameters.add(new RectParams(rect, paint));
        return this;
    }


    /**
     * Add a rounded rect to the render stack
     * @param left The left bound of the rect
     * @param top The top bound of the rect
     * @param right The right bound of the rect
     * @param bottom The bottom bound of the rect
     * @param rx The x-radius of the oval corners
     * @param ry The y-radius of the oval corners
     * @param paint The custom paint to draw with
     * @return self for chaining
     * @see Canvas#drawRoundRect(float, float, float, float, float, float, Paint)
     */
    public CanvasScript roundedRect(float left, float top, float right, float bottom, float rx, float ry,
                                    @NonNull Paint paint) {
        parameters.add(new RoundRectParams(left, top, right, bottom, rx, ry, paint));
        return this;
    }


    /**
     * Add a rounded rect to the render stack
     * @param left The left bound of the rect
     * @param top The top bound of the rect
     * @param right The right bound of the rect
     * @param bottom The bottom bound of the rect
     * @param radius The radius of the circle corners
     * @param paint The custom paint to draw with
     * @return self for chaining
     * @see Canvas#drawRoundRect(float, float, float, float, float, float, Paint)
     */
    public CanvasScript roundedRect(float left, float top, float right, float bottom, float radius,
                                    @NonNull Paint paint) {
        parameters.add(new RoundRectParams(left, top, right, bottom, radius, paint));
        return this;
    }


    /**
     * Add a rounded rect to the render stack
     * @param bounds The bounds of the rect
     * @param rx The x-radius of the oval corners
     * @param ry The y-radius of the oval corners
     * @param paint The custom paint to draw with
     * @return self for chaining
     * @see Canvas#drawRoundRect(float, float, float, float, float, float, Paint)
     */
    public CanvasScript roundedRect(@NonNull RectF bounds, float rx, float ry, @NonNull Paint paint) {
        parameters.add(new RoundRectParams(bounds, rx, ry, paint));
        return this;
    }


    /**
     * Add a rounded rect to the render stack
     * @param bounds The bounds of the rect
     * @param radius The radius of the circle corners
     * @param paint The custom paint to draw with
     * @return self for chaining
     * @see Canvas#drawRoundRect(float, float, float, float, float, float, Paint)
     */
    public CanvasScript roundedRect(@NonNull RectF bounds, float radius, @NonNull Paint paint) {
        parameters.add(new RoundRectParams(bounds, radius, paint));
        return this;
    }


    /**
     * Add a rounded rect to the render stack
     * @param left The left bound of the rect
     * @param top The top bound of the rect
     * @param right The right bound of the rect
     * @param bottom The bottom bound of the rect
     * @param rx The x-radius of the oval corners
     * @param ry The y-radius of the oval corners
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawRoundRect(float, float, float, float, float, float, Paint)
     */
    public CanvasScript roundedRect(float left, float top, float right, float bottom, float rx, float ry) {
        checkNonNullPaint();
        parameters.add(new RoundRectParams(left, top, right, bottom, rx, ry, getPaintCopy()));
        return this;
    }


    /**
     * Add a rounded rect to the render stack
     * @param left The left bound of the rect
     * @param top The top bound of the rect
     * @param right The right bound of the rect
     * @param bottom The bottom bound of the rect
     * @param radius The radius of the circle corners
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawRoundRect(float, float, float, float, float, float, Paint)
     */
    public CanvasScript roundedRect(float left, float top, float right, float bottom, float radius) {
        checkNonNullPaint();
        parameters.add(new RoundRectParams(left, top, right, bottom, radius, getPaintCopy()));
        return this;
    }


    /**
     * Add a rounded rect to the render stack
     * @param bounds The bounds of the rect
     * @param rx The x-radius of the oval corners
     * @param ry The y-radius of the oval corners
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawRoundRect(float, float, float, float, float, float, Paint)
     */
    public CanvasScript roundedRect(@NonNull RectF bounds, float rx, float ry) {
        checkNonNullPaint();
        parameters.add(new RoundRectParams(bounds, rx, ry, getPaintCopy()));
        return this;
    }


    /**
     * Add a rounded rect to the render stack
     * @param bounds The bounds of the rect
     * @param radius The radius of the circle corners
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawRoundRect(float, float, float, float, float, float, Paint)
     */
    public CanvasScript roundedRect(@NonNull RectF bounds, float radius) {
        checkNonNullPaint();
        parameters.add(new RoundRectParams(bounds, radius, getPaintCopy()));
        return this;
    }


    /**
     * Add an arc to the render stack
     * @param left The left bound of the oval source of the arc
     * @param top The top bound of the oval source of the arc
     * @param right The right bound of the oval source of the arc
     * @param bottom The bottom bound of the oval source of the arc
     * @param startAngle The starting angle of the arc
     * @param sweepAngle The amount in degrees that the arc spans
     * @param useCenter whether to include rendering the center with the arc
     * @param paint The paint to draw with
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawArc(float, float, float, float, float, float, boolean, Paint)
     */
    public CanvasScript arc(float left, float top, float right, float bottom, float startAngle, float sweepAngle,
                            boolean useCenter, @NonNull Paint paint) {
        parameters.add(new ArcParams(left, top, right, bottom, startAngle, sweepAngle, useCenter, paint));
        return this;
    }


    /**
     * Add an arc to the render stack
     * @param bounds The bounds of the oval source of the arc
     * @param startAngle The starting angle of the arc
     * @param sweepAngle The amount in degrees that the arc spans
     * @param useCenter whether to include rendering the center with the arc
     * @param paint The paint to draw with
     * @return self for chaining
     * @see Canvas#drawArc(RectF, float, float, boolean, Paint)
     */
    public CanvasScript arc(@NonNull RectF bounds, float startAngle, float sweepAngle, boolean useCenter,
                            @NonNull Paint paint) {
        parameters.add(new ArcParams(bounds, startAngle, sweepAngle, useCenter, paint));
        return this;
    }


    /**
     * Add an arc to the render stack
     * @param left The left bound of the oval source of the arc
     * @param top The top bound of the oval source of the arc
     * @param right The right bound of the oval source of the arc
     * @param bottom The bottom bound of the oval source of the arc
     * @param startAngle The starting angle of the arc
     * @param sweepAngle The amount in degrees that the arc spans
     * @param useCenter whether to include rendering the center with the arc
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawArc(float, float, float, float, float, float, boolean, Paint)
     */
    public CanvasScript arc(float left, float top, float right, float bottom, float startAngle, float sweepAngle,
                            boolean useCenter) {
        checkNonNullPaint();
        parameters.add(new ArcParams(left, top, right, bottom, startAngle, sweepAngle, useCenter, getPaintCopy()));
        return this;
    }


    /**
     * Add an arc to The render stack
     * @param bounds The bounds of the oval source of the arc
     * @param startAngle The starting angle of the arc
     * @param sweepAngle The amount in degrees that the arc spans
     * @param useCenter whether to include rendering the center with the arc
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawArc(RectF, float, float, boolean, Paint)
     */
    public CanvasScript arc(@NonNull RectF bounds, float startAngle, float sweepAngle, boolean useCenter) {
        checkNonNullPaint();
        parameters.add(new ArcParams(bounds, startAngle, sweepAngle, useCenter, getPaintCopy()));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, float, float, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap) {
        parameters.add(new BitmapParams(bitmap, 0f, 0f, currentPaint));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @param paint May be null. The paint to render the bitmap with
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, float, float, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap, @Nullable Paint paint) {
        parameters.add(new BitmapParams(bitmap, 0f, 0f, paint));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @param width The new width of the bitmap to be stretched/scaled into
     * @param height The new height of the bitmap to be stretched/scaled into
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, float, float, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap, int width, int height) {
        parameters.add(new BitmapParams(bitmap, null, new Rect(0, 0, width, height), currentPaint));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @param width The new width of the bitmap to be stretched/scaled into
     * @param height The new height of the bitmap to be stretched/scaled into
     * @param paint May be null. The paint to render the bitmap with
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, float, float, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap, int width, int height, @Nullable Paint paint) {
        parameters.add(new BitmapParams(bitmap, null, new Rect(0, 0, width, height), paint));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @param left The left coordinate to render at
     * @param top The top coordinate to render at
     * @param paint The paint to render with, or null for default
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, float, float, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap, float left, float top, @Nullable Paint paint) {
        parameters.add(new BitmapParams(bitmap, left, top, paint));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @param src    May be null. The subset of the bitmap to be drawn
     * @param dst    The rectangle that the bitmap will be scaled/translated
     *               to fit into
     * @param paint The paint to render with, or null for default
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, Rect, Rect, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap, @Nullable Rect src, @NonNull Rect dst, @Nullable Paint paint) {
        parameters.add(new BitmapParams(bitmap, src, dst, paint));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @param src    May be null. The subset of the bitmap to be drawn
     * @param dst    The rectangle that the bitmap will be scaled/translated
     *               to fit into
     * @param paint The paint to render with, or null for default
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, Rect, RectF, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap, @Nullable Rect src, @NonNull RectF dst, @Nullable Paint paint) {
        parameters.add(new BitmapParams(bitmap, src, dst, paint));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @param matrix The matrix used to transform the bitmap when it is drawn
     * @param paint The paint to render with, or null for default
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, Matrix, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap, @NonNull Matrix matrix, @Nullable Paint paint) {
        parameters.add(new BitmapParams(bitmap, matrix, paint));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @param left The left coordinate to render at
     * @param top The top coordinate to render at
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, float, float, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap, float left, float top) {
        parameters.add(new BitmapParams(bitmap, left, top, getPaintCopy()));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @param src    May be null. The subset of the bitmap to be drawn
     * @param dst    The rectangle that the bitmap will be scaled/translated
     *               to fit into
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, Rect, Rect, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap, @Nullable Rect src, @NonNull Rect dst) {
        parameters.add(new BitmapParams(bitmap, src, dst, getPaintCopy()));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @param src    May be null. The subset of the bitmap to be drawn
     * @param dst    The rectangle that the bitmap will be scaled/translated
     *               to fit into
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, Rect, RectF, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap, @Nullable Rect src, @NonNull RectF dst) {
        parameters.add(new BitmapParams(bitmap, src, dst, getPaintCopy()));
        return this;
    }


    /**
     * Add a bitmap to the render stack
     * @param bitmap The bitmap to render
     * @param matrix The matrix used to transform the bitmap when it is drawn
     * @return self for chaining
     * @see Canvas#drawBitmap(Bitmap, Matrix, Paint)
     */
    public CanvasScript bitmap(@NonNull Bitmap bitmap, @NonNull Matrix matrix) {
        parameters.add(new BitmapParams(bitmap, matrix, getPaintCopy()));
        return this;
    }


    /**
     * Add a point to the render stack
     * @param x The x-coordinate of the point
     * @param y The y-coordinate of the point
     * @param paint The paint to draw with
     * @return self for chaining
     * @see Canvas#drawPoint(float, float, Paint)
     */
    public CanvasScript point(float x, float y, @NonNull Paint paint) {
        parameters.add(new PointParams(x, y, paint));
        return this;
    }


    /**
     * Add a set of points to the render stack
     * @param pts Array of points to draw [x0 y0 x1 y1 x2 y2 ...]
     * @param paint The paint used to draw the points
     * @return self for chaining
     * @see Canvas#drawPoints(float[], Paint)
     */
    public CanvasScript points(@Size(multiple = 2) @NonNull float[] pts, @NonNull Paint paint) {
        parameters.add(new PointParams(pts, paint));
        return this;
    }


    /**
     * Add a set of points to the render stack
     * @param pts Array of points to draw [x0 y0 x1 y1 x2 y2 ...]
     * @param offset Number of values to skip before starting to draw.
     * @param count The number of values to process, after skipping offset
     *              of them.
     * @param paint The paint used to draw the points
     * @return self for chaining
     * @see Canvas#drawPoints(float[], int, int, Paint)
     */
    public CanvasScript points(@Size(multiple = 2) @NonNull float[] pts, int offset, int count, @NonNull Paint paint) {
        parameters.add(new PointParams(pts, offset, count, paint));
        return this;
    }


    /**
     * Add a point to the render stack
     * @param x The x-coordinate of the point
     * @param y The y-coordinate of the point
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawPoint(float, float, Paint)
     */
    public CanvasScript point(float x, float y) {
        checkNonNullPaint();
        parameters.add(new PointParams(x, y, getPaintCopy()));
        return this;
    }


    /**
     * Add a set of points to the render stack
     * @param pts Array of points to draw [x0 y0 x1 y1 x2 y2 ...]
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawPoints(float[], Paint)
     */
    public CanvasScript points(@Size(multiple = 2) @NonNull float[] pts) {
        checkNonNullPaint();
        parameters.add(new PointParams(pts, getPaintCopy()));
        return this;
    }


    /**
     * Add a set of points to the render stack
     * @param pts Array of points to draw [x0 y0 x1 y1 x2 y2 ...]
     * @param offset Number of values to skip before starting to draw.
     * @param count The number of values to process, after skipping offset
     *              of them
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawPoints(float[], int, int, Paint)
     */
    public CanvasScript points(@Size(multiple = 2) @NonNull float[] pts, int offset, int count) {
        checkNonNullPaint();
        parameters.add(new PointParams(pts, offset, count, getPaintCopy()));
        return this;
    }


    /**
     * Add a path to the render stack
     * @param path The path to render
     * @param paint The paint to draw the path with
     * @return self for chaining
     * @see Canvas#drawPath(Path, Paint)
     */
    public CanvasScript path(@NonNull Path path, @NonNull Paint paint) {
        parameters.add(new PathParams(path, paint));
        return this;
    }


    /**
     * Add a path to the render stack
     * @param path The path to render
     * @return self for chaining
     * @throws IllegalStateException when no previous paint method is called
     * @see Canvas#drawPath(Path, Paint)
     */
    public CanvasScript path(@NonNull Path path) {
        checkNonNullPaint();
        parameters.add(new PathParams(path, getPaintCopy()));
        return this;
    }


    /**
     * Add a picture to the render stack
     * @param picture The picture to render
     * @return self for chaining
     * @see Canvas#drawPicture(Picture)
     */
    public CanvasScript picture(@NonNull Picture picture) {
        parameters.add(new PictureParams(picture));
        return this;
    }


    /**
     * Add a picture to the render stack
     * @param picture The picture to render
     * @param dst The bounds to stretch and fit the picture to
     * @return self for chaining
     * @see Canvas#drawPicture(Picture, Rect)
     */
    public CanvasScript picture(@NonNull Picture picture, @NonNull Rect dst) {
        parameters.add(new PictureParams(picture, dst));
        return this;
    }


    /**
     * Add a picture to the render stack
     * @param picture The picture to render
     * @param dst The bounds to stretch and fit the picture to
     * @return self for chaining
     * @see Canvas#drawPicture(Picture, RectF)
     */
    public CanvasScript picture(@NonNull Picture picture, @NonNull RectF dst) {
        parameters.add(new PictureParams(picture, dst));
        return this;
    }


    /**
     * Add a color render to the stack
     * @param color The color to draw to the canvas
     * @return self for chaining
     * @see Canvas#drawColor(int)
     */
    public CanvasScript drawColor(@ColorInt int color) {
        parameters.add(new ColorParams(color));
        return this;
    }


    /**
     * Add a color render to the stack
     * @param color The color to draw to the canvas
     * @param mode The porter duff mode to render with
     * @return self for chaining
     * @see Canvas#drawColor(int, android.graphics.PorterDuff.Mode)
     */
    public CanvasScript drawColor(@ColorInt int color, PorterDuff.Mode mode) {
        parameters.add(new ColorParams(color, mode));
        return this;
    }


    /**
     * Add a paint render to the stack
     * @param paint a paint object to draw to the canvas
     * @return self for chaining
     * @see Canvas#drawPaint(Paint)
     */
    public CanvasScript drawPaint(@NonNull Paint paint) {
        parameters.add(new PaintParams(paint));
        return this;
    }


    /**
     * Add a canvas save to the stack
     * @return self for chaining
     * @see Canvas#save()
     */
    public CanvasScript save() {
        parameters.add(new SaveParams());
        return this;
    }


    /**
     * Add a canvas save to the stack
     * @param saveFlags flag bits that specify which parts of the Canvas state
     *                  to save/restore
     * @return self for chaining
     */
    public CanvasScript save(int saveFlags) {
        parameters.add(new SaveParams(saveFlags));
        return this;
    }


    /**
     * Add a canvas save layer to the stack
     * @return self for chaining
     * @see Canvas#saveLayer(RectF, Paint)
     */
    public CanvasScript saveLayer() {
        parameters.add(new SaveLayerParams(null, null));
        return this;
    }


    /**
     * Add a canvas save layer to the stack
     * @param left The left bound of the offscreen bitmap
     * @param top The top bound of the offscreen bitmap
     * @param right The right bound of the offscreen bitmap
     * @param bottom The bottom bound of the offscreen bitmap
     * @param paint  This is copied, and is applied to the offscreen when
     *               restore() is called.
     * @return self for chaining
     * @see Canvas#saveLayer(RectF, Paint)
     */
    public CanvasScript saveLayer(float left, float top, float right, float bottom, @Nullable Paint paint) {
        parameters.add(new SaveLayerParams(left, top, right, bottom, paint));
        return this;
    }


    /**
     * Add a canvas save layer to the stack
     * @param bounds May be null. The maximum size the offscreen bitmap
     *               needs to be (in local coordinates)
     * @param paint  This is copied, and is applied to the offscreen when
     *               restore() is called.
     * @return self for chaining
     * @see Canvas#saveLayer(RectF, Paint)
     */
    public CanvasScript saveLayer(@Nullable RectF bounds, @Nullable Paint paint) {
        parameters.add(new SaveLayerParams(bounds, paint));
        return this;
    }


    /**
     * Add a canvas save layer to the stack
     * @param left The left bound of the offscreen bitmap
     * @param top The top bound of the offscreen bitmap
     * @param right The right bound of the offscreen bitmap
     * @param bottom The bottom bound of the offscreen bitmap
     * @param paint  This is copied, and is applied to the offscreen when
     *               restore() is called.
     * @param saveFlags see _SAVE_FLAG constants, generally {@link Canvas#ALL_SAVE_FLAG} is recommended
     *                  for performance reasons.
     * @return self for chaining
     * @see Canvas#saveLayer(RectF, Paint)
     */
    public CanvasScript saveLayer(float left, float top, float right, float bottom, @Nullable Paint paint,
                                  int saveFlags) {
        parameters.add(new SaveLayerParams(left, top, right, bottom, paint, saveFlags));
        return this;
    }


    /**
     * Add a canvas save layer to the stack
     * @param bounds May be null. The maximum size the offscreen bitmap
     *               needs to be (in local coordinates)
     * @param paint  This is copied, and is applied to the offscreen when
     *               restore() is called.
     * @param saveFlags see _SAVE_FLAG constants, generally {@link Canvas#ALL_SAVE_FLAG} is recommended
     *                  for performance reasons.
     * @return self for chaining
     * @see Canvas#saveLayer(RectF, Paint)
     */
    public CanvasScript saveLayer(@Nullable RectF bounds, @Nullable Paint paint, int saveFlags) {
        parameters.add(new SaveLayerParams(bounds, paint, saveFlags));
        return this;
    }


    /**
     * Add a canvas save layer alpha to the stack
     * @param left The left bound of the offscreen bitmap
     * @param top The top bound of the offscreen bitmap
     * @param right The right bound of the offscreen bitmap
     * @param bottom The bottom bound of the offscreen bitmap
     * @param alpha The alpha to apply to the offscreen when it is
     *              drawn during restore()
     * @param saveFlags see _SAVE_FLAG constants, generally {@link Canvas#ALL_SAVE_FLAG} is recommended
     *                  for performance reasons.
     * @return self for chaining
     * @see Canvas#saveLayerAlpha(float, float, float, float, int, int)
     */
    public CanvasScript saveLayer(float left, float top, float right, float bottom,
                                  @IntRange(from = 0, to = MAX_ALPHA) int alpha, int saveFlags) {
        parameters.add(new SaveLayerParams(left, top, right, bottom, alpha, saveFlags));
        return this;
    }


    /**
     * Add a canvas save layer alpha to the stack
     * @param bounds    The maximum size the offscreen bitmap needs to be
     *                  (in local coordinates)
     * @param alpha The alpha to apply to the offscreen when it is
     *              drawn during restore()
     * @param saveFlags see _SAVE_FLAG constants, generally {@link Canvas#ALL_SAVE_FLAG} is recommended
     *                  for performance reasons.
     * @return self for chaining
     * @see Canvas#saveLayerAlpha(RectF, int, int)
     */
    public CanvasScript saveLayer(RectF bounds, @IntRange(from = 0, to = MAX_ALPHA) int alpha, int saveFlags) {
        parameters.add(new SaveLayerParams(bounds, alpha, saveFlags));
        return this;
    }


    /**
     * Add a canvas restore to the stack
     * @return self for chaining
     * @see Canvas#restore()
     */
    public CanvasScript restore() {
        parameters.add(new RestoreParams());
        return this;
    }


    /**
     * Add a canvas restoreToCount to the stack
     * @param saveCount The save level to restore to.
     * @return self for chaining
     * @see Canvas#restoreToCount(int)
     */
    public CanvasScript restore(int saveCount) {
        parameters.add(new RestoreParams(saveCount));
        return this;
    }


    /**
     * Add a canvas translation to the stack
     * @param dx The distance to translate x
     * @param dy The distance to translate y
     * @return self for chaining
     * @see Canvas#translate(float, float)
     */
    public CanvasScript translate(float dx, float dy) {
        parameters.add(new TranslateParams(dx, dy));
        return this;
    }


    /**
     * Add a canvas rotation to the stack
     * @param degrees The amount in degrees to rotate
     * @return self for chaining
     * @see Canvas#rotate(float)
     */
    public CanvasScript rotate(float degrees) {
        parameters.add(new RotateParams(degrees));
        return this;
    }


    /**
     * Add a canvas rotation to the stack
     * @param degrees The amount in degrees to rotate
     * @param px The x-coord for the pivot point (unchanged by the rotation)
     * @param py The y-coord for the pivot point (unchanged by the rotation)
     * @return self for chaining
     * @see Canvas#rotate(float, float, float)
     */
    public CanvasScript rotate(float degrees, float px, float py) {
        parameters.add(new RotateParams(degrees, px, py));
        return this;
    }


    /**
     * Add a canvas skew to the stack
     * @param sx The amount to skew in X
     * @param sy The amount to skew in Y
     * @return self for chaining
     * @see Canvas#skew(float, float)
     */
    public CanvasScript skew(float sx, float sy) {
        parameters.add(new SkewParams(sx, sy));
        return this;
    }


    /**
     * Add a canvas scale to the stack
     * @param sx The amount to scale in X
     * @param sy The amount to scale in Y
     * @return self for chaining
     * @see Canvas#scale(float, float)
     */
    public CanvasScript scale(float sx, float sy) {
        parameters.add(new ScaleParams(sx, sy));
        return this;
    }


    /**
     * Add a canvas scale to the stack
     * @param sx The amount to scale in X
     * @param sy The amount to scale in Y
     * @param px The x-coord for the pivot point (unchanged by the scale)
     * @param py The y-coord for the pivot point (unchanged by the scale)
     * @return self for chaining
     * @see Canvas#scale(float, float, float, float)
     */
    public CanvasScript scale(float sx, float sy, float px, float py) {
        parameters.add(new ScaleParams(sx, sy, px, py));
        return this;
    }


    /**
     * Add a canvas matric concatentation to the stack
     * @param matrix May be null. The matrix to concat to the canvas
     * @return self for chaining
     * @see Canvas#concat(Matrix)
     */
    public CanvasScript concat(@Nullable Matrix matrix) {
        parameters.add(new ConcatParams(matrix));
        return this;
    }


    /**
     * Render all previously compiled commands into a single bitmap and return it
     * @return The bitmap that is a result of all the scripted commands, returns null if this
     *         script wasn't created with a bitmap or bitmap parameters
     */
    @Nullable
    public Bitmap draw() {
        for (CanvasParams parameter : parameters) {
            if (parameter instanceof RestoreParams && currentSaveCount != CanvasParams.NO_SAVE) {
                ((RestoreParams) parameter).setCount(currentSaveCount);
            }

            Log.d(TAG, "Rendering Script (%s): saveCount(%d)", parameter, currentSaveCount);

            int saveCount = parameter.draw(rootCanvas);
            if (saveCount != CanvasParams.NO_SAVE) {
                currentSaveCount = saveCount;
            }
        }
        return bitmap;
    }


    /**
     * Get a copy of the current paint if not null to avoid changing the configuration for subsequent
     * commands later in the chain.
     * @return a duplicate copy of the current paint, or null if there isn't any
     */
    @Nullable
    private Paint getPaintCopy() {
        if (currentPaint != null) {
            return new Paint(currentPaint);
        }
        return null;
    }


    private void checkNonNullPaint() {
        if (currentPaint == null) {
            throw new IllegalStateException("The current Paint state cannot be null, be sure to configure "
                    + "the scripts painting preference");
        }
    }


    private void createPaintIfNull() {
        if (currentPaint == null) {
            currentPaint = new Paint(DEFAULT_PAINT_FLAGS);
        }
    }
}
