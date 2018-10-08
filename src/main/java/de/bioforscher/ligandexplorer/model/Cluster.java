package de.bioforscher.ligandexplorer.model;

import java.util.List;

public class Cluster implements NGLRenderable {
    private final List<StructureIdentifier> structureIdentifiers;
    private final String name;
    private final String pdbRepresentation;

    public Cluster(List<StructureIdentifier> structureIdentifiers,
                   String name,
                   String pdbRepresentation) {
        this.structureIdentifiers = structureIdentifiers;
        this.name = name;
        this.pdbRepresentation = pdbRepresentation;
    }

    public String getName() {
        return name;
    }

    public List<StructureIdentifier> getStructureIdentifiers() {
        return structureIdentifiers;
    }

    @Override
    public String getPdbRepresentation() {
        return pdbRepresentation;
    }

}
