package de.bioforscher.ligandexplorer.service.ligand;

import de.bioforscher.jstructure.mathematics.Transformation;
import de.bioforscher.jstructure.model.structure.Group;

import java.util.List;

public interface LigandAligner {
    List<Transformation> alignLigands(List<Group> ligands);
}
