package de.bioforscher.ligandexplorer.model;

import java.util.List;

public class Cluster implements NGLRenderable {
    private final String id;
    private final List<StructureIdentifier> structureIdentifiers;
    private final String pdbRepresentation;

    public Cluster(String id,
                   List<StructureIdentifier> structureIdentifiers,
                   String pdbRepresentation) {
        this.id = id;
        this.structureIdentifiers = structureIdentifiers;
        this.pdbRepresentation = pdbRepresentation;
    }

    public String getId() {
        return id;
    }

    public List<StructureIdentifier> getStructureIdentifiers() {
        return structureIdentifiers;
    }

    @Override
    public String getPdbRepresentation() {
        return pdbRepresentation;
    }

}
