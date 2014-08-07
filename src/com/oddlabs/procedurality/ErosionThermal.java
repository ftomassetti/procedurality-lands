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

import java.util.*;

public final class ErosionThermal {

	public final static Channel erode1(Channel channel, float min_threshold, float max_threshold, int iterations) {
		assert min_threshold >= 0 && min_threshold < 1 && max_threshold > 0 && max_threshold <= 1 && min_threshold < max_threshold : "invalid threshold values";
		Channel diff = new Channel(channel.width, channel.height);
		for (int i = 0; i < iterations; i++) {
			// save frames
			if (channel.width > 128 && i%10 == 0) {
				if (i < 10) {
					channel.toLayer().saveAsPNG("erosion00" + i);
				} else if (i < 100) {
					channel.toLayer().saveAsPNG("erosion0" + i);
				} else {
					channel.toLayer().saveAsPNG("erosion" + i);
				}
			}
			// material transport
			for (int y = 1; y < channel.height - 1; y++) {
				for (int x = 1; x < channel.width - 1; x++) {
					// calculate height differences
					float h = channel.getPixel(x, y);
					float h1 = channel.getPixel(x, y + 1);
					float h2 = channel.getPixel(x - 1, y);
					float h3 = channel.getPixel(x + 1, y);
					float h4 = channel.getPixel(x, y - 1);
					float d1 = h - h1;
					float d2 = h - h2;
					float d3 = h - h3;
					float d4 = h - h4;
					// find greatest height difference
					float max_diff = Float.MIN_VALUE;
					float total_diff = 0;
					if (d1 > 0) {
						total_diff+= d1;
						if (d1 > max_diff) {
							max_diff = d1;
						}
					}
					if (d2 > 0) {
						total_diff+= d2;
						if (d2 > max_diff) {
							max_diff = d2;
						}
					}
					if (d3 > 0) {
						total_diff+= d3;
						if (d3 > max_diff) {
							max_diff = d3;
						}
					}
					if (d4 > 0) {
						total_diff+= d4;
						if (d4 > max_diff) {
							max_diff = d4;
						}
					}
					// skip if outside talus threshold
					if (max_diff < min_threshold && max_diff > max_threshold) {
						continue;
					}
					max_diff = max_diff/2f;
					float factor = max_diff/total_diff;
					diff.putPixel(x, y, diff.getPixel(x, y) - max_diff);
					// transport material
					if (d1 > 0)
						diff.putPixel(x, y + 1, diff.getPixel(x, y + 1) + factor*d1);
					if (d2 > 0)
						diff.putPixel(x - 1, y, diff.getPixel(x - 1, y) + factor*d2);
					if (d3 > 0)
						diff.putPixel(x + 1, y, diff.getPixel(x + 1, y) + factor*d3);
					if (d4 > 0)
						diff.putPixel(x, y - 1, diff.getPixel(x, y - 1) + factor*d4);
				}
			}
			// apply changes to channel
			channel.channelAddNoClip(diff);
			diff.fill(0f);
		}
		return channel;
	}

}