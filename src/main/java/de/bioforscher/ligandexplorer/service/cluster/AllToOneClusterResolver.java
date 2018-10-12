package de.bioforscher.ligandexplorer.service.cluster;

import de.bioforscher.jstructure.feature.interaction.PLIPLigandAnnotator;
import de.bioforscher.jstructure.feature.plip.ProteinLigandInteractionProfiler;
import de.bioforscher.jstructure.feature.plip.model.Interaction;
import de.bioforscher.jstructure.feature.plip.model.InteractionContainer;
import de.bioforscher.jstructure.mathematics.Transformation;
import de.bioforscher.jstructure.model.structure.Group;
import de.bioforscher.jstructure.model.structure.Structure;
import de.bioforscher.jstructure.model.structure.StructureParser;
import de.bioforscher.ligandexplorer.model.Cluster;
import de.bioforscher.ligandexplorer.model.StructureIdentifier;
import de.bioforscher.ligandexplorer.service.ligand.LigandAligner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Component("allToOneClusterResolver")
public class AllToOneClusterResolver implements ClusterResolver {
    private final LigandAligner ligandAligner;
    private final PLIPLigandAnnotator plipLigandAnnotator;

    @Autowired
    public AllToOneClusterResolver(LigandAligner ligandAligner) {
        this.ligandAligner = ligandAligner;
        this.plipLigandAnnotator = new PLIPLigandAnnotator();
    }

    @Override
    public List<Cluster> getClusters(String ligandName, List<String> pdbIds) {
        List<Cluster> clusters = new ArrayList<>();

        StringJoiner pdbRepresentation = new StringJoiner(System.lineSeparator());
        List<StructureIdentifier> structureIdentifiers = new ArrayList<>();
        List<Group> ligands = new ArrayList<>();

        pdbIds.forEach(pdbId -> {
                Structure structure = StructureParser.fromPdbId(pdbId).parse();
                InteractionContainer container = ProteinLigandInteractionProfiler.getInstance().annotateLigandInteractions(structure);

                container.getInteractions()
                        .stream()
                        .map(interaction -> interaction.getClass().getSimpleName() + " " +
                                interaction.getInteractingAtoms1().get(0).getParentGroup() + " " +
                                interaction.getInteractingAtoms2().get(0).getParentGroup())
                        .forEach(System.out::println);

                structure.select()
                        .ligands()
                        .groupName(ligandName)
                        .asFilteredGroups()
                        .forEach(ligand -> {
                            structureIdentifiers.add(new StructureIdentifier(pdbId,
                                    ligandName,
                                    ligand.getParentChain().getChainIdentifier().getChainId(),
                                    ligand.getResidueIdentifier().toString(),
                                    structure.getTitle()));

                            ligands.add(ligand);

                            String ligandChainId = ligand.getParentChain().getChainIdentifier().getChainId();
                            String ligandResidueNumber = ligand.getResidueIdentifier().toString();
                            List<Interaction> filteredInteractions = container.getSubsetOfInteractions(ligand).getInteractions();
                            System.out.println(pdbId + " - " + ligandChainId + " - " + ligandResidueNumber + " - " + filteredInteractions.size());
                        });
        });

        List<Transformation> transformations = ligandAligner.alignLigands(ligands);
        for(int i = 0; i < ligands.size(); i++) {
            Group ligand = ligands.get(i);
            ligand.calculate().transform(transformations.get(i));
            pdbRepresentation.add(ligand.getPdbRepresentation());
        }

//        ligands.stream()
//                .map(group -> group.getFeature(PLIPInteractionContainer.class).getInteractionsFor(group))
//                .forEach(System.out::println);

        clusters.add(new Cluster("1",
                structureIdentifiers,
                pdbRepresentation.toString()));
        return clusters;
    }
}
