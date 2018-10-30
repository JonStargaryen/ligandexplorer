package de.bioforscher.ligandexplorer.service;

import de.bioforscher.jstructure.feature.plip.ProteinLigandInteractionProfiler;
import de.bioforscher.jstructure.feature.plip.model.InteractionContainer;
import de.bioforscher.jstructure.model.structure.Group;
import de.bioforscher.jstructure.model.structure.Structure;
import de.bioforscher.jstructure.model.structure.StructureParser;
import de.bioforscher.ligandexplorer.model.BindingSite;
import de.bioforscher.ligandexplorer.model.Cluster;
import de.bioforscher.ligandexplorer.model.Query;
import de.bioforscher.ligandexplorer.model.StructureIdentifier;
import de.bioforscher.ligandexplorer.service.cluster.ClusterResolver;
import de.bioforscher.ligandexplorer.service.ligand.align.LigandAligner;
import de.bioforscher.ligandexplorer.service.query.QueryResolver;
import de.bioforscher.ligandexplorer.service.tensor.BitTensorTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ExplorerService {
    private final QueryResolver queryResolver;
    private final ClusterResolver clusterResolver;
    private final LigandAligner ligandAligner;
    private final BitTensorTransformer bitTensorTransformer;

    @Autowired
    public ExplorerService(@Qualifier("queryResolverImpl") QueryResolver queryResolver,
                           @Qualifier("clusterResolverImpl") ClusterResolver clusterResolver,
                           LigandAligner ligandAligner,
                           BitTensorTransformer bitTensorTransformer) {
        this.queryResolver = queryResolver;
        this.clusterResolver = clusterResolver;
        this.ligandAligner = ligandAligner;
        this.bitTensorTransformer = bitTensorTransformer;
    }

    public Query getQuery(String query) {
        return queryResolver.getQuery(query);
    }

    public List<Cluster> getClusters(String ligandName,
                                     String pdbIdString) {
        //TODO how to handle multiple ligands in multiple structures

        List<String> pdbIds = fragmentizePdbIdString(pdbIdString);
        List<BindingSite> bindingSites = pdbIds.stream()
//                .limit(3)
//                .filter(pdbId -> "1iol".equals(pdbId))
                .flatMap(pdbId -> {
                    // parse structure
                    Structure structure = StructureParser.fromPdbId(pdbId).parse();

                    // annotate ligand interactions
                    InteractionContainer interactionContainer = ProteinLigandInteractionProfiler.getInstance()
                            .annotateLigandInteractions(structure);

                    // extract binding sites around ligands
                    return structure.select()
                            .groupName(ligandName)
                            .asFilteredGroups()
                            .map(ligand -> extractLigandBindingSites(ligand, interactionContainer));
                })
                .collect(Collectors.toList());

        // align all binding sites with respect to their respective ligand
        ligandAligner.alignBindingSites(bindingSites);

        // based on alignment: compose bit tensor representations
        bitTensorTransformer.transform(bindingSites);

        return clusterResolver.getClusters(bindingSites);
    }

    private List<String> fragmentizePdbIdString(String pdbIdString) {
        return IntStream.range(0, pdbIdString.length() / 4)
                .mapToObj(i -> pdbIdString.substring(i * 4, i * 4 + 4))
                .collect(Collectors.toList());
    }

    private BindingSite extractLigandBindingSites(Group ligand, InteractionContainer interactionContainer) {
        String ligandName = ligand.getThreeLetterCode();
        Structure structure = ligand.getParentStructure();
        StructureIdentifier structureIdentifier = new StructureIdentifier(structure.getProteinIdentifier().getPdbId(),
                ligandName,
                ligand.getParentChain().getChainIdentifier().getChainId(),
                ligand.getResidueIdentifier().toString(),
                structure.getTitle());
        return new BindingSite(structureIdentifier,
                ligand,
                interactionContainer);
    }
}