package de.bioforscher.ligandexplorer.model;

import de.bioforscher.jstructure.feature.plip.model.HydrophobicInteraction;
import de.bioforscher.jstructure.feature.plip.model.Interaction;
import de.bioforscher.jstructure.feature.plip.model.InteractionContainer;
import de.bioforscher.jstructure.mathematics.Transformation;
import de.bioforscher.jstructure.model.structure.Group;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BindingSite {
    private final StructureIdentifier structureIdentifier;
    private final Group originalLigand;
    private Group alignedLigand;
    private final List<Interaction> originalInteractions;
    private List<AlignedInteraction> alignedInteractions;
    private final List<Group> originalBindingSiteResidues;
    private List<Group> alignedBindingSiteResidues;
    private Transformation transformation;
    private BitTensor bitTensor;

    public BindingSite(StructureIdentifier structureIdentifier,
                       Group ligand,
                       InteractionContainer globalInteractionContainer) {
        this.structureIdentifier = structureIdentifier;
        this.originalLigand = ligand;
        this.originalInteractions = globalInteractionContainer.getSubsetOfInteractions(ligand)
                .getInteractions()
                .stream()
                //FIXME propagate hydrophobic interactions to front-end
                .filter(interaction -> !(interaction instanceof HydrophobicInteraction))
                .collect(Collectors.toList());
        this.originalBindingSiteResidues = originalInteractions.stream()
                .map(interaction -> interaction.getOpposingGroup(ligand))
                .collect(Collectors.toList());
    }

    public StructureIdentifier getStructureIdentifier() {
        return structureIdentifier;
    }

    public Group getOriginalLigand() {
        return originalLigand;
    }

    public List<Interaction> getOriginalInteractions() {
        return originalInteractions;
    }

    public List<Group> getOriginalBindingSiteResidues() {
        return originalBindingSiteResidues;
    }

    public Transformation getTransformation() {
        return transformation;
    }

    public List<AlignedInteraction> getAlignedInteractions() {
        return alignedInteractions;
    }

    public void setTransformation(Transformation transformation) {
        this.transformation = transformation;

        // interactions
        this.alignedInteractions = originalInteractions.stream()
                .map(interaction -> new AlignedInteraction(interaction, transformation))
                .collect(Collectors.toList());

        // ligand
        this.alignedLigand = originalLigand.createDeepCopy();
        alignedLigand.calculate().transform(transformation);

        // binding site residues
        this.alignedBindingSiteResidues = originalBindingSiteResidues.stream()
                .map(Group::createDeepCopy)
                .collect(Collectors.toList());
        alignedBindingSiteResidues.forEach(alignedBindingSiteResidue -> alignedBindingSiteResidue.calculate().transform(transformation));
    }

    public Group getAlignedLigand() {
        return alignedLigand;
    }

    public List<Group> getAlignedBindingSiteResidues() {
        return alignedBindingSiteResidues;
    }

    public String getAlignedPdbRepresentation() {
        return Stream.of(alignedLigand.getPdbRepresentation()/*alignedLigand.atoms()
                        //TODO some PDB representations (e.g. trying to ignore hydrogen atoms) are insanely slow in NGL
                .filter(atom -> atom.getElement().isHeavyAtom())
                .map(Atom::getPdbRepresentation)
                .collect(Collectors.joining(System.lineSeparator()))*/,
                alignedBindingSiteResidues.stream()
                        //FIXME remove to propagate binding site residues to front-end
//                        .limit(0)
                        .map(Group::getPdbRepresentation)
//                        .flatMap(Group::atoms)
//                        .filter(atom -> atom.getElement().isHeavyAtom())
//                        .map(Atom::getPdbRepresentation)
                        .collect(Collectors.joining(System.lineSeparator())))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public BitTensor getBitTensor() {
        return bitTensor;
    }

    public void setBitTensor(BitTensor bitTensor) {
        this.bitTensor = bitTensor;
    }
}
