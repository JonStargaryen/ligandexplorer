package de.bioforscher.ligandexplorer.model;

public class StructureIdentifier {
    private final String pdbId;
    private final String ligandId;
    private final String chainId;
    private final int residueNumber;

    public StructureIdentifier(String pdbId,
                               String ligandId,
                               String chainId,
                               int residueNumber) {
        this.pdbId = pdbId;
        this.ligandId = ligandId;
        this.chainId = chainId;
        this.residueNumber = residueNumber;
    }

    public String getPdbId() {
        return pdbId;
    }

    public String getLigandId() {
        return ligandId;
    }

    public String getChainId() {
        return chainId;
    }

    public int getResidueNumber() {
        return residueNumber;
    }
}
