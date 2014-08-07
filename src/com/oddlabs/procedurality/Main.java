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

This class demonstrates generation of an eroded terrain with lots of flats and smooth ramps betweens areas of different height, much in the same way as it is done in Tribal Trouble (http://oddlabs.com/technology.php).

The erode(float talus-angle, int iterations) method is the inverse thermal erosion algorithm described in Realtime Procedural Terrain Generation (http://oddlabs.com/download/terrain_generation.pdf)

For standard thermal erosion, use erodeThermal(float talus-angle, int iterations)

*/

package com.oddlabs.procedurality;

public final class Main {
	
	public final static void main(String[] args) {
		
		System.out.println("Procedurality v. 0.1 rev. 20070305\n");
		System.out.println("For examples, try:\n");
		System.out.println("ant example-terrain");
		System.out.println("ant example-texture");
		System.out.println("ant example-grass");
		System.out.println("ant example-hydraulic");
		System.out.println("ant example-analyzer");
		
	}
}
