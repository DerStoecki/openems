package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

public enum DoubleUartRegistries {
    Sc16IS752(0x01, 0x02,0x02, 0x05, 0x07,0x0A, 0x0B, 0x0C, 0x0E);

    int ier;
    int fcr;
    int iir;
    int lsr;
    int tlr;
    int ioDir;
    int ioState;
    int ioIntEna;
    int ioControl;


    DoubleUartRegistries(int ier, int fcr, int iir, int lsr, int tlr,int ioDir, int ioState, int ioIntEna, int ioControl) {
        this.ier = ier;
        this.fcr = fcr;
        this.iir = iir;
        this.lsr = lsr;
        this.tlr = tlr;
        this.ioDir = ioDir;
        this.ioState = ioState;
        this.ioIntEna = ioIntEna;
        this.ioControl = ioControl;
    }
}
