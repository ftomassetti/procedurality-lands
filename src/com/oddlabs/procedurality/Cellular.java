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

import java.util.Random;

public final strictfp class Cellular {
	// coordinate indexing
	public static final int X = 0;
	public static final int Y = 1;
	public static final int SEED = 2;

	// hitpoint distribution
	public static final int SINGULAR = 0;
	public static final int HEXAGONAL = 1;

	// distance metrics
	public static final int NORMAL = 0;
	public static final int OVAL = 1;
	public static final int SQUARE = 2;
	public static final int MANHATTAN = 3;
	public static final int EUCLIDEAN = 4;

	// value type
	public static final int DISTANCE = 0;
	public static final int HITPOINT = 1;
	public static final int HITCLIP = 2;

	private Random random;
	public Channel channel;
	public Channel dist1;
	public Channel dist2;
	public Channel dist3;

	public Cellular(int width, int height, int x_order, int y_order, int checkradius, float randomness, float[] coefficients, long seed, int distribution_type, int metric_type, int value_type) {
		x_order = Math.max(1, x_order);
		y_order = Math.max(1, y_order);
		checkradius = Math.max(1, checkradius);
		random = new Random(seed);
		float pixelwidth = 2f/Math.min(width, height);

		// create domains
		float[][][] domains = new float[x_order][y_order][3];

		// fill in hitpoints according to distribution_type
		switch (distribution_type) {
			case SINGULAR:
				for (int j = 0; j < y_order; j++) {
					for (int i = 0; i < x_order; i++) {
						domains[i][j][X] = (1 - randomness)*((i + 0.5f)/x_order) + randomness*((i + random.nextFloat())/x_order);
						domains[i][j][Y] = (1 - randomness)*((j + 0.5f)/y_order) + randomness*((j + random.nextFloat())/y_order);
						domains[i][j][SEED] = random.nextFloat();
					}
				}
				break;
			case HEXAGONAL:
				for (int j = 0; j < y_order; j++) {
					for (int i = 0; i < x_order; i++) {
						if ((j & 0x1) == 0) {
							domains[i][j][X] = (1 - randomness)*((float)i/x_order) + randomness*((i + random.nextFloat())/x_order);
						} else {
							domains[i][j][X] = (1 - randomness)*((i + 0.5f)/x_order) + randomness*((i + random.nextFloat())/x_order);
						}
						domains[i][j][Y] = (1 - randomness)*((float)j/y_order) + randomness*((j + random.nextFloat())/y_order);
						domains[i][j][SEED] = random.nextFloat();
					}
				}
				break;
			default:
				assert false: "incorrect distribution_type";
		}

		// create image
		channel = new Channel(width, height);

		// fill in pixelvalues
		for (int y = 0; y < height; y++) {
			float y_coord = (float)y/height;
			int j = (int)(y_coord * y_order);
			for (int x = 0; x < width; x++) {
				float x_coord = (float)x/width;
				float min1 = Float.MAX_VALUE;
				float min2 = Float.MAX_VALUE;
				float min3 = Float.MAX_VALUE;
				float dist = 0;
				float hitpoint1 = 0;
				float hitpoint2 = 0;
				float hitpoint3 = 0;
				int i = (int)(x_coord * x_order);

				// traverse neighboring domains in wrap-around style (for seamless textures)
				for (int l = -checkradius; l <= checkradius; l++) {
					int l_wrap = j + l;
					if (l_wrap < 0 || l_wrap >= y_order) {
						l_wrap = l_wrap % y_order;
						if (l_wrap < 0) l_wrap += y_order;
					}
					for (int k = -checkradius; k <= checkradius; k++) {

						// calculate wrapped domain coords
						int k_wrap = i + k;
						if (k_wrap < 0 || k_wrap >= x_order) {
							k_wrap = k_wrap % x_order;
							if (k_wrap < 0) k_wrap += x_order;
						}

						float dx = 0;
						float dy = 0;

						// calculate distance to current hit point taking wrap-around into consideration
						if (i + k >= 0 && i + k < x_order) {
							dx = Math.abs(domains[k_wrap][l_wrap][X] - x_coord);
						} else if (i + k < 0) {
							dx = Math.abs(1 - domains[k_wrap][l_wrap][X] + x_coord);
						} else if (i + k >= x_order) {
							dx = Math.abs(1 - x_coord + domains[k_wrap][l_wrap][X]);
						}

						if (j + l >= 0 && j + l < y_order) {
							dy = Math.abs(domains[k_wrap][l_wrap][Y] - y_coord);
						} else if (j + l < 0) {
							dy = Math.abs(1 - domains[k_wrap][l_wrap][Y] + y_coord);
						} else if (j + l >= y_order) {
							dy = Math.abs(1 - y_coord + domains[k_wrap][l_wrap][Y]);
						}

						dx*=x_order;
						dy*=y_order;

						// metric type
						switch (metric_type) {
							case NORMAL:
								dist = dx*dx + dy*dy;
								break;
							case OVAL:
								dist = dx*dx*dx + dy*dy*dy;
								break;
							case SQUARE:
								dist = dx*dx*dx*dx + dy*dy*dy*dy;
								break;
							case MANHATTAN:
								dist = dx + dy;//Math.max(dx, dy);
								break;
							case EUCLIDEAN:
								dist = (float)Math.sqrt(dx*dx + dy*dy);
								break;
							default:
								assert false: "incorrect metric_type";
						}

						// maintain F1, F2, F3 and nearest hit point values
						if (value_type == DISTANCE) {
							if (dist <= min1) {
								min3 = min2;
								min2 = min1;
								min1 = dist;
							} else if (dist <= min2 && dist > min1) {
								min3 = min2;
								min2 = dist;
							} else if (dist <= min3 && dist > min2) {
								min3 = dist;
							}
						} else {
							if (dist <= min1) {
								min3 = min2;
								min2 = min1;
								min1 = dist;
								hitpoint1 = domains[k_wrap][l_wrap][SEED];
							} else if (dist <= min2 && dist > min1) {
								min3 = min2;
								min2 = dist;
								hitpoint2 = domains[k_wrap][l_wrap][SEED];
							} else if (dist <= min3 && dist > min2) {
								min3 = dist;
								hitpoint3 = domains[k_wrap][l_wrap][SEED];
							}
						}
					}
				}

				// calculate final pixel values
				switch (value_type) {
					case DISTANCE:
						channel.putPixel(x, y, coefficients[0]*min1 + coefficients[1]*min2 + coefficients[2]*min3);
						break;
					case HITPOINT:
						channel.putPixel(x, y, coefficients[0]*hitpoint1 + coefficients[1]*hitpoint2 + coefficients[2]*hitpoint3);
						break;
					case HITCLIP:
						if (coefficients[0]*min1 + coefficients[1]*min2 + coefficients[2]*min3 < pixelwidth) {
							channel.putPixel(x, y, 0f);
						} else {
							channel.putPixel(x, y, 1f);
						}
						break;
					default:
						assert false: "incorrect value_type";
				}
			}
		}

		// normalize image
		channel = channel.normalize();
	}

	public final Layer toLayer() {
		return new Layer(channel, channel, channel);
	}

	public final Channel toChannel() {
		return channel;
	}

}
