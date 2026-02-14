/*
 * Copyright 2024-2026 DynamisFX Contributors
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
 *
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */

package org.dynamisfx.shapes.primitives.helper.delaunay.jdt;

import java.io.Serializable;

/**
 * @author Yonatan Graber https://github.com/yonatang/JDT
 *
 * this class represents a simple circle. <br />
 * it is used by the Delaunay Triangulation class. <br />
 * <br />
 * note that this class is immutable.
 * 
 * @see DelaunayTriangulation
 */
@SuppressWarnings("serial")
public class Circle implements Serializable {

	private Point center;
	private double radius;

	/**
	 * Constructor. <br />
	 * Constructs a new Circle
	 * 
	 * @param c
	 *            Center of the circle.
	 * @param r
	 *            Radius of the circle.
	 */
	public Circle(Point c, double r) {
		this.center = c;
		this.radius = r;
	}

	/**
	 * Copy Constructor. <br />
	 * Creates a new Circle with same properties of <code>circ</code>.
	 * 
	 * @param circ
	 *            Circle to clone.
	 */
	public Circle(Circle circ) {
		this.center = circ.center;
		this.radius = circ.radius;
	}

	@Override
	public String toString() {
		return "Circle [center=" + center + ", raduis=" + radius + "]";
	}

	/**
	 * Gets the center of the circle.
	 * 
	 * @return the center of the circle.
	 */
	public Point center() {
		return this.center;
	}

	/**
	 * Gets the radius of the circle.
	 * 
	 * @return the radius of the circle.
	 */
	public double radius() {
		return this.radius;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((center == null) ? 0 : center.hashCode());
		long temp;
		temp = Double.doubleToLongBits(radius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Circle other = (Circle) obj;
		if (center == null) {
			if (other.center != null)
				return false;
		} else if (!center.equals(other.center))
			return false;
		if (Double.doubleToLongBits(radius) != Double.doubleToLongBits(other.radius))
			return false;
		return true;
	}

}
