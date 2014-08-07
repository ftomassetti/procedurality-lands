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

This class demonstrates generation of a rock-like texture using noise- and Voronoi maps.

*/

package com.oddlabs.procedurality;

public final class ExampleTexture {
	
	public final static void main(String[] args) {
		
		int size = 512;
		int seed = 42;
		
		// shared noise maps
		Channel noise8 = new Midpoint(size, 3, 0.45f, seed).toChannel();
		Channel noise256 = new Midpoint(size, 8, 1f, seed).toChannel();
		
		// shared voronoi maps
		float c1 = -1f;
		float c2 = 1f;
		float c3 = 0f;
		Voronoi voronoi;
		Channel voronoi1 = new Voronoi(size, 5, 5, 1, 1f, seed).getDistance(c1, c2, c3);
		voronoi = new Voronoi(size, 7, 7, 1, 1f, seed);
		Channel voronoi2 = voronoi.getDistance(c1, c2, c3);
		voronoi = new Voronoi(size, 11, 11, 1, 1f, seed);
		Channel voronoi3 = voronoi.getDistance(c1, c2, c3);
		voronoi = new Voronoi(size, 13, 13, 1, 1f, seed);
		Channel voronoi4 = voronoi.getDistance(c1, c2, c3);
		
		// shared misc maps
		Channel empty = new Channel(size, size).fill(1f);
		
		// grass
		Channel grass_bump = noise8.copy().rotate(90).channelAdd(noise256.copy().brightness(0.05f));
		Layer grass = new Layer(empty.copy().fill(0.2f), empty.copy().fill(0.45f), empty.copy().fill(0f));
		grass.r.channelAdd(noise8.copy().brightness(0.2f));
		grass.bump(grass_bump, size/256f, 0f, 0.6f, 1f, 1f, 1f, 0f, 0f, 0f);
		
		// dirt
		Channel dirt_bump1 = noise8.copy().brightness(0.8f);
		Channel dirt_bump2 = noise256.copy().brightness(0.1f);
		Channel dirt_bump3 = voronoi4.copy().brightness(0.1f);
		Layer dirt = new Layer(empty.copy(), empty.copy().fill(.7f), empty.copy().fill(0.5f));
		Channel dirt_bump = dirt_bump1.channelAdd(dirt_bump2).channelAdd(dirt_bump3);
		dirt_bump.perturb(noise8, 0.05f);
		dirt.bump(dirt_bump, size/128f, 0f, 0.5f, 1f, 1f, 1f, 0f, 0f, 0f);
		
		// rubble
		Layer rubble = dirt.copy();
		Channel rubble_bump1 = voronoi1.copy().multiply(0.4f);
		Channel rubble_bump2 = voronoi2.copy().multiply(0.3f);
		Channel rubble_bump3 = voronoi3.copy().multiply(0.2f);
		Channel rubble_bump = rubble_bump1.channelAdd(rubble_bump2).channelAdd(rubble_bump3).normalize();
		rubble_bump.perturb(noise8, .05f);
		rubble.bump(rubble_bump, size/128f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 0f);
		
		// rock
		Channel rock_bump1 = voronoi1.copy().normalize().contrast(1.05f).brightness(2f).gamma2().multiply(0.40f);
		Channel rock_bump2 = voronoi2.copy().normalize().contrast(1.05f).brightness(2f).gamma2().multiply(0.35f);
		Channel rock_bump3 = voronoi3.copy().normalize().contrast(1.05f).brightness(2f).gamma2().multiply(0.1f);
		Channel rock_bump = rock_bump1.channelAdd(rock_bump2).channelAdd(rock_bump3).channelAdd(noise256.copy().multiply(0.05f));
		rock_bump.normalize().perturb(noise8, .075f);
		Layer rock = rubble.copy();
		rock.toHSV();
		rock.r = noise8.copy().normalize(0.05f, 0.1f);
		rock.toRGB();
		rock.layerBlend(rubble.multiply(1f, 0.8f, 0.6f), noise8.copy().gamma8().invert().contrast(4f));
		rock.layerBlend(grass.multiply(0.5f), noise8.copy().rotate(90).multiply(0.75f));
		rock.bump(rock_bump, size/192f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f);
		rock.gamma2();

		rock.saveAsPNG("texture");
	}
}
