package io.openems.edge.heatpump.alphainnotec.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.heatpump.smartgrid.generalized.api.HeatpumpSmartGridGeneralizedChannel;

public interface HeatpumpAlphaInnotecChannel extends HeatpumpSmartGridGeneralizedChannel {


    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {


        // Discrete Inputs (DI) 0 to 7, read only. 0 = Off, 1 = On. Represented as boolean

        /**
         * EVU, Energie Versorger Unterbrechung. Scheduled off time.
         */
        DI_0_EVU(Doc.of(OpenemsType.BOOLEAN).unit(Unit.ON_OFF)),

        /**
         * EVU2. Like EVU, but triggered because of smart grid setting.
         */
        DI_1_EVU2(Doc.of(OpenemsType.BOOLEAN).unit(Unit.ON_OFF)),

        /**
         * SWT, Schwimmbadthermostat.
         */
        DI_2_SWT(Doc.of(OpenemsType.BOOLEAN).unit(Unit.ON_OFF)),

        /**
         * VD1, Verdichter 1.
         */
        DI_3_VD1(Doc.of(OpenemsType.BOOLEAN).unit(Unit.ON_OFF)),

        /**
         * VD2, Verdichter 2.
         */
        DI_4_VD2(Doc.of(OpenemsType.BOOLEAN).unit(Unit.ON_OFF)),

        /**
         * ZWE1, zusätzlicher Wärmeerzeuger 1.
         */
        DI_5_ZWE1(Doc.of(OpenemsType.BOOLEAN).unit(Unit.ON_OFF)),

        /**
         * ZWE2, zusätzlicher Wärmeerzeuger 2.
         */
        DI_6_ZWE2(Doc.of(OpenemsType.BOOLEAN).unit(Unit.ON_OFF)),

        /**
         * ZWE3, zusätzlicher Wärmeerzeuger 3. Optional, depends on heat pump model if available.
         */
        DI_7_ZWE3(Doc.of(OpenemsType.BOOLEAN).unit(Unit.ON_OFF)),


        // Input Registers (IR) 0 to 46, read only. They are 16 bit unsigned numbers unless stated otherwise.

        /**
         * Mitteltemperatur.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_0_MITTELTEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Vorlauftemperatur.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_1_VORLAUFTEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Rücklauftemperatur.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_2_RUECKLAUFTEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Rücklauf extern.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_3_RUECKEXTERN(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Trinkwarmwassertemperatur.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_4_TRINKWWTEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Mischkreis 1 Vorlauf.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_5_MK1VORLAUF(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Mischkreis 2 Vorlauf. Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_6_MK2VORLAUF(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Mischkreis 3 Vorlauf. Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_7_MK3VORLAUF(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Heissgastemperatur.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_8_HEISSGASTEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Wärmequelle Eintritt.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_9_WQEINTRITT(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Wärmequelle Austritt.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_10_WQAUSTRITT(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Raumfernversteller 1.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_11_RAUMFV1(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Raumfernversteller 2. Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_12_RAUMFV2(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Raumfernversteller 3. Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_13_RAUMFV3(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Solarkollektor. Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_14_SOLARKOLLEKTOR(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Solarspeicher. Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_15_SOLARSPEICHER(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Externe Energiequelle. Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_16_EXTEQ(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Zulufttemperatur. Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_17_ZULUFTTEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Ablufttemperatur.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_18_ABLUFTTEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Ansaugtemperatur Verdichter.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_19_ANSAUGTEMPVDICHTER(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Ansaugtemperatur Verdampfer.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_20_ANSAUGTEMPVDAMPFER(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Temperatur Verdichterheizung.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_21_TEMPVDHEIZUNG(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Überhitzung.
         * <li>Unit: Dezidegree Kelvin</li>
         */
        IR_22_UEBERHITZ(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_KELVIN)),

        /**
         * Überhitzung Soll.
         * <li>Unit: Dezidegree Kelvin</li>
         */
        IR_23_UEBERHITZSOLL(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_KELVIN)),

        /**
         * RBE (Raumbedieneinheit) Raumtemperatur Ist.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_24_RBERAUMTEMPIST(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * RBE (Raumbedieneinheit) Raumtemperatur Soll.
         * <li>Unit: Dezidegree Celsius</li>
         */
        IR_25_RBERAUMTEMPSOLL(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Druck HD (Hochdruck).
         * <li>Unit: Centi bar</li>
         */
        IR_26_DRUCKHD(Doc.of(OpenemsType.INTEGER)),

        /**
         * Druck ND (Niederdruck).
         * <li>Unit: Centi bar</li>
         */
        IR_27_DRUCKND(Doc.of(OpenemsType.INTEGER)),

        /**
         * Betriebsstunden VD1 (Verdichter).
         * <li>Unit: hours</li>
         */
        IR_28_TVD1(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),

        /**
         * Betriebsstunden VD2 (Verdichter).
         * <li>Unit: hours</li>
         */
        IR_29_TVD2(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),

        /**
         * Betriebsstunden ZWE1 (Zusätzlicher Wärmeerzeuger).
         * <li>Unit: hours</li>
         */
        IR_30_TZWE1(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),

        /**
         * Betriebsstunden ZWE2 (Zusätzlicher Wärmeerzeuger).
         * <li>Unit: hours</li>
         */
        IR_31_TZWE2(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),

        /**
         * Betriebsstunden ZWE3 (Zusätzlicher Wärmeerzeuger). Optional, depends on heat pump model if available.
         * <li>Unit: hours</li>
         */
        IR_32_TZWE3(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),

        /**
         * Betriebsstunden Wärmepumpe.
         * <li>Unit: hours</li>
         */
        IR_33_TWAERMEPUMPE(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),

        /**
         * Betriebsstunden Heizung.
         * <li>Unit: hours</li>
         */
        IR_34_THEIZUNG(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),

        /**
         * Betriebsstunden Trinkwarmwasser.
         * <li>Unit: hours</li>
         */
        IR_35_TTRINKWW(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),

        /**
         * Betriebsstunden SWoPV (Schwimmbad oder Photovoltaik). Optional, depends on heat pump model if available.
         * <li>Unit: hours</li>
         */
        IR_36_TSWOPV(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),

        /**
         * Anlagenstatus. Current operating state of the heat pump.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0 ... 7
         *      <li> State 0: Heizbetrieb / Heating
         *      <li> State 1: Trinkwarmwasser / Heating potable water
         *      <li> State 2: Schwimmbad / Swimming pool
         *      <li> State 3: EVU-Sperre / Forced off by energy supplier
         *      <li> State 4: Abtauen / Defrost
         *      <li> State 5: Off
         *      <li> State 6: Externe Energiequelle / External energy source
         *      <li> State 7: Kühlung / Cooling
         * </ul>
         */
        IR_37_STATUS(Doc.of(CurrentState.values())),

        /**
         * Wärmemenge Heizung. 32 bit unsigned doubleword. IR 38 is high, IR 39 is low.
         * <li>Unit: kWh * 10E-1</li>
         */
        IR_38_WHHEIZUNG(Doc.of(OpenemsType.INTEGER).unit(Unit.HECTOWATT_HOURS)),

        /**
         * Wärmemenge Trinkwarmwasser. 32 bit unsigned doubleword. IR 40 is high, IR 41 is low.
         * <li>Unit: kWh * 10E-1</li>
         */
        IR_40_WHTRINKWW(Doc.of(OpenemsType.INTEGER).unit(Unit.HECTOWATT_HOURS)),

        /**
         * Wärmemenge Schwimmbad. 32 bit unsigned doubleword. IR 42 is high, IR 43 is low.
         * Optional, depends on heat pump model if available.
         * <li>Unit: kWh * 10E-1</li>
         */
        IR_42_WHPOOL(Doc.of(OpenemsType.INTEGER).unit(Unit.HECTOWATT_HOURS)),

        /**
         * Wärmemenge gesamt. 32 bit unsigned doubleword. IR 44 is high, IR 45 is low.
         * <li>Unit: kWh * 10E-1</li>
         */
        IR_44_WHTOTAL(Doc.of(OpenemsType.INTEGER).unit(Unit.HECTOWATT_HOURS)),

        /**
         * Error buffer. Only displays current error.
         */
        IR_46_ERROR(Doc.of(OpenemsType.INTEGER)),


        // Coils 0 to 13, read/write. When reading, 0 = Off, 1 = On. When writing, 0 = automatic, 1 = force on.
        // Represented as boolean.

        /**
         * Error reset. Reset current error message.
         */
        COIL_0_ERRORRESET(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        // Coil 1 not used

        /**
         * HUP, force on.
         */
        COIL_2_HUP(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * VEN (Ventilator), force on.
         */
        COIL_3_VEN(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * ZUP (Zusatz-Umwälzpumpe), force on.
         */
        COIL_4_ZUP(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * BUP (Trinkwarmwasser-Umwälzpumpe), force on.
         */
        COIL_5_BUP(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * BOSUP (Brunnen oder Sole-Umwälzpumpe), force on.
         */
        COIL_6_BOSUP(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * ZIP (Zirkulationspumpe), force on.
         */
        COIL_7_ZIP(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * FUP2 (Fußbodenheizungs-Umwälzpumpe), force on. Optional, depends on heat pump model if available.
         */
        COIL_8_FUP2(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * FUP3 (Fußbodenheizungs-Umwälzpumpe), force on. Optional, depends on heat pump model if available.
         */
        COIL_9_FUP3(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * SLP (Solar-Ladepumpe), force on. Optional, depends on heat pump model if available.
         */
        COIL_10_SLP(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * SUP (Schwimmbad-Umwälzpumpe), force on. Optional, depends on heat pump model if available.
         */
        COIL_11_SUP(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * VSK (Bypassklappe), force on. Optional, depends on heat pump model if available.
         */
        COIL_12_VSK(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * FRH (Schütz Defrostheizung), force on. Optional, depends on heat pump model if available.
         */
        COIL_13_FRH(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),


        // Holding Registers (HR) 0 to 23, read/write. They are 16 bit unsigned numbers unless stated otherwise.

        /**
         * Outside temperature. Signed 16 bit. Minimum -200, maximum 800.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_0_OUTSIDETEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Rücklauf-Temperatur Soll. Minimum 150, maximum 800. <- Aus Handbuch. Minimum Wert sicher falsch, schon Wert 50 ausgelesen.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_1_RUECKTEMPSOLL(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Mischkreis 1 Vorlauf Solltemperatur. Minimum 150, maximum 800.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_2_MK1VORTEMPSOLL(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Mischkreis 2 Vorlauf Solltemperatur. Minimum 150, maximum 800.
         * Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_3_MK2VORTEMPSOLL(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Mischkreis 3 Vorlauf Solltemperatur. Minimum 150, maximum 800.
         * Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_4_MK3VORTEMPSOLL(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Trinkwarmwasse-Temperatur Wunschwert. Minimum 150, maximum 800.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_5_TRINKWWTEMPSOLL(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Sperre / Freigabe Wärmepumpe. Heat pump operation clearance.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0 ... 2
         *      <li> State 0: Sperre / Off
         *      <li> State 1: Freigabe 1 Verdichter / Clearance 1 compressor
         *      <li> State 2: Freigabe 2 Verdichter / Clearance 2 compressors
         * </ul>
         */
        HR_6_RUNCLEARANCE(Doc.of(Clearance.values()).accessMode(AccessMode.READ_WRITE)),

        /**
         * Betriebsart Heizung. Heating operation status.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0 ... 4
         *      <li> State 0: Automatik
         *      <li> State 1: Zusätzlicher Wärmeerzeuger / Additional heater
         *      <li> State 2: Party / No late night throttling
         *      <li> State 3: Ferien / Vacation, full time throttling
         *      <li> State 4: Off
         * </ul>
         */
        HR_7_HEIZUNGRUNSTATE(Doc.of(HeatingMode.values()).accessMode(AccessMode.READ_WRITE)),

        /**
         * Betriebsart Trinkwarmwasser. Potable water heating operation status.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0 ... 4
         *      <li> State 0: Automatik
         *      <li> State 1: Zusätzlicher Wärmeerzeuger / Additional heater
         *      <li> State 2: Party / No late night throttling
         *      <li> State 3: Ferien / Vacation, full time throttling
         *      <li> State 4: Off
         * </ul>
         */
        HR_8_TRINKWWRUNSTATE(Doc.of(HeatingMode.values()).accessMode(AccessMode.READ_WRITE)),

        /**
         * Betriebsart Mischkreis 2. Diluted heating circuit 2 operation status.
         * Optional, depends on heat pump model if available.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0 ... 4
         *      <li> State 0: Automatik
         *      <li> State 1: Zusätzlicher Wärmeerzeuger / Additional heater
         *      <li> State 2: Party / No late night throttling
         *      <li> State 3: Ferien / Vacation, full time throttling
         *      <li> State 4: Off
         * </ul>
         */
        HR_9_MK2RUNSTATE(Doc.of(HeatingMode.values()).accessMode(AccessMode.READ_WRITE)),

        /**
         * Betriebsart Mischkreis 3. Diluted heating circuit 3 operation status.
         * Optional, depends on heat pump model if available.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0 ... 4
         *      <li> State 0: Automatik
         *      <li> State 1: Zusätzlicher Wärmeerzeuger / Additional heater
         *      <li> State 2: Party / No late night throttling
         *      <li> State 3: Ferien / Vacation, full time throttling
         *      <li> State 4: Off
         * </ul>
         */
        HR_10_MK3RUNSTATE(Doc.of(HeatingMode.values()).accessMode(AccessMode.READ_WRITE)),

        /**
         * Betriebsart Kühlung. Cooling operation status.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0 ... 1
         *      <li> State 0: Off
         *      <li> State 1: Automatik
         * </ul>
         */
        HR_11_COOLINGRUNSTATE(Doc.of(CoolingMode.values()).accessMode(AccessMode.READ_WRITE)),

        /**
         * Betriebsart Lüftung. Ventilation operation status.
         * Optional, depends on heat pump model if available.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0 ... 3
         *      <li> State 0: Automatik
         *      <li> State 1: Party / No late night throttling
         *      <li> State 2: Ferien / Vacation, full time throttling
         *      <li> State 3: Off
         * </ul>
         */
        HR_12_VENTILATIONRUNSTATE(Doc.of(VentilationMode.values()).accessMode(AccessMode.READ_WRITE)),

        /**
         * Betriebsart Schwimmbad. Pool heating operation status.
         * Optional, depends on heat pump model if available.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0 ... 4
         *      <li> State 0: Automatik
         *      <li> State 1: Wert nicht benutzt / Value not in use
         *      <li> State 2: Party / No late night throttling
         *      <li> State 3: Ferien / Vacation, full time throttling
         *      <li> State 4: Off
         * </ul>
         */
        HR_13_POOLRUNSTATE(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),

        // HR_14 = Smart Grid, use channel of parent interface.

        /**
         * Heizkurve Heizung Endpunkt. Minimum 200, maximum 700.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_15_HKHEIZUNGENDPKT(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Heizkurve Heizung Parallelverschiebung. Minimum 50, maximum 350.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_16_HKHEIZUNGPARAVER(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Heizkurve Mischkreis 1 Endpunkt. Minimum 200, maximum 700.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_17_HKMK1ENDPKT(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Heizkurve Mischkreis 1 Parallelverschiebung. Minimum 50, maximum 350.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_18_HKMK1PARAVER(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Heizkurve Mischkreis 2 Endpunkt. Minimum 200, maximum 700.
         * Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_19_HKMK2ENDPKT(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Heizkurve Mischkreis 2 Parallelverschiebung. Minimum 50, maximum 350.
         * Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_20_HKMK2PARAVER(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Heizkurve Mischkreis 3 Endpunkt. Minimum 200, maximum 700.
         * Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_21_HKMK3ENDPKT(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Heizkurve Mischkreis 3 Parallelverschiebung. Minimum 50, maximum 350.
         * Optional, depends on heat pump model if available.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_22_HKMK3PARAVER(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Temperatur +-. Minimum -50, maximum 50. Signed 16 bit number.
         * <li>Unit: Dezidegree Celsius</li>
         */
        HR_23_TEMPPM(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }

    // Discrete Inputs

    /**
     * EVU, Energie Versorger Unterbrechung. Scheduled off time.
     *
     * @return the Channel
     */
    default Channel<Boolean> isEVUactive() { return this.channel(ChannelId.DI_0_EVU); }

    /**
     * EVU2. Like EVU, but triggered because of smart grid setting.
     *
     * @return the Channel
     */
    default Channel<Boolean> isEVU2active() { return this.channel(ChannelId.DI_1_EVU2); }

    /**
     * SWT, Schwimmbadthermostat.
     *
     * @return the Channel
     */
    default Channel<Boolean> isSWTactive() { return this.channel(ChannelId.DI_2_SWT); }

    /**
     * VD1, Verdichter 1.
     *
     * @return the Channel
     */
    default Channel<Boolean> isVD1active() { return this.channel(ChannelId.DI_3_VD1); }

    /**
     * VD2, Verdichter 2.
     *
     * @return the Channel
     */
    default Channel<Boolean> isVD2active() { return this.channel(ChannelId.DI_4_VD2); }

    /**
     * ZWE1, zusätzlicher Wärmeerzeuger 1.
     *
     * @return the Channel
     */
    default Channel<Boolean> isZWE1active() { return this.channel(ChannelId.DI_5_ZWE1); }

    /**
     * ZWE2, zusätzlicher Wärmeerzeuger 2.
     *
     * @return the Channel
     */
    default Channel<Boolean> isZWE2active() { return this.channel(ChannelId.DI_6_ZWE2); }

    /**
     * ZWE3, zusätzlicher Wärmeerzeuger 3. Optional, depends on heat pump model if available.
     *
     * @return the Channel
     */
    default Channel<Boolean> isZWE3active() { return this.channel(ChannelId.DI_7_ZWE3); }


    // Input Registers

    /**
     * Mitteltemperatur.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getMittelTemp() { return this.channel(ChannelId.IR_0_MITTELTEMP); }

    /**
     * Vorlauftemperatur.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getVorlaufTemp() { return this.channel(ChannelId.IR_1_VORLAUFTEMP); }

    /**
     * Rücklauftemperatur.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getRuecklaufTemp() { return this.channel(ChannelId.IR_2_RUECKLAUFTEMP); }

    /**
     * Rücklauf extern.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getRuecklaufExternTemp() { return this.channel(ChannelId.IR_3_RUECKEXTERN); }

    /**
     * Trinkwarmwassertemperatur.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getTrinkWWTemp() { return this.channel(ChannelId.IR_4_TRINKWWTEMP); }

    /**
     * Mischkreis 1 Vorlauf.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getMischkreis1VorlaufTemp() { return this.channel(ChannelId.IR_5_MK1VORLAUF); }

    /**
     * Mischkreis 2 Vorlauf. Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getMischkreis2VorlaufTemp() { return this.channel(ChannelId.IR_6_MK2VORLAUF); }

    /**
     * Mischkreis 3 Vorlauf. Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getMischkreis3VorlaufTemp() { return this.channel(ChannelId.IR_7_MK3VORLAUF); }

    /**
     * Heissgastemperatur.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHeissGasTemp() { return this.channel(ChannelId.IR_8_HEISSGASTEMP); }

    /**
     * Wärmequelle Eintritt.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getWaermequelleEintrittTemp() { return this.channel(ChannelId.IR_9_WQEINTRITT); }

    /**
     * Wärmequelle Austritt.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getWaermequelleAustrittTemp() { return this.channel(ChannelId.IR_10_WQAUSTRITT); }

    /**
     * Raumfernversteller 1.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getRaumfernversteller1Temp() { return this.channel(ChannelId.IR_11_RAUMFV1); }

    /**
     * Raumfernversteller 2. Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getRaumfernversteller2Temp() { return this.channel(ChannelId.IR_12_RAUMFV2); }

    /**
     * Raumfernversteller 3. Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getRaumfernversteller3Temp() { return this.channel(ChannelId.IR_13_RAUMFV3); }

    /**
     * Solarkollektor. Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getSolarkollektorTemp() { return this.channel(ChannelId.IR_14_SOLARKOLLEKTOR); }

    /**
     * Solarspeicher. Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getSolarspeicherTemp() { return this.channel(ChannelId.IR_15_SOLARSPEICHER); }

    /**
     * Externe Energiequelle. Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getEnergiequelleExternTemp() { return this.channel(ChannelId.IR_16_EXTEQ); }

    /**
     * Zulufttemperatur. Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getZuluftTemp() { return this.channel(ChannelId.IR_17_ZULUFTTEMP); }

    /**
     * Ablufttemperatur.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getAbluftTemp() { return this.channel(ChannelId.IR_18_ABLUFTTEMP); }

    /**
     * Ansaugtemperatur Verdichter.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getAnsaugTempVerdichter() { return this.channel(ChannelId.IR_19_ANSAUGTEMPVDICHTER); }

    /**
     * Ansaugtemperatur Verdampfer.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getAnsaugTempVerdampfer() { return this.channel(ChannelId.IR_20_ANSAUGTEMPVDAMPFER); }

    /**
     * Temperatur Verdichterheizung.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getVerdichterHeizungTemp() { return this.channel(ChannelId.IR_21_TEMPVDHEIZUNG); }

    /**
     * Überhitzung.
     * <li>Unit: Dezidegree Kelvin</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getUeberhitzung() { return this.channel(ChannelId.IR_22_UEBERHITZ); }

    /**
     * Überhitzung Soll.
     * <li>Unit: Dezidegree Kelvin</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getUeberhitzungSoll() { return this.channel(ChannelId.IR_23_UEBERHITZSOLL); }

    /**
     * RBE (Raumbedieneinheit) Raumtemperatur Ist.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getRbeRaumtempIst() { return this.channel(ChannelId.IR_24_RBERAUMTEMPIST); }

    /**
     * RBE (Raumbedieneinheit) Raumtemperatur Soll.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getRbeRaumtempSoll() { return this.channel(ChannelId.IR_25_RBERAUMTEMPSOLL); }

    /**
     * Druck HD (Hochdruck).
     * <li>Unit: Centi bar</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getDruckHD() { return this.channel(ChannelId.IR_26_DRUCKHD); }

    /**
     * Druck ND (Niederdruck).
     * <li>Unit: Centi bar</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getDruckND() { return this.channel(ChannelId.IR_27_DRUCKND); }

    /**
     * Betriebsstunden VD1 (Verdichter).
     * <li>Unit: hours</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHoursVD1() { return this.channel(ChannelId.IR_28_TVD1); }

    /**
     * Betriebsstunden VD2 (Verdichter).
     * <li>Unit: hours</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHoursVD2() { return this.channel(ChannelId.IR_29_TVD2); }

    /**
     * Betriebsstunden ZWE2 (Zusätzlicher Wärmeerzeuger).
     * <li>Unit: hours</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHoursZWE1() { return this.channel(ChannelId.IR_30_TZWE1); }

    /**
     * Betriebsstunden ZWE2 (Zusätzlicher Wärmeerzeuger).
     * <li>Unit: hours</li>i>
     *
     * @return the Channel
     */
    default Channel<Integer> getHoursZWE2() { return this.channel(ChannelId.IR_31_TZWE2); }

    /**
     * Betriebsstunden ZWE3 (Zusätzlicher Wärmeerzeuger). Optional, depends on heat pump model if available.
     * <li>Unit: hours</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHoursZWE3() { return this.channel(ChannelId.IR_32_TZWE3); }

    /**
     * Betriebsstunden Wärmepumpe.
     * <li>Unit: hours</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHoursWaermepumpe() { return this.channel(ChannelId.IR_33_TWAERMEPUMPE); }

    /**
     * Betriebsstunden Heizung.
     * <li>Unit: hours</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHoursHeizung() { return this.channel(ChannelId.IR_34_THEIZUNG); }

    /**
     * Betriebsstunden Trinkwarmwasser.
     * <li>Unit: hours</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHoursTrinkWW() { return this.channel(ChannelId.IR_35_TTRINKWW); }

    /**
     * Betriebsstunden SWoPV (Schwimmbad oder Photovoltaik). Optional, depends on heat pump model if available.
     * <li>Unit: hours</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHoursSWoPV() { return this.channel(ChannelId.IR_36_TSWOPV); }

    /**
     * Anlagenstatus. Current operating state of the heat pump.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0 ... 7
     *      <li> State 0: Heizbetrieb / Heating
     *      <li> State 1: Trinkwarmwasser / Heating potable water
     *      <li> State 2: Schwimmbad / Swimming pool
     *      <li> State 3: EVU-Sperre / Forced off by energy supplier
     *      <li> State 4: Abtauen / Defrost
     *      <li> State 5: Off
     *      <li> State 6: Externe Energiequelle / External energy source
     *      <li> State 7: Kühlung / Cooling
     * </ul>
     *
     * @return the Channel
     */
    default Channel<Integer> getHeatpumpOperatingMode() { return this.channel(ChannelId.IR_37_STATUS); }

    /**
     * Wärmemenge Heizung. 32 bit unsigned doubleword. IR 38 is high, IR 39 is low.
     * <li>Unit: kWh * 10E-1</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHeatAmountHeizung() { return this.channel(ChannelId.IR_38_WHHEIZUNG); }

    /**
     * Wärmemenge Trinkwarmwasser. 32 bit unsigned doubleword. IR 40 is high, IR 41 is low.
     * <li>Unit: kWh * 10E-1</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHeatAmountTrinkWW() { return this.channel(ChannelId.IR_40_WHTRINKWW); }

    /**
     * Wärmemenge Schwimmbad. 32 bit unsigned doubleword. IR 42 is high, IR 43 is low.
     * Optional, depends on heat pump model if available.
     * <li>Unit: kWh * 10E-1</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHeatAmountPool() { return this.channel(ChannelId.IR_42_WHPOOL); }

    /**
     * Wärmemenge gesamt. 32 bit unsigned doubleword. IR 44 is high, IR 45 is low.
     * <li>Unit: kWh * 10E-1</li>
     *
     * @return the Channel
     */
    default Channel<Integer> getHeatAmountAll() { return this.channel(ChannelId.IR_44_WHTOTAL); }

    /**
     * Error buffer. Only displays current error.
     *
     * @return the Channel
     */
    default Channel<Integer> getErrorCode() { return this.channel(ChannelId.IR_46_ERROR); }


    // Coils. When reading, false = Off, true = On. When writing, false = automatic, true = force on.

    /**
     * Error reset. Reset current error message.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> clearError() { return this.channel(ChannelId.COIL_0_ERRORRESET); }

    /**
     * HUP, force on.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnHUP() { return this.channel(ChannelId.COIL_2_HUP); }

    /**
     * VEN (Ventilator), force on.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnVEN() { return this.channel(ChannelId.COIL_3_VEN); }

    /**
     * ZUP (Zusatz-Umwälzpumpe), force on.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnZUP() { return this.channel(ChannelId.COIL_4_ZUP); }

    /**
     * BUP (Trinkwarmwasser-Umwälzpumpe), force on.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnBUP() { return this.channel(ChannelId.COIL_5_BUP); }

    /**
     * BOSUP (Brunnen oder Sole-Umwälzpumpe), force on.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnBOSUP() { return this.channel(ChannelId.COIL_6_BOSUP); }

    /**
     * ZIP (Zirkulationspumpe), force on.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnZIP() { return this.channel(ChannelId.COIL_7_ZIP); }

    /**
     * FUP2 (Fußbodenheizungs-Umwälzpumpe), force on. Optional, depends on heat pump model if available.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnFUP2() { return this.channel(ChannelId.COIL_8_FUP2); }

    /**
     * FUP3 (Fußbodenheizungs-Umwälzpumpe), force on. Optional, depends on heat pump model if available.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnFUP3() { return this.channel(ChannelId.COIL_9_FUP3); }

    /**
     * SLP (Solar-Ladepumpe), force on. Optional, depends on heat pump model if available.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnSLP() { return this.channel(ChannelId.COIL_10_SLP); }

    /**
     * SUP (Schwimmbad-Umwälzpumpe), force on. Optional, depends on heat pump model if available.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnSUP() { return this.channel(ChannelId.COIL_11_SUP); }

    /**
     * VSK (Bypassklappe), force on. Optional, depends on heat pump model if available.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnVSK() { return this.channel(ChannelId.COIL_12_VSK); }

    /**
     * FRH (Schütz Defrostheizung), force on. Optional, depends on heat pump model if available.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> turnOnFRH() { return this.channel(ChannelId.COIL_13_FRH); }


    // Holding Registers

    /**
     * Outside temperature. Signed 16 bit. Minimum -200, maximum 800.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetOutsideTemp() { return this.channel(ChannelId.HR_0_OUTSIDETEMP); }

    /**
     * Rücklauf-Temperatur Soll. Minimum 150, maximum 800. <- Aus Handbuch. Minimum Wert sicher falsch, schon Wert 50 ausgelesen.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetRuecklaufTempSoll() { return this.channel(ChannelId.HR_1_RUECKTEMPSOLL); }

    /**
     * Mischkreis 1 Vorlauf Solltemperatur. Minimum 150, maximum 800.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetMK1VorlaufTempSoll() { return this.channel(ChannelId.HR_2_MK1VORTEMPSOLL); }

    /**
     * Mischkreis 2 Vorlauf Solltemperatur. Minimum 150, maximum 800.
     * Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetMK2VorlaufTempSoll() { return this.channel(ChannelId.HR_3_MK2VORTEMPSOLL); }

    /**
     * Mischkreis 3 Vorlauf Solltemperatur. Minimum 150, maximum 800.
     * Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetMK3VorlaufTempSoll() { return this.channel(ChannelId.HR_4_MK3VORTEMPSOLL); }

    /**
     * Trinkwarmwasse-Temperatur Wunschwert. Minimum 150, maximum 800.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetTrinkWWTempSoll() { return this.channel(ChannelId.HR_5_TRINKWWTEMPSOLL); }

    /**
     * Sperre / Freigabe Wärmepumpe. Heat pump operation clearance.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0 ... 2
     *      <li> State 0: Sperre / Off
     *      <li> State 1: Freigabe 1 Verdichter / Clearance 1 compressor
     *      <li> State 2: Freigabe 2 Verdichter / Clearance 2 compressors
     * </ul>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetRunClearance() { return this.channel(ChannelId.HR_6_RUNCLEARANCE); }

    /**
     * Betriebsart Heizung. Heating operation status.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0 ... 4
     *      <li> State 0: Automatik
     *      <li> State 1: Zusätzlicher Wärmeerzeuger / Additional heater
     *      <li> State 2: Party / No late night throttling
     *      <li> State 3: Ferien / Vacation, full time throttling
     *      <li> State 4: Off
     * </ul>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetHeizungOperationMode() { return this.channel(ChannelId.HR_7_HEIZUNGRUNSTATE); }

    /**
     * Betriebsart Trinkwarmwasser. Potable water heating operation status.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0 ... 4
     *      <li> State 0: Automatik
     *      <li> State 1: Zusätzlicher Wärmeerzeuger / Additional heater
     *      <li> State 2: Party / No late night throttling
     *      <li> State 3: Ferien / Vacation, full time throttling
     *      <li> State 4: Off
     * </ul>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetTrinkWWOperationMode() { return this.channel(ChannelId.HR_8_TRINKWWRUNSTATE); }

    /**
     * Betriebsart Mischkreis 2. Diluted heating circuit 2 operation status.
     * Optional, depends on heat pump model if available.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0 ... 4
     *      <li> State 0: Automatik
     *      <li> State 1: Zusätzlicher Wärmeerzeuger / Additional heater
     *      <li> State 2: Party / No late night throttling
     *      <li> State 3: Ferien / Vacation, full time throttling
     *      <li> State 4: Off
     * </ul>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetMK2OperationMode() { return this.channel(ChannelId.HR_9_MK2RUNSTATE); }

    /**
     * Betriebsart Mischkreis 3. Diluted heating circuit 3 operation status.
     * Optional, depends on heat pump model if available.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0 ... 4
     *      <li> State 0: Automatik
     *      <li> State 1: Zusätzlicher Wärmeerzeuger / Additional heater
     *      <li> State 2: Party / No late night throttling
     *      <li> State 3: Ferien / Vacation, full time throttling
     *      <li> State 4: Off
     * </ul>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetMK3OperationMode() { return this.channel(ChannelId.HR_10_MK3RUNSTATE); }

    /**
     * Betriebsart Kühlung. Cooling operation status.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0 ... 1
     *      <li> State 0: Off
     *      <li> State 1: Automatik
     * </ul>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetCoolingOperationMode() { return this.channel(ChannelId.HR_11_COOLINGRUNSTATE); }

    /**
     * Betriebsart Lüftung. Ventilation operation status.
     * Optional, depends on heat pump model if available.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0 ... 3
     *      <li> State 0: Automatik
     *      <li> State 1: Party / No late night throttling
     *      <li> State 2: Ferien / Vacation, full time throttling
     *      <li> State 3: Off
     * </ul>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetVentilationOperationMode() { return this.channel(ChannelId.HR_12_VENTILATIONRUNSTATE); }

    /**
     * Betriebsart Schwimmbad. Pool heating operation status.
     * Optional, depends on heat pump model if available.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0 ... 4
     *      <li> State 0: Automatik
     *      <li> State 1: Wert nicht benutzt / Value not in use
     *      <li> State 2: Party / No late night throttling
     *      <li> State 3: Ferien / Vacation, full time throttling
     *      <li> State 4: Off
     * </ul>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetPoolOperationMode() { return this.channel(ChannelId.HR_13_POOLRUNSTATE); }

    /**
     * Heizkurve Heizung Endpunkt. Minimum 200, maximum 700.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetHeizkurveHeizungEndpunkt() { return this.channel(ChannelId.HR_15_HKHEIZUNGENDPKT); }

    /**
     * Heizkurve Heizung Parallelverschiebung. Minimum 50, maximum 350.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetHeizkurveHeizungParallelverschiebung() { return this.channel(ChannelId.HR_16_HKHEIZUNGPARAVER); }

    /**
     * Heizkurve Mischkreis 1 Endpunkt. Minimum 200, maximum 700.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetHeizkurveMK1Endpunkt() { return this.channel(ChannelId.HR_17_HKMK1ENDPKT); }

    /**
     * Heizkurve Mischkreis 1 Parallelverschiebung. Minimum 50, maximum 350.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetHeizkurveMK1Parallelverschiebung() { return this.channel(ChannelId.HR_18_HKMK1PARAVER); }

    /**
     * Heizkurve Mischkreis 2 Endpunkt. Minimum 200, maximum 700.
     * Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetHeizkurveMK2Endpunkt() { return this.channel(ChannelId.HR_19_HKMK2ENDPKT); }

    /**
     * Heizkurve Mischkreis 2 Parallelverschiebung. Minimum 50, maximum 350.
     * Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetHeizkurveMK2Parallelverschiebung() { return this.channel(ChannelId.HR_20_HKMK2PARAVER); }

    /**
     * Heizkurve Mischkreis 3 Endpunkt. Minimum 200, maximum 700.
     * Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetHeizkurveMK3Endpunkt() { return this.channel(ChannelId.HR_21_HKMK3ENDPKT); }

    /**
     * Heizkurve Mischkreis 3 Parallelverschiebung. Minimum 50, maximum 350.
     * Optional, depends on heat pump model if available.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetHeizkurveMK3Parallelverschiebung() { return this.channel(ChannelId.HR_22_HKMK3PARAVER); }

    /**
     * Temperatur +-. Minimum -50, maximum 50. Signed 16 bit number.
     * <li>Unit: Dezidegree Celsius</li>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetTempPlusMinus() { return this.channel(ChannelId.HR_23_TEMPPM); }

}
