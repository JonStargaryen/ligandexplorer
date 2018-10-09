package de.bioforscher.ligandexplorer.model;

public class StructureIdentifier {
    private final String pdbId;
    private final String ligandId;
    private final String chainId;
    private final String residueNumber;
    private final String title;

    public StructureIdentifier(String pdbId,
                               String ligandId,
                               String chainId,
                               String residueNumber,
                               String title) {
        this.pdbId = pdbId;
        this.ligandId = ligandId;
        this.chainId = chainId;
        this.residueNumber = residueNumber;
        this.title = title;
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

    public String getResidueNumber() {
        return residueNumber;
    }

    public String getTitle() {
        return title;
    }
}
