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

/*

This class demonstrates the simple image analysis program used in Realtime Procedural Terrain Generation (http://oddlabs.com/download/terrain_generation.pdf)

*/

package com.oddlabs.procedurality;

public final class ExampleAnalyzer {
	
	public final static void main(String[] args) {
		
		// parameters
		int size = 512; // image size
		int seed = 42; // random seed
		int features = 4; // frequency of mountainpeaks [1...size]
		float hills = 0.5f; // overall roughness of the terrain [0f...1f]
		
		// start off with some noise
		Channel height = new Mountain(size, Utils.powerOf2Log2(size) - 6, 0.5f, seed).toChannel();
		height.copy().toLayer().saveAsPNG("terrain1");
		
		// add mountain peaks
		Voronoi voronoi = new Voronoi(size, features, features, 1, 1f, seed);
		Channel cliffs = voronoi.getDistance(-1f, 1f, 0f).brightness(1.5f).multiply(0.33f);
		height.multiply(0.67f).channelAdd(cliffs);
		height.copy().toLayer().saveAsPNG("terrain2");
		
		// punch a few holes
		height.channelSubtract(voronoi.getDistance(1f, 0f, 0f).gamma(.5f).flipV().rotate(90));
		height.copy().toLayer().saveAsPNG("terrain3");
		
		// let an ice age pass
		height.perturb(new Midpoint(size, 2, 0.5f, seed).toChannel(), 0.25f);
		height.copy().toLayer().saveAsPNG("terrain4");
		
		// let it rain for a couple of thousand years
 		height.erode((24f - hills*12f)/size, size>>2);
 		height.copy().toLayer().saveAsPNG("terrain5");
 		
 		// smooth things out to avoid jagged lines
		height.smooth(1);
		height.copy().toLayer().saveAsPNG("terrain6");
		
		// analyze it!
		Analyzer.analyze(height, "analysis");
		
	}
}
