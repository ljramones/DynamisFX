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

public class TetrahedronMesh extends TexturedMesh {

	private static final double DEFAULT_HEIGHT = 100.0D;

	public TetrahedronMesh(){
		this(DEFAULT_HEIGHT);
	}

	public TetrahedronMesh(double height) {
		setHeight(height);
		setDepthTest(DepthTest.ENABLE);
		updateMesh();
    }

	@Override
	protected final void updateMesh() {
		setMesh(null);
		mesh = createTetrahedron((float)getHeight());
		setMesh(mesh);
	}

	private TriangleMesh createTetrahedron(double height){

		TriangleMesh m = new TriangleMesh();

		float he = (float)height;

		m.getPoints().addAll(
				0  ,  			 0 	    	   , (float)(-he/4) ,	///point O
			    0  , (float)(he/(Math.sqrt(3))) , (float)(he/4),	///point A
		(float)(-he/2) , (float)(-he/(2*Math.sqrt(3))) , (float)(he/4) , ///point B
		(float)(he/2)  , (float)(-he/(2*Math.sqrt(3))) , (float)(he/4)   ///point C
				);


		m.getTexCoords().addAll(0,0);

		m.getFaces().addAll(
				1 , 0 , 0 , 0 , 2 , 0 ,		// A-O-B
				2 , 0 , 0 , 0 , 3 , 0 ,		// B-O-C
				3 , 0 , 0 , 0 , 1 , 0 ,		// C-O-A
				1 , 0 , 2 , 0 , 3 , 0  		// A-B-C
				);


		return m;

	}


	 /*
    	Properties
	  */

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
