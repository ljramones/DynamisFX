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

package org.dynamisfx.shapes.primitives;

import javafx.beans.property.DoubleProperty;
import javafx.scene.DepthTest;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.shapes.primitives.helper.MeshProperty;

/**
 *
 * @author Moussaab AMRINE dy_amrine@esi.dz
 * @author  Yehya BELHAMRA dy_belhamra@esi.dz
 */

public class PyramidMesh extends TexturedMesh {

	private static final double DEFAULT_HEIGHT = 100.0D;
    private static final double DEFAULT_HYPOTENUSE = 100.0D;

	public PyramidMesh(){
		this(DEFAULT_HEIGHT, DEFAULT_HYPOTENUSE);
	}

	public PyramidMesh(double height, double hypotenuse) {
		setHypotenuse(hypotenuse);
		setHeight(height);
		setDepthTest(DepthTest.ENABLE);
		updateMesh();
    }

	@Override
	protected final void updateMesh() {
		setMesh(null);
		mesh = createPyramid(getHypotenuse(), (float)getHeight());
		setMesh(mesh);
	}

	private TriangleMesh createPyramid(double hypotenuse, double height){

		TriangleMesh m = new TriangleMesh();

		float hy = (float)hypotenuse;
		float he = (float)height;

		m.getPoints().addAll(
				  0 ,   0 ,   0,    //point O
				  0 ,  he , -hy/2,  //point A
				-hy/2, he ,   0,    //point B
				 hy/2, he ,   0,	//point C
				  0 ,  he ,  hy/2	//point D
				);


		m.getTexCoords().addAll(0,0);

		m.getFaces().addAll(
				0 , 0 , 2 , 0 , 1 , 0 ,		// O-B-A
				0 , 0 , 1 , 0 , 3 , 0 ,		// O-A-C
				0 , 0 , 3 , 0 , 4 , 0 ,		// O-C-D
				0 , 0 , 4 , 0 , 2 , 0 ,		// O-D-B
				4 , 0 , 1 , 0 , 2 , 0 ,		// D-A-B
				4 , 0 , 3 , 0 , 1 , 0 		// D-C-A
				);


		return m;

	}


	 /*
    	Properties
	  */
	private final DoubleProperty hypotenuse = MeshProperty.createDoubleUnguarded(
			DEFAULT_HYPOTENUSE, this::updateMesh);

	public final double getHypotenuse() {
		return hypotenuse.get();
	}

	public final void setHypotenuse(double value) {
		hypotenuse.set(value);
	}

	public DoubleProperty hypotenuseProperty() {
		return hypotenuse;
	}


	private final DoubleProperty height = MeshProperty.createDoubleUnguarded(
			DEFAULT_HEIGHT, this::updateMesh);

    public final double getHeight() {
        return height.get();
    }

    public final void setHeight(double value) {
        height.set(value);
    }

    public DoubleProperty heightProperty() {
        return height;
    }

}
