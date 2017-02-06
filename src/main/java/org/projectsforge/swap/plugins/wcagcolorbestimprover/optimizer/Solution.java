package org.projectsforge.swap.plugins.wcagcolorbestimprover.optimizer;

import org.projectsforge.swap.core.mime.css.property.color.dictionary.ArrayBackedColorDictionary;

/**
 * The class storing the color dictionary
 * 
 * @author Sébastien Aupetit
 * 
 */
public class Solution {
	private final ArrayBackedColorDictionary colorDictionary;

	private final double fitness;

	public Solution(final ArrayBackedColorDictionary colorDictionary,
			final double fitness) {
		this.colorDictionary = colorDictionary;
		this.fitness = fitness;
	}

	public ArrayBackedColorDictionary getColorDictionary() {
		return colorDictionary;
	}

	public double getFitness() {
		return fitness;
	}
}
