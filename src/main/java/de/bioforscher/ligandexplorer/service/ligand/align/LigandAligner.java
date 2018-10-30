package de.bioforscher.ligandexplorer.service.ligand.align;

import de.bioforscher.ligandexplorer.model.BindingSite;

import java.util.List;

/**
 * Align binding sites in their ligands.
 */
public interface LigandAligner {
    void alignBindingSites(List<BindingSite> bindingSites);
}
