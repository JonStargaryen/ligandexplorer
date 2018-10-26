package de.bioforscher.ligandexplorer.model;

import de.bioforscher.jstructure.feature.plip.model.Interaction;
import de.bioforscher.jstructure.mathematics.LinearAlgebra;
import de.bioforscher.jstructure.mathematics.Transformation;
import de.bioforscher.jstructure.model.structure.Group;

public class AlignedInteraction {
    private final StructureIdentifier ligandIdentifier;
    private final StructureIdentifier bindingSiteResidueIdentifier;
    private final double[] alignedCoordinates;
    private final String interactionType;

    public AlignedInteraction(Interaction interaction, Transformation transformation) {
        double[] pseudoAtomCoordinates = interaction.getCentroid();
        this.alignedCoordinates = LinearAlgebra.on(pseudoAtomCoordinates)
                .multiply(transformation.getRotation())
                .add(transformation.getTranslation())
                .getValue();

        Group ligand;
        Group bindingSiteResidue;
        if(interaction.getInteractingGroup1().isLigand()) {
            ligand = interaction.getInteractingGroup1();
            bindingSiteResidue = interaction.getInteractingGroup2();
        } else {
            ligand = interaction.getInteractingGroup2();
            bindingSiteResidue = interaction.getInteractingGroup1();
        }
        this.ligandIdentifier = new StructureIdentifier(ligand.getParentStructure().getProteinIdentifier().getPdbId(),
                ligand.getThreeLetterCode(),
                ligand.getParentChain().getChainIdentifier().getChainId(),
                ligand.getResidueIdentifier().toString());
        this.bindingSiteResidueIdentifier = new StructureIdentifier(bindingSiteResidue.getParentStructure().getProteinIdentifier().getPdbId(),
                bindingSiteResidue.getThreeLetterCode(),
                bindingSiteResidue.getParentChain().getChainIdentifier().getChainId(),
                bindingSiteResidue.getResidueIdentifier().toString());
        this.interactionType = interaction.getClass().getSimpleName();
    }

    public StructureIdentifier getLigandIdentifier() {
        return ligandIdentifier;
    }

    public StructureIdentifier getBindingSiteResidueIdentifier() {
        return bindingSiteResidueIdentifier;
    }

    public double[] getAlignedCoordinates() {
        return alignedCoordinates;
    }

    public String getInteractionType() {
        return interactionType;
    }
}
