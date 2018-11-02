package de.bioforscher.ligandexplorer.service;

import de.bioforscher.jstructure.feature.plip.model.InteractionContainer;
import de.bioforscher.jstructure.model.structure.Group;
import de.bioforscher.jstructure.model.structure.Structure;
import de.bioforscher.jstructure.model.structure.StructureParser;
import de.bioforscher.ligandexplorer.model.*;
import de.bioforscher.ligandexplorer.service.cluster.ClusterResolver;
import de.bioforscher.ligandexplorer.service.interaction.InteractionAnnotator;
import de.bioforscher.ligandexplorer.service.ligand.align.LigandAligner;
import de.bioforscher.ligandexplorer.service.query.QueryResolver;
import de.bioforscher.ligandexplorer.service.tensor.BitTensorTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class ExplorerService {
    private static final Logger logger = LoggerFactory.getLogger(ExplorerService.class);
    private final QueryResolver queryResolver;
    private final InteractionAnnotator interactionAnnotator;
    private final LigandAligner ligandAligner;
    private final BitTensorTransformer bitTensorTransformer;
    private final ClusterResolver clusterResolver;

    @Autowired
    public ExplorerService(@Qualifier("queryResolverImpl") QueryResolver queryResolver,
                           @Qualifier("mockPlipAnnotator") InteractionAnnotator interactionAnnotator,
                           LigandAligner ligandAligner,
                           BitTensorTransformer bitTensorTransformer,
                           @Qualifier("clusterResolverImpl") ClusterResolver clusterResolver) {
        this.queryResolver = queryResolver;
        this.interactionAnnotator = interactionAnnotator;
        this.ligandAligner = ligandAligner;
        this.bitTensorTransformer = bitTensorTransformer;
        this.clusterResolver = clusterResolver;
    }

    public Query getQuery(String query) {
        logger.info("[{}] handling query {}",
                "init",
                query);
        Query result = queryResolver.getQuery(query);

        logger.info("[{}] matched to {} ligands: {}",
                "init",
                result.getLigands().size(),
                result.getLigands().stream().map(Ligand::getId).collect(Collectors.toList()));

        for(Ligand ligand : result.getLigands()) {
            logger.info("[{}] ligand present in {} structures: {}",
                    ligand.getId(),
                    ligand.getNumberOfStructures(),
                    ligand.getPdbIds());
        }

        return result;
    }

    public List<Cluster> getClusters(String ligandName,
                                     String pdbIdString) {
        //TODO how to handle multiple ligands in multiple structures
        List<String> pdbIds = fragmentizePdbIdString(pdbIdString);
        //TODO update front-end about job progress
        List<BindingSite> bindingSites = pdbIds.parallelStream()
                .flatMap(pdbId -> {
                    try {
                        // parse structure
                        Structure structure = StructureParser.fromPdbId(pdbId).parse();

                        // annotate ligand interactions
                        InteractionContainer interactionContainer = interactionAnnotator.annotateInteractions(structure);

                        // extract binding sites around ligands
                        return structure.select()
                                .groupName(ligandName)
                                .asFilteredGroups()
                                .map(ligand -> extractLigandBindingSites(ligand, interactionContainer));
                    } catch (Exception e) {
                        logger.warn("[{}] could not extract binding sites for {}",
                                ligandName,
                                pdbId,
                                e);
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());

        // align all binding sites with respect to their respective ligand
        logger.info("[{}] aligning {} binding sites",
                ligandName,
                bindingSites.size());
        ligandAligner.alignBindingSites(bindingSites);

        // based on alignment: compose bit tensor representations
        logger.info("[{}] composing bit tensor representations",
                ligandName);
        bitTensorTransformer.transform(bindingSites);

        // cluster binding sites
        logger.info("[{}] clustering binding sites",
                ligandName);
        List<Cluster> clusters = clusterResolver.getClusters(bindingSites);
        logger.info("[{}] clusters are: {}",
                ligandName,
                clusters);


        return clusters;
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