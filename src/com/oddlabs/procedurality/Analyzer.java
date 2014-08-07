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

public final class Analyzer {

	public final static void analyze(Channel channel, String name) {
		System.out.println("*** Performing image analysis on \"" + name + "\" ****");
		channel.flipV();
		System.out.print("Intensity map...");
		channel.toLayer().saveAsPNG(name);
		System.out.println("OK");

		System.out.print("Slope map...");
		channel.copy().lineart().normalize().toLayer().saveAsPNG(name + "_slope");
		System.out.println("OK");

		System.out.print("Relative intensity map...");
		channel.copy().relativeIntensity(1).toLayer().saveAsPNG(name + "_relative1");
		channel.copy().relativeIntensity(2).toLayer().saveAsPNG(name + "_relative2");
		channel.copy().relativeIntensity(3).toLayer().saveAsPNG(name + "_relative3");
		channel.copy().relativeIntensity(4).toLayer().saveAsPNG(name + "_relative4");
		channel.copy().relativeIntensity(5).toLayer().saveAsPNG(name + "_relative5");
		System.out.println("OK");

		System.out.print("Intensity histogram...");
		histogram(channel, channel.width).flipV().toLayer().saveAsPNG(name + "_histogram");
		System.out.println("OK");

		System.out.print("Slope histogram...");
		histogram(channel.copy().lineart().normalize(), channel.width).flipV().toLayer().saveAsPNG(name + "_slope_histogram");
		System.out.println("OK");

		System.out.print("Relative intensity histogram...");
		histogram(channel.copy().relativeIntensity(1), channel.width).flipV().toLayer().saveAsPNG(name + "_relative1_histogram");
		histogram(channel.copy().relativeIntensity(2), channel.width).flipV().toLayer().saveAsPNG(name + "_relative2_histogram");
		histogram(channel.copy().relativeIntensity(3), channel.width).flipV().toLayer().saveAsPNG(name + "_relative3_histogram");
		histogram(channel.copy().relativeIntensity(4), channel.width).flipV().toLayer().saveAsPNG(name + "_relative4_histogram");
		histogram(channel.copy().relativeIntensity(5), channel.width).flipV().toLayer().saveAsPNG(name + "_relative5_histogram");
		System.out.println("OK");

		System.out.print("Accessibility map...");
		Channel access = channel.copy().lineart().threshold(0f, 0.025f);
		access.toLayer().saveAsPNG(name + "_access");
		System.out.println("OK");
		System.out.println("% accessible area: " + (100f*access.count(1f)/(access.width*access.height)));
		Channel access_conn = access.copy().largestConnected(1f);
		access_conn.toLayer().saveAsPNG(name + "_access_conn");
		System.out.println("% largest connected accessible area: " + (100f*access_conn.count(1f)/(access.width*access.height)));

		System.out.print("Flatness map...");
		Channel flat = channel.copy().lineart().threshold(0f, 0.005f);
		flat.toLayer().saveAsPNG(name + "_flatness");
		System.out.println("OK");
		System.out.println("% flat area: " + (100f*flat.count(1f)/(flat.width*flat.height)));
		Channel flat_conn = flat.copy().largestConnected(1f);
		flat_conn.toLayer().saveAsPNG(name + "_flatness_conn");
		System.out.println("% largest connected flat area: " + (100f*flat_conn.count(1f)/(flat.width*flat.height)));

		System.out.print("Fourier transform...");
		Channel[] ffts = channel.fft();
		ffts[0].copy().normalize().toLayer().saveAsPNG(name + "_fft_magnitude");
		ffts[0].log().normalize().toLayer().saveAsPNG(name + "_fft_magnitude_log");
		ffts[1].normalize().toLayer().saveAsPNG(name + "_fft_phase");
		System.out.println("OK");

		System.out.println("*** Image analysis complete ****");
	}

	public final static Channel histogram(Channel channel, int size) {
		assert channel.findMin() >= 0 && channel.findMax() <= 1 : "image must be normalized";
		Channel hist = new Channel(size, size).fill(1f);
		int[] histogram = new int[size];
		for (int y = 0; y < channel.width; y++) {
			for (int x = 0; x < channel.height; x++) {
				histogram[(int)(channel.getPixel(x, y)*(size - 1))]++;
			}
		}
		int max = 0;
		for (int i = 0; i < size; i++) {
			if (histogram[i] > max) {
				max = histogram[i];
			}
		}
		float scale = (float)(size)/max;
		for (int x = 0; x < size; x++) {
			int lineheight = (int)(histogram[x]*scale);
			for (int y = 0; y < lineheight; y++) {
				hist.putPixel(x, y, 0f);
			}
		}
		return hist;
	}

}