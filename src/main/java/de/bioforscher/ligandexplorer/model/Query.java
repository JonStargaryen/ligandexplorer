package de.bioforscher.ligandexplorer.model;

import java.util.List;

public class Query {
    private final QueryType queryType;
    private final String query;
    private final List<Ligand> ligands;
    private final int numberOfLigands;

    public Query(QueryType queryType,
                 String query,
                 List<Ligand> ligands) {
        this.queryType = queryType;
        this.query = query;
        this.ligands = ligands;
        this.numberOfLigands = ligands.size();
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public String getQuery() {
        return query;
    }

    public List<Ligand> getLigands() {
        return ligands;
    }

    public int getNumberOfLigands() {
        return numberOfLigands;
    }

    //TODO add further query types: e.g. SMILES, substructure etc
    public enum QueryType {
        NAME
    }
}
