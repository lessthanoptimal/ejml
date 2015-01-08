/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.ops;

import org.ejml.data.D1Matrix64F;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * Renders a matrix as an image.  Positive elements are shades of red, negative shades of blue, 0 is black.
 *
 * @author Peter Abeles
 */
public class MatrixComponent extends JPanel {
    BufferedImage image;

    public MatrixComponent( int width , int height ) {
        image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(width,height));
        setMinimumSize(new Dimension(width,height));
    }

    public synchronized void setMatrix( D1Matrix64F A ) {
        double maxValue = CommonOps.elementMaxAbs(A);
        renderMatrix(A,image,maxValue);
        repaint();
    }

    public static void renderMatrix( D1Matrix64F M , BufferedImage image , double maxValue )
    {
        int w = image.getWidth();
        int h = image.getHeight();

        double widthStep = (double)M.numCols / image.getWidth();
        double heightStep = (double)M.numRows / image.getHeight();

        for( int i = 0; i < h; i++ ) {
            for( int j = 0; j < w; j++ ) {
                double value = M.get( (int)(i*heightStep) , (int)(j*widthStep) );

                if( value == 0 ){
                    image.setRGB(j,i,255 << 24);
                } else if( value > 0 ) {
                    int p = 255-(int)(255.0*(value/maxValue));
                    int rgb = 255 << 24 | 255 << 16 | p << 8 | p;

                    image.setRGB(j,i,rgb);
                } else {
                    int p = 255+(int)(255.0*(value/maxValue));
                    int rgb = 255 << 24 | p << 16 | p << 8 | 255;

                    image.setRGB(j,i,rgb);
                }
            }
        }


    }

    @Override
    public synchronized void paint( Graphics g ) {
        g.drawImage(image,0,0,this);
    }

}
