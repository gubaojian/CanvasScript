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

package com.ftinc.canvasscript.params;


import android.graphics.Canvas;


public class TranslateParams implements CanvasParams {

    private final float dx;
    private final float dy;

    /**
     * @see Canvas#translate(float, float)
     */
    public TranslateParams(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }


    @Override
    public int draw(Canvas canvas) {
        canvas.translate(dx, dy);
        return NO_SAVE;
    }
}
