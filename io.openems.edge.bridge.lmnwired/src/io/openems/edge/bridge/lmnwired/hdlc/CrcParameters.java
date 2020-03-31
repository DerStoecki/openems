package io.openems.edge.bridge.lmnwired.hdlc;

public class CrcParameters {
    private int width; // Width of the CRC expressed in bits
    private long polynomial; // Polynomial used in this CRC calculation
    private boolean reflectIn; // Refin indicates whether input bytes should be reflected
    private boolean reflectOut; // Refout indicates whether output bytes should be reflected
    private long init; // Init is initial value for CRC calculation
    private long finalXor; // Xor is a value for final xor to be applied before returning result

    public CrcParameters(int width, long polynomial, long init, boolean reflectIn, boolean reflectOut, long finalXor) {
        this.width = width;
        this.polynomial = polynomial;
        this.reflectIn = reflectIn;
        this.reflectOut = reflectOut;
        this.init = init;
        this.finalXor = finalXor;
    }

    public CrcParameters(CrcParameters orig) {
        width = orig.width;
        polynomial = orig.polynomial;
        reflectIn = orig.reflectIn;
        reflectOut = orig.reflectOut;
        init = orig.init;
        finalXor = orig.finalXor;
    }

    public int getWidth() {
        return width;
    }

    public long getPolynomial() {
        return polynomial;
    }

    public boolean isReflectIn() {
        return reflectIn;
    }

    public boolean isReflectOut() {
        return reflectOut;
    }

    public void setReflectOut(boolean r) {
        this.reflectOut = r;
    }

    public long getInit() {
        return init;
    }

    public void setInit(long i) {
        this.init = i;
    }

    public long getFinalXor() {
        return finalXor;
    }

    public void setFinalXor(long f) {
        this.finalXor = f;
    }

    /** CCITT CRC parameters */
    public static final CrcParameters CCITT = new CrcParameters(16, 0x1021, 0x00FFFF, false, false, 0x0);
}
