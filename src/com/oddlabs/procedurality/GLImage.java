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

public abstract strictfp class GLImage {
	private final int width;
	private final int height;
	private final int format;
	private final ByteBuffer pixel_data;

	public final int getWidth() {
		return width;
	}

	public final int getHeight() {
		return height;
	}

	public abstract int getPixel(int x, int y);

	public abstract void putPixel(int x, int y, int pixel);

	public GLImage(int width, int height, ByteBuffer pixel_data, int format) {
		this.width = width;
		this.height = height;
		this.pixel_data = pixel_data;
		this.format = format;
	}

	public final int getGLFormat() {
		return format;
	}

	public abstract GLImage createImage(int width, int height, int format);

	public final GLImage[] createMipMaps() {
		GLImage[] result = buildMipMaps();
		updateMipMapsArea(result, 10000, 1.0f, 0, 0, getWidth(), getHeight(), false);
		return result;
	}

	public final GLImage[] buildMipMaps() {
		int current_width = width;
		int current_height = height;
		int max = Math.max(height, width);
		int max_level = (int)(Math.log(max)/Math.log(2));
		GLImage[] result = new GLImage[max_level + 1];
		result[0] = this;
		for (int i = 1; i < result.length; i++) {
			current_width /= 2;
			current_height /= 2;
			if (current_height == 0)
				current_height = 1;
			if (current_width == 0)
				current_width = 1;
			result[i] = createImage(current_width, current_height, format);
		}
		return result;
	}

	public final static void updateMipMapsArea(GLImage[] mipmaps, int base_fadeout_level, float fadeout_factor, int start_x, int start_y, int width, int height, boolean max_alpha) {
		for (int i = 1; i < mipmaps.length; i++) {
			int height_div = mipmaps[i - 1].getHeight()/mipmaps[i].getHeight();
			int width_div = mipmaps[i - 1].getWidth()/mipmaps[i].getWidth();
			start_x /= width_div;
			start_y /= height_div;
			width = (int)Math.ceil((float)width/width_div);
			height = (int)Math.ceil((float)height/height_div);
			for (int y = start_y; y < start_y + height; y++)
				for (int x = start_x; x < start_x + width; x++)
					mipmaps[i].putPixel(x, y, averagePixel(mipmaps[i - 1], width_div*x, height_div*y, height_div, width_div, base_fadeout_level, fadeout_factor, i, max_alpha));
		}
	}

	public final static void blendMipMapsArea(GLImage[] dest_mipmaps, GLImage[] source_mipmaps, int base_fadeout_level, float fadeout_factor, int start_x, int start_y, int width, int height) {
		int mip_map_level = 0;
		while (source_mipmaps[0].getWidth() != dest_mipmaps[mip_map_level].getWidth() && source_mipmaps[0].getHeight() != dest_mipmaps[mip_map_level].getHeight())
			mip_map_level++;
		for (int i = 1; i < dest_mipmaps.length; i++) {
			int height_div = dest_mipmaps[i - 1].getHeight()/dest_mipmaps[i].getHeight();
			int width_div = dest_mipmaps[i - 1].getWidth()/dest_mipmaps[i].getWidth();
			start_x /= width_div;
			start_y /= height_div;
			width = (int)Math.ceil((float)width/width_div);
			height = (int)Math.ceil((float)height/height_div);
			if (i >= base_fadeout_level) {
				if (i >= mip_map_level)
					dest_mipmaps[i].drawImageBlended(source_mipmaps[i - mip_map_level], start_x, start_y, start_x, start_y, width, height, 1.0f - fadeout_factor);
				fadeout_factor *= fadeout_factor;
			}
		}
	}

	private final static int averagePixel(GLImage last_img, int x, int y, int height_div, int width_div, int base_fadeout_level, float fadeout_factor, int current_level, boolean max_alpha) {
		float inv_num_averaged = 1f/(height_div * width_div);
		int col1 = 0;
		int col2 = 0;
		int col3 = 0;
		int col4 = 0;
		for (int offset_y = 0; offset_y < height_div; offset_y++)
			for (int offset_x = 0; offset_x < width_div; offset_x++) {
				int pixel = last_img.getPixel(x + offset_x, y + offset_y);
				col1 += (pixel >>> 24);
				col2 += (pixel >>> 16) & 0xff;
				col3 += (pixel >>> 8) & 0xff;
				col4 += pixel & 0xff;
			}
		if (current_level >= base_fadeout_level) {
			col1 = (int)(col1*fadeout_factor);
			col2 = (int)(col2*fadeout_factor);
			col3 = (int)(col3*fadeout_factor);
			col4 = (int)(col4*fadeout_factor);
		}
		col1 = (int)(col1*inv_num_averaged);
		col2 = (int)(col2*inv_num_averaged);
		col3 = (int)(col3*inv_num_averaged);
		col4 = (int)(col4*inv_num_averaged);
		if (max_alpha) {
			if (col4 >= 128)
				col4 = 255;
		}
		return (col1 << 24) + (col2 << 16) + (col3 << 8) + col4;
	}

	public final void clearAll(int color) {
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				putPixel(x, y, color);
	}

	public final void clear(int x, int y, int width, int height, int color) {
		int yy = y;
		for (; yy < y + height; yy++) {
			for (int xx = x; xx < x + width; xx++)
				putPixel(yy, xx, color);
		}
	}

	public final void drawImageBlended(GLImage img, int dx, int dy, int sx, int sy, int w, int h, float alpha_factor) {
		int spixel;
		int dpixel;
		int sr;
		int sg;
		int sb;
		int sa;
		int sa_inverse;
		int dr;
		int dg;
		int db;
		int da;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				spixel = img.getPixel(x + sx, y + sy);
				sa = (spixel >>> 24);
				if (alpha_factor != 1f) {
					sa = Math.round(sa * alpha_factor) & 0xff;
				}
				if (sa == 0) {
					continue;
				}
				if (sa == 255) {
					putPixel(x + dx, y + dy, spixel);
					continue;
				}
				sa_inverse = 255 - sa;
				dpixel = getPixel(x + dx, y + dy);
				sr = spixel >>> 16 & 0xff; sg = spixel >>> 8 & 0xff; sb = spixel & 0xff;
				dr = dpixel >>> 16 & 0xff; dg = dpixel >>> 8 & 0xff; db = dpixel & 0xff; da = dpixel >>> 24;
				putPixel(x + dx, y + dy, (((sa * sa + da * sa_inverse)/255) << 24) + (((sr * sa + dr * sa_inverse)/255) << 16) + ((sg * sa + dg * sa_inverse)/255 << 8) + ((sb * sa + db * sa_inverse)/255));
			}
		}
	}

	protected abstract int getPixelSize();

	public final ByteBuffer getPixels() {
		return pixel_data;
	}

	public final void drawImage(GLImage img, int dx, int dy, int sx, int sy, int w, int h) {
		int pixel_size = getPixelSize();
		assert pixel_size == img.getPixelSize();
		ByteBuffer pixels = getPixels();
		ByteBuffer other_pixels = img.getPixels();
		int byte_width = w*pixel_size;
		for (int i = 0; i < h; i++) {
			int other_pos = ((sy+i)*img.getWidth() + sx)*pixel_size;
			int pos = ((dy+i)*getWidth() + dx)*pixel_size;
			other_pixels.position(other_pos);
			other_pixels.limit(other_pos + byte_width);
			pixels.position(pos);
			pixels.limit(pos + byte_width);
			pixels.put(other_pixels);
			pixels.clear();
			other_pixels.clear();
		}
	}

	public Layer toLayer() {
		int width = getWidth();
		int height = getHeight();
		Channel r = new Channel(width, height);
		Channel g = new Channel(width, height);
		Channel b = new Channel(width, height);
		Channel a = new Channel(width, height);
		int pixel;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixel = getPixel(x, y);
				a.putPixel(x, y, (pixel>>24)/255f);
				r.putPixel(x, y, ((pixel>>16) & 0xff)/255f);
				g.putPixel(x, y, ((pixel>>8) & 0xff)/255f);
				b.putPixel(x, y, (pixel & 0xff)/255f);
			}
		}
		return new Layer(r, g, b, a);
	}

	public final void saveAsPNG(String filename) {
		toLayer().saveAsPNG(filename);
	}
}
