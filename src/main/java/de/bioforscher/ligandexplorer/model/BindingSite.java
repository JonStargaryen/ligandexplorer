package de.bioforscher.ligandexplorer.model;

import de.bioforscher.jstructure.feature.plip.model.Interaction;
import de.bioforscher.jstructure.feature.plip.model.InteractionContainer;
import de.bioforscher.jstructure.mathematics.LinearAlgebra;
import de.bioforscher.jstructure.mathematics.Transformation;
import de.bioforscher.jstructure.model.structure.Atom;
import de.bioforscher.jstructure.model.structure.Group;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class BindingSite {
    private final StructureIdentifier structureIdentifier;
    private final Group originalLigand;
    private final List<Interaction> originalInteractions;
    private final List<Group> originalBindingSiteResidues;
    private Transformation transformation;

    public BindingSite(StructureIdentifier structureIdentifier,
                       Group ligand,
                       InteractionContainer globalInteractionContainer) {
        this.structureIdentifier = structureIdentifier;
        this.originalLigand = ligand;
        this.originalInteractions = globalInteractionContainer.getSubsetOfInteractions(ligand)
                .getInteractions();
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

    public String getAlignedPdbRepresentation() {
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());

        for(Atom ligandAtom : originalLigand.getAtoms()) {
            // skip interaction pseudo-atoms
            if(ligandAtom.isVirtual()) {
                continue;
            }

            double[] ligandAtomCoordinates = ligandAtom.getCoordinates();
            double[] alignedAtomCoordinates = LinearAlgebra.on(ligandAtomCoordinates)
                    .multiply(transformation.getRotation())
                    .add(transformation.getTranslation())
                    .getValue();
            ligandAtom.setCoordinates(alignedAtomCoordinates);
            stringJoiner.add(ligandAtom.getPdbRepresentation());
        }

//        for(Group bindingSiteResidues : originalBindingSiteResidues) {
//            for(Atom bindingSiteAtom : bindingSiteResidues.getAtoms()) {
//                // skip interaction pseudo-atoms
//                if(bindingSiteAtom.isVirtual()) {
//                    continue;
//                }
//
//                double[] bindingSiteAtomCoordinates = bindingSiteAtom.getCoordinates();
//                double[] alignedBindingSiteAtomCoordinates = LinearAlgebra.on(bindingSiteAtomCoordinates)
//                        .multiply(transformation.getRotation())
//                        .add(transformation.getTranslation())
//                        .getValue();
//                Atom bindingSiteAtomCopy = bindingSiteAtom.createDeepCopy();
//                bindingSiteAtomCopy.setCoordinates(alignedBindingSiteAtomCoordinates);
//
//                String pdbRepresentation = bindingSiteAtomCopy.getPdbRepresentation();
//                stringJoiner.add(pdbRepresentation);
//            }
//        }

        return stringJoiner.toString();
    }

    public List<AlignedInteraction> getAlignedInteractions() {
        return originalInteractions.stream()
                .map(interaction -> new AlignedInteraction(interaction, transformation))
                .collect(Collectors.toList());
    }

    public void setTransformation(Transformation transformation) {
        this.transformation = transformation;
    }
}
