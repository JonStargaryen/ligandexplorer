package de.bioforscher.ligandexplorer.service.query;

import de.bioforscher.ligandexplorer.model.Query;

/**
 * Resolve queries for certain ligands.
 */
public interface QueryResolver {
    /**
     * Process a query and retrieve a handle to all matched ligands.
     * @param query a query string, e.g. the ligand name, SMILES, trivial name
     * @return the resolved query, summarizing the query and all matched ligands
     */
    Query getQuery(String query);
}
