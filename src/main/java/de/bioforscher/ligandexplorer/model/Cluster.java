package de.bioforscher.ligandexplorer.model;

import java.util.List;

public class Cluster implements NGLRenderable {
    private final String id;
    private final List<StructureIdentifier> structureIdentifiers;
    private final String pdbRepresentation;
    private final List<AlignedInteraction> alignedInteractions;

    public Cluster(String id,
                   List<StructureIdentifier> structureIdentifiers,
                   String pdbRepresentation,
                   List<AlignedInteraction> alignedInteractions) {
        this.id = id;
        this.structureIdentifiers = structureIdentifiers;
        this.pdbRepresentation = pdbRepresentation;
        this.alignedInteractions = alignedInteractions;
    }

    public String getId() {
        return id;
    }

    public List<AlignedInteraction> getAlignedInteractions() {
        return alignedInteractions;
    }

    public List<StructureIdentifier> getStructureIdentifiers() {
        return structureIdentifiers;
    }

    @Override
    public String getPdbRepresentation() {
        return pdbRepresentation;
    }
}
