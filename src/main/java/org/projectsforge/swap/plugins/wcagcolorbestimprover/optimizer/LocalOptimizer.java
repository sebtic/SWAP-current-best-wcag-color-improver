package org.projectsforge.swap.plugins.wcagcolorbestimprover.optimizer;


public interface LocalOptimizer {

  Solution optimize(Solution initialSolution, Solution newSolution);

}
