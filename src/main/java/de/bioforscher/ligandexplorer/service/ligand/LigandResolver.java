package de.bioforscher.ligandexplorer.service.ligand;

import de.bioforscher.ligandexplorer.model.Ligand;

/**
 * Fetch ligand information.
 */
public interface LigandResolver {
    /**
     * Fetch all relevant information for a given three-letter-code of a ligand. Wrap it into a {@link Ligand} instance.
     * @param ligandName the three-letter-code of the ligand
     * @return a container with all information
     */
    Ligand getLigand(String ligandName);
}
