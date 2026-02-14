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

package org.dynamisfx.model;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TreeItem;
import org.dynamisfx.DynamisFXSample;

public class SampleTree {

    private TreeNode root;

    private int count = 0;

    public SampleTree(DynamisFXSample rootSample) {
        root = new TreeNode(null, null, rootSample);
    }

    public TreeNode getRoot() {
        return root;
    }

    public Object size() {
        return count;
    }

    public void addSample(String[] packages, DynamisFXSample sample) {
        if (packages.length == 0) {
            root.addSample(sample);
            return;
        }

        TreeNode n = root;
        for (String packageName : packages) {
            if (n.containsChild(packageName)) {
                n = n.getChild(packageName);
            } else {
                TreeNode newNode = new TreeNode(packageName);
                n.addNode(newNode);
                n = newNode;
            }
        }

        if (n.packageName.equals(packages[packages.length - 1])) {
            n.addSample(sample);
            count++;
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public static class TreeNode {

        private final DynamisFXSample sample;
        private final String packageName;

        private final TreeNode parent;
        private List<TreeNode> children;

        public TreeNode() {
            this(null, null, null);
        }

        public TreeNode(String packageName) {
            this(null, packageName, null);
        }

        public TreeNode(TreeNode parent, String packageName, DynamisFXSample sample) {
            this.children = new ArrayList<>();
            this.sample = sample;
            this.parent = parent;
            this.packageName = packageName;
        }

        public boolean containsChild(String packageName) {
            if (packageName == null) {
                return false;
            }

            return children.stream().anyMatch((n) -> (packageName.equals(n.packageName)));
        }

        public TreeNode getChild(String packageName) {
            if (packageName == null) {
                return null;
            }

            for (TreeNode n : children) {
                if (packageName.equals(n.packageName)) {
                    return n;
                }
            }
            return null;
        }

        public void addSample(DynamisFXSample sample) {
            children.add(new TreeNode(this, null, sample));
        }

        public void addNode(TreeNode n) {
            children.add(n);
        }

        public DynamisFXSample getSample() {
            return sample;
        }

        public String getPackageName() {
            return packageName;
        }

        public TreeItem<DynamisFXSample> createTreeItem() {
            TreeItem<DynamisFXSample> treeItem = null;

            if (sample != null) {
                treeItem = new TreeItem<>(sample);
            } else if (packageName != null) {
                treeItem = new TreeItem<>(new EmptySample(packageName));
            }
            if (treeItem != null) {
                treeItem.setExpanded(true);

                // recursively add in children
                for (TreeNode n : children) {
                    treeItem.getChildren().add(n.createTreeItem());
                }
            }
            return treeItem;
        }

        @Override
        public String toString() {
            if (sample != null) {
                return " Sample [ sampleName: " + sample.getSampleName() + ", children: " + children + " ]";
            } else {
                return " Sample [ packageName: " + packageName + ", children: " + children + " ]";
            }
        }
    }
}
