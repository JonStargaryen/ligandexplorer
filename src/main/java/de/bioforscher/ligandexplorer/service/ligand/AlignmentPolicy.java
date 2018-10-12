package de.bioforscher.ligandexplorer.service.ligand;

import de.bioforscher.jstructure.mathematics.Pair;
import de.bioforscher.jstructure.model.structure.Atom;
import de.bioforscher.jstructure.model.structure.Group;
import de.bioforscher.jstructure.model.structure.StructureCollectors;
import de.bioforscher.jstructure.model.structure.aminoacid.AminoAcid;
import de.bioforscher.jstructure.model.structure.container.GroupContainer;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Rules for structural alignments.
 * Created by bittrich on 6/19/17.
 */
public interface AlignmentPolicy {
    /**
     *
     */
    @FunctionalInterface
    interface AtomMapping {
        /**
         * Traverses both containers and returns a comparable arrangement of atoms.
         * @param reference the reference container
         * @param query the query container
         * @return a collection of compatible, paired atoms of both containers
         */
        List<Pair<Atom, Atom>> determineAtomMapping(GroupContainer reference, GroupContainer query);
    }

    /**
     * Describes which atoms or groups are used (and in which way) to align both containers.
     */
    interface MatchingBehavior {
        /**
         * Assumes equal number of groups. Allows for variable matched groups (e.g. Ala vs Ile), they must share at
         * least 1 atom however.
         */
        AtomMapping comparableAtomNames = (reference, query) -> {
            return IntStream.range(0, reference.getGroups().size())
                    .mapToObj(index -> determineSharedAtoms(reference.getGroups().get(index), query.getGroups().get(index)))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        };

        /**
         * Matches amino acids of both containers. Assumes equal number of amino acids. Allows for variable matched
         * groups (e.g. Ala vs Ile), they must share at least 1 atom however.
         */
        AtomMapping aminoAcidsComparableAtomNames = (reference, query) -> {
            GroupContainer referenceAminoAcids = reference.aminoAcids().collect(StructureCollectors.toIsolatedStructure());
            GroupContainer queryAminoAcids = query.aminoAcids().collect(StructureCollectors.toIsolatedStructure());
            return IntStream.range(0, referenceAminoAcids.getGroups().size())
                    .mapToObj(index -> determineSharedAtoms(referenceAminoAcids.getGroups().get(index), queryAminoAcids.getGroups().get(index)))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        };

        AtomMapping aminoAcidsComparableBackboneAtomNames = (reference, query) -> {
            GroupContainer referenceAminoAcids = reference.aminoAcids().collect(StructureCollectors.toIsolatedStructure());
            GroupContainer queryAminoAcids = query.aminoAcids().collect(StructureCollectors.toIsolatedStructure());
            return IntStream.range(0, referenceAminoAcids.getGroups().size())
                    .mapToObj(index -> determineSharedBackboneAtoms(referenceAminoAcids.getGroups().get(index), queryAminoAcids.getGroups().get(index)))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        };

        AtomMapping aminoAcidsAlphaCarbonsTolerant = (reference, query) -> {
            List<AminoAcid> referenceAminoAcids = reference.aminoAcids().collect(Collectors.toList());
            List<AminoAcid> queryAminoAcids = query.aminoAcids().collect(Collectors.toList());
            int limitingSize = Math.min(referenceAminoAcids.size(), queryAminoAcids.size());
            return IntStream.range(0, limitingSize)
                    //TODO fallback to centroid?
                    .mapToObj(index -> new Pair<>(referenceAminoAcids.get(index).getCa(), queryAminoAcids.get(index).getCa()))
                    .collect(Collectors.toList());
        };

        /**
         * Pairs two {@link de.bioforscher.jstructure.model.structure.container.AtomContainer} entities in a comparable
         * way. Will determine the set of shared atom names and pair matching names of both containers.
         * @param referenceGroup the reference container
         * @param queryGroup the query container
         * @return a collection of compatible atom pairs
         */
        static List<Pair<Atom, Atom>> determineSharedAtoms(Group referenceGroup, Group queryGroup) {
            // determine set of shared atom names
            Set<String> sharedAtomNames = referenceGroup.atoms()
                    .map(Atom::getName)
                    .filter(atomName -> queryGroup.atoms().anyMatch(queryGroupAtom -> atomName.equals(queryGroupAtom.getName())))
                    .collect(Collectors.toSet());

            // validate that both groups share atoms
            if(sharedAtomNames.isEmpty()) {
                throw new IllegalArgumentException("groups " + referenceGroup + " and " + queryGroup + " do not share " +
                        "any atoms at all - cannot align");
            }

            // pair atoms
            return sharedAtomNames.stream()
                    .map(atomName -> new Pair<>(selectAtom(referenceGroup, atomName), selectAtom(queryGroup, atomName)))
                    .collect(Collectors.toList());
        }

        static List<Pair<Atom, Atom>> determineSharedBackboneAtoms(Group referenceGroup, Group queryGroup) {
            return determineSharedAtoms(referenceGroup, queryGroup).stream()
                    .filter(pair -> AminoAcid.isBackboneAtom(pair.getLeft()) && AminoAcid.isBackboneAtom(pair.getRight()))
                    .collect(Collectors.toList());
        }

        /**
         * Native implementation of selection method. Could use
         * {@link de.bioforscher.jstructure.model.structure.selection.Selection} as well, but performance ought to be
         * favorable for the straight-forward implementation.
         * @param group the container to process
         * @param atomName the atom name to retrieve
         * @return the desired atom
         */
        static Atom selectAtom(Group group, String atomName) {
            return group.atoms()
                    .filter(atom -> atomName.equals(atom.getName()))
                    //TODO test case for multiple atom names within the same group
                    .findFirst()
                    // presence is guaranteed by looking at shared atom names
                    .get();
        }
    }
}
