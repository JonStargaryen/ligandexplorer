package de.bioforscher.ligandexplorer.service.tensor;

import de.bioforscher.jstructure.feature.plip.model.*;
import de.bioforscher.jstructure.mathematics.LinearAlgebra;
import de.bioforscher.jstructure.mathematics.Pair;
import de.bioforscher.jstructure.model.structure.Structure;
import de.bioforscher.jstructure.model.structure.StructureCollectors;
import de.bioforscher.ligandexplorer.model.BindingSite;
import de.bioforscher.ligandexplorer.model.BitTensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class BitTensorTransformerImpl implements BitTensorTransformer {
    private static final Logger logger = LoggerFactory.getLogger(BitTensorTransformerImpl.class);

    private static final double LENGTH_OF_BIN = 1.0;
    //TODO compute real value if bins are not 1x1x1 A
//    private static final double BINS_PER_ANGSTROM = 1;

    private static final List<String> INTERACTION_CLASSES = Stream.of(HalogenBond.class,
                    HydrogenBond.class,
                    HydrophobicInteraction.class,
                    MetalComplex.class,
                    PiCationInteraction.class,
                    PiStackingInteraction.class,
                    SaltBridge.class,
                    WaterBridge.class)
            .map(Class::getSimpleName)
            .collect(Collectors.toList());

    @Override
    public void transform(List<BindingSite> alignedBindingSites) {
        Structure mergedBindingSites = alignedBindingSites.stream()
                .map(BindingSite::getAlignedLigand)
                .collect(StructureCollectors.toIsolatedStructure());
        double[] centroid = mergedBindingSites.calculate().centroid().getValue();
        double maximalExtent = mergedBindingSites.calculate().maximalExtent();

        logger.info("centroid: {}, maximal extent: {}",
                Arrays.toString(centroid),
                maximalExtent);

        // for now: number of bins is next bigger integer depending on maximal extent, times 2
        int ceiledMaximalExtent = (int) Math.ceil(maximalExtent);
        int numberOfSpatialBins = 2 * ceiledMaximalExtent;
        List<Pair<Double, Double>> intervalBoundaries = IntStream.rangeClosed(0, numberOfSpatialBins)
                .mapToObj(i -> new Pair<>(i * LENGTH_OF_BIN - ceiledMaximalExtent, (i + 1) * LENGTH_OF_BIN - ceiledMaximalExtent))
                .collect(Collectors.toList());
        int[] dimensions = new int[] { numberOfSpatialBins, numberOfSpatialBins, numberOfSpatialBins, INTERACTION_CLASSES.size() };

        alignedBindingSites.forEach(alignedBindingSite -> {
            // initialize bit tensor
            BitTensor bitTensor = new BitTensor(dimensions);

            alignedBindingSite.getAlignedInteractions()
                    .forEach(alignedInteraction -> {
                        double[] alignedCoordinates = alignedInteraction.getAlignedCoordinates();
                        double[] centeredCoordinates = LinearAlgebra.on(centroid).subtract(alignedCoordinates).getValue();
                        bitTensor.setBit(mapToSpatialBin(centeredCoordinates[0], intervalBoundaries),
                                mapToSpatialBin(centeredCoordinates[1], intervalBoundaries),
                                mapToSpatialBin(centeredCoordinates[2], intervalBoundaries),
                                mapToInteractionDimension(alignedInteraction.getInteractionType()));
                    });

            logger.trace("bit tensor representation of {}", alignedBindingSite.getStructureIdentifier());
            logger.trace("bit tensor:\n{}", bitTensor);
            alignedBindingSite.setBitTensor(bitTensor);
        });
    }

    private int mapToSpatialBin(double centeredValue, List<Pair<Double, Double>> intervalBoundaries) {
        try {
            Pair<Double, Double> bin = intervalBoundaries.stream()
                    .filter(interval -> centeredValue >= interval.getLeft() && centeredValue <= interval.getRight())
                    .findFirst()
                    .get();
            return intervalBoundaries.indexOf(bin);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private int mapToInteractionDimension(String interactionType) {
        return INTERACTION_CLASSES.indexOf(interactionType);
    }
}
