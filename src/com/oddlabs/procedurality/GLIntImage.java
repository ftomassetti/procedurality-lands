/*
 *  Procedurality v. 0.1 rev. 20070307
 *  Copyright 2007 Oddlabs ApS
 *
 *
 *  This file is part of Procedurality.
 *  Procedurality is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *
 *  Procedurality is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package com.oddlabs.procedurality;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public final strictfp class GLIntImage extends GLImage {
	private final IntBuffer pixels;

	public final int getPixelSize() {
		return 4;
	}

	public final IntBuffer getIntPixels() {
		return pixels;
	}

	public GLIntImage(int width, int height, ByteBuffer pixel_data, int format) {
		super(width, height, pixel_data, format);
		pixels = pixel_data.asIntBuffer();
	}

	public GLIntImage(int width, int height, int format) {
		this(width, height, ByteBuffer.allocateDirect(width*height*4), format);
	}

	public GLIntImage(Layer layer) {
		this(layer.getWidth(), layer.getHeight(), 0);
		for (int y = 0; y < getHeight(); y++)
			for (int x = 0; x < getWidth(); x++) {
				int ri = ((int)(layer.r.getPixel(x, y)*255 + .5f)) & 0xff;
				int gi = ((int)(layer.g.getPixel(x, y)*255 + .5f)) & 0xff;
				int bi = ((int)(layer.b.getPixel(x, y)*255 + .5f)) & 0xff;
				int ai;
				if (layer.a != null) {
					ai = ((int)(layer.a.getPixel(x, y)*255 + .5f)) & 0xff;
				} else {
					ai = 255;
				}
				int pixel = (ri << 24) | (gi << 16) | (bi << 8) | ai;
				putPixel(x, y, pixel);
			}
	}

	public final GLImage createImage(int width, int height, int format) {
		return new GLIntImage(width, height, format);
	}

	public final int getPixel(int x, int y) {
		return pixels.get(y*getWidth() + x);
	}

	public final void putPixel(int x, int y, int pixel) {
		pixels.put(y*getWidth() + x, pixel);
	}
}
