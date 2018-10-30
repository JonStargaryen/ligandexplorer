package de.bioforscher.ligandexplorer.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Cluster implements NGLRenderable {
    private final String id;
    private final List<StructureIdentifier> structureIdentifiers;
    private final String pdbRepresentation;
    private final List<AlignedInteraction> alignedInteractions;

    public Cluster(String id, List<BindingSite> bindingSites) {
        this.id = id;
        this.structureIdentifiers = bindingSites.stream()
                .map(BindingSite::getStructureIdentifier)
                .collect(Collectors.toList());
        this.pdbRepresentation = bindingSites.stream()
                .map(BindingSite::getAlignedPdbRepresentation)
                .collect(Collectors.joining(System.lineSeparator()));
        this.alignedInteractions = bindingSites.stream()
                .map(BindingSite::getAlignedInteractions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
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
