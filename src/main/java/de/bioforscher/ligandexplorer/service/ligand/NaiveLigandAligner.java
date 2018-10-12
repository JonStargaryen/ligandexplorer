package de.bioforscher.ligandexplorer.service.ligand;

import de.bioforscher.jstructure.mathematics.LinearAlgebra;
import de.bioforscher.jstructure.mathematics.Pair;
import de.bioforscher.jstructure.mathematics.Transformation;
import de.bioforscher.jstructure.model.structure.Atom;
import de.bioforscher.jstructure.model.structure.Group;
import de.bioforscher.jstructure.model.structure.StructureCollectors;
import de.bioforscher.jstructure.model.structure.container.AtomContainer;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NaiveLigandAligner implements LigandAligner {
    @Override
    public List<Transformation> alignLigands(List<Group> ligands) {
        List<Transformation> transformations = new ArrayList<>();
        Group reference = ligands.get(0);
        transformations.add(new Transformation(Transformation.NEUTRAL_TRANSLATION, Transformation.NEUTRAL_ROTATION));

        for(int i = 1; i < ligands.size(); i++) {
            Group query = ligands.get(i);

            // determine mapping
            List<Pair<Atom, Atom>> atomMapping = determineSharedAtoms(reference, query);
            AtomContainer referenceSelectedAtoms = atomMapping.stream()
                    .map(Pair::getLeft)
                    .collect(StructureCollectors.toIsolatedStructure());
            AtomContainer querySelectedAtoms = atomMapping.stream()
                    .map(Pair::getRight)
                    .collect(StructureCollectors.toIsolatedStructure());

            // calculate centroids and center atoms
            double[] centroid1 = referenceSelectedAtoms.calculate().center().getValue();
            double[] centroid2 = querySelectedAtoms.calculate().center().getValue();

            // compose covariance matrix and calculate SVD
            RealMatrix matrix1 = convertToMatrix(referenceSelectedAtoms);
            RealMatrix matrix2 = convertToMatrix(querySelectedAtoms);
            RealMatrix covariance = matrix2.transpose().multiply(matrix1);
            SingularValueDecomposition svd = new SingularValueDecomposition(covariance);
            // R = (V * U')'
            RealMatrix ut = svd.getU().transpose();
            RealMatrix rotationMatrix = svd.getV().multiply(ut).transpose();
            // check if reflection
            if (new LUDecomposition(rotationMatrix).getDeterminant() < 0) {
                RealMatrix v = svd.getV().transpose();
                v.setEntry(2, 0, (0 - v.getEntry(2, 0)));
                v.setEntry(2, 1, (0 - v.getEntry(2, 1)));
                v.setEntry(2, 2, (0 - v.getEntry(2, 2)));
                rotationMatrix = v.transpose().multiply(ut).transpose();
            }
            double[][] rotation = rotationMatrix.getData();

            // calculate translation
            double[] translation = LinearAlgebra.on(centroid1)
                    .subtract(LinearAlgebra.on(centroid2)
                            .multiply(rotation))
                    .getValue();

            // transform 2nd atom select - employ neutral translation (3D vector of zeros), because the atoms are
            // already centered and calculate RMSD
            querySelectedAtoms.calculate().transform(new Transformation(rotation));
            double rmsd = calculateRmsd(referenceSelectedAtoms, querySelectedAtoms);

            Transformation transformation = new Transformation(translation, rotation);
            // superimpose query onto reference
//            querySelectedAtoms.calculate().transform(transformation);

            // align original structure
//            System.out.println("original:");
//            System.out.println(query.getPdbRepresentation());
//            query.getParentStructure().calculate().transform(transformation);
//            System.out.println("aligned:");
//            System.out.println(query.getPdbRepresentation());

//            System.out.println(rmsd);

            transformations.add(transformation);
        }

        return transformations;
    }

    /**
     * Pairs two {@link de.bioforscher.jstructure.model.structure.container.AtomContainer} entities in a comparable
     * way. Will determine the set of shared atom names and pair matching names of both containers.
     * @param referenceGroup the reference container
     * @param queryGroup the query container
     * @return a collection of compatible atom pairs
     */
    private List<Pair<Atom, Atom>> determineSharedAtoms(Group referenceGroup, Group queryGroup) {
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

    /**
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

    /**
     * Computes the root-mean square deviation between 2 sets of atoms.
     * @param reference container 1 - must arrange atoms in the exact same manner
     * @param query container 2 - must arrange atoms in the exact same manner
     * @return the RMSD value of the alignment
     * @throws IllegalArgumentException if no matching atom pairs were provided
     */
    private double calculateRmsd(AtomContainer reference, AtomContainer query) {
        double msd = IntStream.range(0, reference.getAtoms().size())
                .mapToDouble(atomIndex -> LinearAlgebra.on(reference.getAtoms().get(atomIndex))
                        .distanceFast(query.getAtoms().get(atomIndex)))
                .average()
                .orElseThrow(() -> new IllegalArgumentException("cannot calculate rmsd for empty or non-intersecting containers"));
        return Math.sqrt(msd);
    }

    /**
     * Converts a collection of atoms to a <tt>n x 3</tt> matrix, where <tt>n</tt> is equal to the number of processed
     * atoms.
     * @param atomContainer a collection of atoms
     * @return a matrix containing the coordinates of all atoms
     */
    private RealMatrix convertToMatrix(AtomContainer atomContainer) {
        List<Atom> atomList = atomContainer.getAtoms();
        double[][] matrix = new double[atomList.size()][3];
        for (int index = 0; index < atomList.size(); index++) {
            matrix[index] = atomList.get(index).getCoordinates();
        }
        return new Array2DRowRealMatrix(matrix);
    }
}
