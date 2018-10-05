package de.bioforscher.ligandexplorer.model;

import java.util.List;

public class Ligand {
    private final String id;
    private final String name;
    private final int numberOfStructures;
    private final String pdbRepresentation;
    private final List<String> pdbIds;

    public Ligand(String shortname,
                  String fullname,
                  String pdbRepresentation,
                  List<String> pdbIds) {
        this.id = shortname;
        this.name = fullname;
        this.numberOfStructures = pdbIds.size();
        this.pdbRepresentation = pdbRepresentation;
        this.pdbIds = pdbIds;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfStructures() {
        return numberOfStructures;
    }

    public String getPdbRepresentation() {
        return pdbRepresentation;
    }

    public List<String> getPdbIds() {
        return pdbIds;
    }
}
